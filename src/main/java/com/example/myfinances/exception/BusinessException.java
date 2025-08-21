package com.example.myfinances.exception;

/**
 * Base exception for business logic errors
 */
public class BusinessException extends RuntimeException {
    
    private final String userMessage;
    private final String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.userMessage = message;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String userMessage, String errorCode) {
        super(message);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String message, String userMessage, String errorCode, Throwable cause) {
        super(message, cause);
        this.userMessage = userMessage;
        this.errorCode = errorCode;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}