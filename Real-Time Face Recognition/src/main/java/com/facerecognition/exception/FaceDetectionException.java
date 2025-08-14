package com.facerecognition.exception;

/**
 * Exception thrown when face detection operations fail
 * 
 * @author Face Recognition System
 * @version 1.0
 */
public class FaceDetectionException extends FaceRecognitionException {
    
    public FaceDetectionException(String message) {
        super(message);
    }
    
    public FaceDetectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
