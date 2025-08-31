package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BusinessValidationException;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService extends BaseService implements DTOConverter<CustomerProfile, CustomerProfileDTO> {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final AddressRepository addressRepository;
    private final AddressApiService addressApiService;

    // ==================== PROFILE MANAGEMENT ====================

    @Transactional(readOnly = true)
    @Cacheable(value = "customerProfiles", key = "#userId")
    public CustomerProfileDTO getCustomerProfile(Long userId) {
        logBusinessOperation("GET_CUSTOMER_PROFILE", "userId=" + userId);
        
        validatePositiveId(userId, "userId");
        User user = findUserById(userId);
        validateUserRole(user, Role.CUSTOMER);

        CustomerProfile profile = findCustomerProfile(user);
        return convertToDTO(profile);
    }

    @Transactional(readOnly = true)
    public CustomerProfileDTO getMyProfile() {
        logBusinessOperation("GET_MY_PROFILE");
        
        User currentUser = getCurrentUser();
        requireRole(Role.CUSTOMER);

        CustomerProfile profile = findCustomerProfile(currentUser);
        return convertToDTO(profile);
    }

    @Transactional
    @CacheEvict(value = "customerProfiles", key = "#userId")
    public CustomerProfileDTO updateCustomerProfile(Long userId, UpdateCustomerProfileRequest request) {
        logBusinessOperation("UPDATE_CUSTOMER_PROFILE", "userId=" + userId);
        
        validatePositiveId(userId, "userId");
        validateRequired(request, "request");

        User user = findUserById(userId);
        validateUserRole(user, Role.CUSTOMER);

        // Admin can update any customer, customers can only update their own
        if (!hasRole(Role.ADMIN)) {
            requireAccessToUserResource(userId);
        }

        CustomerProfile profile = findCustomerProfile(user);

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

        userRepository.save(user);
        CustomerProfile savedProfile = customerProfileRepository.save(profile);

        return convertToDTO(savedProfile);
    }

    @Transactional
    public CustomerProfileDTO updateMyProfile(UpdateCustomerProfileRequest request) {
        logBusinessOperation("UPDATE_MY_PROFILE");
        
        Long currentUserId = getCurrentUserId();
        return updateCustomerProfile(currentUserId, request);
    }

    @Transactional(readOnly = true)
    public List<CustomerProfileDTO> getAllCustomers() {
        logBusinessOperation("GET_ALL_CUSTOMERS");
        requireRole(Role.ADMIN);
        
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
        logBusinessOperation("GET_CUSTOMERS_PAGINATED", "page=" + page, "size=" + size, "sortBy=" + sortBy);
        requireRole(Role.ADMIN);
        
        validatePaginationParams(page, size);
        validateSortDirection(sortDir);
        
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
        logBusinessOperation("GET_CUSTOMER_ADDRESSES", "userId=" + userId);
        
        validatePositiveId(userId, "userId");
        User user = findUserById(userId);
        
        // Admin can view any customer's addresses, customers can only view their own
        if (!hasRole(Role.ADMIN)) {
            requireAccessToUserResource(userId);
        }

        List<Address> addresses = addressRepository.findByUser(user);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getMyAddresses() {
        logBusinessOperation("GET_MY_ADDRESSES");
        
        Long currentUserId = getCurrentUserId();
        return getCustomerAddresses(currentUserId);
    }

    @Transactional(readOnly = true)
    public AddressDTO getAddressById(Long addressId) {
        logBusinessOperation("GET_ADDRESS_BY_ID", "addressId=" + addressId);
        
        validatePositiveId(addressId, "addressId");
        Address address = findAddressById(addressId);
        
        // Check access rights
        if (!hasRole(Role.ADMIN)) {
            requireAccessToUserResource(address.getUser().getId());
        }

        return convertToAddressDTO(address);
    }

    @Transactional
    public AddressDTO createAddress(CreateAddressRequest request) {
        logBusinessOperation("CREATE_ADDRESS");
        
        validateRequired(request, "request");
        User currentUser = getCurrentUser();

        // Validate address using Vietnam Administrative API if codes are provided
        if (request.getProvinceCode() != null && request.getWardCode() != null) {
            validateAddressCodes(request.getProvinceCode(), request.getWardCode());
        }

        Address address = Address.builder()
                .user(currentUser)
                .recipientName(request.getRecipientName())
                .recipientPhone(request.getRecipientPhone())
                .addressLine(request.getAddressLine())
                .ward(request.getWard())
                .district(request.getDistrict())
                .city(request.getCity())
                .provinceCode(request.getProvinceCode())
                .wardCode(request.getWardCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .build();

        Address savedAddress = addressRepository.save(address);
        return convertToAddressDTO(savedAddress);
    }

    @Transactional
    public AddressDTO updateAddress(Long addressId, UpdateAddressRequest request) {
        logBusinessOperation("UPDATE_ADDRESS", "addressId=" + addressId);
        
        validatePositiveId(addressId, "addressId");
        validateRequired(request, "request");

        Address address = findAddressById(addressId);
        
        // Check access rights (users can only update their own addresses)
        requireAccessToUserResource(address.getUser().getId());

        // Update fields
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
        
        // Handle province and ward codes with validation
        if (StringUtils.hasText(request.getProvinceCode())) {
            address.setProvinceCode(request.getProvinceCode());
        }
        
        if (StringUtils.hasText(request.getWardCode())) {
            address.setWardCode(request.getWardCode());
        }
        
        // Validate address codes if both are provided
        if (StringUtils.hasText(address.getProvinceCode()) && StringUtils.hasText(address.getWardCode())) {
            validateAddressCodes(address.getProvinceCode(), address.getWardCode());
        }
        
        if (request.getLatitude() != null) {
            address.setLatitude(request.getLatitude());
        }
        
        if (request.getLongitude() != null) {
            address.setLongitude(request.getLongitude());
        }

        Address savedAddress = addressRepository.save(address);
        return convertToAddressDTO(savedAddress);
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        logBusinessOperation("DELETE_ADDRESS", "addressId=" + addressId);
        
        validatePositiveId(addressId, "addressId");
        Address address = findAddressById(addressId);
        
        // Check access rights (users can only delete their own addresses)
        requireAccessToUserResource(address.getUser().getId());

        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByProvince(String provinceCode) {
        logBusinessOperation("GET_ADDRESSES_BY_PROVINCE", "provinceCode=" + provinceCode);
        
        Long currentUserId = getCurrentUserId();
        List<Address> addresses = addressRepository.findByUserIdAndProvinceCode(currentUserId, provinceCode);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getAddressesByWard(String wardCode) {
        logBusinessOperation("GET_ADDRESSES_BY_WARD", "wardCode=" + wardCode);
        
        Long currentUserId = getCurrentUserId();
        List<Address> addresses = addressRepository.findByUserIdAndWardCode(currentUserId, wardCode);
        return addresses.stream()
                .map(this::convertToAddressDTO)
                .toList();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private CustomerProfile findCustomerProfile(User user) {
        return customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessValidationException("Customer profile not found for user: " + user.getId()));
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessValidationException("Address not found with id: " + addressId));
    }

    private void validateAddressCodes(String provinceCode, String wardCode) {
        try {
            com.fix4home.fix4home.model.dto.common.AddressValidationResult result = 
                    addressApiService.validateAddress(provinceCode, wardCode);
            
            if (!result.getValid()) {
                throw new BusinessValidationException("Invalid address: " + result.getMessage());
            }
            
            log.info("Address validation successful for province: {} and ward: {}", provinceCode, wardCode);
        } catch (Exception e) {
            log.warn("Address validation failed for province: {} and ward: {} - {}", 
                    provinceCode, wardCode, e.getMessage());
            // Don't fail the request if external API is down, just log the warning
        }
    }

    // ==================== DTO CONVERSION METHODS ====================

    @Override
    public CustomerProfileDTO convertToDTO(CustomerProfile profile) {
        return convertToCustomerProfileDTO(profile.getUser(), profile);
    }

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
            builder.fullName(profile.getFullName())
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
                .provinceCode(address.getProvinceCode())
                .wardCode(address.getWardCode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .build();
    }
} 