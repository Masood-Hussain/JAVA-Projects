package com.facerecognition.exception;

/**
 * Exception thrown when database operations fail
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class DatabaseException extends FaceRecognitionException {
    
    public DatabaseException(String message) {
        super(message);
    }
    
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
