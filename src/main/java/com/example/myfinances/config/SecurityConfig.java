package com.example.myfinances.config;

import com.example.myfinances.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**") // Only disable CSRF for API endpoints
                )
                .authorizeHttpRequests(authz -> authz
                        // Public resources and pages
                        .requestMatchers("/", "/home", "/login", "/register").permitAll()
                        .requestMatchers("/error", "/favicon.ico").permitAll()
                        // Static resources
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        // H2 Console and security test endpoints are handled by DevSecurityConfig in dev profile
                        // API endpoints (future Android app)
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .permitAll()
                )
                // Temporarily disable remember-me until persistent_logins table is created
                // .rememberMe(rememberMe -> rememberMe
                //         .key("MyFinances-RememberMe-Key")
                //         .tokenRepository(persistentTokenRepository())
                //         .tokenValiditySeconds(86400 * 30)
                //         .userDetailsService(userDetailsService)
                // )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/login?expired=true")
                        .and()
                        .sessionFixation().migrateSession()
                        .invalidSessionUrl("/login?invalid=true")
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // For AJAX requests, return 401
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json")) {
                                response.setStatus(401);
                                response.getWriter().write("{\"error\":\"Authentication required\"}");
                            } else {
                                // For regular requests, redirect to login
                                response.sendRedirect("/login");
                            }
                        })
                );

        // Configure headers properly
        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                        .maxAgeInSeconds(31536000)
                        .includeSubDomains(true)
                )
        );


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Temporarily disabled until persistent_logins table is created
    // @Bean
    // public PersistentTokenRepository persistentTokenRepository() {
    //     JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
    //     tokenRepository.setDataSource(dataSource);
    //     return tokenRepository;
    // }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}