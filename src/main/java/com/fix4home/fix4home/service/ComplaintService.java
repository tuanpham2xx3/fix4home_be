package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.complaint.*;
import com.fix4home.fix4home.model.dto.servicerequest.ServiceRequestSummaryDTO;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.*;
import com.fix4home.fix4home.repository.*;
import com.fix4home.fix4home.service.event.ComplaintCreatedEvent;
import com.fix4home.fix4home.service.event.ComplaintStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintService extends BaseService implements DTOConverter<Complaint, ComplaintDTO> {

    private final ComplaintRepository complaintRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==================== CUSTOMER & TECHNICIAN OPERATIONS ====================

    @Transactional
    public ComplaintDTO createComplaint(CreateComplaintRequest request) {
        logBusinessOperation("CREATE_COMPLAINT", "serviceRequestId=" + request.getServiceRequestId());

        validateRequired(request, "request");
        User complainant = getCurrentUser();
        
        // Validate service request exists
        ServiceRequest serviceRequest = findServiceRequestById(request.getServiceRequestId());
        
        // Validate accused user exists
        User accused = findUserById(request.getAccusedId());
        
        // Business validations
        validateComplaintCreation(serviceRequest, complainant, accused);
        
        // Create complaint
        Complaint complaint = Complaint.builder()
                .serviceRequest(serviceRequest)
                .complainant(complainant)
                .accused(accused)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .build();

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Publish complaint created event
        eventPublisher.publishEvent(new ComplaintCreatedEvent(
                savedComplaint.getId(),
                savedComplaint.getServiceRequest().getId(),
                savedComplaint.getComplainant().getId(),
                savedComplaint.getAccused().getId()
        ));

        // Update service request status to COMPLAINING
        updateServiceRequestToComplaining(serviceRequest);
        
        return convertToDTO(savedComplaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getMyComplaints() {
        logBusinessOperation("GET_MY_COMPLAINTS");

        User currentUser = getCurrentUser();
        List<Complaint> complaints = complaintRepository.findByUserId(currentUser.getId());
        return convertToDTO(complaints);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsAgainstMe() {
        logBusinessOperation("GET_COMPLAINTS_AGAINST_ME");

        User currentUser = getCurrentUser();
        List<Complaint> complaints = complaintRepository.findByAccusedId(currentUser.getId());
        return convertToDTO(complaints);
    }

    @Transactional(readOnly = true)
    public ComplaintDTO getComplaintById(Long id) {
        logBusinessOperation("GET_COMPLAINT_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        Complaint complaint = findComplaintById(id);
        User currentUser = getCurrentUser();

        // Authorization check
        validateComplaintAccess(complaint, currentUser);

        return convertToDTO(complaint);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public Page<ComplaintDTO> getAllComplaints(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_COMPLAINTS", "page=" + page);
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Complaint> complaintPage = complaintRepository.findAll(pageable);
        
        return complaintPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getComplaintsByStatus(ComplaintStatus status) {
        logBusinessOperation("GET_COMPLAINTS_BY_STATUS", "status=" + status);
        requireRole(Role.ADMIN);

        validateRequired(status, "status");
        List<Complaint> complaints = complaintRepository.findByStatus(status);
        return convertToDTO(complaints);
    }

    @Transactional(readOnly = true)
    public List<ComplaintDTO> getPendingComplaints() {
        logBusinessOperation("GET_PENDING_COMPLAINTS");
        requireRole(Role.ADMIN);

        List<Complaint> complaints = complaintRepository.findPendingComplaints(ComplaintStatus.PENDING);
        return convertToDTO(complaints);
    }

    @Transactional
    public ComplaintDTO startInvestigation(Long id) {
        logBusinessOperation("START_INVESTIGATION", "id=" + id);
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        Complaint complaint = findComplaintById(id);
        User admin = getCurrentUser();

        // Validate can investigate
        if (!complaint.canBeInvestigated()) {
            throw ComplaintResolutionException.cannotInvestigate(complaint.getStatus());
        }

        complaint.startInvestigation(admin);
        Complaint savedComplaint = complaintRepository.save(complaint);

        return convertToDTO(savedComplaint);
    }

    @Transactional
    public ComplaintDTO resolveComplaint(Long id, ResolveComplaintRequest request) {
        logBusinessOperation("RESOLVE_COMPLAINT", "id=" + id, "status=" + request.getStatus());
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        validateRequired(request, "request");
        
        Complaint complaint = findComplaintById(id);
        ComplaintStatus oldStatus = complaint.getStatus();
        User admin = getCurrentUser();

        // Validate can resolve
        if (!complaint.canBeResolved()) {
            throw ComplaintResolutionException.cannotResolve(complaint.getStatus());
        }

        // Apply resolution
        if (request.getStatus() == ComplaintStatus.RESOLVED) {
            complaint.resolve(request.getAdminResponse(), admin);
        } else if (request.getStatus() == ComplaintStatus.REJECTED) {
            complaint.reject(request.getAdminResponse(), admin);
        } else {
            throw new BusinessValidationException("Invalid resolution status: " + request.getStatus());
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Publish complaint status changed event
        eventPublisher.publishEvent(new ComplaintStatusChangedEvent(
                savedComplaint.getId(),
                oldStatus,
                savedComplaint.getStatus()
        ));

        // Update service request status to COMPLAITED
        updateServiceRequestToComplaited(complaint.getServiceRequest());

        return convertToDTO(savedComplaint);
    }

    @Transactional(readOnly = true)
    public ComplaintStatsDTO getComplaintStats() {
        logBusinessOperation("GET_COMPLAINT_STATS");
        requireRole(Role.ADMIN);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime urgentThreshold = now.minusHours(48);

        // Basic counts
        long totalComplaints = complaintRepository.count();
        long pendingComplaints = complaintRepository.countByStatus(ComplaintStatus.PENDING);
        long investigatingComplaints = complaintRepository.countByStatus(ComplaintStatus.INVESTIGATING);
        long resolvedComplaints = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long rejectedComplaints = complaintRepository.countByStatus(ComplaintStatus.REJECTED);

        // Time-based counts
        long complaintsToday = complaintRepository.findByDateRange(startOfDay, now).size();
        long complaintsThisWeek = complaintRepository.findByDateRange(startOfWeek, now).size();
        long complaintsThisMonth = complaintRepository.findByDateRange(startOfMonth, now).size();

        // Resolution statistics
        List<Complaint> resolvedInPeriod = complaintRepository.findByResolvedDateRange(startOfMonth, now);
        Double averageResolutionTimeHours = calculateAverageResolutionTime(resolvedInPeriod);
        
        User currentAdmin = getCurrentUser();
        long totalResolvedByCurrentAdmin = complaintRepository.countByResolvedBy(currentAdmin);
        
        // Calculate resolution rate
        long totalProcessed = resolvedComplaints + rejectedComplaints;
        Double resolutionRate = totalProcessed > 0 ? (double) resolvedComplaints / totalProcessed * 100 : 0.0;

        // Urgent complaints (older than 48 hours)
        List<Complaint> urgentComplaints = complaintRepository.findByDateRange(LocalDateTime.MIN, urgentThreshold)
                .stream()
                .filter(c -> c.getStatus() == ComplaintStatus.PENDING)
                .toList();

        // Calculate percentages
        double pendingPercentage = totalComplaints > 0 ? (double) pendingComplaints / totalComplaints * 100 : 0.0;
        double investigatingPercentage = totalComplaints > 0 ? (double) investigatingComplaints / totalComplaints * 100 : 0.0;
        double resolvedPercentage = totalComplaints > 0 ? (double) resolvedComplaints / totalComplaints * 100 : 0.0;
        double rejectedPercentage = totalComplaints > 0 ? (double) rejectedComplaints / totalComplaints * 100 : 0.0;

        return ComplaintStatsDTO.builder()
                .totalComplaints(totalComplaints)
                .pendingComplaints(pendingComplaints)
                .investigatingComplaints(investigatingComplaints)
                .resolvedComplaints(resolvedComplaints)
                .rejectedComplaints(rejectedComplaints)
                .complaintsToday(complaintsToday)
                .complaintsThisWeek(complaintsThisWeek)
                .complaintsThisMonth(complaintsThisMonth)
                .averageResolutionTimeHours(averageResolutionTimeHours)
                .totalResolvedByCurrentAdmin(totalResolvedByCurrentAdmin)
                .resolutionRate(resolutionRate)
                .pendingPercentage(pendingPercentage)
                .investigatingPercentage(investigatingPercentage)
                .resolvedPercentage(resolvedPercentage)
                .rejectedPercentage(rejectedPercentage)
                .hasUrgentComplaints(!urgentComplaints.isEmpty())
                .urgentComplaintsCount((long) urgentComplaints.size())
                .generatedAt(now)
                .build();
    }

    // ==================== HELPER METHODS ====================

    private Complaint findComplaintById(Long id) {
        return complaintRepository.findByIdWithDetails(id)
                .orElseThrow(() -> ComplaintNotFoundException.withId(id));
    }

    private ServiceRequest findServiceRequestById(Long id) {
        return serviceRequestRepository.findById(id)
                .orElseThrow(() -> new ServiceRequestNotFoundException(id));
    }

    private void validateComplaintCreation(ServiceRequest serviceRequest, User complainant, User accused) {
        // Cannot complain against yourself
        if (complainant.getId().equals(accused.getId())) {
            throw ComplaintResolutionException.cannotComplainAgainstSelf();
        }

        // Service request must be completed or done
        if (serviceRequest.getStatus() != ServiceRequestStatus.DONE && 
            serviceRequest.getStatus() != ServiceRequestStatus.CANCELLED) {
            throw ComplaintResolutionException.serviceRequestNotCompleted();
        }

        // Check if complainant is involved in the service request
        if (!complainant.getId().equals(serviceRequest.getCustomer().getId()) && 
            (serviceRequest.getTechnician() == null || !complainant.getId().equals(serviceRequest.getTechnician().getId()))) {
            throw new SecurityException("You can only file complaints for service requests you are involved in");
        }

        // Check if accused is involved in the service request
        if (!accused.getId().equals(serviceRequest.getCustomer().getId()) && 
            (serviceRequest.getTechnician() == null || !accused.getId().equals(serviceRequest.getTechnician().getId()))) {
            throw new BusinessValidationException("You can only file complaints against users involved in the service request");
        }

        // Check if complaint already exists
        if (complaintRepository.existsByServiceRequestAndComplainant(serviceRequest, complainant)) {
            throw ComplaintAlreadyExistsException.forServiceRequest(serviceRequest.getId(), complainant.getId());
        }
    }

    private void validateComplaintAccess(Complaint complaint, User currentUser) {
        Role userRole = currentUser.getRole();
        
        if (userRole == Role.ADMIN) {
            return; // Admin can access all complaints
        }
        
        // Users can only access complaints they are involved in
        if (!currentUser.getId().equals(complaint.getComplainant().getId()) && 
            !currentUser.getId().equals(complaint.getAccused().getId())) {
            throw new SecurityException("Access denied: You can only access complaints you are involved in");
        }
    }

    private void updateServiceRequestToComplaining(ServiceRequest serviceRequest) {
        serviceRequest.setStatus(ServiceRequestStatus.COMPLAINING);
        serviceRequestRepository.save(serviceRequest);
    }

    private void updateServiceRequestToComplaited(ServiceRequest serviceRequest) {
        serviceRequest.setStatus(ServiceRequestStatus.COMPLAITED);
        serviceRequestRepository.save(serviceRequest);
    }

    private Double calculateAverageResolutionTime(List<Complaint> complaints) {
        if (complaints.isEmpty()) {
            return 0.0;
        }

        double totalHours = complaints.stream()
                .filter(c -> c.getCreatedAt() != null && c.getResolvedAt() != null)
                .mapToDouble(c -> ChronoUnit.HOURS.between(c.getCreatedAt(), c.getResolvedAt()))
                .sum();

        return totalHours / complaints.size();
    }

    // ==================== DTO CONVERSION ====================

    @Override
    public ComplaintDTO convertToDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        return ComplaintDTO.builder()
                .id(complaint.getId())
                .serviceRequestId(complaint.getServiceRequest().getId())
                .serviceRequest(convertToServiceRequestSummary(complaint.getServiceRequest()))
                .complainantId(complaint.getComplainant().getId())
                .complainantName(getUserDisplayName(complaint.getComplainant()))
                .complainantPhone(complaint.getComplainant().getPhoneNumber())
                .complainantEmail(complaint.getComplainant().getEmail())
                .accusedId(complaint.getAccused().getId())
                .accusedName(getUserDisplayName(complaint.getAccused()))
                .accusedPhone(complaint.getAccused().getPhoneNumber())
                .accusedEmail(complaint.getAccused().getEmail())
                .reason(complaint.getReason())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .adminResponse(complaint.getAdminResponse())
                .resolvedById(complaint.getResolvedBy() != null ? complaint.getResolvedBy().getId() : null)
                .resolvedByName(complaint.getResolvedBy() != null ? getUserDisplayName(complaint.getResolvedBy()) : null)
                .resolvedByEmail(complaint.getResolvedBy() != null ? complaint.getResolvedBy().getEmail() : null)
                .createdAt(complaint.getCreatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .isPending(complaint.isPending())
                .isInvestigating(complaint.isInvestigating())
                .isResolved(complaint.isResolved())
                .isRejected(complaint.isRejected())
                .canBeModified(complaint.canBeModified())
                .canBeInvestigated(complaint.canBeInvestigated())
                .canBeResolved(complaint.canBeResolved())
                .build();
    }

    private ServiceRequestSummaryDTO convertToServiceRequestSummary(ServiceRequest serviceRequest) {
        if (serviceRequest == null) {
            return null;
        }

        return ServiceRequestSummaryDTO.builder()
                .id(serviceRequest.getId())
                .serviceName(serviceRequest.getService().getName())
                .customerName(getUserDisplayName(serviceRequest.getCustomer()))
                .technicianName(serviceRequest.getTechnician() != null ? getUserDisplayName(serviceRequest.getTechnician()) : null)
                .status(serviceRequest.getStatus())
                .createdAt(serviceRequest.getCreatedAt())
                .price(serviceRequest.getPrice())
                .build();
    }

    private String getUserDisplayName(User user) {
        if (user == null) {
            return null;
        }
        
        // Try to get full name from profile based on user role
        switch (user.getRole()) {
            case CUSTOMER:
                return customerProfileRepository.findByUser(user)
                        .map(CustomerProfile::getFullName)
                        .orElse(user.getUsername());
            case TECHNICIAN:
                return technicianProfileRepository.findByUser(user)
                        .map(TechnicianProfile::getFullName)
                        .orElse(user.getUsername());
            case ADMIN:
            default:
                return user.getUsername();
        }
    }
} 