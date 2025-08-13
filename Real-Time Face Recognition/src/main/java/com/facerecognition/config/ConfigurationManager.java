package com.facerecognition.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration Manager for the Face Recognition System
 * Handles loading and managing application configuration properties
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static final String CONFIG_FILE = "application.properties";
    
    private static ConfigurationManager instance;
    private final Properties properties;
    
    private ConfigurationManager() {
        properties = new Properties();
        loadConfiguration();
    }
    
    /**
     * Get singleton instance
     * @return ConfigurationManager instance
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.warn("Configuration file {} not found, using defaults", CONFIG_FILE);
                loadDefaultConfiguration();
                return;
            }
            
            properties.load(input);
            logger.info("Configuration loaded successfully from {}", CONFIG_FILE);
            
        } catch (IOException e) {
            logger.error("Error loading configuration: ", e);
            loadDefaultConfiguration();
        }
    }
    
    /**
     * Load default configuration values
     */
    private void loadDefaultConfiguration() {
        // Camera settings
        properties.setProperty("camera.width", "640");
        properties.setProperty("camera.height", "480");
        properties.setProperty("camera.frame.rate", "30");
        
        // Recognition settings
        properties.setProperty("recognition.threshold", "0.6");
        properties.setProperty("recognition.max.distance", "100.0");
        
        // Database settings
        properties.setProperty("database.file", "face_recognition.db");
        properties.setProperty("database.init", "true");
        
        // Face detection settings
        properties.setProperty("face.detection.scale.factor", "1.1");
        properties.setProperty("face.detection.min.neighbors", "5");
        properties.setProperty("face.detection.min.size.width", "30");
        properties.setProperty("face.detection.min.size.height", "30");
        properties.setProperty("face.detection.max.size.width", "300");
        properties.setProperty("face.detection.max.size.height", "300");
        
        // GUI settings
        properties.setProperty("gui.window.width", "800");
        properties.setProperty("gui.window.height", "600");
        properties.setProperty("gui.window.title", "Real-Time Face Recognition System");
        
        logger.info("Default configuration loaded");
    }
    
    /**
     * Get string property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value
     */
    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for key {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get double property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value
     */
    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for key {}, using default: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    // Convenience methods for commonly used configuration values
    
    public int getCameraWidth() {
        return getInt("camera.width", 640);
    }
    
    public int getCameraHeight() {
        return getInt("camera.height", 480);
    }
    
    public int getCameraFrameRate() {
        return getInt("camera.frame.rate", 30);
    }
    
    public double getRecognitionThreshold() {
        return getDouble("recognition.threshold", 0.6);
    }
    
    public String getDatabaseFile() {
        return getString("database.file", "face_recognition.db");
    }
    
    public double getFaceDetectionScaleFactor() {
        return getDouble("face.detection.scale.factor", 1.1);
    }
    
    public int getFaceDetectionMinNeighbors() {
        return getInt("face.detection.min.neighbors", 5);
    }
    
    public int getGuiWindowWidth() {
        return getInt("gui.window.width", 800);
    }
    
    public int getGuiWindowHeight() {
        return getInt("gui.window.height", 600);
    }
    
    public String getGuiWindowTitle() {
        return getString("gui.window.title", "Real-Time Face Recognition System");
    }
}
