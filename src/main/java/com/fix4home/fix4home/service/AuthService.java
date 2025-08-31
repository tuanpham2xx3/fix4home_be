package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
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
import com.fix4home.fix4home.security.CustomUserDetails;
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
import org.springframework.beans.factory.annotation.Value;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService extends BaseService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    @Value("${admin.registration.key}")
    private String adminRegistrationKey;

    @Value("${admin.registration.enabled:false}")
    private boolean adminRegistrationEnabled;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logBusinessOperation("REGISTER_USER", "username=" + request.getUsername(), "role=" + request.getRole());

        // Validate request
        validateRequired(request, "request");
        validateRequired(request.getUsername(), "username");
        validateRequired(request.getPassword(), "password");
        validateRequired(request.getEmail(), "email");
        validateRequired(request.getPhoneNumber(), "phoneNumber");
        validateRequired(request.getRole(), "role");
        validateRequired(request.getFullName(), "fullName");

        // Validate admin registration
        if (request.getRole() == Role.ADMIN) {
            if (!adminRegistrationEnabled) {
                throw new BusinessValidationException("Admin registration is currently disabled");
            }
            if (request.getAdminKey() == null || !request.getAdminKey().equals(adminRegistrationKey)) {
                throw new BusinessValidationException("Invalid admin registration key");
            }
        }

        // Validate unique constraints
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
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

        // Send email verification code for non-admin users
        if (savedUser.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
            try {
                boolean emailSent = emailVerificationService.sendVerificationCode(
                        savedUser.getEmail(), 
                        "registration", 
                        savedUser.getId()
                );
                if (!emailSent) {
                    log.warn("Failed to send verification email to: {}", savedUser.getEmail());
                }
            } catch (Exception e) {
                log.error("Error sending verification email to: {}", savedUser.getEmail(), e);
            }
        }

        // Generate token (note: user still needs to verify email before they can login)
        String token = tokenProvider.generateToken(savedUser.getUsername());

        return buildAuthResponse(savedUser, token, request);
    }

    public AuthResponse login(LoginRequest request) {
        logBusinessOperation("LOGIN_USER", "usernameOrEmail=" + request.getUsernameOrEmail());

        // Validate request
        validateRequired(request, "request");
        validateRequired(request.getUsernameOrEmail(), "usernameOrEmail");
        validateRequired(request.getPassword(), "password");

        // Authenticate user
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = findUserByUsernameOrEmail(request.getUsernameOrEmail());

        // Check user status
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw AccountNotActiveException.inactive();
        }
        
        if (user.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
            throw AccountNotActiveException.withStatus(UserStatus.PENDING_EMAIL_VERIFICATION);
        }

        // For technician, check if approved
        if (user.getRole() == Role.TECHNICIAN) {
            TechnicianProfile techProfile = findTechnicianProfileByUser(user);
            
            if (techProfile.getStatus() == UserStatus.PENDING_APPROVAL) {
                throw AccountNotActiveException.pendingApproval();
            } else if (techProfile.getStatus() == UserStatus.REJECTED) {
                throw AccountNotActiveException.withStatus(UserStatus.REJECTED);
            }
        }

        // Generate token
        String token = tokenProvider.generateToken(authentication);

        return buildAuthResponse(user, token, null);
    }

    private UserStatus determineUserStatus(Role role) {
        // All users need email verification first
        // Technician will need additional admin approval after email verification
        return role == Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING_EMAIL_VERIFICATION;
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
                // Validate technician-specific fields
                validateRequired(request.getSkills(), "skills");
                validateRequired(request.getExperience(), "experience");

                TechnicianProfile technicianProfile = TechnicianProfile.builder()
                        .user(user)
                        .fullName(request.getFullName())
                        .skills(request.getSkills())
                        .experience(request.getExperience())
                        .rating(0.0f)
                        .status(UserStatus.PENDING_APPROVAL) // Pending approval
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
                CustomerProfile profile = findCustomerProfileByUser(user);
                if (profile != null) {
                    builder.fullName(profile.getFullName());
                }
            }
            case TECHNICIAN -> {
                TechnicianProfile profile = findTechnicianProfileByUser(user);
                if (profile != null) {
                    builder.fullName(profile.getFullName())
                           .skills(profile.getSkills())
                           .experience(profile.getExperience())
                           .rating(profile.getRating());
                }
            }
            case ADMIN -> {
                // Admin profile handling if needed
                builder.fullName(user.getUsername()); // Use username for admin since no profile table
            }
        }

        return builder.build();
    }

    private User findUserByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new InvalidCredentialsException()));
    }

    private CustomerProfile findCustomerProfileByUser(User user) {
        return customerProfileRepository.findByUser(user)
                .orElseThrow(() -> UserNotFoundException.withMessage("Customer profile not found for user: " + user.getId()));
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianNotFoundException(user.getId(), "technician profile"));
    }

    public String generateAccessToken(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        return tokenProvider.generateToken(authentication);
    }
} 