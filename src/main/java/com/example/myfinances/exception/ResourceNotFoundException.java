package com.example.myfinances.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, Object id) {
        super(
            String.format("%s with id %s not found", resource, id),
            String.format("%s not found", resource),
            "RESOURCE_NOT_FOUND"
        );
    }
    
    public ResourceNotFoundException(String message) {
        super(message, message, "RESOURCE_NOT_FOUND");
    }
    
    public ResourceNotFoundException(String message, String userMessage) {
        super(message, userMessage, "RESOURCE_NOT_FOUND");
    }
}