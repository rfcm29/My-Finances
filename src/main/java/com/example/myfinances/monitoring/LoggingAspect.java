package com.example.myfinances.monitoring;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for logging method execution times and parameters
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.example.myfinances.service..*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        if (log.isDebugEnabled()) {
            log.debug("Entering method: {} with args: {}", methodName, Arrays.toString(joinPoint.getArgs()));
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            if (executionTime > 1000) { // Log slow operations (> 1 second)
                log.warn("Slow operation detected: {} took {}ms", methodName, executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("Method {} completed in {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Exception in method {}: {}", methodName, e.getMessage(), e);
            throw e;
        }
    }

    @Around("execution(* com.example.myfinances.repository..*(..))")
    public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            if (executionTime > 500) { // Log slow database operations (> 500ms)
                log.warn("Slow database operation: {} took {}ms", methodName, executionTime);
            } else if (log.isTraceEnabled()) {
                log.trace("Database operation {} completed in {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Database exception in method {}: {}", methodName, e.getMessage());
            throw e;
        }
    }

    @Around("execution(* com.example.myfinances.controller.web..*(..))")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            
            if (executionTime > 2000) { // Log slow web requests (> 2 seconds)
                log.warn("Slow web request: {} took {}ms", methodName, executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("Web request {} completed in {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Controller exception in method {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}