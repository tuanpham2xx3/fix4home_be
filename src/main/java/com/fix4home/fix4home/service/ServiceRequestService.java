package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.model.dto.servicerequest.*;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestService {

    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;

    // ==================== CUSTOMER OPERATIONS ====================

    @Transactional
    public ServiceRequestDTO createServiceRequest(CreateServiceRequestRequest request) {
        log.info("Creating new service request for service ID: {}", request.getServiceId());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User customer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        if (customer.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only customers can create service requests");
        }

        // Validate service exists and is active
        com.fix4home.fix4home.model.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + request.getServiceId()));

        if (service.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Service is not available");
        }

        // Validate address belongs to customer
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BadRequestException("Address not found with id: " + request.getAddressId()));

        CustomerProfile customerProfile = customerProfileRepository.findByUser(customer)
                .orElseThrow(() -> new BadRequestException("Customer profile not found"));

        if (!address.getUser().getId().equals(customer.getId())) {
            throw new BadRequestException("Address does not belong to current customer");
        }

        // Create service request
        ServiceRequest serviceRequest = ServiceRequest.builder()
                .customer(customer)
                .service(service)
                .address(address)
                .description(request.getDescription())
                .scheduledTime(request.getScheduledTime())
                .price(request.getPrice())
                .status(ServiceRequestStatus.PENDING)
                .build();

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        log.info("Service request created successfully with ID: {}", savedRequest.getId());

        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getMyServiceRequests() {
        log.info("Fetching service requests for current customer");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User customer = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        List<ServiceRequest> requests = serviceRequestRepository.findByCustomerId(customer.getId());
        return requests.stream()
                .map(this::convertToServiceRequestDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDTO getServiceRequestById(Long id) {
        log.info("Fetching service request details for ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findByIdWithDetails(id);
        if (serviceRequest == null) {
            throw new BadRequestException("Service request not found with id: " + id);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        // Authorization check
        if (currentUser.getRole() == Role.CUSTOMER && !serviceRequest.getCustomer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only view your own service requests");
        }

        if (currentUser.getRole() == Role.TECHNICIAN && 
            (serviceRequest.getTechnician() == null || !serviceRequest.getTechnician().getId().equals(currentUser.getId()))) {
            throw new BadRequestException("You can only view your assigned service requests");
        }

        return convertToServiceRequestDTO(serviceRequest);
    }

    @Transactional
    public ServiceRequestDTO cancelServiceRequest(Long id) {
        log.info("Canceling service request with ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        // Authorization check
        if (currentUser.getRole() == Role.CUSTOMER && !serviceRequest.getCustomer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("You can only cancel your own service requests");
        }

        // Business rule validation
        if (serviceRequest.getStatus() == ServiceRequestStatus.IN_PROGRESS ||
            serviceRequest.getStatus() == ServiceRequestStatus.DONE ||
            serviceRequest.getStatus() == ServiceRequestStatus.CANCELLED) {
            throw new BadRequestException("Cannot cancel service request in " + serviceRequest.getStatus() + " status");
        }

        serviceRequest.setStatus(ServiceRequestStatus.CANCELLED);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        log.info("Service request canceled successfully with ID: {}", id);
        return convertToServiceRequestDTO(savedRequest);
    }

    // ==================== TECHNICIAN OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDTO> getAvailableServiceRequests() {
        log.info("Fetching available service requests for technicians");

        List<ServiceRequest> requests = serviceRequestRepository.findAvailableRequests(ServiceRequestStatus.PENDING);
        return requests.stream()
                .map(this::convertToServiceRequestSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getMyAssignedRequests() {
        log.info("Fetching assigned requests for current technician");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User technician = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("Only technicians can view assigned requests");
        }

        List<ServiceRequest> requests = serviceRequestRepository.findByTechnicianId(technician.getId());
        return requests.stream()
                .map(this::convertToServiceRequestDTO)
                .toList();
    }

    @Transactional
    public ServiceRequestDTO acceptServiceRequest(Long id) {
        log.info("Technician accepting service request with ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User technician = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("Only technicians can accept service requests");
        }

        // Validate technician profile and status
        TechnicianProfile technicianProfile = technicianProfileRepository.findByUser(technician)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        if (technicianProfile.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Technician is not approved to accept requests");
        }

        // Business rule: Can only accept PENDING requests
        if (serviceRequest.getStatus() != ServiceRequestStatus.PENDING) {
            throw new BadRequestException("Can only accept PENDING service requests");
        }

        // Business rule: Request must be unassigned
        if (serviceRequest.getTechnician() != null) {
            throw new BadRequestException("Service request is already assigned to another technician");
        }

        serviceRequest.setTechnician(technician);
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        log.info("Service request accepted successfully by technician ID: {}", technician.getId());
        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO declineServiceRequest(Long id) {
        log.info("Technician declining service request with ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User technician = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        // Authorization check: Must be assigned technician
        if (serviceRequest.getTechnician() == null || !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new BadRequestException("You can only decline your assigned service requests");
        }

        // Business rule: Can only decline ASSIGNED requests
        if (serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            throw new BadRequestException("Can only decline ASSIGNED service requests");
        }

        serviceRequest.setTechnician(null);
        serviceRequest.setStatus(ServiceRequestStatus.PENDING);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        log.info("Service request declined by technician ID: {}", technician.getId());
        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO startWork(Long id) {
        log.info("Technician starting work on service request with ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User technician = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        // Authorization check
        if (serviceRequest.getTechnician() == null || !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new BadRequestException("You can only start work on your assigned service requests");
        }

        // Business rule: Can only start ASSIGNED requests
        if (serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            throw new BadRequestException("Can only start work on ASSIGNED service requests");
        }

        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        log.info("Work started on service request ID: {}", id);
        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO completeWork(Long id) {
        log.info("Technician completing work on service request with ID: {}", id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User technician = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        // Authorization check
        if (serviceRequest.getTechnician() == null || !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new BadRequestException("You can only complete your assigned service requests");
        }

        // Business rule: Can only complete IN_PROGRESS requests
        if (serviceRequest.getStatus() != ServiceRequestStatus.IN_PROGRESS) {
            throw new BadRequestException("Can only complete IN_PROGRESS service requests");
        }

        serviceRequest.setStatus(ServiceRequestStatus.DONE);
        serviceRequest.setCompletedTime(LocalDateTime.now());
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        log.info("Work completed on service request ID: {}", id);
        return convertToServiceRequestDTO(savedRequest);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<ServiceRequestSummaryDTO> getAllServiceRequests(int page, int size, String sortBy, String sortDir) {
        log.info("Admin fetching all service requests - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceRequest> requestPage = serviceRequestRepository.findAll(pageable);
        
        return requestPage.map(this::convertToServiceRequestSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDTO> getServiceRequestsByStatus(ServiceRequestStatus status) {
        log.info("Admin fetching service requests by status: {}", status);

        List<ServiceRequest> requests = serviceRequestRepository.findByStatus(status);
        return requests.stream()
                .map(this::convertToServiceRequestSummaryDTO)
                .toList();
    }

    @Transactional
    public ServiceRequestDTO assignTechnician(Long id, AssignTechnicianRequest request) {
        log.info("Admin assigning technician {} to service request {}", request.getTechnicianId(), id);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        User technician = userRepository.findById(request.getTechnicianId())
                .orElseThrow(() -> new BadRequestException("Technician not found with id: " + request.getTechnicianId()));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        // Validate technician profile and status
        TechnicianProfile technicianProfile = technicianProfileRepository.findByUser(technician)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        if (technicianProfile.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Technician is not approved");
        }

        // Business rule: Can assign to PENDING or reassign ASSIGNED requests
        if (serviceRequest.getStatus() == ServiceRequestStatus.IN_PROGRESS ||
            serviceRequest.getStatus() == ServiceRequestStatus.DONE ||
            serviceRequest.getStatus() == ServiceRequestStatus.CANCELLED) {
            throw new BadRequestException("Cannot assign technician to service request in " + serviceRequest.getStatus() + " status");
        }

        serviceRequest.setTechnician(technician);
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        if (request.getScheduledTime() != null) {
            serviceRequest.setScheduledTime(request.getScheduledTime());
        }

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        log.info("Technician assigned successfully to service request ID: {}", id);

        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO updateServiceRequestStatus(Long id, UpdateServiceRequestStatusRequest request) {
        log.info("Admin updating status of service request {} to {}", id, request.getStatus());

        ServiceRequest serviceRequest = serviceRequestRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + id));

        // Validate status transition
        validateStatusTransition(serviceRequest.getStatus(), request.getStatus());

        serviceRequest.setStatus(request.getStatus());
        
        if (request.getScheduledTime() != null) {
            serviceRequest.setScheduledTime(request.getScheduledTime());
        }
        
        if (request.getCompletedTime() != null) {
            serviceRequest.setCompletedTime(request.getCompletedTime());
        }

        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);
        log.info("Service request status updated successfully to: {}", request.getStatus());

        return convertToServiceRequestDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public ServiceRequestStatsDTO getServiceRequestStats() {
        log.info("Generating service request statistics");

        long totalRequests = serviceRequestRepository.count();
        long pendingRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.PENDING);
        long assignedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.ASSIGNED);
        long inProgressRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS);
        long completedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.DONE);
        long cancelledRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.CANCELLED);

        // Calculate rates
        double completionRate = totalRequests > 0 ? (double) completedRequests / totalRequests * 100 : 0;
        double cancellationRate = totalRequests > 0 ? (double) cancelledRequests / totalRequests * 100 : 0;

        // Calculate revenue (simplified - would need actual payment data)
        List<ServiceRequest> completedRequestsList = serviceRequestRepository.findByStatus(ServiceRequestStatus.DONE);
        BigDecimal totalRevenue = completedRequestsList.stream()
                .map(ServiceRequest::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averagePrice = completedRequests > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(completedRequests), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        // Count active users (simplified)
        long activeCustomers = userRepository.countByRole(Role.CUSTOMER);
        long activeTechnicians = userRepository.countByRole(Role.TECHNICIAN);

        return ServiceRequestStatsDTO.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .assignedRequests(assignedRequests)
                .inProgressRequests(inProgressRequests)
                .completedRequests(completedRequests)
                .cancelledRequests(cancelledRequests)
                .totalRevenue(totalRevenue)
                .averagePrice(averagePrice)
                .activeCustomers(activeCustomers)
                .activeTechnicians(activeTechnicians)
                .completionRate(completionRate)
                .cancellationRate(cancellationRate)
                .build();
    }

    // ==================== UTILITY METHODS ====================

    private void validateStatusTransition(ServiceRequestStatus currentStatus, ServiceRequestStatus newStatus) {
        // Define valid status transitions
        boolean isValidTransition = switch (currentStatus) {
            case PENDING -> newStatus == ServiceRequestStatus.ASSIGNED || newStatus == ServiceRequestStatus.CANCELLED;
            case ASSIGNED -> newStatus == ServiceRequestStatus.IN_PROGRESS || 
                            newStatus == ServiceRequestStatus.PENDING || 
                            newStatus == ServiceRequestStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == ServiceRequestStatus.DONE || newStatus == ServiceRequestStatus.CANCELLED;
            case DONE -> false; // Done is final
            case CANCELLED -> false; // Cancelled is final
        };

        if (!isValidTransition) {
            throw new BadRequestException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private ServiceRequestDTO convertToServiceRequestDTO(ServiceRequest serviceRequest) {
        // Get customer profile for full name
        CustomerProfile customerProfile = customerProfileRepository.findByUser(serviceRequest.getCustomer()).orElse(null);
        String customerFullName = customerProfile != null ? customerProfile.getFullName() : serviceRequest.getCustomer().getUsername();

        ServiceRequestDTO.CustomerSummaryDTO customerSummary = ServiceRequestDTO.CustomerSummaryDTO.builder()
                .userId(serviceRequest.getCustomer().getId())
                .username(serviceRequest.getCustomer().getUsername())
                .email(serviceRequest.getCustomer().getEmail())
                .phoneNumber(serviceRequest.getCustomer().getPhoneNumber())
                .fullName(customerFullName)
                .build();

        // Convert service
        com.fix4home.fix4home.model.dto.service.ServiceDTO serviceDTO = 
                com.fix4home.fix4home.model.dto.service.ServiceDTO.builder()
                .id(serviceRequest.getService().getId())
                .name(serviceRequest.getService().getName())
                .description(serviceRequest.getService().getDescription())
                .basePrice(serviceRequest.getService().getBasePrice())
                .status(serviceRequest.getService().getStatus())
                .build();

        // Convert technician (if assigned)
        ServiceRequestDTO.TechnicianSummaryDTO technicianSummary = null;
        if (serviceRequest.getTechnician() != null) {
            TechnicianProfile technicianProfile = technicianProfileRepository.findByUser(serviceRequest.getTechnician()).orElse(null);
            String technicianFullName = technicianProfile != null ? technicianProfile.getFullName() : serviceRequest.getTechnician().getUsername();
            Float technicianRating = technicianProfile != null ? technicianProfile.getRating() : 0.0f;

            technicianSummary = ServiceRequestDTO.TechnicianSummaryDTO.builder()
                    .userId(serviceRequest.getTechnician().getId())
                    .username(serviceRequest.getTechnician().getUsername())
                    .email(serviceRequest.getTechnician().getEmail())
                    .phoneNumber(serviceRequest.getTechnician().getPhoneNumber())
                    .fullName(technicianFullName)
                    .rating(technicianRating)
                    .build();
        }

        // Convert address
        com.fix4home.fix4home.model.dto.customer.AddressDTO addressDTO = 
                com.fix4home.fix4home.model.dto.customer.AddressDTO.builder()
                .id(serviceRequest.getAddress().getId())
                .userId(serviceRequest.getAddress().getUser() != null ? serviceRequest.getAddress().getUser().getId() : null)
                .recipientName(serviceRequest.getAddress().getRecipientName())
                .recipientPhone(serviceRequest.getAddress().getRecipientPhone())
                .addressLine(serviceRequest.getAddress().getAddressLine())
                .ward(serviceRequest.getAddress().getWard())
                .district(serviceRequest.getAddress().getDistrict())
                .city(serviceRequest.getAddress().getCity())
                .latitude(serviceRequest.getAddress().getLatitude())
                .longitude(serviceRequest.getAddress().getLongitude())
                .build();

        return ServiceRequestDTO.builder()
                .id(serviceRequest.getId())
                .status(serviceRequest.getStatus())
                .description(serviceRequest.getDescription())
                .scheduledTime(serviceRequest.getScheduledTime())
                .completedTime(serviceRequest.getCompletedTime())
                .createdAt(serviceRequest.getCreatedAt())
                .price(serviceRequest.getPrice())
                .customer(customerSummary)
                .service(serviceDTO)
                .technician(technicianSummary)
                .address(addressDTO)
                .build();
    }

    private ServiceRequestSummaryDTO convertToServiceRequestSummaryDTO(ServiceRequest serviceRequest) {
        // Get customer profile for full name
        CustomerProfile customerProfile = customerProfileRepository.findByUser(serviceRequest.getCustomer()).orElse(null);
        String customerFullName = customerProfile != null ? customerProfile.getFullName() : serviceRequest.getCustomer().getUsername();

        // Get technician info (if assigned)
        String technicianFullName = null;
        Float technicianRating = null;
        if (serviceRequest.getTechnician() != null) {
            TechnicianProfile technicianProfile = technicianProfileRepository.findByUser(serviceRequest.getTechnician()).orElse(null);
            technicianFullName = technicianProfile != null ? technicianProfile.getFullName() : serviceRequest.getTechnician().getUsername();
            technicianRating = technicianProfile != null ? technicianProfile.getRating() : 0.0f;
        }

        // Format address
        Address addr = serviceRequest.getAddress();
        String addressString = String.format("%s, %s, %s, %s", addr.getAddressLine(), addr.getWard(), addr.getDistrict(), addr.getCity());

        return ServiceRequestSummaryDTO.builder()
                .id(serviceRequest.getId())
                .status(serviceRequest.getStatus())
                .description(serviceRequest.getDescription().length() > 100 ? 
                           serviceRequest.getDescription().substring(0, 100) + "..." : 
                           serviceRequest.getDescription())
                .scheduledTime(serviceRequest.getScheduledTime())
                .createdAt(serviceRequest.getCreatedAt())
                .price(serviceRequest.getPrice())
                .customerName(customerFullName)
                .customerPhone(serviceRequest.getCustomer().getPhoneNumber())
                .serviceName(serviceRequest.getService().getName())
                .serviceCategory("General") // Default category since Service entity doesn't have category field
                .technicianName(technicianFullName)
                .technicianRating(technicianRating)
                .address(addressString)
                .city(addr.getCity())
                .build();
    }
} 