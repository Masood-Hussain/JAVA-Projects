package com.facerecognition;

import com.facerecognition.config.ConfigurationManager;
import com.facerecognition.core.FaceDetector;
import com.facerecognition.core.FaceRecognizer;
import com.facerecognition.database.DatabaseManager;
import com.facerecognition.gui.FaceRecognitionGUI;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame; // Explicit import to resolve ambiguity
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * Main entry point for the Real-Time Face Recognition System
 * Provides both GUI and command-line interfaces for face recognition
 * 
 * Features:
 * - Real-time webcam face detection and recognition
 * - Person registration with face embeddings
 * - Configurable recognition threshold
 * - Optional Swing GUI interface
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    // Configuration manager
    private final ConfigurationManager config = ConfigurationManager.getInstance();
    
    // Configuration constants - loaded from configuration
    private final double recognitionThreshold;
    private final int cameraWidth;
    private final int cameraHeight;
    private final int frameRate;
    
    // Core components
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private DatabaseManager databaseManager;
    private OpenCVFrameGrabber grabber;
    private CanvasFrame canvas;
    private FaceRecognitionGUI gui;
    private boolean isRunning = false;
    
    // Security components
    private final SecureRandom secureRandom = new SecureRandom();
    private String sessionId;
    private long sessionStartTime;
    private int failedAttempts = 0;
    
    /**
     * Constructor - initialize with configuration
     */
    public Main() {
        // Load configuration values
        this.recognitionThreshold = config.getDouble(ConfigurationManager.RECOGNITION_THRESHOLD);
        this.cameraWidth = config.getInt(ConfigurationManager.CAMERA_WIDTH);
        this.cameraHeight = config.getInt(ConfigurationManager.CAMERA_HEIGHT);
        this.frameRate = config.getInt(ConfigurationManager.CAMERA_FRAME_RATE);
        
        // Generate session ID for security tracking
        this.sessionId = generateSessionId();
        this.sessionStartTime = System.currentTimeMillis();
        
        logger.info("Main application initialized with session ID: {}", sessionId);
    }
    
    /**
     * Generate secure session ID
     */
    private String generateSessionId() {
        byte[] sessionBytes = new byte[16];
        secureRandom.nextBytes(sessionBytes);
        return java.util.Base64.getEncoder().encodeToString(sessionBytes);
    }
    
    /**
     * Main method - entry point of the application
     * @param args Command line arguments
     *             --gui: Launch with GUI interface
     *             --console: Launch with console interface (default)
     */
    public static void main(String[] args) {
        logger.info("Starting Real-Time Face Recognition System");
        
        try {
            Main app = new Main();
            
            // Check for GUI argument
            boolean useGui = false;
            for (String arg : args) {
                if ("--gui".equals(arg)) {
                    useGui = true;
                    break;
                }
            }
            
            if (useGui) {
                app.launchGUI();
            } else {
                app.launchConsole();
            }
            
        } catch (Exception e) {
            logger.error("Error starting application: ", e);
            System.exit(1);
        }
    }
    
    /**
     * Initialize the face recognition system components
     */
    private void initialize() throws Exception {
        logger.info("Initializing face recognition components with session {}", sessionId);
        
        // Security check
        if (config.isSecureMode()) {
            logger.info("Running in secure mode");
            validateSecurityRequirements();
        }
        
        // Initialize database
        databaseManager = new DatabaseManager();
        databaseManager.initialize();
        
        // Initialize face detector with configuration
        faceDetector = new FaceDetector();
        
        // Initialize face recognizer with configured threshold
        faceRecognizer = new FaceRecognizer(recognitionThreshold);
        
        // Initialize camera with enhanced configuration
        try {
            logger.info("Initializing camera (device: 0, resolution: {}x{}, fps: {})", cameraWidth, cameraHeight, frameRate);
            
            grabber = new OpenCVFrameGrabber(0); // Default camera
            grabber.setImageWidth(cameraWidth);
            grabber.setImageHeight(cameraHeight);
            grabber.setFrameRate(frameRate);
            
            // Additional camera optimizations
            grabber.setFormat("video4linux2"); // Linux optimization
            
            logger.info("Camera initialization configured successfully");
        } catch (Exception e) {
            logger.error("Error configuring camera: ", e);
            // Try fallback configuration
            try {
                grabber = new OpenCVFrameGrabber(0);
                grabber.setImageWidth(640);
                grabber.setImageHeight(480);
                grabber.setFrameRate(30);
                logger.info("Using fallback camera configuration");
            } catch (Exception fallbackError) {
                logger.error("Fallback camera configuration also failed: ", fallbackError);
                throw new RuntimeException("Camera initialization failed", fallbackError);
            }
        }
        
        logger.info("Face recognition system initialized successfully");
    }
    
    /**
     * Validate security requirements
     */
    private void validateSecurityRequirements() {
        // Check if encryption is properly configured
        if (config.getBoolean(ConfigurationManager.ENABLE_ENCRYPTION)) {
            logger.info("Encryption is enabled");
        }
        
        // Validate session timeout
        long timeout = config.getInt(ConfigurationManager.SESSION_TIMEOUT);
        if (timeout < 300000) { // Less than 5 minutes
            logger.warn("Session timeout is very short: {} ms", timeout);
        }
        
        // Log security configuration
        logger.info("Max login attempts: {}", config.getInt(ConfigurationManager.MAX_LOGIN_ATTEMPTS));
        logger.info("Audit logging: {}", config.getBoolean(ConfigurationManager.AUDIT_LOGGING));
    }
    
    /**
     * Launch the application with GUI interface
     */
    private void launchGUI() {
        try {
            initialize();
            
            SwingUtilities.invokeLater(() -> {
                try {
                    // Use default look and feel
                    gui = new FaceRecognitionGUI(this);
                    gui.setVisible(true);
                } catch (Exception e) {
                    logger.error("Error launching GUI: ", e);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error initializing GUI application: ", e);
        }
    }
    
    /**
     * Launch the application with console interface
     */
    private void launchConsole() {
        try {
            initialize();
            
            // Create display window
            canvas = new CanvasFrame("Face Recognition System", CanvasFrame.getDefaultGamma() / 2.2);
            canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            canvas.setAlwaysOnTop(true);
            
            startCamera();
            
            // Keep the application running
            Runtime.getRuntime().addShutdownHook(new Thread(this::stopCamera));
            
            logger.info("Face recognition system started. Press Ctrl+C to exit.");
            logger.info("Commands:");
            logger.info("- Close the window to exit");
            logger.info("- Face detection and recognition will run automatically");
            
        } catch (Exception e) {
            logger.error("Error starting console application: ", e);
        }
    }
    
    /**
     * Start the camera and begin face recognition process
     */
    public void startCamera() throws Exception {
        if (isRunning) return;
        
        grabber.start();
        isRunning = true;
        
        logger.info("Camera started - Beginning face recognition");
        
        // Enable face checking in GUI if available
        if (gui != null) {
            gui.setFaceCheckingEnabled(true);
        }
        
        // Start processing in a separate thread
        Thread processingThread = new Thread(this::processFrames);
        processingThread.setDaemon(true);
        processingThread.start();
    }
    
    /**
     * Stop the camera and face recognition process
     */
    public void stopCamera() {
        if (!isRunning) return;
        
        isRunning = false;
        
        // Disable face checking in GUI if available
        if (gui != null) {
            gui.setFaceCheckingEnabled(false);
        }
        
        try {
            if (grabber != null) {
                grabber.stop();
            }
            if (canvas != null) {
                canvas.dispose();
            }
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            logger.info("Camera stopped and resources cleaned up");
        } catch (Exception e) {
            logger.error("Error stopping camera: ", e);
        }
    }
    
    /**
     * Main frame processing loop for face detection and recognition
     */
    private void processFrames() {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        
        try {
            while (isRunning) {
                try {
                    Frame frame = grabber.grab();
                    if (frame == null) continue;
                    
                    Mat mat = converter.convert(frame);
                    if (mat == null) continue;
                    
                    // Detect faces in the current frame
                    RectVector faces = faceDetector.detectFaces(mat);
                    
                    boolean faceDetected = faces.size() > 0;
                    String recognizedPerson = null;
                    double maxConfidence = 0.0;
                    
                    // Process each detected face
                    for (int i = 0; i < faces.size(); i++) {
                        Rect faceRect = faces.get(i);
                        
                        // Extract face region
                        Mat faceRegion = new Mat(mat, faceRect);
                        
                        // Recognize the face
                        String personName = faceRecognizer.recognizeFace(faceRegion, databaseManager);
                        double confidence = faceRecognizer.getLastRecognitionConfidence();
                        
                        // Keep track of the best recognition result
                        if (confidence > maxConfidence) {
                            maxConfidence = confidence;
                            recognizedPerson = personName;
                        }
                        
                        // Draw rectangle and label on the frame
                        drawFaceAnnotation(mat, faceRect, personName);
                    }
                    
                    // Update GUI if available
                    if (gui != null) {
                        // Update video feed first
                        gui.updateVideoFrame(frame);
                        
                        if (faceDetected && recognizedPerson != null) {
                            gui.onPersonRecognized(recognizedPerson, maxConfidence);
                        } else if (faceDetected) {
                            gui.onPersonRecognized("Unknown", 0.0);
                        } else {
                            gui.onNoFaceDetected();
                        }
                    }
                    
                    // Display the frame
                    if (canvas != null) {
                        canvas.showImage(converter.convert(mat));
                    }
                    
                    // Cleanup
                    mat.release();
                    
                } catch (Exception e) {
                    if (isRunning) {
                        logger.error("Error processing frame: ", e);
                    }
                }
            }
        } finally {
            converter.close();
        }
    }
    
    /**
     * Draw face detection rectangle and recognition label on the frame
     * @param mat The image matrix
     * @param faceRect The detected face rectangle
     * @param personName The recognized person name (or "Unknown")
     */
    private void drawFaceAnnotation(Mat mat, Rect faceRect, String personName) {
        // Draw rectangle around face
        opencv_imgproc.rectangle(mat, faceRect, 
            personName.equals("Unknown") ? 
                new org.bytedeco.opencv.opencv_core.Scalar(0, 0, 255, 0) :  // Red for unknown
                new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0),   // Green for recognized
            2, 8, 0);
        
        // Draw text label
        opencv_imgproc.putText(mat, personName,
            new org.bytedeco.opencv.opencv_core.Point(faceRect.x(), faceRect.y() - 10),
            opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
            new org.bytedeco.opencv.opencv_core.Scalar(255, 255, 255, 0), 2, 8, false);
    }
    
    /**
     * Register a new person in the face recognition system
     * @param personName The name of the person to register
     * @return true if registration successful, false otherwise
     */
    public boolean registerPerson(String personName) {
        try {
            logger.info("Starting registration process for: {}", personName);
            
            Frame frame = grabber.grab();
            if (frame == null) {
                logger.error("Could not capture frame for registration");
                return false;
            }
            
            OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
            try {
                Mat mat = converter.convert(frame);
                
                // Detect faces
                RectVector faces = faceDetector.detectFaces(mat);
                if (faces.size() == 0) {
                    logger.warn("No faces detected for registration");
                    return false;
                }
                
                // Use the first detected face
                Rect faceRect = faces.get(0);
                Mat faceRegion = new Mat(mat, faceRect);
                
                // Generate face embedding
                double[] embedding = faceRecognizer.generateFaceEmbedding(faceRegion);
                
                // Store in database
                boolean success = databaseManager.storeFaceEmbedding(personName, embedding);
                
                if (success) {
                    logger.info("Successfully registered person: {}", personName);
                } else {
                    logger.error("Failed to register person: {}", personName);
                }
                
                // Cleanup
                mat.release();
                
                return success;
                
            } finally {
                converter.close();
            }
            
        } catch (Exception e) {
            logger.error("Error during registration: ", e);
            return false;
        }
    }
    
    /**
     * Perform a single recognition test for accuracy testing
     * @return Name of recognized person or "Unknown"
     */
    public String performSingleRecognitionTest() throws Exception {
        if (!isRunning || grabber == null) {
            return null;
        }
        
        Frame frame = grabber.grab();
        if (frame == null) {
            return null;
        }
        
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        try {
            Mat mat = converter.convert(frame);
            
            // Detect faces
            RectVector faces = faceDetector.detectFaces(mat);
            if (faces.size() == 0) {
                return null;
            }
            
            // Use the first detected face
            Rect faceRect = faces.get(0);
            Mat faceRegion = new Mat(mat, faceRect);
            
            // Recognize the face
            String personName = faceRecognizer.recognizeFace(faceRegion, databaseManager);
            
            // Cleanup
            mat.release();
            
            return personName;
            
        } finally {
            converter.close();
        }
    }
    
    // Getters for GUI access
    public FaceDetector getFaceDetector() { return faceDetector; }
    public FaceRecognizer getFaceRecognizer() { return faceRecognizer; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public boolean isRunning() { return isRunning; }
    
    /**
     * Enhanced camera start method for GUI integration
     */
    public void startCameraForGUI() {
        try {
            if (!isRunning) {
                grabber.start();
                isRunning = true;
                logger.info("Camera started for GUI");
                
                // Start video processing thread for GUI
                if (gui != null) {
                    Thread videoThread = new Thread(this::processVideoForGUI);
                    videoThread.setDaemon(true);
                    videoThread.start();
                }
            }
        } catch (Exception e) {
            logger.error("Error starting camera for GUI: ", e);
            isRunning = false;
        }
    }
    
    /**
     * Enhanced camera stop method for GUI integration
     */
    public void stopCameraForGUI() {
        try {
            if (isRunning) {
                isRunning = false;
                if (grabber != null) {
                    grabber.stop();
                }
                logger.info("Camera stopped for GUI");
            }
        } catch (Exception e) {
            logger.error("Error stopping camera for GUI: ", e);
        }
    }
    
    /**
     * Process video frames for GUI display and recognition
     */
    private void processVideoForGUI() {
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        
        try {
            while (isRunning) {
                Frame frame = grabber.grab();
                if (frame == null) {
                    continue;
                }
                
                // Update GUI video display
                if (gui != null) {
                    gui.updateVideoFrame(frame);
                }
                
                // Perform face recognition
                try {
                    Mat mat = converter.convert(frame);
                    if (mat != null && !mat.empty()) {
                        // Detect faces
                        RectVector faces = faceDetector.detectFaces(mat);
                        
                        if (faces.size() > 0) {
                            // Use the largest face for recognition
                            Rect largestFace = findLargestFace(faces);
                            Mat faceRegion = new Mat(mat, largestFace);
                            
                            // Recognize the face
                            String personName = faceRecognizer.recognizeFace(faceRegion, databaseManager);
                            double confidence = faceRecognizer.getLastRecognitionConfidence();
                            
                            // Update GUI with recognition result
                            if (gui != null) {
                                gui.updateRecognitionResult(personName, confidence);
                            }
                            
                            faceRegion.release();
                        } else {
                            // No face detected
                            if (gui != null) {
                                gui.updateRecognitionResult("No face detected", 0.0);
                            }
                        }
                        
                        mat.release();
                    }
                } catch (Exception e) {
                    logger.debug("Error in frame processing: ", e);
                }
                
                // Limit frame rate
                Thread.sleep(33); // ~30 FPS
            }
        } catch (Exception e) {
            logger.error("Error in video processing: ", e);
        } finally {
            converter.close();
        }
    }
    
    /**
     * Find the largest face in the detected faces
     */
    private Rect findLargestFace(RectVector faces) {
        Rect largestFace = faces.get(0);
        int largestArea = largestFace.width() * largestFace.height();
        
        for (int i = 1; i < faces.size(); i++) {
            Rect face = faces.get(i);
            int area = face.width() * face.height();
            if (area > largestArea) {
                largestArea = area;
                largestFace = face;
            }
        }
        
        return largestFace;
    }
    
    /**
     * Set the GUI reference for integration
     */
    public void setGUI(FaceRecognitionGUI gui) {
        this.gui = gui;
    }
    
    /**
     * Update recognition threshold dynamically
     */
    public void updateRecognitionThreshold(double newThreshold) {
        if (faceRecognizer != null) {
            // Create new recognizer with updated threshold
            faceRecognizer = new FaceRecognizer(newThreshold);
            logger.info("Recognition threshold updated to: {}", newThreshold);
        }
    }
    
    /**
     * Get current frame for GUI processing
     */
    public Frame getCurrentFrame() {
        try {
            if (grabber != null && isRunning) {
                return grabber.grab();
            }
        } catch (Exception e) {
            logger.debug("Error getting current frame: ", e);
        }
        return null;
    }
}
