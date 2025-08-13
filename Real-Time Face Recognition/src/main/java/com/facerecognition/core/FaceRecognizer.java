package com.facerecognition.core;

import com.facerecognition.database.DatabaseManager;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Face Recognition component using face embeddings and similarity matching
 * Handles face feature extraction and person identification
 * 
 * Features:
 * - Face embedding generation using histogram-based features
 * - Configurable recognition threshold
 * - Person matching with confidence scoring
 * - Optimized for real-time performance
 * 
 * Note: This implementation uses basic histogram features. For production use,
 * consider integrating deep learning models like FaceNet or ArcFace for better accuracy.
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceRecognizer {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognizer.class);
    
    // Recognition parameters
    private final double recognitionThreshold;
    private double lastRecognitionConfidence = 0.0;
    private static final int FACE_SIZE = 100;  // Standardized face size for processing
    private static final int HISTOGRAM_BINS = 256;
    private static final int FEATURE_VECTOR_SIZE = 512; // Size of face embedding
    
    // Feature extraction parameters
    private static final double GAUSSIAN_BLUR_SIGMA = 1.0;
    private static final Size BLUR_KERNEL_SIZE = new Size(5, 5);
    
    /**
     * Constructor with configurable recognition threshold
     * @param recognitionThreshold Threshold for face matching (0.0 to 1.0)
     *                           Lower values = more strict matching
     *                           Higher values = more lenient matching
     */
    public FaceRecognizer(double recognitionThreshold) {
        this.recognitionThreshold = Math.max(0.0, Math.min(1.0, recognitionThreshold));
        logger.info("Face recognizer initialized with threshold: {}", this.recognitionThreshold);
    }
    
    /**
     * Generate face embedding from a face region
     * @param faceRegion Mat containing the detected face region
     * @return Feature vector representing the face embedding
     */
    public double[] generateFaceEmbedding(Mat faceRegion) {
        if (faceRegion == null || faceRegion.empty()) {
            logger.warn("Face region is null or empty");
            return new double[FEATURE_VECTOR_SIZE];
        }
        
        try {
            // Preprocess the face region
            Mat processedFace = preprocessFaceRegion(faceRegion);
            
            // Extract features using multiple methods for robust representation
            double[] features = new double[FEATURE_VECTOR_SIZE];
            int featureIndex = 0;
            
            // 1. Histogram features (256 bins)
            double[] histogramFeatures = extractHistogramFeatures(processedFace);
            System.arraycopy(histogramFeatures, 0, features, featureIndex, histogramFeatures.length);
            featureIndex += histogramFeatures.length;
            
            // 2. Local Binary Pattern features
            double[] lbpFeatures = extractLBPFeatures(processedFace);
            System.arraycopy(lbpFeatures, 0, features, featureIndex, 
                            Math.min(lbpFeatures.length, FEATURE_VECTOR_SIZE - featureIndex));
            featureIndex += Math.min(lbpFeatures.length, FEATURE_VECTOR_SIZE - featureIndex);
            
            // 3. Edge-based features (if space remaining)
            if (featureIndex < FEATURE_VECTOR_SIZE) {
                double[] edgeFeatures = extractEdgeFeatures(processedFace);
                int remainingSpace = FEATURE_VECTOR_SIZE - featureIndex;
                System.arraycopy(edgeFeatures, 0, features, featureIndex, 
                               Math.min(edgeFeatures.length, remainingSpace));
            }
            
            // Normalize the feature vector
            features = normalizeFeatureVector(features);
            
            processedFace.release();
            logger.debug("Generated face embedding with {} features", features.length);
            
            return features;
            
        } catch (Exception e) {
            logger.error("Error generating face embedding: ", e);
            return new double[FEATURE_VECTOR_SIZE];
        }
    }
    
    /**
     * Preprocess face region for feature extraction
     * @param faceRegion Input face region
     * @return Preprocessed face matrix
     */
    private Mat preprocessFaceRegion(Mat faceRegion) {
        Mat processed = new Mat();
        
        // Convert to grayscale if needed
        if (faceRegion.channels() > 1) {
            opencv_imgproc.cvtColor(faceRegion, processed, opencv_imgproc.COLOR_BGR2GRAY);
        } else {
            processed = faceRegion.clone();
        }
        
        // Resize to standard size
        Mat resized = new Mat();
        opencv_imgproc.resize(processed, resized, new Size(FACE_SIZE, FACE_SIZE));
        
        // Apply Gaussian blur for noise reduction
        Mat blurred = new Mat();
        opencv_imgproc.GaussianBlur(resized, blurred, BLUR_KERNEL_SIZE, GAUSSIAN_BLUR_SIGMA);
        
        // Histogram equalization for lighting normalization
        Mat equalized = new Mat();
        opencv_imgproc.equalizeHist(blurred, equalized);
        
        // Cleanup intermediate matrices
        if (!processed.equals(faceRegion)) {
            processed.release();
        }
        resized.release();
        blurred.release();
        
        return equalized;
    }
    
    /**
     * Extract histogram-based features from the face
     * @param face Preprocessed face matrix
     * @return Histogram feature vector
     */
    private double[] extractHistogramFeatures(Mat face) {
        try {
            // Create histogram bins
            double[] features = new double[HISTOGRAM_BINS];
            
            // Simple histogram calculation using direct pixel access
            Mat grayFace = new Mat();
            if (face.channels() == 3) {
                opencv_imgproc.cvtColor(face, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                grayFace = face.clone();
            }
            
            // Calculate histogram manually
            int[] histogram = new int[256];
            for (int y = 0; y < grayFace.rows(); y++) {
                for (int x = 0; x < grayFace.cols(); x++) {
                    int pixel = (int) grayFace.ptr(y, x).get();
                    if (pixel >= 0 && pixel < 256) {
                        histogram[pixel]++;
                    }
                }
            }
            
            // Bin the histogram
            int binSize = 256 / HISTOGRAM_BINS;
            for (int i = 0; i < HISTOGRAM_BINS; i++) {
                double sum = 0;
                for (int j = i * binSize; j < (i + 1) * binSize && j < 256; j++) {
                    sum += histogram[j];
                }
                features[i] = sum;
            }
            
            // Cleanup
            grayFace.release();
            
            return features;
            
        } catch (Exception e) {
            logger.error("Error extracting histogram features: ", e);
            return new double[HISTOGRAM_BINS];
        }
    }
    
    /**
     * Extract Local Binary Pattern (LBP) features
     * Simple implementation for texture analysis
     * @param face Preprocessed face matrix
     * @return LBP feature vector
     */
    private double[] extractLBPFeatures(Mat face) {
        try {
            int rows = face.rows();
            int cols = face.cols();
            Mat lbpImage = Mat.zeros(rows, cols, opencv_core.CV_8UC1).asMat();
            
            // Calculate LBP for each pixel (excluding borders)
            for (int i = 1; i < rows - 1; i++) {
                for (int j = 1; j < cols - 1; j++) {
                    double centerPixel = face.ptr(i, j).get();
                    int lbpValue = 0;
                    
                    // Check 8 surrounding pixels
                    int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
                    int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};
                    
                    for (int k = 0; k < 8; k++) {
                        double neighborPixel = face.ptr(i + dx[k], j + dy[k]).get();
                        if (neighborPixel >= centerPixel) {
                            lbpValue |= (1 << k);
                        }
                    }
                    
                    lbpImage.ptr(i, j).put((byte) lbpValue);
                }
            }
            
            // Calculate histogram of LBP image manually
            double[] features = new double[256];
            int[] lbpHistogram = new int[256];
            
            // Calculate histogram manually
            for (int y = 0; y < lbpImage.rows(); y++) {
                for (int x = 0; x < lbpImage.cols(); x++) {
                    int pixel = (int) lbpImage.ptr(y, x).get();
                    if (pixel >= 0 && pixel < 256) {
                        lbpHistogram[pixel]++;
                    }
                }
            }
            
            // Convert to feature vector
            for (int i = 0; i < 256; i++) {
                features[i] = lbpHistogram[i];
            }
            
            // Cleanup
            lbpImage.release();
            
            return features;
            
        } catch (Exception e) {
            logger.error("Error extracting LBP features: ", e);
            return new double[256];
        }
    }
    
    /**
     * Extract edge-based features using Canny edge detection
     * @param face Preprocessed face matrix
     * @return Edge feature vector
     */
    private double[] extractEdgeFeatures(Mat face) {
        try {
            Mat edges = new Mat();
            opencv_imgproc.Canny(face, edges, 50, 150);
            
            // Calculate edge density in different regions
            int regions = 4; // 2x2 grid
            double[] features = new double[regions * regions];
            
            int regionHeight = face.rows() / regions;
            int regionWidth = face.cols() / regions;
            
            int featureIndex = 0;
            for (int i = 0; i < regions; i++) {
                for (int j = 0; j < regions; j++) {
                    Rect regionRect = new Rect(j * regionWidth, i * regionHeight, 
                                             regionWidth, regionHeight);
                    Mat region = new Mat(edges, regionRect);
                    
                    // Count edge pixels in this region manually
                    double edgeCount = 0;
                    for (int y = 0; y < region.rows(); y++) {
                        for (int x = 0; x < region.cols(); x++) {
                            if (region.ptr(y, x).get() > 0) {
                                edgeCount++;
                            }
                        }
                    }
                    features[featureIndex++] = edgeCount;
                    
                    region.release();
                }
            }
            
            edges.release();
            return features;
            
        } catch (Exception e) {
            logger.error("Error extracting edge features: ", e);
            return new double[16]; // 4x4 regions
        }
    }
    
    /**
     * Normalize feature vector to unit length
     * @param features Input feature vector
     * @return Normalized feature vector
     */
    private double[] normalizeFeatureVector(double[] features) {
        double magnitude = 0.0;
        
        // Calculate magnitude
        for (double feature : features) {
            magnitude += feature * feature;
        }
        magnitude = Math.sqrt(magnitude);
        
        // Normalize (avoid division by zero)
        if (magnitude > 1e-10) {
            for (int i = 0; i < features.length; i++) {
                features[i] /= magnitude;
            }
        }
        
        return features;
    }
    
    /**
     * Recognize a face by comparing with stored embeddings
     * @param faceRegion The detected face region
     * @param databaseManager Database containing stored embeddings
     * @return Name of recognized person or "Unknown"
     */
    public String recognizeFace(Mat faceRegion, DatabaseManager databaseManager) {
        try {
            // Generate embedding for the input face
            double[] inputEmbedding = generateFaceEmbedding(faceRegion);
            
            // Get all stored embeddings from database
            List<Map<String, Object>> storedEmbeddings = databaseManager.getAllFaceEmbeddings();
            
            if (storedEmbeddings.isEmpty()) {
                logger.debug("No stored embeddings found for comparison");
                return "Unknown";
            }
            
            // Find best match
            String bestMatch = "Unknown";
            double bestSimilarity = 0.0;
            
            for (Map<String, Object> record : storedEmbeddings) {
                String personName = (String) record.get("person_name");
                double[] storedEmbedding = (double[]) record.get("embedding");
                
                double similarity = calculateCosineSimilarity(inputEmbedding, storedEmbedding);
                
                logger.debug("Similarity with {}: {}", personName, similarity);
                
                if (similarity > bestSimilarity && similarity >= recognitionThreshold) {
                    bestSimilarity = similarity;
                    bestMatch = personName;
                }
            }
            
            if (!bestMatch.equals("Unknown")) {
                logger.info("Recognized person: {} (similarity: {:.3f})", bestMatch, bestSimilarity);
                lastRecognitionConfidence = bestSimilarity;
            } else {
                logger.debug("No match found above threshold {}", recognitionThreshold);
                lastRecognitionConfidence = bestSimilarity;
            }
            
            return bestMatch;
            
        } catch (Exception e) {
            logger.error("Error during face recognition: ", e);
            return "Unknown";
        }
    }
    
    /**
     * Calculate cosine similarity between two feature vectors
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Cosine similarity value (0.0 to 1.0)
     */
    private double calculateCosineSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1.length != embedding2.length) {
            logger.warn("Embedding vectors have different lengths: {} vs {}", 
                       embedding1.length, embedding2.length);
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }
        
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        
        if (norm1 < 1e-10 || norm2 < 1e-10) {
            return 0.0;
        }
        
        double similarity = dotProduct / (norm1 * norm2);
        
        // Ensure result is between 0 and 1
        return Math.max(0.0, Math.min(1.0, similarity));
    }
    
    /**
     * Get recognition threshold
     * @return Current recognition threshold
     */
    public double getRecognitionThreshold() {
        return recognitionThreshold;
    }
    
    /**
     * Get the confidence score from the last recognition operation
     * @return Confidence score between 0.0 and 1.0
     */
    public double getLastRecognitionConfidence() {
        return lastRecognitionConfidence;
    }
    
    /**
     * Get face recognizer information
     * @return String containing recognizer configuration
     */
    public String getRecognizerInfo() {
        return String.format(
            "FaceRecognizer Configuration:\n" +
            "- Recognition Threshold: %.3f\n" +
            "- Face Size: %dx%d\n" +
            "- Feature Vector Size: %d\n" +
            "- Histogram Bins: %d",
            recognitionThreshold, FACE_SIZE, FACE_SIZE, 
            FEATURE_VECTOR_SIZE, HISTOGRAM_BINS
        );
    }
}
