package com.example.myfinances.exception;

/**
 * Exception thrown for security-related errors
 */
public class SecurityException extends BusinessException {
    
    public SecurityException(String message) {
        super(message, "Access denied", "SECURITY_ERROR");
    }
    
    public SecurityException(String message, String userMessage) {
        super(message, userMessage, "SECURITY_ERROR");
    }
    
    public SecurityException(String message, Throwable cause) {
        super(message, "Access denied", "SECURITY_ERROR", cause);
    }
}