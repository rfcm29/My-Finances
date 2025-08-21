package com.example.myfinances.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Validates security properties on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityPropertiesValidator implements ApplicationListener<ApplicationReadyEvent> {

    private final ApplicationProperties applicationProperties;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            applicationProperties.validateSecurityProperties(activeProfile);
            log.info("Security properties validation passed for profile: {}", activeProfile);
        } catch (IllegalStateException e) {
            log.error("Security properties validation failed: {}", e.getMessage());
            
            if (!"dev".equals(activeProfile)) {
                log.error("Application will not start due to missing security configuration");
                System.exit(1);
            } else {
                log.warn("Development mode: Security properties validation failed but continuing startup");
            }
        }
    }
}