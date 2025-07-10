package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.technician.*;
import com.fix4home.fix4home.model.entity.*;
import com.fix4home.fix4home.model.enums.Role;
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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService extends BaseService {

    private final UserRepository userRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final SkillRepository skillRepository;
    private final TechnicianSkillRepository technicianSkillRepository;

    // ==================== TECHNICIAN PROFILE MANAGEMENT ====================

    @Transactional(readOnly = true)
    public TechnicianProfileDTO getTechnicianProfile(Long userId) {
        logBusinessOperation("GET_TECHNICIAN_PROFILE", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        return convertToTechnicianProfileDTO(user, profile);
    }

    @Transactional(readOnly = true)
    public TechnicianProfileDTO getMyProfile() {
        logBusinessOperation("GET_MY_PROFILE");
        requireRole(Role.TECHNICIAN);

        User user = getCurrentUser();
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        return convertToTechnicianProfileDTO(user, profile);
    }

    @Transactional
    public TechnicianProfileDTO updateTechnicianProfile(Long userId, UpdateTechnicianProfileRequest request) {
        logBusinessOperation("UPDATE_TECHNICIAN_PROFILE", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");

        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        // Update User fields
        if (StringUtils.hasText(request.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Update Profile fields
        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        
        if (StringUtils.hasText(request.getSkills())) {
            profile.setSkills(request.getSkills());
        }
        
        if (StringUtils.hasText(request.getExperience())) {
            profile.setExperience(request.getExperience());
        }
        
        if (request.getRating() != null) {
            profile.setRating(request.getRating());
        }

        User savedUser = userRepository.save(user);
        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        return convertToTechnicianProfileDTO(savedUser, savedProfile);
    }

    @Transactional
    public TechnicianProfileDTO updateMyProfile(UpdateTechnicianProfileRequest request) {
        logBusinessOperation("UPDATE_MY_PROFILE");
        requireRole(Role.TECHNICIAN);

        validateRequired(request, "request");
        User user = getCurrentUser();

        return updateTechnicianProfile(user.getId(), request);
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getAllTechnicians() {
        logBusinessOperation("GET_ALL_TECHNICIANS");
        requireRole(Role.ADMIN);
        
        List<User> technicians = userRepository.findByRole(Role.TECHNICIAN);
        return technicians.stream()
                .map(user -> {
                    TechnicianProfile profile = technicianProfileRepository.findByUser(user).orElse(null);
                    return convertToTechnicianProfileDTO(user, profile);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getActiveTechnicians() {
        logBusinessOperation("GET_ACTIVE_TECHNICIANS");
        
        List<TechnicianProfile> activeProfiles = technicianProfileRepository.findByStatus(UserStatus.ACTIVE);
        return activeProfiles.stream()
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getPendingTechnicians() {
        logBusinessOperation("GET_PENDING_TECHNICIANS");
        requireRole(Role.ADMIN);
        
        List<TechnicianProfile> pendingProfiles = technicianProfileRepository.findByStatus(UserStatus.PENDING_APPROVAL);
        return pendingProfiles.stream()
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TechnicianProfileDTO> getAllTechniciansWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_TECHNICIANS_PAGINATED");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> technicianPage = userRepository.findByRole(Role.TECHNICIAN, pageable);
        
        return technicianPage.map(user -> {
            TechnicianProfile profile = technicianProfileRepository.findByUser(user).orElse(null);
            return convertToTechnicianProfileDTO(user, profile);
        });
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> searchTechnicians(String keyword) {
        logBusinessOperation("SEARCH_TECHNICIANS", "keyword=" + keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getActiveTechnicians();
        }
        
        List<TechnicianProfile> technicians = technicianProfileRepository.findByFullNameContainingIgnoreCase(keyword);
        return technicians.stream()
                .filter(profile -> profile.getStatus() == UserStatus.ACTIVE)
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getTechniciansByRating(Float minRating) {
        logBusinessOperation("GET_TECHNICIANS_BY_RATING", "minRating=" + minRating);
        
        validateRequired(minRating, "minRating");
        
        List<TechnicianProfile> technicians = technicianProfileRepository.findByRatingGreaterThanEqualOrderByRatingDesc(minRating);
        return technicians.stream()
                .filter(profile -> profile.getStatus() == UserStatus.ACTIVE)
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    // ==================== ONLINE/OFFLINE STATUS AND LOCATION MANAGEMENT ====================

    @Transactional
    public TechnicianProfileDTO updateMyStatus(UpdateStatusRequest request) {
        logBusinessOperation("UPDATE_MY_STATUS", "isOnline=" + request.getIsOnline());
        requireRole(Role.TECHNICIAN);

        validateRequired(request, "request");
        validateRequired(request.getIsOnline(), "isOnline");

        User user = getCurrentUser();
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        profile.setIsOnline(request.getIsOnline());
        profile.setLastSeenAt(java.time.LocalDateTime.now());

        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    @Transactional
    public TechnicianProfileDTO updateMyLocation(UpdateLocationRequest request) {
        logBusinessOperation("UPDATE_MY_LOCATION");
        requireRole(Role.TECHNICIAN);

        validateRequired(request, "request");

        User user = getCurrentUser();
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        if (request.getLatitude() != null) {
            profile.setCurrentLatitude(request.getLatitude());
        }
        
        if (request.getLongitude() != null) {
            profile.setCurrentLongitude(request.getLongitude());
        }
        
        if (StringUtils.hasText(request.getAddress())) {
            profile.setCurrentAddress(request.getAddress());
        }
        
        if (request.getWorkingRadius() != null) {
            profile.setWorkingRadius(request.getWorkingRadius());
        }

        profile.setLastSeenAt(java.time.LocalDateTime.now());

        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);
        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    @Transactional(readOnly = true)
    public List<NearbyTechnicianDTO> findNearbyTechnicians(Double latitude, Double longitude, 
                                                           Integer radiusKm, Boolean onlineOnly, Long serviceId) {
        logBusinessOperation("FIND_NEARBY_TECHNICIANS", 
                "lat=" + latitude + ", lng=" + longitude + ", radius=" + radiusKm + "km");

        validateRequired(latitude, "latitude");
        validateRequired(longitude, "longitude");
        validateRequired(radiusKm, "radiusKm");

        // Get all active technicians with location data
        List<TechnicianProfile> candidates = technicianProfileRepository.findByStatus(UserStatus.ACTIVE).stream()
                .filter(profile -> profile.getCurrentLatitude() != null && profile.getCurrentLongitude() != null)
                .filter(profile -> !onlineOnly || Boolean.TRUE.equals(profile.getIsOnline()))
                .toList();

        // Calculate distances and filter by radius
        return candidates.stream()
                .map(profile -> {
                    double distance = calculateDistance(latitude, longitude, 
                            profile.getCurrentLatitude(), profile.getCurrentLongitude());
                    
                    if (distance <= radiusKm) {
                        return convertToNearbyTechnicianDTO(profile, distance, serviceId);
                    }
                    return null;
                })
                .filter(dto -> dto != null)
                .sorted((a, b) -> Double.compare(a.getDistanceKm(), b.getDistanceKm()))
                .toList();
    }

    // ==================== TECHNICIAN APPROVAL WORKFLOW ====================

    @Transactional
    public TechnicianProfileDTO approveTechnician(Long userId) {
        logBusinessOperation("APPROVE_TECHNICIAN", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        if (profile.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new BusinessValidationException("Technician is not pending approval. Current status: " + profile.getStatus());
        }

        profile.setStatus(UserStatus.ACTIVE);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(getCurrentUser().getId());
        profile.setRejectionReason(null); // Clear any previous rejection reason
        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    @Transactional
    public TechnicianProfileDTO rejectTechnician(Long userId, String rejectionReason) {
        logBusinessOperation("REJECT_TECHNICIAN", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(rejectionReason, "rejectionReason");
        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        if (profile.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new BusinessValidationException("Technician is not pending approval. Current status: " + profile.getStatus());
        }

        profile.setStatus(UserStatus.REJECTED);
        profile.setRejectionReason(rejectionReason);
        profile.setApprovedAt(LocalDateTime.now());
        profile.setApprovedBy(getCurrentUser().getId());
        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    // ==================== SKILLS MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<SkillDTO> getAllSkills() {
        logBusinessOperation("GET_ALL_SKILLS");
        requireRole(Role.ADMIN);
        
        List<Skill> skills = skillRepository.findAllOrderByName();
        return skills.stream()
                .map(this::convertToSkillDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> searchSkills(String keyword) {
        logBusinessOperation("SEARCH_SKILLS", "keyword=" + keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getAllSkills();
        }
        
        List<Skill> skills = skillRepository.findByNameContainingIgnoreCase(keyword);
        return skills.stream()
                .map(this::convertToSkillDTO)
                .toList();
    }

    @Transactional
    public SkillDTO createSkill(CreateSkillRequest request) {
        logBusinessOperation("CREATE_SKILL", "name=" + request.getName());
        requireRole(Role.ADMIN);

        validateRequired(request, "request");
        validateRequired(request.getName(), "name");

        if (skillRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Skill with name '" + request.getName() + "' already exists");
        }

        Skill skill = Skill.builder()
                .name(request.getName())
                .build();

        Skill savedSkill = skillRepository.save(skill);
        return convertToSkillDTO(savedSkill);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        logBusinessOperation("DELETE_SKILL", "skillId=" + skillId);
        requireRole(Role.ADMIN);

        validatePositiveId(skillId, "skillId");
        if (!skillRepository.existsById(skillId)) {
            throw new SkillNotFoundException(skillId);
        }

        // Remove all technician-skill associations first
        technicianSkillRepository.deleteBySkillId(skillId);
        skillRepository.deleteById(skillId);
    }

    // ==================== TECHNICIAN SKILLS MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<SkillDTO> getTechnicianSkills(Long userId) {
        logBusinessOperation("GET_TECHNICIAN_SKILLS", "userId=" + userId);

        validatePositiveId(userId, "userId");
        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        List<TechnicianSkill> technicianSkills = technicianSkillRepository.findByTechnicianProfile(profile);
        return technicianSkills.stream()
                .map(ts -> convertToSkillDTO(ts.getSkill()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getMySkills() {
        logBusinessOperation("GET_MY_SKILLS");
        requireRole(Role.TECHNICIAN);

        User user = getCurrentUser();
        return getTechnicianSkills(user.getId());
    }

    @Transactional
    public List<SkillDTO> assignSkillsToTechnician(Long userId, AssignSkillsRequest request) {
        logBusinessOperation("ASSIGN_SKILLS_TO_TECHNICIAN", "userId=" + userId);
        requireRole(Role.ADMIN);

        validatePositiveId(userId, "userId");
        validateRequired(request, "request");
        validateRequired(request.getSkillIds(), "skillIds");

        User user = findTechnicianById(userId);
        TechnicianProfile profile = findTechnicianProfileByUser(user);

        // Remove existing skills
        technicianSkillRepository.deleteByTechnicianProfileId(profile.getId());

        // Add new skills
        for (Long skillId : request.getSkillIds()) {
            validatePositiveId(skillId, "skillId");
            Skill skill = findSkillById(skillId);

            TechnicianSkill technicianSkill = TechnicianSkill.builder()
                    .technicianProfile(profile)
                    .skill(skill)
                    .build();

            technicianSkillRepository.save(technicianSkill);
        }

        return getTechnicianSkills(userId);
    }

    @Transactional
    public List<SkillDTO> assignSkillsToMyself(AssignSkillsRequest request) {
        logBusinessOperation("ASSIGN_SKILLS_TO_MYSELF");
        requireRole(Role.TECHNICIAN);

        validateRequired(request, "request");
        validateRequired(request.getSkillIds(), "skillIds");

        User user = getCurrentUser();
        return assignSkillsToTechnician(user.getId(), request);
    }

    // ==================== HELPER METHODS ====================

    private User findTechnicianById(Long userId) {
        User user = findUserById(userId);
        if (user.getRole() != Role.TECHNICIAN) {
            throw new BusinessValidationException("User is not a technician");
        }
        return user;
    }

    private TechnicianProfile findTechnicianProfileByUser(User user) {
        return technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new TechnicianProfileNotFoundException(user.getId()));
    }

    private Skill findSkillById(Long skillId) {
        return skillRepository.findById(skillId)
                .orElseThrow(() -> new SkillNotFoundException(skillId));
    }

    private TechnicianProfileDTO convertToTechnicianProfileDTO(User user, TechnicianProfile profile) {
        TechnicianProfileDTO.TechnicianProfileDTOBuilder builder = TechnicianProfileDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userStatus(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt());

        if (profile != null) {
            builder.profileId(profile.getId())
                   .fullName(profile.getFullName())
                   .skills(profile.getSkills())
                   .experience(profile.getExperience())
                   .rating(profile.getRating())
                   .status(profile.getStatus())
                   .isOnline(profile.getIsOnline())
                   .lastSeenAt(profile.getLastSeenAt())
                   .currentLatitude(profile.getCurrentLatitude())
                   .currentLongitude(profile.getCurrentLongitude())
                   .currentAddress(profile.getCurrentAddress())
                   .workingRadius(profile.getWorkingRadius())
                   .verificationDocuments(profile.getVerificationDocuments())
                   .rejectionReason(profile.getRejectionReason())
                   .approvedAt(profile.getApprovedAt())
                   .approvedBy(profile.getApprovedBy());

            // Get approved by username if available
            if (profile.getApprovedBy() != null) {
                User approver = userRepository.findById(profile.getApprovedBy()).orElse(null);
                if (approver != null) {
                    builder.approvedByUsername(approver.getUsername());
                }
            }

            // Get skills list
            List<TechnicianSkill> technicianSkills = technicianSkillRepository.findByTechnicianProfile(profile);
            List<SkillDTO> skillList = technicianSkills.stream()
                    .map(ts -> convertToSkillDTO(ts.getSkill()))
                    .toList();
            builder.skillList(skillList);
        }

        return builder.build();
    }

    private SkillDTO convertToSkillDTO(Skill skill) {
        return SkillDTO.builder()
                .id(skill.getId())
                .name(skill.getName())
                .build();
    }

    private NearbyTechnicianDTO convertToNearbyTechnicianDTO(TechnicianProfile profile, double distance, Long serviceId) {
        // Get technician skills
        List<TechnicianSkill> technicianSkills = technicianSkillRepository.findByTechnicianProfile(profile);
        List<SkillDTO> skillList = technicianSkills.stream()
                .map(ts -> convertToSkillDTO(ts.getSkill()))
                .toList();

        // Filter by service skills if serviceId is provided
        if (serviceId != null) {
            // TODO: Add service-skill filtering logic when service skills are defined
            // For now, return all technicians
        }

        return NearbyTechnicianDTO.builder()
                .userId(profile.getUser().getId())
                .profileId(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getUser().getEmail())
                .phoneNumber(profile.getUser().getPhoneNumber())
                .rating(profile.getRating())
                .status(profile.getStatus())
                .currentLatitude(profile.getCurrentLatitude())
                .currentLongitude(profile.getCurrentLongitude())
                .currentAddress(profile.getCurrentAddress())
                .workingRadius(profile.getWorkingRadius())
                .distanceKm(distance)
                .isOnline(profile.getIsOnline())
                .lastSeenAt(profile.getLastSeenAt())
                .skillList(skillList)
                .build();
    }

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS = 6371.0; // Earth radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
} 