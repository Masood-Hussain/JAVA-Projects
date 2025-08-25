package com.facerecognition.core;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_imgproc.CLAHE;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Advanced Face Preprocessing component for enhanced recognition
 * Handles image enhancement, normalization, and quality improvement
 * 
 * Features:
 * - Histogram equalization
 * - Noise reduction
 * - Contrast enhancement
 * - Size normalization
 * - Quality assessment
 * 
 * @author Face Recognition System
 * @version 2.0
 */
public class FacePreprocessor {
    private static final Logger logger = LoggerFactory.getLogger(FacePreprocessor.class);
    
    // Standard face size for processing
    private static final int STANDARD_FACE_WIDTH = 160;
    private static final int STANDARD_FACE_HEIGHT = 160;
    
    // Quality thresholds
    private static final double MIN_BRIGHTNESS = 50.0;
    private static final double MAX_BRIGHTNESS = 200.0;
    private static final double MIN_CONTRAST = 20.0;
    
    /**
     * Preprocess face image for better recognition
     * @param faceImage Input face image
     * @return Preprocessed face image
     */
    public static Mat preprocessFace(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            logger.warn("Input face image is null or empty");
            return null;
        }
        
        try {
            Mat processedFace = faceImage.clone();
            
            // Step 1: Convert to grayscale if needed
            if (processedFace.channels() > 1) {
                Mat grayFace = new Mat();
                opencv_imgproc.cvtColor(processedFace, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
                processedFace.release();
                processedFace = grayFace;
            }
            
            // Step 2: Resize to standard size
            Mat resizedFace = new Mat();
            opencv_imgproc.resize(processedFace, resizedFace, 
                new Size(STANDARD_FACE_WIDTH, STANDARD_FACE_HEIGHT));
            processedFace.release();
            processedFace = resizedFace;
            
            // Step 3: Apply Gaussian blur for noise reduction
            Mat blurredFace = new Mat();
            opencv_imgproc.GaussianBlur(processedFace, blurredFace, 
                new Size(3, 3), 0.5);
            processedFace.release();
            processedFace = blurredFace;
            
            // Step 4: Histogram equalization for better contrast
            Mat equalizedFace = new Mat();
            opencv_imgproc.equalizeHist(processedFace, equalizedFace);
            processedFace.release();
            processedFace = equalizedFace;
            
            // Step 5: Normalize pixel values
            Mat normalizedFace = new Mat();
            opencv_core.normalize(processedFace, normalizedFace, 0, 255, 
                opencv_core.NORM_MINMAX, opencv_core.CV_8UC1, new Mat());
            processedFace.release();
            
            logger.debug("Face preprocessing completed successfully");
            return normalizedFace;
            
        } catch (Exception e) {
            logger.error("Error during face preprocessing: ", e);
            return faceImage.clone();
        }
    }
    
    /**
     * Assess the quality of a face image
     * @param faceImage Input face image
     * @return Quality score (0.0 to 1.0, higher is better)
     */
    public static double assessFaceQuality(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            return 0.0;
        }
        
        try {
            Mat grayFace = faceImage;
            if (faceImage.channels() > 1) {
                grayFace = new Mat();
                opencv_imgproc.cvtColor(faceImage, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
            }
            
            // Calculate brightness (mean intensity)
            Scalar meanScalar = opencv_core.mean(grayFace);
            double brightness = meanScalar.get(0);
            
            // Calculate contrast (standard deviation)
            Mat mean = new Mat();
            Mat stddev = new Mat();
            opencv_core.meanStdDev(grayFace, mean, stddev);
            double contrast = stddev.ptr(0).getDouble(0);
            
            // Calculate sharpness (using Laplacian variance)
            Mat laplacian = new Mat();
            opencv_imgproc.Laplacian(grayFace, laplacian, opencv_core.CV_64F);
            Mat meanLap = new Mat();
            Mat stddevLap = new Mat();
            opencv_core.meanStdDev(laplacian, meanLap, stddevLap);
            double sharpness = Math.pow(stddevLap.ptr(0).getDouble(0), 2);
            
            // Normalize scores
            double brightnessScore = Math.max(0, Math.min(1, 
                1.0 - Math.abs(brightness - 127.5) / 127.5));
            double contrastScore = Math.max(0, Math.min(1, contrast / 100.0));
            double sharpnessScore = Math.max(0, Math.min(1, sharpness / 1000.0));
            
            // Combined quality score
            double qualityScore = (brightnessScore * 0.3 + contrastScore * 0.4 + sharpnessScore * 0.3);
            
            // Cleanup
            if (grayFace != faceImage) {
                grayFace.release();
            }
            mean.release();
            stddev.release();
            laplacian.release();
            meanLap.release();
            stddevLap.release();
            
            logger.debug("Face quality assessment: brightness={}, contrast={}, sharpness={}, score={}", 
                brightness, contrast, sharpness, qualityScore);
            
            return qualityScore;
            
        } catch (Exception e) {
            logger.error("Error assessing face quality: ", e);
            return 0.5; // Default middle quality
        }
    }
    
    /**
     * Check if face image meets minimum quality requirements
     * @param faceImage Input face image
     * @return true if quality is acceptable
     */
    public static boolean isAcceptableQuality(Mat faceImage) {
        double quality = assessFaceQuality(faceImage);
        return quality >= 0.3; // Minimum quality threshold
    }
    
    /**
     * Enhance face image for better recognition
     * @param faceImage Input face image
     * @return Enhanced face image
     */
    public static Mat enhanceFace(Mat faceImage) {
        if (faceImage == null || faceImage.empty()) {
            return null;
        }
        
        try {
            Mat enhanced = preprocessFace(faceImage);
            
            // Additional enhancement: CLAHE (Contrast Limited Adaptive Histogram Equalization)
            CLAHE clahe = opencv_imgproc.createCLAHE();
            clahe.setClipLimit(2.0);
            clahe.setTilesGridSize(new Size(8, 8));
            
            Mat claheResult = new Mat();
            clahe.apply(enhanced, claheResult);
            enhanced.release();
            
            logger.debug("Face enhancement completed");
            return claheResult;
            
        } catch (Exception e) {
            logger.error("Error enhancing face: ", e);
            return preprocessFace(faceImage);
        }
    }
}
