package com.example.myfinances.security;

import com.example.myfinances.exception.SecurityException;
import com.example.myfinances.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility class for security-related operations
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class
    }

    /**
     * Safely extracts the current authenticated user from the security context
     * 
     * @return Optional containing the authenticated User, or empty if not authenticated or not a User
     */
    public static Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getCurrentUser(authentication);
    }

    /**
     * Safely extracts the user from the given authentication object
     * 
     * @param authentication the authentication object
     * @return Optional containing the authenticated User, or empty if not a User
     */
    public static Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user);
        }

        return Optional.empty();
    }

    /**
     * Gets the current authenticated user or throws an exception
     * 
     * @return the authenticated User
     * @throws SecurityException if no user is authenticated
     */
    public static User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new SecurityException("No authenticated user found", "Please log in to continue"));
    }

    /**
     * Gets the current authenticated user from the given authentication or throws an exception
     * 
     * @param authentication the authentication object
     * @return the authenticated User
     * @throws SecurityException if no user is authenticated
     */
    public static User getCurrentUserOrThrow(Authentication authentication) {
        return getCurrentUser(authentication)
                .orElseThrow(() -> new SecurityException("No authenticated user found", "Please log in to continue"));
    }

    /**
     * Checks if the current user is authenticated
     * 
     * @return true if authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && 
               !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Gets the current user's ID safely
     * 
     * @return Optional containing the user ID, or empty if not authenticated
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Gets the current user's email safely
     * 
     * @return Optional containing the user email, or empty if not authenticated
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUser().map(User::getEmail);
    }
}