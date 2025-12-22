package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.customer.AddressDTO;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.servicerequest.*;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.*;
import com.fix4home.fix4home.service.event.ServiceRequestCancelledEvent;
import com.fix4home.fix4home.service.event.ServiceRequestCreatedEvent;
import com.fix4home.fix4home.service.event.ServiceRequestStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceRequestService extends BaseService implements DTOConverter<ServiceRequest, ServiceRequestDTO> {

    private final ServiceRequestRepository serviceRequestRepository;
    private final ServiceRepository serviceRepository;
    private final AddressRepository addressRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== CUSTOMER OPERATIONS ====================

    @Transactional
    public ServiceRequestDTO createServiceRequest(CreateServiceRequestRequest request) {
        logBusinessOperation("CREATE_SERVICE_REQUEST", "serviceId=" + request.getServiceId());

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

        // Publish domain event for notifications
        eventPublisher.publishEvent(new ServiceRequestCreatedEvent(
                savedRequest.getId(),
                savedRequest.getCustomer().getId(),
                savedRequest.getTechnician() != null ? savedRequest.getTechnician().getId() : null,
                savedRequest.getService().getName(),
                savedRequest.getAddress().getAddressLine()
        ));

        return convertToDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getMyServiceRequests() {
        logBusinessOperation("GET_MY_SERVICE_REQUESTS");

        User customer = getCurrentUser();
        List<ServiceRequest> requests = serviceRequestRepository.findByCustomerId(customer.getId());
        return convertToDTO(requests);
    }

    @Transactional(readOnly = true)
    public ServiceRequestDTO getServiceRequestById(Long id) {
        logBusinessOperation("GET_SERVICE_REQUEST_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User currentUser = getCurrentUser();

        // Authorization check
        validateServiceRequestAccess(serviceRequest, currentUser);

        return convertToDTO(serviceRequest);
    }

    @Transactional
    public ServiceRequestDTO cancelServiceRequest(Long id) {
        logBusinessOperation("CANCEL_SERVICE_REQUEST", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User currentUser = getCurrentUser();

        // Authorization check - only customer can cancel their own requests
        if (currentUser.getRole() == Role.CUSTOMER) {
            requireAccessToUserResource(serviceRequest.getCustomer().getId());
        }

        // Business rule validation
        validateCanCancel(serviceRequest);

        ServiceRequestStatus oldStatus = serviceRequest.getStatus();
        serviceRequest.setStatus(ServiceRequestStatus.CANCELLED);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // Publish domain event for notifications
        eventPublisher.publishEvent(new ServiceRequestCancelledEvent(
                savedRequest.getId(),
                savedRequest.getCustomer().getId(),
                savedRequest.getTechnician() != null ? savedRequest.getTechnician().getId() : null,
                null // No explicit cancel reason in current API
        ));

        return convertToDTO(savedRequest);
    }

    // ==================== TECHNICIAN OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDTO> getAvailableServiceRequests() {
        logBusinessOperation("GET_AVAILABLE_SERVICE_REQUESTS");

        List<ServiceRequest> requests = serviceRequestRepository.findAvailableRequests(ServiceRequestStatus.PENDING);
        return requests.stream()
                .map(this::convertToServiceRequestSummaryDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestDTO> getMyAssignedRequests() {
        logBusinessOperation("GET_MY_ASSIGNED_REQUESTS");

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        List<ServiceRequest> requests = serviceRequestRepository.findByTechnicianId(technician.getId());
        return convertToDTO(requests);
    }

    @Transactional
    public ServiceRequestDTO acceptServiceRequest(Long id) {
        logBusinessOperation("ACCEPT_SERVICE_REQUEST", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate technician can accept requests
        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianApproved(technicianProfile);

        // Validate service request can be accepted
        if (serviceRequest.getStatus() != ServiceRequestStatus.PENDING) {
            throw InvalidServiceRequestStatusException.cannotAssign(serviceRequest.getStatus());
        }

        if (serviceRequest.getTechnician() != null) {
            throw new BusinessValidationException("Service request is already assigned to another technician");
        }

        serviceRequest.setTechnician(technician);
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO declineServiceRequest(Long id) {
        logBusinessOperation("DECLINE_SERVICE_REQUEST", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate technician can decline (must be assigned to them)
        if (serviceRequest.getTechnician() == null || 
            !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("You can only decline your assigned service requests");
        }

        if (serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            throw InvalidServiceRequestStatusException.cannotCancel(serviceRequest.getStatus());
        }

        serviceRequest.setTechnician(null);
        serviceRequest.setStatus(ServiceRequestStatus.PENDING);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO startWork(Long id) {
        logBusinessOperation("START_WORK", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate technician can start work
        if (serviceRequest.getTechnician() == null || 
            !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("You can only start work on your assigned service requests");
        }

        if (serviceRequest.getStatus() != ServiceRequestStatus.ASSIGNED) {
            throw InvalidServiceRequestStatusException.cannotStartWork(serviceRequest.getStatus());
        }

        ServiceRequestStatus oldStatus = serviceRequest.getStatus();
        serviceRequest.setStatus(ServiceRequestStatus.IN_PROGRESS);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // Publish status changed event
        eventPublisher.publishEvent(new ServiceRequestStatusChangedEvent(
                savedRequest.getId(),
                savedRequest.getCustomer().getId(),
                savedRequest.getTechnician() != null ? savedRequest.getTechnician().getId() : null,
                oldStatus,
                savedRequest.getStatus()
        ));

        return convertToDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO completeWork(Long id) {
        logBusinessOperation("COMPLETE_WORK", "id=" + id);

        validatePositiveId(id, "id");
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate technician can complete work
        if (serviceRequest.getTechnician() == null || 
            !serviceRequest.getTechnician().getId().equals(technician.getId())) {
            throw new SecurityException("You can only complete your assigned service requests");
        }

        if (serviceRequest.getStatus() != ServiceRequestStatus.IN_PROGRESS) {
            throw InvalidServiceRequestStatusException.cannotComplete(serviceRequest.getStatus());
        }

        ServiceRequestStatus oldStatus = serviceRequest.getStatus();
        serviceRequest.setStatus(ServiceRequestStatus.DONE);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        // Publish status changed event
        eventPublisher.publishEvent(new ServiceRequestStatusChangedEvent(
                savedRequest.getId(),
                savedRequest.getCustomer().getId(),
                savedRequest.getTechnician() != null ? savedRequest.getTechnician().getId() : null,
                oldStatus,
                savedRequest.getStatus()
        ));

        return convertToDTO(savedRequest);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<ServiceRequestSummaryDTO> getAllServiceRequests(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_SERVICE_REQUESTS", "page=" + page);
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceRequest> requestPage = serviceRequestRepository.findAll(pageable);
        
        return requestPage.map(this::convertToServiceRequestSummaryDTO);
    }

    @Transactional(readOnly = true)
    public List<ServiceRequestSummaryDTO> getServiceRequestsByStatus(ServiceRequestStatus status) {
        logBusinessOperation("GET_SERVICE_REQUESTS_BY_STATUS", "status=" + status);
        requireRole(Role.ADMIN);

        validateRequired(status, "status");
        List<ServiceRequest> requests = serviceRequestRepository.findByStatus(status);
        return requests.stream()
                .map(this::convertToServiceRequestSummaryDTO)
                .toList();
    }

    @Transactional
    public ServiceRequestDTO assignTechnician(Long id, AssignTechnicianRequest request) {
        logBusinessOperation("ASSIGN_TECHNICIAN", "id=" + id, "technicianId=" + request.getTechnicianId());
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        validateRequired(request, "request");
        
        ServiceRequest serviceRequest = findServiceRequestById(id);
        User technician = findUserById(request.getTechnicianId());
        
        // Validate assignment
        validateServiceRequestAssignment(serviceRequest, technician);
        
        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianApproved(technicianProfile);

        serviceRequest.setTechnician(technician);
        serviceRequest.setStatus(ServiceRequestStatus.ASSIGNED);
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedRequest);
    }

    @Transactional
    public ServiceRequestDTO updateServiceRequestStatus(Long id, UpdateServiceRequestStatusRequest request) {
        logBusinessOperation("UPDATE_SERVICE_REQUEST_STATUS", "id=" + id, "status=" + request.getStatus());
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        validateRequired(request, "request");
        
        ServiceRequest serviceRequest = findServiceRequestById(id);
        
        validateStatusTransition(serviceRequest.getStatus(), request.getStatus());

        serviceRequest.setStatus(request.getStatus());
        ServiceRequest savedRequest = serviceRequestRepository.save(serviceRequest);

        return convertToDTO(savedRequest);
    }

    @Transactional(readOnly = true)
    public ServiceRequestStatsDTO getServiceRequestStats() {
        logBusinessOperation("GET_SERVICE_REQUEST_STATS");
        requireRole(Role.ADMIN);

        long totalRequests = serviceRequestRepository.count();
        long pendingRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.PENDING);
        long assignedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.ASSIGNED);
        long inProgressRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS);
        long completedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.DONE);
        long cancelledRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.CANCELLED);

        // Calculate completion rate
        double completionRate = totalRequests > 0 ? 
                               (double) completedRequests / totalRequests * 100 : 0.0;

        // Calculate average price
        BigDecimal avgPrice = serviceRequestRepository.findAveragePrice()
                                                    .orElse(BigDecimal.ZERO)
                                                    .setScale(2, RoundingMode.HALF_UP);

        return ServiceRequestStatsDTO.builder()
                .totalRequests(totalRequests)
                .pendingRequests(pendingRequests)
                .assignedRequests(assignedRequests)
                .inProgressRequests(inProgressRequests)
                .completedRequests(completedRequests)
                .cancelledRequests(cancelledRequests)
                .completionRate(completionRate)
                .averagePrice(avgPrice)
                .build();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private ServiceRequest findServiceRequestById(Long id) {
        ServiceRequest serviceRequest = serviceRequestRepository.findByIdWithDetails(id);
        if (serviceRequest == null) {
            throw new ServiceRequestNotFoundException(id);
        }
        return serviceRequest;
    }

    private com.fix4home.fix4home.model.entity.Service findServiceById(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    private Address findAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessValidationException("Address not found with id: " + addressId));
    }

    private CustomerProfile findCustomerProfileByUser(User user) {
        return customerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessValidationException("Customer profile not found"));
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianNotFoundException(user.getId()));
    }

    private void validateServiceRequestAccess(ServiceRequest serviceRequest, User currentUser) {
        switch (currentUser.getRole()) {
            case CUSTOMER -> {
                if (!serviceRequest.getCustomer().getId().equals(currentUser.getId())) {
                    throw new SecurityException("You can only view your own service requests");
                }
            }
            case TECHNICIAN -> {
                if (serviceRequest.getTechnician() == null || 
                    !serviceRequest.getTechnician().getId().equals(currentUser.getId())) {
                    throw new SecurityException("You can only view your assigned service requests");
                }
            }
            case ADMIN -> {
                // Admin can view all requests
            }
        }
    }

    // ==================== DTO CONVERSION METHODS ====================

    @Override
    public ServiceRequestDTO convertToDTO(ServiceRequest serviceRequest) {
        return convertToServiceRequestDTO(serviceRequest);
    }

    private ServiceRequestDTO convertToServiceRequestDTO(ServiceRequest serviceRequest) {
        ServiceRequestDTO.CustomerSummaryDTO customer = ServiceRequestDTO.CustomerSummaryDTO.builder()
                .userId(serviceRequest.getCustomer().getId())
                .username(serviceRequest.getCustomer().getUsername())
                .fullName(getCustomerName(serviceRequest.getCustomer()))
                .build();

        ServiceRequestDTO.ServiceRequestDTOBuilder builder = ServiceRequestDTO.builder()
                .id(serviceRequest.getId())
                .status(serviceRequest.getStatus())
                .description(serviceRequest.getDescription())
                .scheduledTime(serviceRequest.getScheduledTime())
                .completedTime(serviceRequest.getCompletedTime())
                .createdAt(serviceRequest.getCreatedAt())
                .price(serviceRequest.getPrice())
                .customer(customer);

        // Add service information
        ServiceDTO service = ServiceDTO.builder()
                .id(serviceRequest.getService().getId())
                .name(serviceRequest.getService().getName())
                .description(serviceRequest.getService().getDescription())
                .basePrice(serviceRequest.getService().getBasePrice())
                .status(serviceRequest.getService().getStatus())
                .build();
        builder.service(service);

        // Add address information
        AddressDTO address = AddressDTO.builder()
                .id(serviceRequest.getAddress().getId())
                .addressLine(serviceRequest.getAddress().getAddressLine())
                .city(serviceRequest.getAddress().getCity())
                .build();
        builder.address(address);

        if (serviceRequest.getTechnician() != null) {
            ServiceRequestDTO.TechnicianSummaryDTO technician = ServiceRequestDTO.TechnicianSummaryDTO.builder()
                    .userId(serviceRequest.getTechnician().getId())
                    .username(serviceRequest.getTechnician().getUsername())
                    .fullName(getTechnicianName(serviceRequest.getTechnician()))
                    .build();
            builder.technician(technician);
        }

        return builder.build();
    }

    private ServiceRequestSummaryDTO convertToServiceRequestSummaryDTO(ServiceRequest serviceRequest) {
        return ServiceRequestSummaryDTO.builder()
                .id(serviceRequest.getId())
                .status(serviceRequest.getStatus())
                .description(serviceRequest.getDescription())
                .scheduledTime(serviceRequest.getScheduledTime())
                .createdAt(serviceRequest.getCreatedAt())
                .price(serviceRequest.getPrice())
                .customerName(getCustomerName(serviceRequest.getCustomer()))
                .serviceName(serviceRequest.getService().getName())
                .address(serviceRequest.getAddress().getAddressLine())
                .city(serviceRequest.getAddress().getCity())
                .technicianName(serviceRequest.getTechnician() != null ? getTechnicianName(serviceRequest.getTechnician()) : null)
                .build();
    }

    private String getCustomerName(User customer) {
        return customerProfileRepository.findByUser(customer)
                .map(CustomerProfile::getFullName)
                .orElse(customer.getUsername());
    }

    private String getTechnicianName(User technician) {
        return technicianProfileRepository.findByUser(technician)
                .map(TechnicianProfile::getFullName)
                .orElse(technician.getUsername());
    }
} 