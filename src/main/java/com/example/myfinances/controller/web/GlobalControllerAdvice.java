package com.example.myfinances.controller.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice to add common model attributes to all controllers
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    
    /**
     * Adds the current request path to all models for navigation highlighting
     */
    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}