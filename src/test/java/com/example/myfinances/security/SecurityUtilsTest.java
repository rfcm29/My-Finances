package com.example.myfinances.security;

import com.example.myfinances.exception.SecurityException;
import com.example.myfinances.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class SecurityUtilsTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .password("encoded-password")
                .build();
    }

    @Test
    void getCurrentUser_WithValidAuthentication_ReturnsUser() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        Optional<User> result = SecurityUtils.getCurrentUser();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ReturnsEmpty() {
        // When
        Optional<User> result = SecurityUtils.getCurrentUser();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUser_WithNonUserPrincipal_ReturnsEmpty() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("username", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        Optional<User> result = SecurityUtils.getCurrentUser();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUserFromAuthentication_WithValidAuth_ReturnsUser() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);

        // When
        Optional<User> result = SecurityUtils.getCurrentUser(auth);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testUser);
    }

    @Test
    void getCurrentUserFromAuthentication_WithNullAuth_ReturnsEmpty() {
        // When
        Optional<User> result = SecurityUtils.getCurrentUser(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUserOrThrow_WithValidAuth_ReturnsUser() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        User result = SecurityUtils.getCurrentUserOrThrow();

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getCurrentUserOrThrow_WithNoAuth_ThrowsSecurityException() {
        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getCurrentUserOrThrow())
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void getCurrentUserOrThrowFromAuth_WithValidAuth_ReturnsUser() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);

        // When
        User result = SecurityUtils.getCurrentUserOrThrow(auth);

        // Then
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getCurrentUserOrThrowFromAuth_WithNullAuth_ThrowsSecurityException() {
        // When & Then
        assertThatThrownBy(() -> SecurityUtils.getCurrentUserOrThrow(null))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void isAuthenticated_WithValidAuth_ReturnsTrue() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        boolean result = SecurityUtils.isAuthenticated();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isAuthenticated_WithNoAuth_ReturnsFalse() {
        // When
        boolean result = SecurityUtils.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isAuthenticated_WithStringPrincipal_ReturnsFalse() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("username", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        boolean result = SecurityUtils.isAuthenticated();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getCurrentUserId_WithValidAuth_ReturnsId() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        Optional<Long> result = SecurityUtils.getCurrentUserId();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(1L);
    }

    @Test
    void getCurrentUserId_WithNoAuth_ReturnsEmpty() {
        // When
        Optional<Long> result = SecurityUtils.getCurrentUserId();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUserEmail_WithValidAuth_ReturnsEmail() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(testUser, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        Optional<String> result = SecurityUtils.getCurrentUserEmail();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("test@example.com");
    }

    @Test
    void getCurrentUserEmail_WithNoAuth_ReturnsEmpty() {
        // When
        Optional<String> result = SecurityUtils.getCurrentUserEmail();

        // Then
        assertThat(result).isEmpty();
    }
}