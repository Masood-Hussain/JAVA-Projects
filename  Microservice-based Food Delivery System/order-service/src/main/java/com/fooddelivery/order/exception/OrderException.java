package com.fooddelivery.order.exception;

/**
 * Custom exception for order-related business logic errors
 */
public class OrderException extends RuntimeException {
    
    public OrderException(String message) {
        super(message);
    }
    
    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}