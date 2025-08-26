package com.facerecognition.core;

import com.facerecognition.config.ConfigurationManager;
import com.facerecognition.database.DatabaseManager;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    
    // Configuration manager
    private final ConfigurationManager config = ConfigurationManager.getInstance();
    
    // Recognition parameters - loaded from configuration
    private final double recognitionThreshold;
    private final double highQualityThreshold;
    private final double mediumQualityThreshold;
    private final double lowQualityThreshold;
    // Removed unused adaptive/multi-modal/boost flags
    private final boolean strictModeEnabled;
    private final boolean ultraPrecisionEnabled;
    private final boolean biometricAnalysisEnabled;
    private double lastRecognitionConfidence = 0.0;
    private final int faceSize;
    private final int histogramBins;
    private final int featureVectorSize;
    
    // Advanced recognition tracking
    private final Map<String, Double> personConfidenceHistory;
    private final Map<String, Integer> personRecognitionCount;
    private final Map<String, List<Double>> personQualityHistory;
    private final Map<String, double[]> personBiometricSignature;
    
    // Enhanced feature extraction parameters (Optimized)
    private static final double GAUSSIAN_BLUR_SIGMA = 0.5; // Reduced for speed
    private static final Size BLUR_KERNEL_SIZE = new Size(3, 3);
    // Removed unused LBP constants
    
    // Anti-spoofing and quality assessment
    private final boolean antiSpoofingEnabled;
    private final boolean livenessDetectionEnabled;
    private final boolean faceQualityCheckEnabled;
    private final boolean fastModeEnabled;
    // Removed unused cacheEnabled flag
    private final Map<String, Double> faceQualityCache;
    
    // Performance optimization
    private final Map<String, double[]> embeddingCache;
    private final int maxCacheSize;
    
    /**
     * Constructor with configurable recognition threshold
     * @param recognitionThreshold Threshold for face matching (0.0 to 1.0)
     *                           Lower values = more strict matching
     *                           Higher values = more lenient matching
     */
    public FaceRecognizer(double recognitionThreshold) {
        this.recognitionThreshold = Math.max(0.0, Math.min(1.0, recognitionThreshold));
        
        // Load configuration values
        this.faceSize = 160; // Enhanced face size
        this.histogramBins = 256;
        this.featureVectorSize = 1536; // Larger feature vector
        
        // Load advanced recognition thresholds
        this.highQualityThreshold = config.getDouble("recognition.threshold.high.quality", 0.90);
        this.mediumQualityThreshold = config.getDouble("recognition.threshold.medium.quality", 0.85);
        this.lowQualityThreshold = config.getDouble("recognition.threshold.low.quality", 0.80);
        
        // Load advanced features configuration
        this.antiSpoofingEnabled = config.getBoolean("features.anti.spoofing.enabled", true);
        this.livenessDetectionEnabled = config.getBoolean("features.liveness.detection", true);
        this.faceQualityCheckEnabled = config.getBoolean("features.face.quality.check", true);
        this.fastModeEnabled = config.getBoolean("recognition.fast.mode", true);
    // Removed unused config flags (cache, adaptive, confidence boost, multi-modal)
        this.strictModeEnabled = config.getBoolean("recognition.strict.mode", true);
        this.ultraPrecisionEnabled = config.getBoolean("recognition.ultra.precision", true);
        this.biometricAnalysisEnabled = config.getBoolean("recognition.biometric.analysis", true);
        
        // Initialize caches
        this.maxCacheSize = config.getInt(ConfigurationManager.CACHE_SIZE, 1000);
        this.embeddingCache = new ConcurrentHashMap<>();
        this.faceQualityCache = new ConcurrentHashMap<>();
        
        // Initialize advanced tracking maps
        this.personConfidenceHistory = new ConcurrentHashMap<>();
        this.personRecognitionCount = new ConcurrentHashMap<>();
        this.personQualityHistory = new ConcurrentHashMap<>();
        this.personBiometricSignature = new ConcurrentHashMap<>();
        
        logger.info("Ultra-Advanced Face recognizer initialized - Threshold: {}, Strict: {}, Ultra-Precision: {}, Biometric: {}", 
                   this.recognitionThreshold, strictModeEnabled, ultraPrecisionEnabled, biometricAnalysisEnabled);
    }
    
    /**
     * Generate face embedding from a face region with quality and anti-spoofing checks
     * @param faceRegion Mat containing the detected face region
     * @return Feature vector representing the face embedding
     */
    public double[] generateFaceEmbedding(Mat faceRegion) {
        if (faceRegion == null || faceRegion.empty()) {
            logger.warn("Face region is null or empty");
            return new double[featureVectorSize];
        }
        
        try {
            // Fast mode: Skip expensive quality checks for better performance
            if (!fastModeEnabled) {
                // Perform face quality assessment only in non-fast mode
                if (faceQualityCheckEnabled) {
                    double qualityScore = assessFaceQuality(faceRegion);
                    if (qualityScore < 0.5) {
                        logger.debug("Face quality too low: {}", qualityScore);
                        return new double[featureVectorSize];
                    }
                }
                
                // Perform anti-spoofing check only in non-fast mode
                if (antiSpoofingEnabled) {
                    if (detectSpoofing(faceRegion)) {
                        logger.warn("Potential spoofing attempt detected");
                        return new double[featureVectorSize];
                    }
                }
            }
            
            // Preprocess the face region
            Mat processedFace = preprocessFaceRegion(faceRegion);
            
            // Extract features - use simpler feature set in fast mode
            double[] features = new double[featureVectorSize];
            int featureIndex = 0;
            
            if (fastModeEnabled) {
                // Fast mode: Use only histogram features for speed
                double[] histogramFeatures = extractHistogramFeatures(processedFace);
                System.arraycopy(histogramFeatures, 0, features, featureIndex, 
                               Math.min(histogramFeatures.length, featureVectorSize));
                featureIndex += Math.min(histogramFeatures.length, featureVectorSize);
                
                // Fill remaining with simplified edge features
                if (featureIndex < featureVectorSize) {
                    double[] simpleEdgeFeatures = extractSimpleEdgeFeatures(processedFace);
                    int remainingSpace = featureVectorSize - featureIndex;
                    System.arraycopy(simpleEdgeFeatures, 0, features, featureIndex, 
                                   Math.min(simpleEdgeFeatures.length, remainingSpace));
                }
            } else {
                // Normal mode: Use all feature extraction methods
                // 1. Histogram features
                double[] histogramFeatures = extractHistogramFeatures(processedFace);
                System.arraycopy(histogramFeatures, 0, features, featureIndex, histogramFeatures.length);
                featureIndex += histogramFeatures.length;
                
                // 2. Local Binary Pattern features
                double[] lbpFeatures = extractLBPFeatures(processedFace);
                System.arraycopy(lbpFeatures, 0, features, featureIndex, 
                                Math.min(lbpFeatures.length, featureVectorSize - featureIndex));
                featureIndex += Math.min(lbpFeatures.length, featureVectorSize - featureIndex);
                
                // 3. Edge-based features (if space remaining)
                if (featureIndex < featureVectorSize) {
                    double[] edgeFeatures = extractEdgeFeatures(processedFace);
                    int remainingSpace = featureVectorSize - featureIndex;
                    System.arraycopy(edgeFeatures, 0, features, featureIndex, 
                                   Math.min(edgeFeatures.length, remainingSpace));
                }
            }
            
            // Normalize the feature vector
            features = normalizeFeatureVector(features);
            
            processedFace.release();
            // Embedding generated successfully
            
            return features;
            
        } catch (Exception e) {
            logger.error("Error generating face embedding: ", e);
            return new double[featureVectorSize];
        }
    }
    
    /**
     * Assess face quality for recognition suitability
     * @param faceRegion Face region to assess
     * @return Quality score between 0.0 and 1.0
     */
    public double assessFaceQuality(Mat faceRegion) {
        try {
            double qualityScore = 0.0;
            
            // 1. Check face size
            double sizeScore = Math.min(1.0, (double) Math.min(faceRegion.rows(), faceRegion.cols()) / 80.0);
            qualityScore += sizeScore * 0.3;
            
            // 2. Check image sharpness (using Laplacian variance)
            Mat gray = new Mat();
            if (faceRegion.channels() > 1) {
                opencv_imgproc.cvtColor(faceRegion, gray, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceRegion.clone();
            }
            
            Mat laplacian = new Mat();
            opencv_imgproc.Laplacian(gray, laplacian, opencv_core.CV_64F);
            
            // Calculate variance using simpler approach
            Scalar meanScalar = opencv_core.mean(laplacian);
            double mean = meanScalar.get(0);
            
            // Calculate variance manually
            double variance = 0.0;
            int totalPixels = laplacian.rows() * laplacian.cols();
            for (int y = 0; y < laplacian.rows(); y++) {
                for (int x = 0; x < laplacian.cols(); x++) {
                    double pixel = laplacian.ptr(y, x).getDouble();
                    variance += Math.pow(pixel - mean, 2);
                }
            }
            variance /= totalPixels;
            
            double sharpnessScore = Math.min(1.0, variance / 1000.0);
            qualityScore += sharpnessScore * 0.4;
            
            // 3. Check illumination uniformity
            Scalar grayMean = opencv_core.mean(gray);
            double illuminationScore = Math.max(0.0, 1.0 - (grayMean.get(0) / 255.0));
            qualityScore += illuminationScore * 0.3;
            
            gray.release();
            laplacian.release();
            
            // Cache the quality score
            String cacheKey = String.valueOf(faceRegion.hashCode());
            faceQualityCache.put(cacheKey, qualityScore);
            
            return Math.max(0.0, Math.min(1.0, qualityScore));
            
        } catch (Exception e) {
            logger.error("Error assessing face quality", e);
            return 0.5; // Default to medium quality
        }
    }
    
    /**
     * Simple anti-spoofing detection based on texture analysis
     * @param faceRegion Face region to analyze
     * @return true if spoofing is detected
     */
    public boolean detectSpoofing(Mat faceRegion) {
        try {
            Mat gray = new Mat();
            if (faceRegion.channels() > 1) {
                opencv_imgproc.cvtColor(faceRegion, gray, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                gray = faceRegion.clone();
            }
            
            // Check for uniform texture (common in printed photos)
            Mat lbp = new Mat();
            extractLBPFeatures(gray); // This generates LBP image internally
            lbp.release();
            
            // Calculate texture variance using simpler approach
            Scalar meanScalar = opencv_core.mean(gray);
            double mean = meanScalar.get(0);
            
            double textureVariance = 0.0;
            int totalPixels = gray.rows() * gray.cols();
            for (int y = 0; y < gray.rows(); y++) {
                for (int x = 0; x < gray.cols(); x++) {
                    double pixel = gray.ptr(y, x).get();
                    textureVariance += Math.pow(pixel - mean, 2);
                }
            }
            textureVariance /= totalPixels;
            
            boolean lowTexture = textureVariance < 200; // Threshold for low texture
            
            // Check for screen artifacts (simple approach)
            Mat edges = new Mat();
            opencv_imgproc.Canny(gray, edges, 50, 150);
            
            int edgeCount = opencv_core.countNonZero(edges);
            double edgeRatio = (double) edgeCount / (gray.rows() * gray.cols());
            boolean tooManyEdges = edgeRatio > 0.3; // Too many edges might indicate screen
            
            gray.release();
            lbp.release();
            edges.release();
            
            boolean spoofingDetected = lowTexture || tooManyEdges;
            if (spoofingDetected) {
                logger.warn("Potential spoofing detected - Low texture: {}, Too many edges: {}", 
                           lowTexture, tooManyEdges);
            }
            
            return spoofingDetected;
            
        } catch (Exception e) {
            logger.error("Error in spoofing detection", e);
            return false; // Default to no spoofing detected on error
        }
    }
    
    /**
     * Preprocess face region for feature extraction with enhanced normalization
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
        
        // Resize to standard size using configurable face size
        Mat resized = new Mat();
        opencv_imgproc.resize(processed, resized, new Size(faceSize, faceSize));
        
        // Apply Gaussian blur for noise reduction
        Mat blurred = new Mat();
        opencv_imgproc.GaussianBlur(resized, blurred, BLUR_KERNEL_SIZE, GAUSSIAN_BLUR_SIGMA);
        
        // Enhanced lighting normalization with multiple techniques
        Mat normalized = new Mat();
        
        // Step 1: Apply CLAHE (Contrast Limited Adaptive Histogram Equalization)
        try {
            var clahe = opencv_imgproc.createCLAHE();
            clahe.setClipLimit(3.0);  // Higher clip limit for better contrast
            clahe.setTilesGridSize(new Size(8, 8));
            clahe.apply(blurred, normalized);
            clahe.close();
        } catch (Exception e) {
            // Fallback to standard histogram equalization
            opencv_imgproc.equalizeHist(blurred, normalized);
        }
        
        // Step 2: Additional gamma correction for better illumination
        Mat gammaCorreected = new Mat();
        try {
            // Apply gamma correction (gamma = 0.8 for brightening)
            Mat lookupTable = new Mat(1, 256, opencv_core.CV_8U);
            for (int i = 0; i < 256; i++) {
                double gamma = 0.8;
                double normalizedPixel = i / 255.0;
                double correctedPixel = Math.pow(normalizedPixel, gamma) * 255.0;
                lookupTable.ptr(0, i).put((byte) Math.min(255, (int) correctedPixel));
            }
            opencv_core.LUT(normalized, lookupTable, gammaCorreected);
            lookupTable.release();
        } catch (Exception e) {
            gammaCorreected = normalized.clone();
        }
        
        // Step 3: Advanced noise reduction with bilateral filter for edge preservation
        Mat denoised = new Mat();
        try {
            opencv_imgproc.bilateralFilter(gammaCorreected, denoised, 9, 80, 80);
        } catch (Exception e) {
            denoised = gammaCorreected.clone();
        }
        
        // Step 4: Final sharpening using Laplacian for better edge definition
        Mat sharpened = new Mat();
        try {
            // Use Laplacian for edge enhancement
            Mat laplacian = new Mat();
            opencv_imgproc.Laplacian(denoised, laplacian, opencv_core.CV_8U);
            
            // Add the edge information back to the original with weight
            opencv_core.addWeighted(denoised, 1.0, laplacian, 0.2, 0, sharpened);
            laplacian.release();
        } catch (Exception e) {
            sharpened = denoised.clone();
        }
        
        // Cleanup intermediate matrices
        if (!processed.equals(faceRegion)) {
            processed.release();
        }
        resized.release();
        blurred.release();
        normalized.release();
        gammaCorreected.release();
        denoised.release();
        
        return sharpened;
    }
    
    /**
     * Extract histogram-based features from the face
     * @param face Preprocessed face matrix
     * @return Histogram feature vector
     */
    private double[] extractHistogramFeatures(Mat face) {
        try {
            // Create histogram bins
            double[] features = new double[histogramBins];
            
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
            int binSize = 256 / histogramBins;
            for (int i = 0; i < histogramBins; i++) {
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
            return new double[histogramBins];
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
     * Extract simple edge features for fast mode
     * @param face Preprocessed face matrix
     * @return Simple edge feature vector
     */
    private double[] extractSimpleEdgeFeatures(Mat face) {
        try {
            Mat edges = new Mat();
            opencv_imgproc.Canny(face, edges, 100, 200); // Higher thresholds for faster processing
            
            // Calculate simple edge density - just overall count
            double[] features = new double[64]; // Smaller feature set for speed
            
            // Divide into 8x8 grid for fast processing
            int regionHeight = face.rows() / 8;
            int regionWidth = face.cols() / 8;
            
            int featureIndex = 0;
            for (int i = 0; i < 8 && featureIndex < features.length; i++) {
                for (int j = 0; j < 8 && featureIndex < features.length; j++) {
                    int startY = i * regionHeight;
                    int startX = j * regionWidth;
                    int endY = Math.min(startY + regionHeight, face.rows());
                    int endX = Math.min(startX + regionWidth, face.cols());
                    
                    // Simple pixel counting without creating sub-matrices
                    double edgeCount = 0;
                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            if (edges.ptr(y, x).get() > 0) {
                                edgeCount++;
                            }
                        }
                    }
                    features[featureIndex++] = edgeCount / ((endY - startY) * (endX - startX));
                }
            }
            
            edges.release();
            return features;
            
        } catch (Exception e) {
            logger.error("Error extracting simple edge features", e);
            return new double[64];
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
     * Recognize a face by comparing with stored embeddings using advanced matching
     * @param faceRegion The detected face region
     * @param databaseManager Database containing stored embeddings
     * @return Name of recognized person or "Unknown"
     */
    public String recognizeFace(Mat faceRegion, DatabaseManager databaseManager) {
        try {
            double[] inputEmbedding = generateFaceEmbedding(faceRegion);
            List<Map<String, Object>> stored = databaseManager.getAllFaceEmbeddings();
            if (stored.isEmpty()) return "Unknown";

            // Group embeddings per person
            Map<String, List<double[]>> perPerson = new java.util.HashMap<>();
            for (Map<String, Object> rec : stored) {
                String name = (String) rec.get("person_name");
                double[] emb = (double[]) rec.get("embedding");
                perPerson.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(emb);
            }

            int totalPersons = perPerson.size();
            double faceQuality = faceQualityCheckEnabled ? assessFaceQuality(faceRegion) : 0.6;

            String bestName = "Unknown";
            double bestScore = 0.0;
            double bestMax = 0.0;
            double bestAvg = 0.0;
            double bestCentroidSim = 0.0;
            String secondName = "Unknown";
            double secondScore = 0.0;

            for (Map.Entry<String, List<double[]>> entry : perPerson.entrySet()) {
                String name = entry.getKey();
                List<double[]> embs = entry.getValue();
                if (embs.isEmpty()) continue;

                double maxSim = 0.0;
                double sumSim = 0.0;
                for (double[] e : embs) {
                    double sim = calculateHybridSimilarity(inputEmbedding, e); // simpler & more stable
                    maxSim = Math.max(maxSim, sim);
                    sumSim += sim;
                }
                double avgSim = sumSim / embs.size();

                // Compute centroid similarity
                double[] centroid = new double[inputEmbedding.length];
                for (double[] e : embs) {
                    for (int i = 0; i < centroid.length && i < e.length; i++) centroid[i] += e[i];
                }
                for (int i = 0; i < centroid.length; i++) centroid[i] /= embs.size();
                double centroidSim = calculateCosineSimilarity(inputEmbedding, centroid);

                // Consistency gating: require embedding to be consistently close to all stored samples
                boolean passes = maxSim >= recognitionThreshold &&
                                 avgSim >= (recognitionThreshold - 0.05) &&
                                 centroidSim >= (recognitionThreshold - 0.03);

                if (!passes) continue; // treat as not recognized

                // Combined score emphasizes consistent average & centroid alignment
                double combinedScore = (0.5 * maxSim) + (0.3 * avgSim) + (0.2 * centroidSim);

                if (combinedScore > bestScore) {
                    // shift best to second if different
                    if (!bestName.equals("Unknown") && !bestName.equals(name) && bestScore > secondScore) {
                        secondScore = bestScore;
                        secondName = bestName;
                    }
                    bestScore = combinedScore;
                    bestName = name;
                    bestMax = maxSim;
                    bestAvg = avgSim;
                    bestCentroidSim = centroidSim;
                } else if (!bestName.equals(name) && combinedScore > secondScore) {
                    secondScore = combinedScore;
                    secondName = name;
                }
            }

            // Special handling when only one person enrolled: be stricter to avoid false positives
            if (totalPersons == 1 && !bestName.equals("Unknown")) {
                // Require very small gap between max and avg (high intra-person consistency)
                if ((bestMax - bestAvg) > 0.05 || bestCentroidSim < recognitionThreshold) {
                    return "Unknown";
                }
            }

            // Disambiguation: ensure sufficient margin over second best (if there is another person)
            if (!bestName.equals("Unknown") && !secondName.equals("Unknown")) {
                if ((bestScore - secondScore) < 0.07) {
                    return "Unknown"; // ambiguous
                }
            }

            if (!bestName.equals("Unknown")) {
                lastRecognitionConfidence = bestScore;
                return bestName;
            }
            return "Unknown";
        } catch (Exception ex) {
            logger.error("Error during face recognition", ex);
            return "Unknown";
        }
    }
    
    /**
     * Calculate hybrid similarity combining multiple distance metrics for better accuracy
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Hybrid similarity value (0.0 to 1.0)
     */
    private double calculateHybridSimilarity(double[] embedding1, double[] embedding2) {
        if (embedding1.length != embedding2.length) {
            logger.warn("Embedding vectors have different lengths: {} vs {}", 
                       embedding1.length, embedding2.length);
            return 0.0;
        }
        
        // 1. Cosine similarity (good for direction)
        double cosineSim = calculateCosineSimilarity(embedding1, embedding2);
        
        // 2. Euclidean distance similarity (good for magnitude)
        double euclideanSim = calculateEuclideanSimilarity(embedding1, embedding2);
        
        // 3. Pearson correlation (good for patterns)
        double correlationSim = calculatePearsonCorrelation(embedding1, embedding2);
        
        // Weighted combination for optimal results
        double hybridSimilarity = (0.5 * cosineSim) + (0.3 * euclideanSim) + (0.2 * correlationSim);
        
        // Apply adaptive threshold boosting for low similarities
        if (hybridSimilarity > 0.3 && hybridSimilarity < 0.7) {
            hybridSimilarity = hybridSimilarity * 1.1; // Slight boost for borderline cases
        }
        
        return Math.max(0.0, Math.min(1.0, hybridSimilarity));
    }
    /** Basic Manhattan similarity (1 - normalized L1 distance) */
    private double calculateManhattanSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) sum += Math.abs(a[i] - b[i]);
        double max = 2.0 * a.length; // since features are normalized roughly in [-1,1] after normalization
        double sim = 1.0 - (sum / max);
        return sim < 0 ? 0 : (sim > 1 ? 1 : sim);
    }

    /** Chi-square similarity for histogram-like vectors */
    private double calculateChiSquareSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double chi = 0.0;
        for (int i = 0; i < a.length; i++) {
            double s = a[i] + b[i];
            if (s > 1e-12) {
                double d = a[i] - b[i];
                chi += (d * d) / s;
            }
        }
        double sim = 1.0 / (1.0 + chi / a.length);
        return sim < 0 ? 0 : (sim > 1 ? 1 : sim);
    }

    /** Weighted cosine similarity */
    private double calculateWeightedCosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, n1 = 0.0, n2 = 0.0;
        for (int i = 0; i < a.length; i++) {
            double w = 1.0 + Math.min(0.5, Math.abs(a[i]));
            double v1 = a[i] * w;
            double v2 = b[i] * w;
            dot += v1 * v2;
            n1 += v1 * v1;
            n2 += v2 * v2;
        }
        if (n1 < 1e-9 || n2 < 1e-9) return 0.0;
        double cos = dot / (Math.sqrt(n1) * Math.sqrt(n2));
        return cos < 0 ? 0 : (cos > 1 ? 1 : cos);
    }

    /** Standard cosine similarity */
    private double calculateCosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double dot = 0.0, n1 = 0.0, n2 = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            n1 += a[i] * a[i];
            n2 += b[i] * b[i];
        }
        if (n1 < 1e-10 || n2 < 1e-10) return 0.0;
        double sim = dot / (Math.sqrt(n1) * Math.sqrt(n2));
        return sim < 0 ? 0 : (sim > 1 ? 1 : sim);
    }

    /** Euclidean distance converted to similarity */
    private double calculateEuclideanSimilarity(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        double dist = Math.sqrt(sum);
        double maxDist = Math.sqrt(2.0 * a.length); // assuming normalized
        double sim = 1.0 - (dist / maxDist);
        return sim < 0 ? 0 : (sim > 1 ? 1 : sim);
    }

    /** Pearson correlation converted to 0..1 similarity */
    private double calculatePearsonCorrelation(double[] a, double[] b) {
        if (a.length != b.length) return 0.0;
        int n = a.length;
        double meanA = 0, meanB = 0;
        for (int i = 0; i < n; i++) { meanA += a[i]; meanB += b[i]; }
        meanA /= n; meanB /= n;
        double num = 0, sa = 0, sb = 0;
        for (int i = 0; i < n; i++) {
            double da = a[i] - meanA;
            double db = b[i] - meanB;
            num += da * db;
            sa += da * da;
            sb += db * db;
        }
        double den = Math.sqrt(sa * sb);
        if (den < 1e-10) return 0.0;
        double corr = num / den; // -1..1
        double sim = (corr + 1.0) / 2.0;
        return sim < 0 ? 0 : (sim > 1 ? 1 : sim);
    }

    /** Public accessor for threshold */
    public double getRecognitionThreshold() { return recognitionThreshold; }

    /** Public accessor for last confidence */
    public double getLastRecognitionConfidence() { return lastRecognitionConfidence; }
    
    /**
     * Get dynamic threshold based on face quality
     */
    private double getDynamicThreshold(double faceQuality) {
        if (faceQuality >= 0.8) {
            return highQualityThreshold;
        } else if (faceQuality >= 0.6) {
            return mediumQualityThreshold;
        } else if (faceQuality >= 0.4) {
            return lowQualityThreshold;
        } else {
            // Very low quality faces get highest threshold
            return Math.max(highQualityThreshold, 0.75);
        }
    }
    
    /**
     * Update recognition statistics for adaptive learning
     */
    private void updateRecognitionStatistics(String personName, double confidence, double quality) {
        if (personName.equals("Unknown")) {
            return;
        }
        
        // Update recognition count
        personRecognitionCount.merge(personName, 1, Integer::sum);
        
        // Update confidence history (exponential moving average)
        Double currentAvg = personConfidenceHistory.get(personName);
        if (currentAvg == null) {
            personConfidenceHistory.put(personName, confidence);
        } else {
            double newAvg = 0.8 * currentAvg + 0.2 * confidence; // EMA with alpha=0.2
            personConfidenceHistory.put(personName, newAvg);
        }
        
        // Update quality history
        List<Double> qualityHistory = personQualityHistory.computeIfAbsent(
            personName, k -> new java.util.ArrayList<>());
        qualityHistory.add(quality);
        
        // Keep only last 10 quality scores
        if (qualityHistory.size() > 10) {
            qualityHistory.remove(0);
        }
        
        logger.debug("Updated stats for {}: count={}, avgConfidence={:.3f}, avgQuality={:.3f}",
                    personName, personRecognitionCount.get(personName),
                    personConfidenceHistory.get(personName),
                    qualityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Embedding cache: %d/%d, Quality cache: %d", 
                           embeddingCache.size(), maxCacheSize, faceQualityCache.size());
    }
    
    /**
     * Check if liveness detection is enabled
     */
    public boolean isLivenessDetectionEnabled() {
        return livenessDetectionEnabled;
    }
    
    /**
     * Check if anti-spoofing is enabled
     */
    public boolean isAntiSpoofingEnabled() {
        return antiSpoofingEnabled;
    }
}
