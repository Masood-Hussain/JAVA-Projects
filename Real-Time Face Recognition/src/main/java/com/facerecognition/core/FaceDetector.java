package com.facerecognition.core;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Face Detection component using Haar Cascade Classifiers
 * Handles face detection in images using OpenCV's pre-trained models
 * 
 * Features:
 * - Haar cascade-based face detection
 * - Configurable detection parameters
 * - Optimized for real-time performance
 * - Automatic cascade model loading
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceDetector {
    private static final Logger logger = LoggerFactory.getLogger(FaceDetector.class);
    
    // Detection parameters - improved for better accuracy
    private static final double SCALE_FACTOR = 1.05;  // Smaller scale factor for better detection
    private static final int MIN_NEIGHBORS = 3;       // Reduced for more sensitive detection
    private static final Size MIN_SIZE = new Size(20, 20);  // Smaller minimum size
    private static final Size MAX_SIZE = new Size(500, 500); // Larger maximum size
    private static final int FLAGS = 0;
    
    // Haar cascade classifier for face detection
    private CascadeClassifier faceCascade;
    
    // Default cascade model path (embedded in JavaCV)
    private static final String CASCADE_MODEL = "haarcascade_frontalface_alt.xml";
    
    /**
     * Constructor - Initializes the face detector with Haar cascade model
     */
    public FaceDetector() {
        initializeCascadeClassifier();
    }
    
    /**
     * Initialize the Haar cascade classifier for face detection
     * Loads the pre-trained model from JavaCV resources
     */
    private void initializeCascadeClassifier() {
        try {
            logger.info("Initializing Haar cascade classifier for face detection...");
            
            // Try to load cascade from JavaCV embedded resources
            faceCascade = new CascadeClassifier();
            
            // Load the default frontal face cascade
            String cascadePath = loadCascadeModel();
            
            if (!faceCascade.load(cascadePath)) {
                throw new RuntimeException("Failed to load Haar cascade model from: " + cascadePath);
            }
            
            if (faceCascade.empty()) {
                throw new RuntimeException("Haar cascade model is empty or invalid");
            }
            
            logger.info("Face detector initialized successfully with Haar cascade");
            
        } catch (Exception e) {
            logger.error("Failed to initialize face detector: ", e);
            throw new RuntimeException("Face detector initialization failed", e);
        }
    }
    
    /**
     * Load the Haar cascade model from resources
     * @return Path to the cascade model file
     */
    private String loadCascadeModel() {
        try {
            // Try to load from JavaCV's embedded resources first
            String resourcePath = "/org/bytedeco/opencv/data/" + CASCADE_MODEL;
            InputStream cascadeStream = getClass().getResourceAsStream(resourcePath);
            
            if (cascadeStream == null) {
                // Fallback: try alternative resource paths
                String[] alternativePaths = {
                    "/haarcascade_frontalface_alt.xml",
                    "/opencv_data/" + CASCADE_MODEL,
                    "/" + CASCADE_MODEL
                };
                
                for (String altPath : alternativePaths) {
                    cascadeStream = getClass().getResourceAsStream(altPath);
                    if (cascadeStream != null) {
                        logger.info("Found cascade model at alternative path: {}", altPath);
                        break;
                    }
                }
            }
            
            if (cascadeStream == null) {
                // Use OpenCV's built-in data path (this should work with JavaCV)
                logger.warn("Could not find cascade in resources, using system OpenCV data");
                return CASCADE_MODEL; // Let OpenCV find it in its data directory
            }
            
            // Extract to temporary file
            Path tempCascade = Files.createTempFile("haarcascade_face", ".xml");
            Files.copy(cascadeStream, tempCascade, StandardCopyOption.REPLACE_EXISTING);
            cascadeStream.close();
            
            // Delete on exit
            tempCascade.toFile().deleteOnExit();
            
            logger.info("Cascade model loaded from resources to: {}", tempCascade.toString());
            return tempCascade.toString();
            
        } catch (IOException e) {
            logger.error("Error loading cascade model: ", e);
            // Fallback to system cascade
            return CASCADE_MODEL;
        }
    }
    
    /**
     * Detect faces in the given image matrix
     * @param image The input image as OpenCV Mat
     * @return RectVector containing detected face rectangles
     */
    public RectVector detectFaces(Mat image) {
        if (image == null || image.empty()) {
            logger.warn("Input image is null or empty");
            return new RectVector();
        }
        
        try {
            // Convert to grayscale if needed (cascade works better on grayscale)
            Mat grayImage = new Mat();
            if (image.channels() > 1) {
                opencv_imgproc.cvtColor(image, grayImage, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                grayImage = image.clone();
            }
            
            // Enhanced multi-stage preprocessing for better detection
            Mat enhancedImage = enhanceImageForDetection(grayImage);
            
            // Primary detection with standard parameters
            RectVector faces = new RectVector();
            faceCascade.detectMultiScale(
                enhancedImage,      // Input image
                faces,              // Output rectangles
                SCALE_FACTOR,       // Scale factor for image pyramid
                MIN_NEIGHBORS,      // Minimum neighbors required
                FLAGS,              // Additional flags
                MIN_SIZE,           // Minimum face size
                MAX_SIZE            // Maximum face size
            );
            
            // If no faces detected, try with relaxed parameters
            if (faces.size() == 0) {
                RectVector relaxedFaces = new RectVector();
                faceCascade.detectMultiScale(
                    enhancedImage,
                    relaxedFaces,
                    1.03,              // Smaller scale factor for more thorough search
                    2,                 // Fewer neighbors required
                    FLAGS,
                    new Size(15, 15),  // Smaller minimum size
                    MAX_SIZE
                );
                
                if (relaxedFaces.size() > 0) {
                    faces = relaxedFaces;
                }
            }
            
            logger.debug("Detected {} faces in the image", faces.size());
            
            // Cleanup temporary matrices
            if (!grayImage.equals(image)) {
                grayImage.release();
            }
            enhancedImage.release();
            
            return faces;
            
        } catch (Exception e) {
            logger.error("Error during face detection: ", e);
            return new RectVector();
        }
    }
    
    /**
     * Detect the largest face in the image (useful for single-person scenarios)
     * @param image The input image as OpenCV Mat
     * @return Rect of the largest detected face, null if no face found
     */
    public Rect detectLargestFace(Mat image) {
        RectVector faces = detectFaces(image);
        
        if (faces.size() == 0) {
            return null;
        }
        
        // Find the largest face by area
        Rect largestFace = null;
        long maxArea = 0;
        
        for (int i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            long area = (long) face.width() * face.height();
            
            if (area > maxArea) {
                maxArea = area;
                largestFace = face;
            }
        }
        
        logger.debug("Largest face found with area: {}", maxArea);
        return largestFace;
    }
    
    /**
     * Enhanced image preprocessing for better face detection
     * @param grayImage Input grayscale image
     * @return Enhanced image optimized for face detection
     */
    private Mat enhanceImageForDetection(Mat grayImage) {
        Mat enhanced = new Mat();
        
        try {
            // Stage 1: Noise reduction with slight blur
            Mat denoised = new Mat();
            opencv_imgproc.GaussianBlur(grayImage, denoised, new Size(3, 3), 0.5);
            
            // Stage 2: CLAHE for better contrast (if available)
            Mat claheResult = new Mat();
            try {
                var clahe = opencv_imgproc.createCLAHE();
                clahe.setClipLimit(3.0);
                clahe.setTilesGridSize(new Size(8, 8));
                clahe.apply(denoised, claheResult);
                clahe.close();
            } catch (Exception e) {
                // Fallback to standard histogram equalization
                opencv_imgproc.equalizeHist(denoised, claheResult);
            }
            
            // Stage 3: Simple sharpening alternative without custom kernel
            Mat sharpened = new Mat();
            try {
                // Use Laplacian for edge enhancement instead of custom kernel
                Mat laplacian = new Mat();
                opencv_imgproc.Laplacian(claheResult, laplacian, opencv_core.CV_8U);
                
                // Add the edge information back to the original
                opencv_core.addWeighted(claheResult, 1.0, laplacian, 0.3, 0, sharpened);
                laplacian.release();
            } catch (Exception e) {
                // If sharpening fails, use the CLAHE result
                sharpened = claheResult.clone();
            }
            
            // Cleanup
            denoised.release();
            claheResult.release();
            
            return sharpened;
            
        } catch (Exception e) {
            logger.warn("Error in image enhancement, using histogram equalization fallback: ", e);
            opencv_imgproc.equalizeHist(grayImage, enhanced);
            return enhanced;
        }
    }
    
    /**
     * Check if the face detector is properly initialized
     * @return true if detector is ready, false otherwise
     */
    public boolean isInitialized() {
        return faceCascade != null && !faceCascade.empty();
    }
    
    /**
     * Get detection statistics for debugging
     * @return String containing detector configuration info
     */
    public String getDetectorInfo() {
        return String.format(
            "FaceDetector Configuration:\n" +
            "- Scale Factor: %.2f\n" +
            "- Min Neighbors: %d\n" +
            "- Min Size: %dx%d\n" +
            "- Max Size: %dx%d\n" +
            "- Initialized: %s",
            SCALE_FACTOR, MIN_NEIGHBORS, 
            MIN_SIZE.width(), MIN_SIZE.height(),
            MAX_SIZE.width(), MAX_SIZE.height(),
            isInitialized()
        );
    }
    
    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (faceCascade != null && !faceCascade.isNull()) {
            faceCascade.close();
            logger.info("Face detector resources cleaned up");
        }
    }
    
    /**
     * Cleanup method to ensure resources are properly released
     */
    public void close() {
        cleanup();
    }
}
