package com.facerecognition.exception;

/**
 * Base exception class for Face Recognition System
 * All custom exceptions should extend this class
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceRecognitionException extends Exception {
    
    public FaceRecognitionException(String message) {
        super(message);
    }
    
    public FaceRecognitionException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FaceRecognitionException(Throwable cause) {
        super(cause);
    }
}
