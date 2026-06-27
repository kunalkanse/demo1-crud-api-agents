package com.ecommerce.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for JWT-based authentication.
 * Configures authentication, authorization, and CORS settings.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * Configure security filter chain.
     * - Requires Bearer JWT token for all /api/** endpoints
     * - Allows public access to Swagger UI and health endpoints
     * - Uses stateless session management (JWT-based)
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since we're using JWT tokens
                .csrf().disable()

                // Configure endpoint authorization
                .authorizeHttpRequests()
                    // Allow public access to Swagger and health endpoints
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                    // All other endpoints require authentication
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
                    .and()

                // Use stateless session management (JWT)
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()

                // Configure HTTP Basic authentication for testing
                .httpBasic();

        return http.build();
    }
}
