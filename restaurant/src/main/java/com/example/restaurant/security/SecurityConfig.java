package com.example.restaurant.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            //PUBLIC ENDPOINTS 
            
            // Authentication
            .requestMatchers("/api/auth/**").permitAll()
            
            // Table
            .requestMatchers("/api/table/**", "/api/table/all/**").permitAll()
            
            // menu-item
            .requestMatchers(
                "/api/menu-item/available",
                "/api/menu-item/all/available", 
                "/api/menu-item/all/**",
                "/api/menu-item/{id}"
            ).permitAll()
            
            // Categories
            .requestMatchers("/api/category/**").permitAll()
            
            // Orders - Khách
            .requestMatchers(HttpMethod.GET, "/api/order/table/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/order/**").permitAll()
            
            // WebSocket
            .requestMatchers("/ws/**").permitAll()
            
            // ADMIN ENDPOINTS
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/menu-item/all").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/menu-item").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/menu-item/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PATCH, "/api/menu-item/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/menu-item/**").hasRole("ADMIN")
            
            // Tất cả khác
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}