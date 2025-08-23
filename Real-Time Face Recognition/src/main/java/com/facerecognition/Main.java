package com.facerecognition;

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
    
    // Configuration constants - improved for better recognition
    private static final double RECOGNITION_THRESHOLD = 0.45;  // Lower threshold for better sensitivity
    private static final int CAMERA_WIDTH = 640;
    private static final int CAMERA_HEIGHT = 480;
    private static final int FRAME_RATE = 30;
    
    // Core components
    private FaceDetector faceDetector;
    private FaceRecognizer faceRecognizer;
    private DatabaseManager databaseManager;
    private OpenCVFrameGrabber grabber;
    private CanvasFrame canvas;
    private FaceRecognitionGUI gui;
    private boolean isRunning = false;
    
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
        logger.info("Initializing face recognition components...");
        
        // Initialize database
        databaseManager = new DatabaseManager();
        databaseManager.initialize();
        
        // Initialize face detector with Haar cascade
        faceDetector = new FaceDetector();
        
        // Initialize face recognizer
        faceRecognizer = new FaceRecognizer(RECOGNITION_THRESHOLD);
        
        // Initialize camera
        grabber = new OpenCVFrameGrabber(0); // Default camera
        grabber.setImageWidth(CAMERA_WIDTH);
        grabber.setImageHeight(CAMERA_HEIGHT);
        grabber.setFrameRate(FRAME_RATE);
        
        logger.info("Face recognition system initialized successfully");
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
}
