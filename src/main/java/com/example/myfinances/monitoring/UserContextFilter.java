package com.example.myfinances.monitoring;

import com.example.myfinances.security.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add user context to logging MDC
 */
@Component
@Order(1)
public class UserContextFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Add request ID for tracing
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(REQUEST_ID, requestId);
            response.setHeader("X-Request-ID", requestId);

            // Add user context if authenticated
            SecurityUtils.getCurrentUser().ifPresent(user -> {
                MDC.put(USER_ID, user.getId().toString());
                MDC.put(USER_EMAIL, user.getEmail());
            });

            filterChain.doFilter(request, response);
            
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }
}