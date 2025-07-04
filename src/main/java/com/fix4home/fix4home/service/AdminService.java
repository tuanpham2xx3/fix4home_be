package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final AddressRepository addressRepository;

    // ==================== SYSTEM OVERVIEW ====================

    @Transactional(readOnly = true)
    public SystemOverviewDTO getSystemOverview() {
        log.info("Generating system overview for admin dashboard");

        // User Statistics
        long totalUsers = userRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalTechnicians = userRepository.countByRole(Role.TECHNICIAN);
        long activeUsers = userRepository.findByStatus(UserStatus.ACTIVE).size();
        
        // Technician approval stats (using User status instead)
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

        // Monthly revenue (simplified - current month)
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

        // System Health (mock data - in real implementation would check actual system metrics)
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
                .lastBackupTime(LocalDateTime.now().minusHours(2)) // Mock data
                .systemHealth(systemHealth)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== USER MANAGEMENT ====================

    @Transactional(readOnly = true)
    public Page<UserManagementDTO> getAllUsers(int page, int size, String sortBy, String sortDir, Role role) {
        log.info("Admin fetching all users - page: {}, size: {}, role: {}", page, size, role);

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
        log.info("Admin fetching user details for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        return convertToUserManagementDTO(user);
    }

    @Transactional
    public UserManagementDTO updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        log.info("Admin updating user status for ID: {} to {}", userId, request.getStatus());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        user.setStatus(request.getStatus());
        User savedUser = userRepository.save(user);

        log.info("User status updated successfully for ID: {} to {}", userId, request.getStatus());
        return convertToUserManagementDTO(savedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        // Check if user has active service requests
        List<ServiceRequest> activeRequests = serviceRequestRepository.findByCustomer(user)
                .stream()
                .filter(sr -> sr.getStatus() == ServiceRequestStatus.PENDING || 
                             sr.getStatus() == ServiceRequestStatus.ASSIGNED || 
                             sr.getStatus() == ServiceRequestStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        if (!activeRequests.isEmpty()) {
            throw new BadRequestException("Cannot delete user with active service requests");
        }

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }

    // ==================== BULK OPERATIONS ====================

    @Transactional
    public BulkOperationResultDTO executeBulkOperation(BulkOperationRequest request) {
        log.info("Executing bulk operation: {} for {} targets", request.getOperationType(), request.getTargetIds().size());

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
                        throw new BadRequestException("Unsupported bulk operation: " + request.getOperationType());
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
                .executedBy("admin") // In real implementation, get from security context
                .build();
    }

    // ==================== REPORTING ====================

    @Transactional(readOnly = true)
    public SystemReportDTO generateSystemReport(String reportType, LocalDate startDate, LocalDate endDate) {
        log.info("Generating system report: {} from {} to {}", reportType, startDate, endDate);

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

        // Generate activity trends (daily breakdown)
        List<SystemReportDTO.ActivityTrendDTO> trends = generateActivityTrends(startDate, endDate);

        return SystemReportDTO.builder()
                .reportType(reportType)
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDateTime.now())
                .newUsersCount(newUsersCount)
                .activeUsersCount(userRepository.findByStatus(UserStatus.ACTIVE).size())
                .totalLoginCount(0) // Mock data - would track actual logins
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private void approveTechnician(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        
        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }
        
        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));
        
        profile.setStatus(UserStatus.ACTIVE);
        technicianProfileRepository.save(profile);
    }

    private void rejectTechnician(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        
        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }
        
        // Reject by setting user status to INACTIVE
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private void activateService(Long serviceId) {
        com.fix4home.fix4home.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + serviceId));
        service.setStatus(UserStatus.ACTIVE);
        serviceRepository.save(service);
    }

    private void deactivateService(Long serviceId) {
        com.fix4home.fix4home.model.entity.Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + serviceId));
        service.setStatus(UserStatus.INACTIVE);
        serviceRepository.save(service);
    }

    private void cancelServiceRequest(Long requestId) {
        ServiceRequest request = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BadRequestException("Service request not found with id: " + requestId));
        
        if (request.getStatus() == ServiceRequestStatus.DONE) {
            throw new BadRequestException("Cannot cancel completed service request");
        }
        
        request.setStatus(ServiceRequestStatus.CANCELLED);
        serviceRequestRepository.save(request);
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
                       .profileStatus(user.getStatus().toString()); // Use user status instead
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
        // Simplified implementation - in real scenario would have more complex logic
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
                    long requestCount = serviceRequestRepository.findByServiceId(service.getId()).size();
                    BigDecimal revenue = serviceRequestRepository.findByServiceId(service.getId()).stream()
                            .filter(sr -> sr.getStatus() == ServiceRequestStatus.DONE)
                            .map(ServiceRequest::getPrice)
                            .filter(price -> price != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return SystemReportDTO.PopularServiceDTO.builder()
                            .serviceId(service.getId())
                            .serviceName(service.getName())
                            .requestCount(requestCount)
                            .totalRevenue(revenue)
                            .averageRating(4.5) // Mock data
                            .build();
                })
                .collect(Collectors.toList());
    }
} 