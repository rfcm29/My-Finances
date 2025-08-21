package com.example.myfinances.exception;

import java.util.Map;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends BusinessException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super(message, message, "VALIDATION_ERROR");
        this.fieldErrors = Map.of();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message, message, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
    }
    
    public ValidationException(String message, String userMessage, Map<String, String> fieldErrors) {
        super(message, userMessage, "VALIDATION_ERROR");
        this.fieldErrors = fieldErrors != null ? fieldErrors : Map.of();
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}