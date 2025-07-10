package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.customer.AddressDTO;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.servicepost.*;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.ServicePostStatus;
import com.fix4home.fix4home.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicePostService extends BaseService {

    private final ServicePostRepository servicePostRepository;
    private final ServicePostResponseRepository servicePostResponseRepository;
    private final ServiceRepository serviceRepository;
    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;

    // ==================== CUSTOMER OPERATIONS ====================

    @Transactional
    public ServicePostDTO createServicePost(CreateServicePostRequest request) {
        logBusinessOperation("CREATE_SERVICE_POST", "serviceId=" + request.getServiceId());

        validateRequired(request, "request");
        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        // Validate service exists and is active
        com.fix4home.fix4home.model.entity.Service service = findServiceById(request.getServiceId());
        validateServiceAvailable(service);

        // Validate address belongs to customer
        Address address = findAddressById(request.getAddressId());
        validateAddressOwnership(address, customer);

        // Validate customer profile exists
        CustomerProfile customerProfile = findCustomerProfileByUser(customer);

        // Set default expiry time if not provided (24 hours from now)
        LocalDateTime expiresAt = request.getExpiresAt();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusHours(24);
        }

        // Create service post
        ServicePost servicePost = ServicePost.builder()
                .customer(customer)
                .service(service)
                .address(address)
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedBudget(request.getEstimatedBudget())
                .preferredTime(request.getPreferredTime())
                .type(request.getType())
                .maxTechnicians(request.getMaxTechnicians() != null ? request.getMaxTechnicians() : 5)
                .expiresAt(expiresAt)
                .status(ServicePostStatus.DRAFT)
                .build();

        ServicePost savedPost = servicePostRepository.save(servicePost);
        return convertToDTO(savedPost);
    }

    @Transactional
    public ServicePostDTO publishServicePost(Long id) {
        logBusinessOperation("PUBLISH_SERVICE_POST", "id=" + id);

        validatePositiveId(id, "id");
        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost servicePost = findServicePostById(id);
        validateServicePostOwnership(servicePost, customer);

        if (servicePost.getStatus() != ServicePostStatus.DRAFT) {
            throw new BusinessValidationException("Only draft posts can be published");
        }

        servicePost.setStatus(ServicePostStatus.POSTED);
        ServicePost updatedPost = servicePostRepository.save(servicePost);
        
        return convertToDTO(updatedPost);
    }

    @Transactional(readOnly = true)
    public List<ServicePostDTO> getMyServicePosts() {
        logBusinessOperation("GET_MY_SERVICE_POSTS");

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        List<ServicePost> posts = servicePostRepository.findByCustomerOrderByCreatedAtDesc(customer);
        return posts.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServicePostDTO> getMyServicePostsWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_MY_SERVICE_POSTS_PAGINATED");

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServicePost> postPage = servicePostRepository.findByCustomerOrderByCreatedAtDesc(customer, pageable);
        
        return postPage.map(this::convertToDTO);
    }

    @Transactional
    public ServicePostDTO updateServicePost(Long id, UpdateServicePostRequest request) {
        logBusinessOperation("UPDATE_SERVICE_POST", "id=" + id);

        validatePositiveId(id, "id");
        validateRequired(request, "request");

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost existingPost = findServicePostById(id);
        validateServicePostOwnership(existingPost, customer);
        validateServicePostCanBeUpdated(existingPost);

        // Update fields if provided
        if (StringUtils.hasText(request.getTitle())) {
            existingPost.setTitle(request.getTitle());
        }
        
        if (StringUtils.hasText(request.getDescription())) {
            existingPost.setDescription(request.getDescription());
        }
        
        if (request.getEstimatedBudget() != null) {
            existingPost.setEstimatedBudget(request.getEstimatedBudget());
        }
        
        if (request.getPreferredTime() != null) {
            existingPost.setPreferredTime(request.getPreferredTime());
        }
        
        if (request.getType() != null) {
            existingPost.setType(request.getType());
        }
        
        if (request.getMaxTechnicians() != null) {
            existingPost.setMaxTechnicians(request.getMaxTechnicians());
        }
        
        if (request.getExpiresAt() != null) {
            existingPost.setExpiresAt(request.getExpiresAt());
        }
        
        if (request.getStatus() != null) {
            existingPost.setStatus(request.getStatus());
        }
        
        if (request.getFinalPrice() != null) {
            existingPost.setFinalPrice(request.getFinalPrice());
        }

        ServicePost updatedPost = servicePostRepository.save(existingPost);
        return convertToDTO(updatedPost);
    }

    @Transactional
    public void cancelServicePost(Long id) {
        logBusinessOperation("CANCEL_SERVICE_POST", "id=" + id);

        validatePositiveId(id, "id");
        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost servicePost = findServicePostById(id);
        validateServicePostOwnership(servicePost, customer);
        validateServicePostCanBeCancelled(servicePost);

        servicePost.setStatus(ServicePostStatus.CANCELLED);
        servicePostRepository.save(servicePost);
    }

    // ==================== TECHNICIAN OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ServicePostSummaryDTO> getAvailableServicePosts() {
        logBusinessOperation("GET_AVAILABLE_SERVICE_POSTS");

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Check technician profile exists and is active
        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianApproved(technicianProfile);

        List<ServicePostStatus> activeStatuses = Arrays.asList(
            ServicePostStatus.POSTED, 
            ServicePostStatus.RESPONSES_RECEIVED
        );

        LocalDateTime now = LocalDateTime.now();
        List<ServicePost> availablePosts = servicePostRepository.findPostsNotRespondedByTechnician(
            technician, activeStatuses, now
        );

        return availablePosts.stream()
                .map(this::convertToSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServicePostSummaryDTO> getAvailableServicePostsWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_AVAILABLE_SERVICE_POSTS_PAGINATED");

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianApproved(technicianProfile);

        List<ServicePostStatus> activeStatuses = Arrays.asList(
            ServicePostStatus.POSTED, 
            ServicePostStatus.RESPONSES_RECEIVED
        );

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime now = LocalDateTime.now();
        
        Page<ServicePost> postPage = servicePostRepository.findActivePostsForTechnicians(
            activeStatuses, now, pageable
        );
        
        return postPage.map(this::convertToSummaryDTO);
    }

    @Transactional
    public ServicePostResponseDTO respondToServicePost(Long servicePostId, CreateServicePostResponseRequest request) {
        logBusinessOperation("RESPOND_TO_SERVICE_POST", "servicePostId=" + servicePostId);

        validatePositiveId(servicePostId, "servicePostId");
        validateRequired(request, "request");

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate technician profile
        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianApproved(technicianProfile);

        // Find and validate service post
        ServicePost servicePost = findServicePostById(servicePostId);
        validateServicePostAvailable(servicePost);

        // Check if technician already responded
        boolean alreadyResponded = servicePostResponseRepository.existsByServicePostAndTechnician(servicePost, technician);
        validateTechnicianNotResponded(servicePost, technician, alreadyResponded);

        // Create response
        ServicePostResponse response = ServicePostResponse.builder()
                .servicePost(servicePost)
                .technician(technician)
                .message(request.getMessage())
                .quotedPrice(request.getQuotedPrice())
                .estimatedDuration(request.getEstimatedDuration())
                .proposedTime(request.getProposedTime())
                .build();

        ServicePostResponse savedResponse = servicePostResponseRepository.save(response);

        // Update service post status if this is the first response
        if (servicePost.getStatus() == ServicePostStatus.POSTED) {
            servicePost.setStatus(ServicePostStatus.RESPONSES_RECEIVED);
            servicePostRepository.save(servicePost);
        }

        return convertResponseToDTO(savedResponse);
    }

    @Transactional(readOnly = true)
    public List<ServicePostResponseDTO> getMyResponses() {
        logBusinessOperation("GET_MY_RESPONSES");

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        List<ServicePostResponse> responses = servicePostResponseRepository.findByTechnicianOrderByCreatedAtDesc(technician);
        return responses.stream()
                .map(this::convertResponseToDTO)
                .toList();
    }

    // ==================== SHARED OPERATIONS ====================

    @Transactional(readOnly = true)
    public ServicePostDTO getServicePostById(Long id) {
        logBusinessOperation("GET_SERVICE_POST_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        User currentUser = getCurrentUser();

        ServicePost servicePost = findServicePostById(id);

        // Check access permissions
        if (currentUser.getRole() == Role.CUSTOMER) {
            validateServicePostOwnership(servicePost, currentUser);
        } else if (currentUser.getRole() == Role.TECHNICIAN) {
            // Technicians can view any active post
            // No additional validation needed
        } else if (currentUser.getRole() == Role.ADMIN) {
            // Admins can view any post
        }

        return convertToDTO(servicePost);
    }

    @Transactional(readOnly = true)
    public List<ServicePostResponseDTO> getServicePostResponses(Long servicePostId) {
        logBusinessOperation("GET_SERVICE_POST_RESPONSES", "servicePostId=" + servicePostId);

        validatePositiveId(servicePostId, "servicePostId");
        User currentUser = getCurrentUser();

        ServicePost servicePost = findServicePostById(servicePostId);

        // Only customer who owns the post or admin can view responses
        if (currentUser.getRole() == Role.CUSTOMER) {
            validateServicePostOwnership(servicePost, currentUser);
        } else if (currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Access denied: Cannot view responses for this service post");
        }

        List<ServicePostResponse> responses = servicePostResponseRepository.findByServicePostOrderByCreatedAtDesc(servicePost);
        return responses.stream()
                .map(this::convertResponseToDTO)
                .toList();
    }

    @Transactional
    public ServicePostDTO selectTechnician(Long servicePostId, Long responseId) {
        logBusinessOperation("SELECT_TECHNICIAN", "servicePostId=" + servicePostId + ", responseId=" + responseId);

        validatePositiveId(servicePostId, "servicePostId");
        validatePositiveId(responseId, "responseId");

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost servicePost = findServicePostById(servicePostId);
        validateServicePostOwnership(servicePost, customer);

        ServicePostResponse response = findServicePostResponseById(responseId);
        validateResponseCanBeSelected(servicePost, response);

        // Ensure response belongs to the service post
        if (!response.getServicePost().getId().equals(servicePostId)) {
            throw new BusinessValidationException("Response does not belong to the specified service post");
        }

        // Mark the response as selected
        response.setIsSelected(true);
        servicePostResponseRepository.save(response);

        // Update service post
        servicePost.setSelectedTechnician(response.getTechnician());
        servicePost.setSelectedAt(LocalDateTime.now());
        servicePost.setStatus(ServicePostStatus.TECHNICIAN_SELECTED);
        servicePost.setFinalPrice(response.getQuotedPrice());

        ServicePost updatedPost = servicePostRepository.save(servicePost);
        return convertToDTO(updatedPost);
    }

    @Transactional
    public ServicePostDTO markServicePostInProgress(Long id) {
        logBusinessOperation("MARK_SERVICE_POST_IN_PROGRESS", "id=" + id);

        validatePositiveId(id, "id");
        
        ServicePost servicePost = findServicePostById(id);
        User currentUser = getCurrentUser();

        // Validate user can mark as in progress
        if (currentUser.getRole() == Role.CUSTOMER) {
            validateServicePostOwnership(servicePost, currentUser);
        } else if (currentUser.getRole() == Role.TECHNICIAN) {
            if (servicePost.getSelectedTechnician() == null || 
                !servicePost.getSelectedTechnician().getId().equals(currentUser.getId())) {
                throw new SecurityException("Only the selected technician can mark service post as in progress");
            }
        } else if (currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Access denied");
        }

        if (servicePost.getStatus() != ServicePostStatus.TECHNICIAN_SELECTED) {
            throw new BusinessValidationException("Can only mark posts with selected technician as in progress");
        }

        servicePost.setStatus(ServicePostStatus.IN_PROGRESS);
        ServicePost updatedPost = servicePostRepository.save(servicePost);
        
        return convertToDTO(updatedPost);
    }

    @Transactional
    public ServicePostDTO completeServicePost(Long id) {
        logBusinessOperation("COMPLETE_SERVICE_POST", "id=" + id);

        validatePositiveId(id, "id");
        
        ServicePost servicePost = findServicePostById(id);
        User currentUser = getCurrentUser();

        // Validate user can complete
        if (currentUser.getRole() == Role.CUSTOMER) {
            validateServicePostOwnership(servicePost, currentUser);
        } else if (currentUser.getRole() == Role.TECHNICIAN) {
            if (servicePost.getSelectedTechnician() == null || 
                !servicePost.getSelectedTechnician().getId().equals(currentUser.getId())) {
                throw new SecurityException("Only the selected technician can complete service post");
            }
        } else if (currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Access denied");
        }

        if (servicePost.getStatus() != ServicePostStatus.IN_PROGRESS) {
            throw new BusinessValidationException("Can only complete posts that are in progress");
        }

        servicePost.setStatus(ServicePostStatus.COMPLETED);
        ServicePost updatedPost = servicePostRepository.save(servicePost);
        
        return convertToDTO(updatedPost);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ServicePostDTO> getAllServicePosts() {
        logBusinessOperation("GET_ALL_SERVICE_POSTS");
        requireRole(Role.ADMIN);

        List<ServicePost> posts = servicePostRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return posts.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServicePostDTO> getAllServicePostsWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_SERVICE_POSTS_PAGINATED");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServicePost> postPage = servicePostRepository.findAll(pageable);
        
        return postPage.map(this::convertToDTO);
    }

    @Transactional
    public void processExpiredPosts() {
        logBusinessOperation("PROCESS_EXPIRED_POSTS");

        LocalDateTime now = LocalDateTime.now();
        List<ServicePost> expiredPosts = servicePostRepository.findExpiredPosts(now);

        for (ServicePost post : expiredPosts) {
            post.setStatus(ServicePostStatus.EXPIRED);
            servicePostRepository.save(post);
        }

        log.info("Processed {} expired service posts", expiredPosts.size());
    }

    // ==================== SEARCH OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ServicePostSummaryDTO> searchServicePosts(String keyword) {
        logBusinessOperation("SEARCH_SERVICE_POSTS", "keyword=" + keyword);

        if (!StringUtils.hasText(keyword)) {
            return getAvailableServicePosts();
        }

        List<ServicePostStatus> activeStatuses = Arrays.asList(
            ServicePostStatus.POSTED, 
            ServicePostStatus.RESPONSES_RECEIVED
        );

        List<ServicePost> posts = servicePostRepository.searchPosts(keyword, activeStatuses);
        return posts.stream()
                .map(this::convertToSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicePostSummaryDTO> getServicePostsByLocation(String location) {
        logBusinessOperation("GET_SERVICE_POSTS_BY_LOCATION", "location=" + location);

        List<ServicePostStatus> activeStatuses = Arrays.asList(
            ServicePostStatus.POSTED, 
            ServicePostStatus.RESPONSES_RECEIVED
        );

        List<ServicePost> posts = servicePostRepository.findPostsByLocation(location, activeStatuses);
        return posts.stream()
                .map(this::convertToSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicePostSummaryDTO> getUrgentServicePosts() {
        logBusinessOperation("GET_URGENT_SERVICE_POSTS");

        List<ServicePostStatus> activeStatuses = Arrays.asList(
            ServicePostStatus.POSTED, 
            ServicePostStatus.RESPONSES_RECEIVED
        );

        LocalDateTime now = LocalDateTime.now();
        List<ServicePost> posts = servicePostRepository.findUrgentPosts(activeStatuses, now);
        return posts.stream()
                .map(this::convertToSummaryDTO)
                .toList();
    }

    // ==================== HELPER METHODS ====================

    private ServicePost findServicePostById(Long id) {
        return servicePostRepository.findById(id)
                .orElseThrow(() -> new ServicePostNotFoundException(id));
    }

    private ServicePostResponse findServicePostResponseById(Long id) {
        return servicePostResponseRepository.findById(id)
                .orElseThrow(() -> new ServicePostResponseNotFoundException(id));
    }

    private com.fix4home.fix4home.model.entity.Service findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    private Address findAddressById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new BusinessValidationException("Address not found with ID: " + id));
    }

    private CustomerProfile findCustomerProfileByUser(User user) {
        return customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessValidationException("Customer profile not found"));
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianProfileNotFoundException(user.getId()));
    }

    // ==================== DTO CONVERSION METHODS ====================

    private ServicePostDTO convertToDTO(ServicePost servicePost) {
        // Get customer name
        String customerName = null;
        CustomerProfile customerProfile = customerProfileRepository.findByUser(servicePost.getCustomer()).orElse(null);
        if (customerProfile != null) {
            customerName = customerProfile.getFullName();
        }

        // Get selected technician name
        String selectedTechnicianName = null;
        if (servicePost.getSelectedTechnician() != null) {
            TechnicianProfile techProfile = technicianProfileRepository.findByUser(servicePost.getSelectedTechnician()).orElse(null);
            if (techProfile != null) {
                selectedTechnicianName = techProfile.getFullName();
            }
        }

        return ServicePostDTO.builder()
                .id(servicePost.getId())
                .customerId(servicePost.getCustomer().getId())
                .customerName(customerName)
                .service(convertServiceToDTO(servicePost.getService()))
                .address(convertAddressToDTO(servicePost.getAddress()))
                .title(servicePost.getTitle())
                .description(servicePost.getDescription())
                .estimatedBudget(servicePost.getEstimatedBudget())
                .preferredTime(servicePost.getPreferredTime())
                .type(servicePost.getType())
                .status(servicePost.getStatus())
                .maxTechnicians(servicePost.getMaxTechnicians())
                .expiresAt(servicePost.getExpiresAt())
                .selectedTechnicianId(servicePost.getSelectedTechnician() != null ? servicePost.getSelectedTechnician().getId() : null)
                .selectedTechnicianName(selectedTechnicianName)
                .selectedAt(servicePost.getSelectedAt())
                .finalPrice(servicePost.getFinalPrice())
                .createdAt(servicePost.getCreatedAt())
                .updatedAt(servicePost.getUpdatedAt())
                .responseCount(servicePost.getResponseCount())
                .isActive(servicePost.isActive())
                .canReceiveResponses(servicePost.canReceiveResponses())
                .isExpired(servicePost.isExpired())
                .build();
    }

    private ServicePostSummaryDTO convertToSummaryDTO(ServicePost servicePost) {
        // Get customer name
        String customerName = null;
        CustomerProfile customerProfile = customerProfileRepository.findByUser(servicePost.getCustomer()).orElse(null);
        if (customerProfile != null) {
            customerName = customerProfile.getFullName();
        }

        return ServicePostSummaryDTO.builder()
                .id(servicePost.getId())
                .title(servicePost.getTitle())
                .serviceName(servicePost.getService().getName())
                .address(servicePost.getAddress().getAddress())
                .estimatedBudget(servicePost.getEstimatedBudget())
                .preferredTime(servicePost.getPreferredTime())
                .type(servicePost.getType())
                .status(servicePost.getStatus())
                .createdAt(servicePost.getCreatedAt())
                .expiresAt(servicePost.getExpiresAt())
                .responseCount(servicePost.getResponseCount())
                .isActive(servicePost.isActive())
                .isExpired(servicePost.isExpired())
                .customerName(customerName)
                .build();
    }

    private ServicePostResponseDTO convertResponseToDTO(ServicePostResponse response) {
        // Get technician name and details
        String technicianName = null;
        TechnicianProfile techProfile = technicianProfileRepository.findByUser(response.getTechnician()).orElse(null);
        if (techProfile != null) {
            technicianName = techProfile.getFullName();
        }

        return ServicePostResponseDTO.builder()
                .id(response.getId())
                .servicePostId(response.getServicePost().getId())
                .technicianId(response.getTechnician().getId())
                .technicianName(technicianName)
                .message(response.getMessage())
                .quotedPrice(response.getQuotedPrice())
                .estimatedDuration(response.getEstimatedDuration())
                .proposedTime(response.getProposedTime())
                .isSelected(response.isSelected())
                .createdAt(response.getCreatedAt())
                .build();
    }

    private ServiceDTO convertServiceToDTO(com.fix4home.fix4home.model.entity.Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .status(service.getStatus())
                .build();
    }

    private AddressDTO convertAddressToDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .address(address.getAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .isDefault(address.getIsDefault())
                .build();
    }
} 