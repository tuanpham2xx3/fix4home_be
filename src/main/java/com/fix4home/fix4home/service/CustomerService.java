package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.model.dto.customer.*;
import com.fix4home.fix4home.model.entity.Address;
import com.fix4home.fix4home.model.entity.CustomerProfile;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.repository.AddressRepository;
import com.fix4home.fix4home.repository.CustomerProfileRepository;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final AddressRepository addressRepository;

    // ==================== PROFILE MANAGEMENT ====================

    @Transactional(readOnly = true)
    public CustomerProfileDTO getCustomerProfile(Long userId) {
        log.info("Fetching customer profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("User is not a customer");
        }

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Customer profile not found for user: " + userId));

        return convertToCustomerProfileDTO(user, profile);
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO getMyProfile() {
        log.info("Fetching profile for current authenticated user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Current user is not a customer");
        }

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Customer profile not found"));

        return convertToCustomerProfileDTO(user, profile);
    }

    @Transactional
    public CustomerProfileDTO updateCustomerProfile(Long userId, UpdateCustomerProfileRequest request) {
        log.info("Updating customer profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("User is not a customer");
        }

        CustomerProfile profile = customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Customer profile not found for user: " + userId));

        // Update User fields
        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update Profile fields
        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        
        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }
        
        if (request.getDob() != null) {
            profile.setDob(request.getDob());
        }

        User savedUser = userRepository.save(user);
        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        log.info("Customer profile updated successfully for user ID: {}", userId);
        return convertToCustomerProfileDTO(savedUser, savedProfile);
    }

    @Transactional
    public CustomerProfileDTO updateMyProfile(UpdateCustomerProfileRequest request) {
        log.info("Updating profile for current authenticated user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        return updateCustomerProfile(user.getId(), request);
    }

    @Transactional(readOnly = true)
    public List<CustomerProfileDTO> getAllCustomers() {
        log.info("Fetching all customers");
        
        List<User> customers = userRepository.findByRole(Role.CUSTOMER);
        return customers.stream()
                .map(user -> {
                    CustomerProfile profile = customerProfileRepository.findByUser(user).orElse(null);
                    return convertToCustomerProfileDTO(user, profile);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CustomerProfileDTO> getAllCustomersWithPagination(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching customers with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> customerPage = userRepository.findByRole(Role.CUSTOMER, pageable);
        
        return customerPage.map(user -> {
            CustomerProfile profile = customerProfileRepository.findByUser(user).orElse(null);
            return convertToCustomerProfileDTO(user, profile);
        });
    }

    // ==================== ADDRESS MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<AddressDTO> getCustomerAddresses(Long userId) {
        log.info("Fetching addresses for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        List<Address> addresses = addressRepository.findByUser(user);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getMyAddresses() {
        log.info("Fetching addresses for current authenticated user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        return getCustomerAddresses(user.getId());
    }

    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long addressId) {
        log.info("Fetching address with ID: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BadRequestException("Address not found with id: " + addressId));

        return convertToAddressDTO(address);
    }

    @Transactional
    public AddressDTO createAddress(CreateAddressRequest request) {
        log.info("Creating new address for current authenticated user");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        Address address = Address.builder()
                .user(user)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .addressLine(request.getAddressLine())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with ID: {}", savedAddress.getId());
        
        return convertToAddressDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, UpdateAddressRequest request) {
        log.info("Updating address with ID: {}", addressId);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BadRequestException("Address not found with id: " + addressId));

        // Check if current user owns this address
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only update your own addresses");
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getRecipientName())) {
            address.setRecipientName(request.getRecipientName());
        }
        
        if (StringUtils.hasText(request.getRecipientPhone())) {
            address.setRecipientPhone(request.getRecipientPhone());
        }
        
        if (StringUtils.hasText(request.getAddressLine())) {
            address.setAddressLine(request.getAddressLine());
        }
        
        if (StringUtils.hasText(request.getWard())) {
            address.setWard(request.getWard());
        }
        
        if (StringUtils.hasText(request.getDistrict())) {
            address.setDistrict(request.getDistrict());
        }
        
        if (StringUtils.hasText(request.getCity())) {
            address.setCity(request.getCity());
        }
        
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully with ID: {}", addressId);
        
        return convertToAddressDTO(updatedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        log.info("Deleting address with ID: {}", addressId);
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BadRequestException("Address not found with id: " + addressId));

        // Check if current user owns this address
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only delete your own addresses");
        }

        addressRepository.delete(address);
        log.info("Address deleted successfully with ID: {}", addressId);
    }

    // ==================== UTILITY METHODS ====================

    private CustomerProfileDTO convertToCustomerProfileDTO(User user, CustomerProfile profile) {
        CustomerProfileDTO.CustomerProfileDTOBuilder builder = CustomerProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (profile != null) {
            builder.profileId(profile.getId())
                   .fullName(profile.getFullName())
                   .gender(profile.getGender())
                   .dob(profile.getDob());
        }

        return builder.build();
    }

    private AddressDTO convertToAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .userId(address.getUser().getId())
                .recipientName(address.getRecipientName())
                .recipientPhone(address.getRecipientPhone())
                .addressLine(address.getAddressLine())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
} 