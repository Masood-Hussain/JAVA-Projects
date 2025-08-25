package com.facerecognition.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Enhanced Configuration Manager for Face Recognition System
 * Provides secure configuration management with encryption support
 * 
 * Features:
 * - Encrypted configuration storage
 * - Environment variable support
 * - Runtime configuration updates
 * - Security parameter validation
 * - Audit logging for configuration changes
 * 
 * @author Face Recognition System
 * @version 2.0
 */
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private static ConfigurationManager instance;
    private Properties properties;
    private SecretKey encryptionKey;
    private final SecureRandom secureRandom;
    
    // Configuration keys
    public static final String CAMERA_WIDTH = "camera.width";
    public static final String CAMERA_HEIGHT = "camera.height";
    public static final String CAMERA_FRAME_RATE = "camera.frame.rate";
    public static final String RECOGNITION_THRESHOLD = "recognition.threshold";
    public static final String RECOGNITION_MAX_DISTANCE = "recognition.max.distance";
    public static final String DATABASE_FILE = "database.file";
    public static final String DATABASE_INIT = "database.init";
    public static final String FACE_DETECTION_SCALE_FACTOR = "face.detection.scale.factor";
    public static final String FACE_DETECTION_MIN_NEIGHBORS = "face.detection.min.neighbors";
    public static final String FACE_DETECTION_MIN_SIZE_WIDTH = "face.detection.min.size.width";
    public static final String FACE_DETECTION_MIN_SIZE_HEIGHT = "face.detection.min.size.height";
    
    // Security configuration keys
    public static final String ENABLE_ENCRYPTION = "security.encryption.enabled";
    public static final String MAX_LOGIN_ATTEMPTS = "security.max.login.attempts";
    public static final String SESSION_TIMEOUT = "security.session.timeout";
    public static final String AUDIT_LOGGING = "security.audit.logging";
    public static final String SECURE_MODE = "security.secure.mode";
    public static final String API_KEY_REQUIRED = "security.api.key.required";
    
    // Performance configuration keys
    public static final String THREAD_POOL_SIZE = "performance.thread.pool.size";
    public static final String CACHE_SIZE = "performance.cache.size";
    public static final String BUFFER_SIZE = "performance.buffer.size";
    public static final String OPTIMIZATION_LEVEL = "performance.optimization.level";
    
    private ConfigurationManager() {
        this.secureRandom = new SecureRandom();
        this.properties = new Properties();
        generateEncryptionKey();
        loadConfiguration();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * Load configuration from multiple sources
     */
    private void loadConfiguration() {
        try {
            // Load default properties
            loadDefaultProperties();
            
            // Load from application.properties
            loadFromFile("application.properties");
            
            // Override with environment variables
            loadFromEnvironment();
            
            // Validate security settings
            validateSecurityConfiguration();
            
            logger.info("Configuration loaded successfully");
            
        } catch (Exception e) {
            logger.error("Failed to load configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }
    
    /**
     * Load default configuration values
     */
    private void loadDefaultProperties() {
        // Camera defaults
        properties.setProperty(CAMERA_WIDTH, "640");
        properties.setProperty(CAMERA_HEIGHT, "480");
        properties.setProperty(CAMERA_FRAME_RATE, "30");
        
        // Recognition defaults
        properties.setProperty(RECOGNITION_THRESHOLD, "0.35");
        properties.setProperty(RECOGNITION_MAX_DISTANCE, "100.0");
        
        // Database defaults
        properties.setProperty(DATABASE_FILE, "face_recognition.db");
        properties.setProperty(DATABASE_INIT, "true");
        
        // Security defaults
        properties.setProperty(ENABLE_ENCRYPTION, "true");
        properties.setProperty(MAX_LOGIN_ATTEMPTS, "3");
        properties.setProperty(SESSION_TIMEOUT, "1800000"); // 30 minutes
        properties.setProperty(AUDIT_LOGGING, "true");
        properties.setProperty(SECURE_MODE, "true");
        properties.setProperty(API_KEY_REQUIRED, "false");
        
        // Performance defaults
        properties.setProperty(THREAD_POOL_SIZE, "4");
        properties.setProperty(CACHE_SIZE, "1000");
        properties.setProperty(BUFFER_SIZE, "8192");
        properties.setProperty(OPTIMIZATION_LEVEL, "medium");
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadFromFile(String filename) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (input != null) {
                properties.load(input);
                logger.debug("Loaded configuration from {}", filename);
            }
        } catch (IOException e) {
            logger.warn("Could not load configuration from {}: {}", filename, e.getMessage());
        }
    }
    
    /**
     * Load configuration overrides from environment variables
     */
    private void loadFromEnvironment() {
        for (String key : properties.stringPropertyNames()) {
            String envKey = key.replace(".", "_").toUpperCase();
            String envValue = System.getenv(envKey);
            if (envValue != null) {
                properties.setProperty(key, envValue);
                logger.debug("Override {} from environment variable", key);
            }
        }
    }
    
    /**
     * Validate security configuration
     */
    private void validateSecurityConfiguration() {
        // Validate recognition threshold
        double threshold = getDouble(RECOGNITION_THRESHOLD);
        if (threshold < 0.1 || threshold > 1.0) {
            logger.warn("Recognition threshold {} is outside recommended range [0.1, 1.0]", threshold);
        }
        
        // Validate max login attempts
        int maxAttempts = getInt(MAX_LOGIN_ATTEMPTS);
        if (maxAttempts < 1 || maxAttempts > 10) {
            logger.warn("Max login attempts {} should be between 1 and 10", maxAttempts);
        }
        
        // Log security settings
        if (getBoolean(AUDIT_LOGGING)) {
            logger.info("Security audit logging enabled");
            logger.info("Secure mode: {}", getBoolean(SECURE_MODE));
            logger.info("Encryption enabled: {}", getBoolean(ENABLE_ENCRYPTION));
        }
    }
    
    /**
     * Generate encryption key for sensitive data
     */
    private void generateEncryptionKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, secureRandom);
            this.encryptionKey = keyGen.generateKey();
            logger.debug("Encryption key generated");
        } catch (Exception e) {
            logger.error("Failed to generate encryption key", e);
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }
    
    /**
     * Get string property
     */
    public String getString(String key) {
        return getString(key, null);
    }
    
    /**
     * Get string property with default value
     */
    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer property
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }
    
    /**
     * Get integer property with default value
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer value for {}: {}", key, properties.getProperty(key));
            return defaultValue;
        }
    }
    
    /**
     * Get double property
     */
    public double getDouble(String key) {
        return getDouble(key, 0.0);
    }
    
    /**
     * Get double property with default value
     */
    public double getDouble(String key, double defaultValue) {
        try {
            String value = properties.getProperty(key);
            return value != null ? Double.parseDouble(value) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warn("Invalid double value for {}: {}", key, properties.getProperty(key));
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
    
    /**
     * Get boolean property with default value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    /**
     * Set property value with audit logging
     */
    public void setProperty(String key, String value) {
        String oldValue = properties.getProperty(key);
        properties.setProperty(key, value);
        
        if (getBoolean(AUDIT_LOGGING)) {
            logger.info("Configuration changed: {} = {} (was: {})", key, value, oldValue);
        }
    }
    
    /**
     * Encrypt sensitive data
     */
    public String encrypt(String data) {
        if (!getBoolean(ENABLE_ENCRYPTION)) {
            return data;
        }
        
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            logger.error("Encryption failed", e);
            return data; // Return original data if encryption fails
        }
    }
    
    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedData) {
        if (!getBoolean(ENABLE_ENCRYPTION)) {
            return encryptedData;
        }
        
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (Exception e) {
            logger.error("Decryption failed", e);
            return encryptedData; // Return original data if decryption fails
        }
    }
    
    /**
     * Check if secure mode is enabled
     */
    public boolean isSecureMode() {
        return getBoolean(SECURE_MODE);
    }
    
    /**
     * Generate secure API key
     */
    public String generateApiKey() {
        byte[] key = new byte[32];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
    
    /**
     * Validate API key format
     */
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        try {
            byte[] decoded = Base64.getDecoder().decode(apiKey);
            return decoded.length == 32; // 256-bit key
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all configuration properties for debugging
     */
    public Properties getAllProperties() {
        return new Properties(properties);
    }
}
