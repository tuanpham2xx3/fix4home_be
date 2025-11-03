package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.CustomUserDetailsService;
import com.fix4home.fix4home.security.JwtAuthenticationEntryPoint;
import com.fix4home.fix4home.security.JwtAuthenticationFilter;
import com.fix4home.fix4home.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/test/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Public service endpoints (GET requests for browsing)
                .requestMatchers(HttpMethod.GET, "/api/v1/services").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/search").permitAll()
                .requestMatchers(HttpMethod.GET, SecurityConstants.API_V1_SERVICES_WILDCARD).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/paginated").permitAll()
                
                // Admin service endpoints (modification operations)
                .requestMatchers(HttpMethod.POST, "/api/v1/services").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, SecurityConstants.API_V1_SERVICES_WILDCARD).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, SecurityConstants.API_V1_SERVICES_WILDCARD).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/services/*/toggle-status").hasRole("ADMIN")
                .requestMatchers("/api/v1/services/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/services/*/hard").hasRole("ADMIN")
                
                // Customer endpoints - authenticated users only (role-based security in controller)
                .requestMatchers(SecurityConstants.API_V1_CUSTOMERS).authenticated()
                
                // Service Request endpoints - authenticated users only (role-based security in controller)
                .requestMatchers(SecurityConstants.API_V1_SERVICE_REQUESTS).authenticated()
                
                // Complaint endpoints - authenticated users only (role-based security in controller)
                .requestMatchers("/api/v1/complaints/**").authenticated()
                
                // Public technician endpoints (GET requests for browsing)
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/by-rating").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/*/skills").permitAll()
                .requestMatchers(HttpMethod.GET, SecurityConstants.API_V1_TECHNICIANS_WILDCARD).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/skills").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/skills/search").permitAll()
                
                // Technician self-management endpoints
                .requestMatchers("/api/v1/technicians/me/**").hasRole("TECHNICIAN")
                
                // Admin technician management endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/paginated").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/technicians/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, SecurityConstants.API_V1_TECHNICIANS_WILDCARD).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/technicians/*/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/technicians/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/technicians/*/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/technicians/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/technicians/skills/*").hasRole("ADMIN")
                
                // Notification endpoints - authenticated users only (role-based security in controller)
                .requestMatchers(SecurityConstants.API_V1_NOTIFICATIONS).authenticated()
                
                // Payment endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/payments/methods").permitAll()
                .requestMatchers(SecurityConstants.API_V1_PAYMENTS).authenticated()
                
                // Feedback endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/feedbacks/public").permitAll()
                .requestMatchers(SecurityConstants.API_V1_FEEDBACKS).authenticated()
                
                // File Management endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/files/download/**").permitAll() // Public file access
                .requestMatchers(HttpMethod.GET, "/api/v1/files/view/**").permitAll() // Public file viewing
                .requestMatchers("/api/v1/files/upload/**").authenticated() // File upload requires auth
                .requestMatchers("/api/v1/files/admin/**").hasRole("ADMIN") // Admin file management
                .requestMatchers("/api/v1/files/**").authenticated() // Other file operations require auth
                
                // Chat endpoints - authenticated users only (role-based security in controller)
                .requestMatchers("/api/v1/chat/**").authenticated()
                
                // WebSocket endpoints - authenticated users only
                .requestMatchers("/ws/**").authenticated()
                .requestMatchers("/ws-native/**").authenticated()
                
                // Admin endpoints - all require ADMIN role
                .requestMatchers(SecurityConstants.API_V1_ADMIN).hasRole("ADMIN")
                
                // Role-based access
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/technician/**").hasRole("TECHNICIAN")
                
                // Any other request needs authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow only specific origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // Dev frontend
            "https://fix4home.com"    // Production frontend
        ));
        
        // Allow specific methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Device-Id"
        ));
        
        // Allow credentials (cookies)
        configuration.setAllowCredentials(true);
        
        // Expose the Authorization header
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 