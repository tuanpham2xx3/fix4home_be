package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.model.dto.feedback.*;
import com.fix4home.fix4home.model.entity.Feedback;
import com.fix4home.fix4home.model.entity.ServiceRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.ServiceRequestStatus;
import com.fix4home.fix4home.repository.FeedbackRepository;
import com.fix4home.fix4home.repository.ServiceRequestRepository;
import com.fix4home.fix4home.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final UserRepository userRepository;

    // ========== CUSTOMER OPERATIONS ==========

    @Transactional
    public FeedbackDTO createFeedback(CreateFeedbackRequest request, Long customerId) {
        log.info("Creating feedback for service request ID: {} by customer ID: {}", request.getServiceRequestId(), customerId);

        ServiceRequest serviceRequest = serviceRequestRepository.findById(request.getServiceRequestId())
                .orElseThrow(() -> new BadRequestException("Service request not found"));

        // Validate customer ownership
        if (!serviceRequest.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only provide feedback for your own service requests");
        }

        // Validate service request status
        if (serviceRequest.getStatus() != ServiceRequestStatus.DONE) {
            throw new BadRequestException("Feedback can only be provided for completed service requests");
        }

        // Check if feedback already exists
        if (feedbackRepository.existsByServiceRequestId(request.getServiceRequestId())) {
            throw new BadRequestException("Feedback already exists for this service request");
        }

        User customer = serviceRequest.getCustomer();
        User technician = serviceRequest.getTechnician();

        if (technician == null) {
            throw new BadRequestException("Cannot provide feedback for service request without assigned technician");
        }

        Feedback feedback = Feedback.builder()
                .serviceRequest(serviceRequest)
                .customer(customer)
                .technician(technician)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback created successfully with ID: {}", savedFeedback.getId());

        return convertToDTO(savedFeedback);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getCustomerFeedbacks(Long customerId, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching feedbacks for customer ID: {} - page: {}, size: {}", customerId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Feedback> feedbackPage = feedbackRepository.findByCustomerId(customerId, pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public FeedbackDTO getFeedbackDetails(Long feedbackId, Long customerId) {
        log.info("Customer {} requesting feedback details for ID: {}", customerId, feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BadRequestException("Feedback not found"));

        if (!feedback.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("You can only view your own feedback");
        }

        return convertToDTO(feedback);
    }

    // ========== TECHNICIAN OPERATIONS ==========

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getTechnicianFeedbacks(Long technicianId, int page, int size, String sortBy, String sortDir) {
        log.info("Fetching feedbacks for technician ID: {} - page: {}, size: {}", technicianId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Feedback> feedbackPage = feedbackRepository.findByTechnicianId(technicianId, pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getTechnicianUnrepliedFeedbacks(Long technicianId, int page, int size) {
        log.info("Fetching unreplied feedbacks for technician ID: {} - page: {}, size: {}", technicianId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Feedback> feedbackPage = feedbackRepository.findByTechnicianIdAndReplyIsNull(technicianId, pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional
    public FeedbackDTO replyToFeedback(Long feedbackId, ReplyFeedbackRequest request, Long technicianId) {
        log.info("Technician {} replying to feedback ID: {}", technicianId, feedbackId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new BadRequestException("Feedback not found"));

        if (!feedback.getTechnician().getId().equals(technicianId)) {
            throw new BadRequestException("You can only reply to feedback for your own services");
        }

        if (feedback.getReply() != null) {
            throw new BadRequestException("Feedback has already been replied to");
        }

        feedback.setReply(request.getReply());
        Feedback savedFeedback = feedbackRepository.save(feedback);

        log.info("Feedback reply added successfully for feedback ID: {}", feedbackId);
        return convertToDTO(savedFeedback);
    }

    @Transactional(readOnly = true)
    public FeedbackStatsDTO getTechnicianFeedbackStats(Long technicianId) {
        log.info("Generating feedback statistics for technician ID: {}", technicianId);

        long totalFeedbacks = feedbackRepository.countByTechnicianId(technicianId);
        
        if (totalFeedbacks == 0) {
            return FeedbackStatsDTO.builder()
                    .totalFeedbacks(0)
                    .averageRating(0.0)
                    .build();
        }

        Double averageRating = feedbackRepository.calculateAverageRatingByTechnicianId(technicianId);
        
        // Rating breakdown
        long fiveStarCount = feedbackRepository.countByTechnicianIdAndRating(technicianId, 5);
        long fourStarCount = feedbackRepository.countByTechnicianIdAndRating(technicianId, 4);
        long threeStarCount = feedbackRepository.countByTechnicianIdAndRating(technicianId, 3);
        long twoStarCount = feedbackRepository.countByTechnicianIdAndRating(technicianId, 2);
        long oneStarCount = feedbackRepository.countByTechnicianIdAndRating(technicianId, 1);

        // Calculate reply statistics
        Page<Feedback> unrepliedPage = feedbackRepository.findByTechnicianIdAndReplyIsNull(technicianId, PageRequest.of(0, 1));
        long unrepliedCount = unrepliedPage.getTotalElements();
        long repliedCount = totalFeedbacks - unrepliedCount;

        return FeedbackStatsDTO.builder()
                .totalFeedbacks(totalFeedbacks)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .repliedFeedbacks(repliedCount)
                .unrepliedFeedbacks(unrepliedCount)
                .fiveStarCount(fiveStarCount)
                .fourStarCount(fourStarCount)
                .threeStarCount(threeStarCount)
                .twoStarCount(twoStarCount)
                .oneStarCount(oneStarCount)
                .fiveStarPercentage(totalFeedbacks > 0 ? (double) fiveStarCount / totalFeedbacks * 100 : 0)
                .fourStarPercentage(totalFeedbacks > 0 ? (double) fourStarCount / totalFeedbacks * 100 : 0)
                .threeStarPercentage(totalFeedbacks > 0 ? (double) threeStarCount / totalFeedbacks * 100 : 0)
                .twoStarPercentage(totalFeedbacks > 0 ? (double) twoStarCount / totalFeedbacks * 100 : 0)
                .oneStarPercentage(totalFeedbacks > 0 ? (double) oneStarCount / totalFeedbacks * 100 : 0)
                .replyRate(totalFeedbacks > 0 ? (double) repliedCount / totalFeedbacks * 100 : 0)
                .satisfactionRate(totalFeedbacks > 0 ? (double) (fiveStarCount + fourStarCount) / totalFeedbacks * 100 : 0)
                .excellentRate(totalFeedbacks > 0 ? (double) fiveStarCount / totalFeedbacks * 100 : 0)
                .build();
    }

    // ========== ADMIN OPERATIONS ==========

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getAllFeedbacks(int page, int size, String sortBy, String sortDir) {
        log.info("Admin fetching all feedbacks - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Feedback> feedbackPage = feedbackRepository.findAll(pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getFeedbacksByRating(Integer rating, int page, int size) {
        log.info("Admin fetching feedbacks by rating: {} - page: {}, size: {}", rating, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Feedback> feedbackPage = feedbackRepository.findByRating(rating, pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getUnrepliedFeedbacks(int page, int size) {
        log.info("Admin fetching unreplied feedbacks - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Feedback> feedbackPage = feedbackRepository.findByReplyIsNull(pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public FeedbackStatsDTO getSystemFeedbackStats() {
        log.info("Generating system-wide feedback statistics");

        long totalFeedbacks = feedbackRepository.count();
        
        if (totalFeedbacks == 0) {
            return FeedbackStatsDTO.builder()
                    .totalFeedbacks(0)
                    .averageRating(0.0)
                    .build();
        }

        // Calculate overall average rating
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        double averageRating = allFeedbacks.stream()
                .mapToInt(Feedback::getRating)
                .average()
                .orElse(0.0);

        // Rating breakdown
        long fiveStarCount = allFeedbacks.stream().mapToLong(f -> f.getRating() == 5 ? 1 : 0).sum();
        long fourStarCount = allFeedbacks.stream().mapToLong(f -> f.getRating() == 4 ? 1 : 0).sum();
        long threeStarCount = allFeedbacks.stream().mapToLong(f -> f.getRating() == 3 ? 1 : 0).sum();
        long twoStarCount = allFeedbacks.stream().mapToLong(f -> f.getRating() == 2 ? 1 : 0).sum();
        long oneStarCount = allFeedbacks.stream().mapToLong(f -> f.getRating() == 1 ? 1 : 0).sum();

        // Reply statistics
        Page<Feedback> unrepliedPage = feedbackRepository.findByReplyIsNull(PageRequest.of(0, 1));
        long unrepliedCount = unrepliedPage.getTotalElements();
        long repliedCount = totalFeedbacks - unrepliedCount;

        return FeedbackStatsDTO.builder()
                .totalFeedbacks(totalFeedbacks)
                .averageRating(averageRating)
                .repliedFeedbacks(repliedCount)
                .unrepliedFeedbacks(unrepliedCount)
                .fiveStarCount(fiveStarCount)
                .fourStarCount(fourStarCount)
                .threeStarCount(threeStarCount)
                .twoStarCount(twoStarCount)
                .oneStarCount(oneStarCount)
                .fiveStarPercentage((double) fiveStarCount / totalFeedbacks * 100)
                .fourStarPercentage((double) fourStarCount / totalFeedbacks * 100)
                .threeStarPercentage((double) threeStarCount / totalFeedbacks * 100)
                .twoStarPercentage((double) twoStarCount / totalFeedbacks * 100)
                .oneStarPercentage((double) oneStarCount / totalFeedbacks * 100)
                .replyRate((double) repliedCount / totalFeedbacks * 100)
                .satisfactionRate((double) (fiveStarCount + fourStarCount) / totalFeedbacks * 100)
                .excellentRate((double) fiveStarCount / totalFeedbacks * 100)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> searchFeedbacks(String keyword, int page, int size) {
        log.info("Admin searching feedbacks with keyword: {} - page: {}, size: {}", keyword, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Feedback> feedbackPage = feedbackRepository.searchByKeyword(keyword, pageable);
        
        return feedbackPage.map(this::convertToDTO);
    }

    // ========== PUBLIC OPERATIONS ==========

    @Transactional(readOnly = true)
    public Page<FeedbackDTO> getPublicFeedbacks(int page, int size, Integer minRating) {
        log.info("Fetching public feedbacks - page: {}, size: {}, minRating: {}", page, size, minRating);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Feedback> feedbackPage;
        
        if (minRating != null) {
            feedbackPage = feedbackRepository.findByRatingGreaterThanEqual(minRating, pageable);
        } else {
            feedbackPage = feedbackRepository.findAll(pageable);
        }
        
        return feedbackPage.map(this::convertToPublicDTO);
    }

    // ========== HELPER METHODS ==========

    private FeedbackDTO convertToDTO(Feedback feedback) {
        String timeAgo = calculateTimeAgo(feedback.getCreatedAt());
        
        FeedbackDTO dto = FeedbackDTO.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .reply(feedback.getReply())
                .createdAt(feedback.getCreatedAt())
                .serviceRequestId(feedback.getServiceRequest().getId())
                .serviceRequestDescription(feedback.getServiceRequest().getDescription())
                .serviceName(feedback.getServiceRequest().getService().getName())
                .customerId(feedback.getCustomer().getId())
                .customerName(feedback.getCustomer().getUsername())
                .customerEmail(feedback.getCustomer().getEmail())
                .technicianId(feedback.getTechnician().getId())
                .technicianName(feedback.getTechnician().getUsername())
                .technicianEmail(feedback.getTechnician().getEmail())
                .timeAgo(timeAgo)
                .ratingDisplay(getRatingDisplay(feedback.getRating()))
                .ratingStars(getRatingStars(feedback.getRating()))
                .ratingText(getRatingText(feedback.getRating()))
                .hasReply(feedback.getReply() != null)
                .canReply(feedback.getReply() == null)
                .canEdit(false) // Feedback cannot be edited after creation
                .serviceCompletedAt(feedback.getServiceRequest().getCompletedTime())
                .serviceStatus(feedback.getServiceRequest().getStatus().toString())
                .build();

        return dto;
    }

    private FeedbackDTO convertToPublicDTO(Feedback feedback) {
        // Public version with limited customer information
        String timeAgo = calculateTimeAgo(feedback.getCreatedAt());
        
        return FeedbackDTO.builder()
                .id(feedback.getId())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .reply(feedback.getReply())
                .createdAt(feedback.getCreatedAt())
                .serviceName(feedback.getServiceRequest().getService().getName())
                .customerName(maskCustomerName(feedback.getCustomer().getUsername()))
                .technicianName(feedback.getTechnician().getUsername())
                .timeAgo(timeAgo)
                .ratingDisplay(getRatingDisplay(feedback.getRating()))
                .ratingStars(getRatingStars(feedback.getRating()))
                .ratingText(getRatingText(feedback.getRating()))
                .hasReply(feedback.getReply() != null)
                .build();
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        
        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        
        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }

    private String getRatingDisplay(Integer rating) {
        return rating + "/5 sao";
    }

    private String getRatingStars(Integer rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    private String getRatingText(Integer rating) {
        switch (rating) {
            case 5: return "Xuất sắc";
            case 4: return "Tốt";
            case 3: return "Bình thường";
            case 2: return "Kém";
            case 1: return "Rất kém";
            default: return "Không xác định";
        }
    }

    private String maskCustomerName(String name) {
        if (name == null || name.length() <= 2) {
            return "***";
        }
        return name.substring(0, 1) + "***" + name.substring(name.length() - 1);
    }
} 