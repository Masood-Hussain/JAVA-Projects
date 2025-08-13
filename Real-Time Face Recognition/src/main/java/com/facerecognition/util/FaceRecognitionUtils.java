package com.facerecognition.util;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class with common helper methods
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceRecognitionUtils {
    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionUtils.class);
    
    private FaceRecognitionUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validate if a rectangle is valid (positive dimensions)
     * @param rect Rectangle to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidRect(Rect rect) {
        return rect != null && rect.width() > 0 && rect.height() > 0 
               && rect.x() >= 0 && rect.y() >= 0;
    }
    
    /**
     * Validate if a Mat object is valid and not empty
     * @param mat Mat object to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidMat(Mat mat) {
        return mat != null && !mat.empty() && mat.rows() > 0 && mat.cols() > 0;
    }
    
    /**
     * Create directory if it doesn't exist
     * @param directoryPath Path to directory
     * @return true if directory exists or was created successfully
     */
    public static boolean ensureDirectoryExists(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created directory: {}", directoryPath);
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to create directory: {}", directoryPath, e);
            return false;
        }
    }
    
    /**
     * Get current timestamp as formatted string
     * @return Formatted timestamp
     */
    public static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Get current timestamp for file names (without special characters)
     * @return File-safe timestamp
     */
    public static String getFileTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }
    
    /**
     * Calculate Euclidean distance between two feature vectors
     * @param features1 First feature vector
     * @param features2 Second feature vector
     * @return Euclidean distance
     */
    public static double calculateEuclideanDistance(double[] features1, double[] features2) {
        if (features1.length != features2.length) {
            throw new IllegalArgumentException("Feature vectors must have the same length");
        }
        
        double sum = 0.0;
        for (int i = 0; i < features1.length; i++) {
            double diff = features1[i] - features2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }
    
    /**
     * Normalize a feature vector to unit length
     * @param features Feature vector to normalize
     * @return Normalized feature vector
     */
    public static double[] normalizeFeatureVector(double[] features) {
        double magnitude = 0.0;
        for (double feature : features) {
            magnitude += feature * feature;
        }
        magnitude = Math.sqrt(magnitude);
        
        if (magnitude == 0.0) {
            return features.clone();
        }
        
        double[] normalized = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            normalized[i] = features[i] / magnitude;
        }
        return normalized;
    }
    
    /**
     * Validate person name (not null, not empty, reasonable length)
     * @param name Person name to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPersonName(String name) {
        return name != null && !name.trim().isEmpty() 
               && name.length() >= 2 && name.length() <= 50
               && name.matches("[a-zA-Z0-9\\s._-]+");
    }
    
    /**
     * Clean up file name to be safe for file system
     * @param fileName Original file name
     * @return Cleaned file name
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "unknown";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    
    /**
     * Check if a file exists and is readable
     * @param filePath Path to file
     * @return true if file exists and is readable
     */
    public static boolean isFileReadable(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() && file.isFile() && file.canRead();
        } catch (Exception e) {
            logger.warn("Error checking file readability: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * Format memory size in bytes to human-readable format
     * @param bytes Number of bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
    
    /**
     * Get available memory information
     * @return Memory info string
     */
    public static String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return String.format("Memory - Used: %s, Free: %s, Total: %s, Max: %s",
                formatBytes(usedMemory),
                formatBytes(freeMemory),
                formatBytes(totalMemory),
                formatBytes(maxMemory));
    }
}
