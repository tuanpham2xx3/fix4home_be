package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.consultation.*;
import com.fix4home.fix4home.model.dto.servicepost.ServicePostSummaryDTO;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.ConsultationStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.fix4home.fix4home.service.ServiceValidationUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService extends BaseService {

    private final ConsultationRepository consultationRepository;
    private final ServicePostRepository servicePostRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;

    // ==================== TECHNICIAN OPERATIONS ====================

    @Transactional
    public ConsultationDTO submitConsultation(CreateConsultationRequest request) {
        logBusinessOperation("SUBMIT_CONSULTATION", "servicePostId=" + request.getServicePostId());

        validateRequired(request, "request");
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        // Validate service post exists and is available for consultation
        ServicePost servicePost = findServicePostById(request.getServicePostId());
        validateServicePostAvailableForConsultation(servicePost);

        // Validate technician profile exists and is active
        TechnicianProfile technicianProfile = findTechnicianProfileByUser(technician);
        validateTechnicianActive(technicianProfile);

        // Check if technician already submitted consultation for this post
        Optional<Consultation> existingConsultation = consultationRepository
                .findByServicePostAndTechnician(servicePost, technician);
        
        if (existingConsultation.isPresent()) {
            throw new ResourceAlreadyExistsException("You have already submitted a consultation for this service post");
        }

        // Create consultation
        Consultation consultation = Consultation.builder()
                .servicePost(servicePost)
                .technician(technician)
                .proposal(request.getProposal())
                .quotedPrice(request.getQuotedPrice())
                .notes(request.getNotes())
                .status(ConsultationStatus.PENDING)
                .build();

        Consultation savedConsultation = consultationRepository.save(consultation);
        log.info("Consultation submitted successfully - ID: {}, ServicePost: {}, Technician: {}", 
                savedConsultation.getId(), servicePost.getId(), technician.getId());

        return convertToDTO(savedConsultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getMyConsultations() {
        logBusinessOperation("GET_MY_CONSULTATIONS");

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        List<Consultation> consultations = consultationRepository
                .findByTechnicianOrderBySubmittedAtDesc(technician);
        
        return consultations.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getMyConsultationsWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_MY_CONSULTATIONS_PAGINATED");

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Consultation> consultationPage = consultationRepository
                .findByTechnicianOrderBySubmittedAtDesc(technician, pageable);
        
        return consultationPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getMyConsultationsByStatus(ConsultationStatus status) {
        logBusinessOperation("GET_MY_CONSULTATIONS_BY_STATUS", "status=" + status);

        validateRequired(status, "status");
        User technician = getCurrentUser();
        requireRole(Role.TECHNICIAN);

        List<Consultation> consultations = consultationRepository
                .findByTechnicianAndStatusOrderBySubmittedAtDesc(technician, status);
        
        return consultations.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // ==================== CUSTOMER OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getConsultationsForServicePost(Long servicePostId) {
        logBusinessOperation("GET_CONSULTATIONS_FOR_SERVICE_POST", "servicePostId=" + servicePostId);

        validatePositiveId(servicePostId, "servicePostId");
        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost servicePost = findServicePostById(servicePostId);
        validateServicePostOwnership(servicePost, customer);

        List<Consultation> consultations = consultationRepository
                .findByServicePostOrderBySubmittedAtDesc(servicePost);
        
        return consultations.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getConsultationsForServicePostWithPagination(Long servicePostId, 
                                                                              int page, int size, 
                                                                              String sortBy, String sortDir) {
        logBusinessOperation("GET_CONSULTATIONS_FOR_SERVICE_POST_PAGINATED", "servicePostId=" + servicePostId);

        validatePositiveId(servicePostId, "servicePostId");
        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        ServicePost servicePost = findServicePostById(servicePostId);
        validateServicePostOwnership(servicePost, customer);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Consultation> consultationPage = consultationRepository
                .findByServicePostOrderBySubmittedAtDesc(servicePost, pageable);
        
        return consultationPage.map(this::convertToDTO);
    }

    @Transactional
    public ConsultationDTO updateConsultationStatus(Long consultationId, UpdateConsultationStatusRequest request) {
        logBusinessOperation("UPDATE_CONSULTATION_STATUS", "consultationId=" + consultationId);

        validatePositiveId(consultationId, "consultationId");
        validateRequired(request, "request");
        validateRequired(request.getStatus(), "status");

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        Consultation consultation = findConsultationById(consultationId);
        validateConsultationOwnership(consultation, customer);
        validateConsultationCanBeUpdated(consultation);

        // Only allow ACCEPTED or REJECTED status
        if (request.getStatus() != ConsultationStatus.ACCEPTED && 
            request.getStatus() != ConsultationStatus.REJECTED) {
            throw new BusinessValidationException("Invalid status. Only ACCEPTED or REJECTED are allowed");
        }

        // Update consultation status
        if (request.getStatus() == ConsultationStatus.ACCEPTED) {
            consultation.accept();
            log.info("Consultation accepted - ID: {}, Customer: {}", consultationId, customer.getId());
        } else {
            consultation.reject();
            log.info("Consultation rejected - ID: {}, Customer: {}, Reason: {}", 
                    consultationId, customer.getId(), request.getRejectionReason());
        }

        Consultation updatedConsultation = consultationRepository.save(consultation);
        return convertToDTO(updatedConsultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getMyConsultationsAsCustomer() {
        logBusinessOperation("GET_MY_CONSULTATIONS_AS_CUSTOMER");

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        List<Consultation> consultations = consultationRepository
                .findConsultationsByCustomer(customer);
        
        return consultations.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getMyConsultationsAsCustomerWithPagination(int page, int size, 
                                                                           String sortBy, String sortDir) {
        logBusinessOperation("GET_MY_CONSULTATIONS_AS_CUSTOMER_PAGINATED");

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User customer = getCurrentUser();
        requireRole(Role.CUSTOMER);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Consultation> consultationPage = consultationRepository
                .findConsultationsByCustomer(customer, pageable);
        
        return consultationPage.map(this::convertToDTO);
    }

    // ==================== SHARED OPERATIONS ====================

    @Transactional(readOnly = true)
    public ConsultationDTO getConsultationById(Long id) {
        logBusinessOperation("GET_CONSULTATION_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        User currentUser = getCurrentUser();

        Consultation consultation = findConsultationById(id);
        
        // Check access permissions
        boolean isOwner = consultation.getTechnician().getId().equals(currentUser.getId()) ||
                         consultation.getServicePost().getCustomer().getId().equals(currentUser.getId());
        
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        
        if (!isOwner && !isAdmin) {
            throw new BusinessValidationException("You don't have permission to view this consultation");
        }

        return convertToDTO(consultation);
    }

    // ==================== ADMIN OPERATIONS ====================

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getAllConsultations() {
        logBusinessOperation("GET_ALL_CONSULTATIONS");

        User admin = getCurrentUser();
        requireRole(Role.ADMIN);

        List<Consultation> consultations = consultationRepository
                .findAll(Sort.by(Sort.Direction.DESC, "submittedAt"));
        
        return consultations.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ConsultationDTO> getAllConsultationsWithPagination(int page, int size, 
                                                                  String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_CONSULTATIONS_PAGINATED");

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);

        User admin = getCurrentUser();
        requireRole(Role.ADMIN);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Consultation> consultationPage = consultationRepository.findAll(pageable);
        
        return consultationPage.map(this::convertToDTO);
    }

    // ==================== VALIDATION METHODS ====================

    private ServicePost findServicePostById(Long id) {
        return servicePostRepository.findById(id)
                .orElseThrow(() -> new ServicePostNotFoundException("Service post not found with id: " + id));
    }

    private Consultation findConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new BusinessValidationException("Consultation not found with id: " + id));
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianProfileNotFoundException(user.getId()));
    }

    private void validateServicePostAvailableForConsultation(ServicePost servicePost) {
        if (servicePost.getStatus() != ServicePostStatus.POSTED && 
            servicePost.getStatus() != ServicePostStatus.RESPONSES_RECEIVED) {
            throw new ServicePostNotAvailableException("Service post is not available for consultation");
        }

        if (servicePost.isExpired()) {
            throw new ServicePostExpiredException("Service post has expired");
        }
    }

    private void validateServicePostOwnership(ServicePost servicePost, User customer) {
        if (!servicePost.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessValidationException("You don't have permission to access this service post");
        }
    }

    private void validateConsultationOwnership(Consultation consultation, User customer) {
        if (!consultation.getServicePost().getCustomer().getId().equals(customer.getId())) {
            throw new BusinessValidationException("You don't have permission to access this consultation");
        }
    }

    private void validateConsultationCanBeUpdated(Consultation consultation) {
        if (!consultation.canBeModified()) {
            throw new BusinessValidationException("Consultation status cannot be changed");
        }
    }

    private void validateTechnicianActive(TechnicianProfile profile) {
        if (profile.getUser().getStatus() != com.fix4home.fix4home.model.enums.UserStatus.ACTIVE) {
            throw new TechnicianNotAvailableException("Technician account is not active");
        }
    }

    // ==================== DTO CONVERSION ====================

    private ConsultationDTO convertToDTO(Consultation consultation) {
        ServicePostSummaryDTO servicePostSummary = convertToServicePostSummaryDTO(consultation.getServicePost());
        
        // Get technician details
        TechnicianProfile technicianProfile = null;
        String technicianName = null;
        Double technicianRating = null;
        
        try {
            technicianProfile = findTechnicianProfileByUser(consultation.getTechnician());
            technicianName = technicianProfile.getFullName();
            technicianRating = technicianProfile.getRating() != null ? technicianProfile.getRating().doubleValue() : null;
        } catch (Exception e) {
            log.warn("Could not get technician profile for user {}: {}", consultation.getTechnician().getId(), e.getMessage());
        }
        
        return ConsultationDTO.builder()
                .id(consultation.getId())
                .servicePostId(consultation.getServicePost().getId())
                .servicePost(servicePostSummary)
                .technicianId(consultation.getTechnician().getId())
                .technicianName(technicianName)
                .technicianPhone(consultation.getTechnician().getPhoneNumber())
                .technicianEmail(consultation.getTechnician().getEmail())
                .technicianRating(technicianRating)
                .proposal(consultation.getProposal())
                .quotedPrice(consultation.getQuotedPrice())
                .notes(consultation.getNotes())
                .status(consultation.getStatus())
                .submittedAt(consultation.getSubmittedAt())
                .respondedAt(consultation.getRespondedAt())
                .isPending(consultation.isPending())
                .isAccepted(consultation.isAccepted())
                .isRejected(consultation.isRejected())
                .canBeModified(consultation.canBeModified())
                .build();
    }

    private ServicePostSummaryDTO convertToServicePostSummaryDTO(ServicePost servicePost) {
        // Get customer name
        String customerName = null;
        try {
            CustomerProfile customerProfile = customerProfileRepository.findByUser(servicePost.getCustomer()).orElse(null);
            if (customerProfile != null) {
                customerName = customerProfile.getFullName();
            }
        } catch (Exception e) {
            log.warn("Could not get customer profile for user {}: {}", servicePost.getCustomer().getId(), e.getMessage());
        }

        return ServicePostSummaryDTO.builder()
                .id(servicePost.getId())
                .title(servicePost.getTitle())
                .serviceName(servicePost.getService().getName())
                .address(servicePost.getAddress().getAddressLine())
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
} 