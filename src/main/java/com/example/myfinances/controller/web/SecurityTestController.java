package com.example.myfinances.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Security Test Controller - FOR DEVELOPMENT ONLY
 * This controller helps debug authentication issues
 * Remove in production
 */
@Controller
@RequestMapping("/security-test")
public class SecurityTestController {

    @GetMapping("/protected")
    public String protectedEndpoint(Authentication authentication, Model model, HttpServletRequest request) {
        model.addAttribute("message", "This is a protected endpoint");
        model.addAttribute("authentication", authentication);
        model.addAttribute("isAuthenticated", authentication != null && authentication.isAuthenticated());
        model.addAttribute("principal", authentication != null ? authentication.getPrincipal() : "null");
        model.addAttribute("authorities", authentication != null ? authentication.getAuthorities() : "null");
        model.addAttribute("sessionId", request.getSession().getId());
        
        return "pages/security-test";
    }

    @GetMapping("/public")
    public String publicEndpoint(Authentication authentication, Model model, HttpServletRequest request) {
        model.addAttribute("message", "This should be accessible without authentication");
        model.addAttribute("authentication", authentication);
        model.addAttribute("sessionId", request.getSession().getId());
        
        return "pages/security-test";
    }
}