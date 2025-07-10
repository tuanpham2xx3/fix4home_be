package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.admin.*;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService extends BaseService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final AddressRepository addressRepository;

    // ==================== SYSTEM OVERVIEW ====================

    @Transactional(readOnly = true)
    public SystemOverviewDTO getSystemOverview() {
        logBusinessOperation("GET_SYSTEM_OVERVIEW");
        requireRole(Role.ADMIN);

        // User Statistics
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalTechnicians = userRepository.countByRole(Role.TECHNICIAN);
        long activeUsers = userRepository.findByStatus(UserStatus.ACTIVE).size();
        
        // Technician approval stats
        long pendingTechnicians = userRepository.findByRoleAndStatus(Role.TECHNICIAN, UserStatus.INACTIVE).size();
        long approvedTechnicians = userRepository.findByRoleAndStatus(Role.TECHNICIAN, UserStatus.ACTIVE).size();

        // Service Statistics
        long totalServices = serviceRepository.count();
        long activeServices = serviceRepository.findByStatus(UserStatus.ACTIVE).size();
        long inactiveServices = serviceRepository.findByStatus(UserStatus.INACTIVE).size();

        // Service Request Statistics
        long totalServiceRequests = serviceRequestRepository.count();
        long pendingRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.PENDING);
        long assignedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.ASSIGNED);
        long inProgressRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS);
        long completedRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.DONE);
        long cancelledRequests = serviceRequestRepository.countByStatus(ServiceRequestStatus.CANCELLED);

        // Financial Statistics
        List<ServiceRequest> completedRequestsList = serviceRequestRepository.findByStatus(ServiceRequestStatus.DONE);
        BigDecimal totalRevenue = completedRequestsList.stream()
                .map(ServiceRequest::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Monthly revenue (current month)
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        List<ServiceRequest> monthlyRequests = serviceRequestRepository.findByDateRange(startOfMonth, LocalDateTime.now())
                .stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                .collect(Collectors.toList());
        
        BigDecimal monthlyRevenue = monthlyRequests.stream()
                .map(ServiceRequest::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRequestValue = completedRequests > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(completedRequests), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        // Performance Metrics
        double completionRate = totalServiceRequests > 0 ? 
                (double) completedRequests / totalServiceRequests * 100 : 0;
        double cancellationRate = totalServiceRequests > 0 ? 
                (double) cancelledRequests / totalServiceRequests * 100 : 0;

        // System Health
        SystemOverviewDTO.SystemHealth systemHealth = SystemOverviewDTO.SystemHealth.builder()
                .status("HEALTHY")
                .message("All systems operational")
                .cpuUsage(45.2)
                .memoryUsage(67.8)
                .diskUsage(23.4)
                .build();

        return SystemOverviewDTO.builder()
                .totalUsers(totalUsers)
                .totalCustomers(totalCustomers)
                .totalTechnicians(totalTechnicians)
                .activeUsers(activeUsers)
                .pendingTechnicians(pendingTechnicians)
                .approvedTechnicians(approvedTechnicians)
                .totalServices(totalServices)
                .activeServices(activeServices)
                .inactiveServices(inactiveServices)
                .totalServiceRequests(totalServiceRequests)
                .pendingRequests(pendingRequests)
                .assignedRequests(assignedRequests)
                .inProgressRequests(inProgressRequests)
                .completedRequests(completedRequests)
                .cancelledRequests(cancelledRequests)
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .averageRequestValue(averageRequestValue)
                .completionRate(completionRate)
                .cancellationRate(cancellationRate)
                .customerSatisfactionRate(95.5) // Mock data
                .averageCompletionTime(24.5) // Mock data
                .activeConnections(127) // Mock data
                .lastBackupTime(LocalDateTime.now().minusHours(2))
                .systemHealth(systemHealth)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== USER MANAGEMENT ====================

    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getAllUsers(int page, int size, String sortBy, String sortDir, Role role) {
        logBusinessOperation("GET_ALL_USERS", "page=" + page, "role=" + role);
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<User> userPage;
        if (role != null) {
            userPage = userRepository.findByRole(role, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }
        
        return userPage.map(this::convertToUserManagementDTO);
    }

    @Transactional(readOnly = true)
    public UserManagementDTO getUserById(Long userId) {
        logBusinessOperation("GET_USER_BY_ID", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        User user = findUserById(userId);

        return convertToUserManagementDTO(user);
    }

    @Transactional
    public UserManagementDTO updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        logBusinessOperation("UPDATE_USER_STATUS", "userId=" + userId, "status=" + request.getStatus());
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");
        validateRequired(request.getStatus(), "status");

        User user = findUserById(userId);
        user.setStatus(request.getStatus());
        User savedUser = userRepository.save(user);

        return convertToUserManagementDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        logBusinessOperation("DELETE_USER", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        User user = findUserById(userId);

        // Check if user has active service requests
        List<ServiceRequest> activeRequests = serviceRequestRepository.findByCustomer(user)
                .stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.PENDING || 
                             sr.getStatus() == ServiceRequestStatus.ASSIGNED || 
                             sr.getStatus() == ServiceRequestStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        if (!activeRequests.isEmpty()) {
            throw new BusinessValidationException("Cannot delete user with active service requests");
        }

        userRepository.delete(user);
    }

    // ==================== TECHNICIAN APPROVAL MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<TechnicianApprovalDTO> getPendingTechnicians() {
        logBusinessOperation("GET_PENDING_TECHNICIANS");
        requireRole(Role.ADMIN);

        List<TechnicianProfile> pendingProfiles = technicianProfileRepository.findByStatus(UserStatus.PENDING_APPROVAL);
        return pendingProfiles.stream()
                .map(this::convertToTechnicianApprovalDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TechnicianApprovalDTO> getPendingTechniciansWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_PENDING_TECHNICIANS_PAGINATED");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TechnicianProfile> profilePage = technicianProfileRepository.findByStatus(UserStatus.PENDING_APPROVAL, pageable);
        
        return profilePage.map(this::convertToTechnicianApprovalDTO);
    }

    @Transactional
    public TechnicianApprovalDTO approveTechnicianApplication(Long userId, ApproveTechnicianRequest request) {
        logBusinessOperation("APPROVE_TECHNICIAN", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");

        User technician = findUserById(userId);
        
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }

        TechnicianProfile profile = findTechnicianProfileByUser(technician);
        
        if (profile.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new BusinessValidationException("Technician is not pending approval. Current status: " + profile.getStatus());
        }

        // Update approval status
        profile.setStatus(UserStatus.ACTIVE);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(getCurrentUser().getId());
        profile.setRejectionReason(null); // Clear any previous rejection reason

        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
        
        return convertToTechnicianApprovalDTO(savedProfile);
    }

    @Transactional
    public TechnicianApprovalDTO rejectTechnicianApplication(Long userId, RejectTechnicianRequest request) {
        logBusinessOperation("REJECT_TECHNICIAN", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");
        validateRequired(request.getRejectionReason(), "rejectionReason");

        User technician = findUserById(userId);
        
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }

        TechnicianProfile profile = findTechnicianProfileByUser(technician);
        
        if (profile.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new BusinessValidationException("Technician is not pending approval. Current status: " + profile.getStatus());
        }

        // Update rejection status
        profile.setStatus(UserStatus.REJECTED);
        profile.setRejectionReason(request.getRejectionReason());
        profile.setApprovedAt(LocalDateTime.now()); // Set timestamp when decision was made
        profile.setApprovedBy(getCurrentUser().getId());

        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
        
        return convertToTechnicianApprovalDTO(savedProfile);
    }

    @Transactional(readOnly = true)
    public TechnicianApprovalDTO getTechnicianApprovalDetails(Long userId) {
        logBusinessOperation("GET_TECHNICIAN_APPROVAL_DETAILS", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");

        User technician = findUserById(userId);
        
        if (technician.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }

        TechnicianProfile profile = findTechnicianProfileByUser(technician);
        return convertToTechnicianApprovalDTO(profile);
    }

    // ==================== BULK OPERATIONS ====================

    @Transactional
    public BulkOperationResultDTO executeBulkOperation(BulkOperationRequest request) {
        logBusinessOperation("EXECUTE_BULK_OPERATION", "type=" + request.getOperationType());
        requireRole(Role.ADMIN);

        validateRequired(request, "request");
        validateRequired(request.getOperationType(), "operationType");
        validateRequired(request.getTargetIds(), "targetIds");

        List<BulkOperationResultDTO.OperationError> errors = new ArrayList<>();
        int successCount = 0;

        for (Long targetId : request.getTargetIds()) {
            try {
                switch (request.getOperationType()) {
                    case ACTIVATE_USERS:
                        activateUser(targetId);
                        break;
                    case DEACTIVATE_USERS:
                        deactivateUser(targetId);
                        break;
                    case DELETE_USERS:
                        deleteUser(targetId);
                        break;
                    case APPROVE_TECHNICIANS:
                        approveTechnician(targetId);
                        break;
                    case REJECT_TECHNICIANS:
                        rejectTechnician(targetId);
                        break;
                    case ACTIVATE_SERVICES:
                        activateService(targetId);
                        break;
                    case DEACTIVATE_SERVICES:
                        deactivateService(targetId);
                        break;
                    case CANCEL_REQUESTS:
                        cancelServiceRequest(targetId);
                        break;
                    default:
                        throw new BusinessValidationException("Unsupported bulk operation: " + request.getOperationType());
                }
                successCount++;
            } catch (Exception e) {
                errors.add(BulkOperationResultDTO.OperationError.builder()
                        .targetId(targetId)
                        .errorMessage(e.getMessage())
                        .errorCode("OPERATION_FAILED")
                        .build());
            }
        }

        return BulkOperationResultDTO.builder()
                .operationType(request.getOperationType())
                .totalTargets(request.getTargetIds().size())
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .executedAt(LocalDateTime.now())
                .executedBy(getCurrentUser().getUsername())
                .build();
    }

    // ==================== REPORTING ====================

    @Transactional(readOnly = true)
    public SystemReportDTO generateSystemReport(String reportType, LocalDate startDate, LocalDate endDate) {
        logBusinessOperation("GENERATE_SYSTEM_REPORT", "type=" + reportType);
        requireRole(Role.ADMIN);

        validateRequired(reportType, "reportType");
        validateRequired(startDate, "startDate");
        validateRequired(endDate, "endDate");

        if (startDate.isAfter(endDate)) {
            throw new BusinessValidationException("Start date cannot be after end date");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        // Get data for the period
        List<ServiceRequest> periodRequests = serviceRequestRepository.findByDateRange(startDateTime, endDateTime);
        List<User> periodUsers = userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt() != null && 
                               user.getCreatedAt().isAfter(startDateTime) && 
                               user.getCreatedAt().isBefore(endDateTime))
                .collect(Collectors.toList());

        // Calculate metrics
        long newUsersCount = periodUsers.size();
        long newRequestsCount = periodRequests.size();
        long completedRequestsCount = periodRequests.stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                .count();
        long cancelledRequestsCount = periodRequests.stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.CANCELLED)
                .count();

        BigDecimal totalRevenue = periodRequests.stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                .map(ServiceRequest::getPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageRequestValue = completedRequestsCount > 0 ? 
                totalRevenue.divide(BigDecimal.valueOf(completedRequestsCount), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;

        // Generate activity trends
        List<SystemReportDTO.ActivityTrendDTO> trends = generateActivityTrends(startDate, endDate);

        return SystemReportDTO.builder()
                .reportType(reportType)
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .newUsersCount(newUsersCount)
                .activeUsersCount(userRepository.findByStatus(UserStatus.ACTIVE).size())
                .totalLoginCount(0) // Mock data
                .newRequestsCount(newRequestsCount)
                .completedRequestsCount(completedRequestsCount)
                .cancelledRequestsCount(cancelledRequestsCount)
                .totalRevenue(totalRevenue)
                .totalProfit(totalRevenue.multiply(BigDecimal.valueOf(0.1))) // 10% commission
                .averageRequestValue(averageRequestValue)
                .avgCompletionTime(24.5) // Mock data
                .customerSatisfactionScore(4.5) // Mock data
                .topTechnicians(getTopTechnicians(5))
                .popularServices(getPopularServices(5))
                .activityTrends(trends)
                .build();
    }

    // ==================== HELPER METHODS ====================

    private void activateUser(Long userId) {
        User user = findUserById(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private void deactivateUser(Long userId) {
        User user = findUserById(userId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private void approveTechnician(Long userId) {
        User user = findUserById(userId);
        
        if (user.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }
        
        TechnicianProfile profile = findTechnicianProfileByUser(user);
        profile.setStatus(UserStatus.ACTIVE);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(getCurrentUser().getId());
        technicianProfileRepository.save(profile);
    }

    private void rejectTechnician(Long userId) {
        User user = findUserById(userId);
        
        if (user.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }
        
        TechnicianProfile profile = findTechnicianProfileByUser(user);
        profile.setStatus(UserStatus.REJECTED);
        profile.setRejectionReason("Rejected by bulk operation");
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(getCurrentUser().getId());
        technicianProfileRepository.save(profile);
    }

    private void activateService(Long serviceId) {
        com.fix4home.fix4home.model.entity.Service service = findServiceById(serviceId);
        service.setStatus(UserStatus.ACTIVE);
        serviceRepository.save(service);
    }

    private void deactivateService(Long serviceId) {
        com.fix4home.fix4home.model.entity.Service service = findServiceById(serviceId);
        service.setStatus(UserStatus.INACTIVE);
        serviceRepository.save(service);
    }

    private void cancelServiceRequest(Long requestId) {
        ServiceRequest request = findServiceRequestById(requestId);
        
        if (request.getStatus() == ServiceRequestStatus.DONE) {
            throw new BusinessValidationException("Cannot cancel completed service request");
        }
        
        request.setStatus(ServiceRequestStatus.CANCELLED);
        serviceRequestRepository.save(request);
    }

    private ServiceRequest findServiceRequestById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new ServiceRequestNotFoundException(requestId));
    }

    private com.fix4home.fix4home.model.entity.Service findServiceById(Long serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ServiceNotFoundException(serviceId));
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianNotFoundException(user.getId()));
    }

    private UserManagementDTO convertToUserManagementDTO(User user) {
        UserManagementDTO.UserManagementDTOBuilder builder = UserManagementDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(null); // Mock data

        // Get profile information based on role
        if (user.getRole() == Role.CUSTOMER) {
            CustomerProfile customerProfile = customerProfileRepository.findByUser(user).orElse(null);
            if (customerProfile != null) {
                builder.fullName(customerProfile.getFullName())
                       .profileStatus(user.getStatus().toString());
            }
            
            // Get additional customer info
            long totalAddresses = addressRepository.findAll().stream()
                    .filter(addr -> addr.getUser() != null && addr.getUser().getId().equals(user.getId()))
                    .count();
            builder.totalAddresses(totalAddresses);
            
        } else if (user.getRole() == Role.TECHNICIAN) {
            TechnicianProfile technicianProfile = technicianProfileRepository.findByUser(user).orElse(null);
            if (technicianProfile != null) {
                builder.fullName(technicianProfile.getFullName())
                       .profileStatus(technicianProfile.getStatus().toString())
                       .rating(technicianProfile.getRating())
                       .isApproved(technicianProfile.getStatus() == UserStatus.ACTIVE);
            }
        }

        // Get service request statistics
        List<ServiceRequest> userRequests = serviceRequestRepository.findByCustomer(user);
        long totalRequests = userRequests.size();
        long completedRequests = userRequests.stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                .count();

        builder.totalServiceRequests(totalRequests)
               .completedServiceRequests(completedRequests);

        return builder.build();
    }

    private TechnicianApprovalDTO convertToTechnicianApprovalDTO(TechnicianProfile profile) {
        User user = profile.getUser();
        
        TechnicianApprovalDTO.TechnicianApprovalDTOBuilder builder = TechnicianApprovalDTO.builder()
                .userId(user.getId())
                .profileId(profile.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .fullName(profile.getFullName())
                .skills(profile.getSkills())
                .experience(profile.getExperience())
                .rating(profile.getRating())
                .status(profile.getStatus())
                .verificationDocuments(profile.getVerificationDocuments())
                .rejectionReason(profile.getRejectionReason())
                .approvedAt(profile.getApprovedAt())
                .approvedBy(profile.getApprovedBy())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        // Get approved by username if available
        if (profile.getApprovedBy() != null) {
            User approver = userRepository.findById(profile.getApprovedBy()).orElse(null);
            if (approver != null) {
                builder.approvedByUsername(approver.getUsername());
            }
        }

        return builder.build();
    }

    private List<SystemReportDTO.ActivityTrendDTO> generateActivityTrends(LocalDate startDate, LocalDate endDate) {
        List<SystemReportDTO.ActivityTrendDTO> trends = new ArrayList<>();
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
            
            List<ServiceRequest> dayRequests = serviceRequestRepository.findByDateRange(dayStart, dayEnd);
            BigDecimal dayRevenue = dayRequests.stream()
                    .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                    .map(ServiceRequest::getPrice)
                    .filter(price -> price != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            trends.add(SystemReportDTO.ActivityTrendDTO.builder()
                    .date(currentDate)
                    .requestCount(dayRequests.size())
                    .userCount(0) // Mock data
                    .revenue(dayRevenue)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return trends;
    }

    private List<SystemReportDTO.TopPerformerDTO> getTopTechnicians(int limit) {
        List<User> technicians = userRepository.findByRole(Role.TECHNICIAN);
        
        return technicians.stream()
                .limit(limit)
                .map(technician -> {
                    TechnicianProfile profile = technicianProfileRepository.findByUser(technician).orElse(null);
                    long completedJobs = serviceRequestRepository.findByTechnician(technician).stream()
                            .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                            .count();
                    
                    return SystemReportDTO.TopPerformerDTO.builder()
                            .technicianId(technician.getId())
                            .technicianName(profile != null ? profile.getFullName() : technician.getUsername())
                            .completedJobs(completedJobs)
                            .averageRating(profile != null ? profile.getRating() : 0.0f)
                            .totalEarnings(BigDecimal.valueOf(completedJobs * 150)) // Mock calculation
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<SystemReportDTO.PopularServiceDTO> getPopularServices(int limit) {
        List<com.fix4home.fix4home.model.entity.Service> services = serviceRepository.findAll();
        
        return services.stream()
                .limit(limit)
                .map(service -> {
                    List<ServiceRequest> serviceRequests = serviceRequestRepository.findByServiceId(service.getId());
                    long requestCount = serviceRequests.size();
                    BigDecimal totalRevenue = serviceRequests.stream()
                            .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                            .map(ServiceRequest::getPrice)
                            .filter(price -> price != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return SystemReportDTO.PopularServiceDTO.builder()
                            .serviceId(service.getId())
                            .serviceName(service.getName())
                            .requestCount(requestCount)
                            .totalRevenue(totalRevenue)
                            .averageRating(4.5) // Mock data
                            .build();
                })
                .collect(Collectors.toList());
    }
} 