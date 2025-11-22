package com.lsiproject.app.propertymanagementmicroservice.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    // Removed: private CostumeUserService costumeUserService;
    // This is no longer needed in a stateless, JWT-claim-based microservice.

    /**
     * Defines the security filter chain (CORS, CSRF, Session, Auth rules).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless APIs
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session policy: Do not create or use HTTP sessions
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Allow health checks or public endpoints if needed.
                        // NOTE: If you had authentication endpoints here, they belong in the Auth service, not here.
                        .requestMatchers("/api/public/**").permitAll()

                        // Require authentication for all other endpoints
                        .anyRequest().authenticated()
                );

        // JWT Filter: Runs before the standard Spring Security filters (like the one that checks credentials)
        http.addFilterBefore(
                new JwtAuthFilter(jwtUtil), // Only JwtUtil is needed now
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    /**
     * Defines the detailed CORS configuration.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:4200"));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization"));

        source.registerCorsConfiguration("/**", config);
        return source;
    }
}