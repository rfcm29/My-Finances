package com.example.myfinances.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for the MyFinances application
 */
@Component
@ConfigurationProperties(prefix = "app")
@Data
@Validated
public class ApplicationProperties {

    @NotBlank
    private String name = "MyFinances";

    @NotBlank
    private String version = "1.0.0";

    private Upload upload = new Upload();
    private Security security = new Security();

    @Data
    public static class Upload {
        @NotBlank
        private String dir = "./uploads";
    }

    @Data
    @Validated
    public static class Security {
        private String rememberMeKey;
        private Jwt jwt = new Jwt();

        @Data
        @Validated
        public static class Jwt {
            private String secret;
            
            @NotNull
            @Positive
            private Long expiration = 86400000L; // 24 hours in milliseconds
        }
    }

    /**
     * Validates that required security properties are set in non-dev environments
     */
    public void validateSecurityProperties(String activeProfile) {
        if (!"dev".equals(activeProfile)) {
            if (security.rememberMeKey == null || security.rememberMeKey.trim().isEmpty()) {
                throw new IllegalStateException(
                    "REMEMBER_ME_KEY environment variable must be set in non-development environments"
                );
            }
            
            if (security.rememberMeKey.length() < 32) {
                throw new IllegalStateException(
                    "REMEMBER_ME_KEY must be at least 32 characters long"
                );
            }

            if (security.jwt.secret == null || security.jwt.secret.trim().isEmpty()) {
                throw new IllegalStateException(
                    "JWT_SECRET environment variable must be set in non-development environments"
                );
            }
            
            if (security.jwt.secret.length() < 64) {
                throw new IllegalStateException(
                    "JWT_SECRET must be at least 64 characters long"
                );
            }
        }
    }
}