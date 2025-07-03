package com.fix4home.fix4home.config;

import com.fix4home.fix4home.security.CustomUserDetailsService;
import com.fix4home.fix4home.security.JwtAuthenticationEntryPoint;
import com.fix4home.fix4home.security.JwtAuthenticationFilter;
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
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/test/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // Public service endpoints (GET requests for browsing)
                .requestMatchers(HttpMethod.GET, "/api/v1/services").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/services/paginated").permitAll()
                
                // Admin service endpoints (modification operations)
                .requestMatchers(HttpMethod.POST, "/api/v1/services").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/services/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/services/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/services/*/toggle-status").hasRole("ADMIN")
                .requestMatchers("/api/v1/services/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/services/*/hard").hasRole("ADMIN")
                
                // Customer endpoints - authenticated users only (role-based security in controller)
                .requestMatchers("/api/v1/customers/**").authenticated()
                
                // Public technician endpoints (GET requests for browsing)
                .requestMatchers(HttpMethod.GET, "/api/technicians/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/by-rating").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/*/skills").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/skills").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/technicians/skills/search").permitAll()
                
                // Technician self-management endpoints
                .requestMatchers("/api/technicians/me/**").hasRole("TECHNICIAN")
                
                // Admin technician management endpoints
                .requestMatchers(HttpMethod.GET, "/api/technicians").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/technicians/paginated").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/technicians/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/technicians/*").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/technicians/*/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/technicians/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/technicians/*/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/technicians/skills").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/technicians/skills/*").hasRole("ADMIN")
                
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
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 