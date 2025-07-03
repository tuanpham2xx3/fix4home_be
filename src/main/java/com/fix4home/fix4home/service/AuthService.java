package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.exception.ResourceAlreadyExistsException;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.LoginRequest;
import com.fix4home.fix4home.model.dto.auth.RegisterRequest;
import com.fix4home.fix4home.model.entity.CustomerProfile;
import com.fix4home.fix4home.model.entity.TechnicianProfile;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.CustomerProfileRepository;
import com.fix4home.fix4home.repository.TechnicianProfileRepository;
import com.fix4home.fix4home.repository.UserRepository;
import com.fix4home.fix4home.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with username: {}", request.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already in use!");
        }

        // Create user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole())
                .status(determineUserStatus(request.getRole()))
                .build();

        User savedUser = userRepository.save(user);

        // Create profile based on role
        createUserProfile(savedUser, request);

        // Generate token
        String token = tokenProvider.generateToken(savedUser.getUsername());

        return buildAuthResponse(savedUser, token, request);
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsernameOrEmail());

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new BadRequestException("Invalid credentials")));

        // Check if user is active
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("Account is inactive. Please contact admin.");
        }

        // For technician, check if approved
        if (user.getRole() == Role.TECHNICIAN) {
            TechnicianProfile techProfile = technicianProfileRepository.findByUser(user)
                    .orElseThrow(() -> new BadRequestException("Technician profile not found"));
            
            if (techProfile.getStatus() == UserStatus.INACTIVE) {
                throw new BadRequestException("Technician account is pending approval.");
            }
        }

        // Generate token
        String token = tokenProvider.generateToken(authentication);

        return buildAuthResponse(user, token, null);
    }

    private UserStatus determineUserStatus(Role role) {
        // Technician needs admin approval, others are active immediately
        return role == Role.TECHNICIAN ? UserStatus.INACTIVE : UserStatus.ACTIVE;
    }

    private void createUserProfile(User user, RegisterRequest request) {
        switch (user.getRole()) {
            case CUSTOMER -> {
                CustomerProfile customerProfile = CustomerProfile.builder()
                        .user(user)
                        .fullName(request.getFullName())
                        .build();
                customerProfileRepository.save(customerProfile);
            }
            case TECHNICIAN -> {
                TechnicianProfile technicianProfile = TechnicianProfile.builder()
                        .user(user)
                        .fullName(request.getFullName())
                        .skills(request.getSkills())
                        .experience(request.getExperience())
                        .rating(0.0f)
                        .status(UserStatus.INACTIVE) // Pending approval
                        .build();
                technicianProfileRepository.save(technicianProfile);
            }
            case ADMIN -> {
                // Admin doesn't need profile for now
            }
        }
    }

    private AuthResponse buildAuthResponse(User user, String token, RegisterRequest request) {
        AuthResponse.AuthResponseBuilder builder = AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt());

        // Add profile information
        switch (user.getRole()) {
            case CUSTOMER -> {
                CustomerProfile profile = customerProfileRepository.findByUser(user).orElse(null);
                if (profile != null) {
                    builder.fullName(profile.getFullName());
                }
            }
            case TECHNICIAN -> {
                TechnicianProfile profile = technicianProfileRepository.findByUser(user).orElse(null);
                if (profile != null) {
                    builder.fullName(profile.getFullName())
                           .skills(profile.getSkills())
                           .experience(profile.getExperience())
                           .rating(profile.getRating());
                }
            }
        }

        return builder.build();
    }
} 