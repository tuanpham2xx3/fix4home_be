package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.auth.AuthResponse;
import com.fix4home.fix4home.model.dto.auth.ChangePasswordRequest;
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
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;

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
    private final ActivationTokenService activationTokenService;
    private final RefreshTokenService refreshTokenService;
    private final FileManagementService fileManagementService;
    private final ConversationService conversationService;

    @Value("${admin.registration.key}")
    private String adminRegistrationKey;

    @Value("${admin.registration.enabled:false}")
    private boolean adminRegistrationEnabled;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate request
        validateRequired(request, "request");
        validateRequired(request.getPassword(), "password");
        validateRequired(request.getEmail(), "email");
        validateRequired(request.getRole(), "role");
        
        // Auto-generate username if not provided
        String username;
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            username = generateUniqueUsername();
            log.info("Auto-generated username: {} for email: {}", username, request.getEmail());
        } else {
            // Validate provided username
            username = request.getUsername().trim();
            if (username.length() < 3 || username.length() > 50) {
                throw new BusinessValidationException("Username must be between 3 and 50 characters");
            }
            // Check if username already exists
            if (userRepository.existsByUsername(username)) {
                throw new UserAlreadyExistsException("username", username);
            }
        }
        
        logBusinessOperation("REGISTER_USER", "username=" + username + ", email=" + request.getEmail(), "role=" + request.getRole());
        
        // Normalize empty strings to null for optional fields
        if (request.getFullName() != null && request.getFullName().trim().isEmpty()) {
            request.setFullName(null);
        }
        if (request.getPhoneNumber() != null && request.getPhoneNumber().trim().isEmpty()) {
            request.setPhoneNumber(null);
        }
        
        // Validate phone number format only if provided (for CUSTOMER)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().matches("^\\d{10,11}$")) {
            throw new BusinessValidationException("Phone number must be 10-11 digits");
        }
        
        // Validate role-specific fields
        if (request.getRole() == Role.TECHNICIAN) {
            validateRequired(request.getFullName(), "fullName");
            validateRequired(request.getPhoneNumber(), "phoneNumber");
            // Validate phone format for technician
            if (!request.getPhoneNumber().matches("^\\d{10,11}$")) {
                throw new BusinessValidationException("Phone number must be 10-11 digits");
            }
        }
        // For CUSTOMER, fullName and phoneNumber are optional

        // Validate admin registration
        if (request.getRole() == Role.ADMIN) {
            if (!adminRegistrationEnabled) {
                throw new BusinessValidationException("Admin registration is currently disabled");
            }
            if (request.getAdminKey() == null || !request.getAdminKey().equals(adminRegistrationKey)) {
                throw new BusinessValidationException("Invalid admin registration key");
            }
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Create user
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())  // Can be null for CUSTOMER
                .role(request.getRole())
                .status(determineUserStatus(request.getRole()))
                .build();

        User savedUser = userRepository.save(user);

        // Create profile based on role
        createUserProfile(savedUser, request);

        // Send email verification for non-admin users
        // Email service MUST send email successfully, otherwise registration fails
        if (savedUser.getStatus() == UserStatus.PENDING_EMAIL_VERIFICATION) {
            try {
                ActivationTokenService.ActivationTokenResponse response = 
                    activationTokenService.generateActivationToken(savedUser, "registration");
                if (!response.isSuccess()) {
                    log.error("Failed to send verification email to: {}. Response: {}", 
                             savedUser.getEmail(), response.getMessage());
                    throw new BusinessValidationException(
                        "Failed to send activation email: " + response.getMessage() + 
                        ". Please try again later.");
                }
                log.info("Activation email sent successfully to: {}", savedUser.getEmail());
            } catch (BusinessValidationException e) {
                // Re-throw business exceptions
                throw e;
            } catch (Exception e) {
                log.error("Exception sending verification email to: {}", savedUser.getEmail(), e);
                throw new BusinessValidationException(
                    "Failed to send activation email. Please try again later.");
            }
        }

        // Generate token (note: user still needs to verify email before they can login)
        String token = tokenProvider.generateToken(savedUser);
        
        // Create chatbot conversation for new user (async, don't block registration)
        try {
            conversationService.createChatbotConversation(savedUser.getId());
            log.info("Chatbot conversation created for user: {}", savedUser.getId());
        } catch (Exception e) {
            log.warn("Failed to create chatbot conversation for user {}: {}", 
                savedUser.getId(), e.getMessage());
            // Don't fail registration if chatbot conversation creation fails
        }

        return buildAuthResponse(savedUser, token, request);
    }

    public AuthResponse login(LoginRequest request) {
        logBusinessOperation("LOGIN_USER", "usernameOrEmail=" + request.getUsernameOrEmail());

        // Validate request
        validateRequired(request, "request");
        
        // Get usernameOrEmail from request (supports email, username, or usernameOrEmail field)
        String usernameOrEmailValue = request.getUsernameOrEmail();
        if (usernameOrEmailValue == null || usernameOrEmailValue.trim().isEmpty()) {
            throw new BusinessValidationException("Username or email is required");
        }
        
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
        String token = tokenProvider.generateToken(user);

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
                        .fullName(request.getFullName())  // Can be null for CUSTOMER
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

        // Get user avatar URL
        try {
            String avatarUrl = fileManagementService.getUserAvatarUrl(user.getId());
            builder.avatarUrl(avatarUrl);
        } catch (Exception e) {
            log.warn("Error getting avatar URL for user {}: {}", user.getId(), e.getMessage());
            // Continue without avatar URL - don't fail the response
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
        return tokenProvider.generateToken(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        logBusinessOperation("CHANGE_PASSWORD", "userId=" + getCurrentUserId());

        // Validate request
        validateRequired(request, "request");
        validateRequired(request.getOldPassword(), "oldPassword");
        validateRequired(request.getNewPassword(), "newPassword");

        // Get current authenticated user
        User user = getCurrentUser();

        // Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed: Invalid old password for user: {}", user.getUsername());
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        // Validate new password is different from old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessValidationException("New password must be different from old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Reset mustChangePassword flag
        user.setMustChangePassword(false);

        // Save user
        userRepository.save(user);

        // Revoke all refresh tokens to log out all devices
        refreshTokenService.deleteByUserId(user.getId());

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

    /**
     * Generate unique username with format "user" + random 8-digit number
     * Example: user12345678, user87654321, user45678901
     * 
     * @return unique username
     */
    private String generateUniqueUsername() {
        SecureRandom random = new SecureRandom();
        int maxAttempts = 100; // Prevent infinite loop
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            // Generate random number between 10000000 and 99999999 (8 digits)
            int randomNumber = 10000000 + random.nextInt(90000000); // 8 digits
            String username = "user" + randomNumber;
            
            // Check if username already exists
            if (!userRepository.existsByUsername(username)) {
                return username;
            }
            
            attempt++;
            log.debug("Username {} already exists, generating new one... (attempt {})", username, attempt);
        }
        
        // Fallback: use timestamp if all attempts fail (very unlikely)
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5); // Last 8 digits
        String fallbackUsername = "user" + timestamp;
        
        // If still exists, add random suffix
        if (userRepository.existsByUsername(fallbackUsername)) {
            int randomSuffix = random.nextInt(1000);
            fallbackUsername = "user" + timestamp + randomSuffix;
        }
        
        log.warn("Generated fallback username: {} after {} attempts", fallbackUsername, maxAttempts);
        return fallbackUsername;
    }
} 