package com.example.myfinances.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for the application
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex, 
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Resource not found [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", ex.getUserMessage());
            errorResponse.put("errorCode", ex.getErrorCode());
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", ex.getUserMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(ValidationException.class)
    public Object handleValidationException(ValidationException ex,
                                          RedirectAttributes redirectAttributes,
                                          HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Validation error [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", ex.getUserMessage());
            errorResponse.put("errorCode", ex.getErrorCode());
            errorResponse.put("errorId", errorId);
            
            if (ex.hasFieldErrors()) {
                errorResponse.put("fieldErrors", ex.getFieldErrors());
            }
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", ex.getUserMessage());
        if (ex.hasFieldErrors()) {
            redirectAttributes.addFlashAttribute("fieldErrors", ex.getFieldErrors());
        }
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @ExceptionHandler(SecurityException.class)
    public Object handleSecurityException(SecurityException ex,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Security error [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", ex.getUserMessage());
            errorResponse.put("errorCode", ex.getErrorCode());
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", ex.getUserMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(BusinessException ex,
                                        RedirectAttributes redirectAttributes,
                                        HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Business error [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            // Handle AJAX requests with JSON response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", ex.getUserMessage());
            errorResponse.put("errorCode", ex.getErrorCode());
            errorResponse.put("errorId", errorId);
            
            if (ex instanceof ValidationException validationEx && validationEx.hasFieldErrors()) {
                errorResponse.put("fieldErrors", validationEx.getFieldErrors());
            }
            
            HttpStatus status = switch (ex.getErrorCode()) {
                case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
                case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
                case "SECURITY_ERROR" -> HttpStatus.FORBIDDEN;
                default -> HttpStatus.BAD_REQUEST;
            };
            
            return ResponseEntity.status(status).body(errorResponse);
        }
        
        // Handle regular web requests with redirect
        redirectAttributes.addFlashAttribute("error", ex.getUserMessage());
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Object handleValidationErrors(Exception ex,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Validation error [{}]: {}", errorId, ex.getMessage());
        
        Map<String, String> fieldErrors = new HashMap<>();
        
        if (ex instanceof MethodArgumentNotValidException validationEx) {
            for (FieldError error : validationEx.getBindingResult().getFieldErrors()) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
        } else if (ex instanceof BindException bindEx) {
            for (FieldError error : bindEx.getBindingResult().getFieldErrors()) {
                fieldErrors.put(error.getField(), error.getDefaultMessage());
            }
        }
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Please check the form for errors");
            errorResponse.put("errorCode", "VALIDATION_ERROR");
            errorResponse.put("errorId", errorId);
            errorResponse.put("fieldErrors", fieldErrors);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", "Please check the form for errors");
        redirectAttributes.addFlashAttribute("fieldErrors", fieldErrors);
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex,
                                          RedirectAttributes redirectAttributes,
                                          HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Constraint violation [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Please check your input");
            errorResponse.put("errorCode", "VALIDATION_ERROR");
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", "Invalid data provided. Please check your input.");
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex,
                                             RedirectAttributes redirectAttributes,
                                             HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Data integrity violation [{}]: {}", errorId, ex.getMessage());
        
        String userMessage = "Operation failed due to data constraints. Please check your input.";
        
        // Try to provide more specific error messages
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage().toLowerCase() : "";
        if (rootCause.contains("duplicate") || rootCause.contains("unique")) {
            userMessage = "This record already exists. Please check for duplicates.";
        } else if (rootCause.contains("foreign key") || rootCause.contains("constraint")) {
            userMessage = "Cannot complete operation due to data dependencies.";
        }
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", userMessage);
            errorResponse.put("errorCode", "DATA_INTEGRITY_ERROR");
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", userMessage);
        
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/dashboard");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex,
                                   RedirectAttributes redirectAttributes,
                                   HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Access denied [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "You don't have permission to access this resource");
            errorResponse.put("errorCode", "ACCESS_DENIED");
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", "You don't have permission to access this resource");
        return "redirect:/dashboard";
    }

    @ExceptionHandler(AuthenticationException.class)
    public Object handleAuthenticationException(AuthenticationException ex,
                                              RedirectAttributes redirectAttributes,
                                              HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.warn("Authentication error [{}]: {}", errorId, ex.getMessage());
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "Please log in to continue");
            errorResponse.put("errorCode", "AUTHENTICATION_REQUIRED");
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", "Please log in to continue");
        return "redirect:/login";
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        
        String errorId = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unexpected error [{}]: {}", errorId, ex.getMessage(), ex);
        
        if (isAjaxRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "An error occurred. Please try again later.");
            errorResponse.put("errorCode", "INTERNAL_ERROR");
            errorResponse.put("errorId", errorId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        
        redirectAttributes.addFlashAttribute("error", 
            "An unexpected error occurred. Please try again later. Error ID: " + errorId);
        
        return "redirect:/dashboard";
    }


    private boolean isAjaxRequest(HttpServletRequest request) {
        String xRequestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        return "XMLHttpRequest".equals(xRequestedWith) || 
               (accept != null && accept.contains("application/json"));
    }

}