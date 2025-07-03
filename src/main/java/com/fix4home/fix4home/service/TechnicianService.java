package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.exception.ResourceAlreadyExistsException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TechnicianService {

    private final UserRepository userRepository;
    private final TechnicianProfileRepository technicianProfileRepository;
    private final SkillRepository skillRepository;
    private final TechnicianSkillRepository technicianSkillRepository;

    // ==================== TECHNICIAN PROFILE MANAGEMENT ====================

    @Transactional(readOnly = true)
    public TechnicianProfileDTO getTechnicianProfile(Long userId) {
        log.info("Fetching technician profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found for user: " + userId));

        return convertToTechnicianProfileDTO(user, profile);
    }

    @Transactional(readOnly = true)
    public TechnicianProfileDTO getMyProfile() {
        log.info("Fetching profile for current authenticated technician");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("Current user is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        return convertToTechnicianProfileDTO(user, profile);
    }

    @Transactional
    public TechnicianProfileDTO updateTechnicianProfile(Long userId, UpdateTechnicianProfileRequest request) {
        log.info("Updating technician profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found for user: " + userId));

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

        log.info("Technician profile updated successfully for user ID: {}", userId);
        return convertToTechnicianProfileDTO(savedUser, savedProfile);
    }

    @Transactional
    public TechnicianProfileDTO updateMyProfile(UpdateTechnicianProfileRequest request) {
        log.info("Updating profile for current authenticated technician");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        return updateTechnicianProfile(user.getId(), request);
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getAllTechnicians() {
        log.info("Fetching all technicians");
        
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
        log.info("Fetching active technicians");
        
        List<TechnicianProfile> activeProfiles = technicianProfileRepository.findByStatus(UserStatus.ACTIVE);
        return activeProfiles.stream()
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnicianProfileDTO> getPendingTechnicians() {
        log.info("Fetching pending approval technicians");
        
        List<TechnicianProfile> pendingProfiles = technicianProfileRepository.findByStatus(UserStatus.INACTIVE);
        return pendingProfiles.stream()
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TechnicianProfileDTO> getAllTechniciansWithPagination(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching technicians with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
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
        log.info("Searching technicians with keyword: {}", keyword);
        
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
        log.info("Fetching technicians with rating >= {}", minRating);
        
        List<TechnicianProfile> technicians = technicianProfileRepository.findByRatingGreaterThanEqualOrderByRatingDesc(minRating);
        return technicians.stream()
                .filter(profile -> profile.getStatus() == UserStatus.ACTIVE)
                .map(profile -> convertToTechnicianProfileDTO(profile.getUser(), profile))
                .toList();
    }

    // ==================== TECHNICIAN APPROVAL WORKFLOW ====================

    @Transactional
    public TechnicianProfileDTO approveTechnician(Long userId) {
        log.info("Approving technician with user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        profile.setStatus(UserStatus.ACTIVE);
        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        log.info("Technician approved successfully with user ID: {}", userId);
        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    @Transactional
    public TechnicianProfileDTO rejectTechnician(Long userId) {
        log.info("Rejecting technician with user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        profile.setStatus(UserStatus.INACTIVE);
        TechnicianProfile savedProfile = technicianProfileRepository.save(profile);

        log.info("Technician rejected with user ID: {}", userId);
        return convertToTechnicianProfileDTO(user, savedProfile);
    }

    // ==================== SKILLS MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<SkillDTO> getAllSkills() {
        log.info("Fetching all skills");
        
        List<Skill> skills = skillRepository.findAllOrderByName();
        return skills.stream()
                .map(this::convertToSkillDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> searchSkills(String keyword) {
        log.info("Searching skills with keyword: {}", keyword);
        
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
        log.info("Creating new skill: {}", request.getName());

        if (skillRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Skill with name '" + request.getName() + "' already exists");
        }

        Skill skill = Skill.builder()
                .name(request.getName())
                .build();

        Skill savedSkill = skillRepository.save(skill);
        log.info("Skill created successfully with ID: {}", savedSkill.getId());
        
        return convertToSkillDTO(savedSkill);
    }

    @Transactional
    public void deleteSkill(Long skillId) {
        log.info("Deleting skill with ID: {}", skillId);

        if (!skillRepository.existsById(skillId)) {
            throw new BadRequestException("Skill not found with id: " + skillId);
        }

        // Remove all technician-skill associations first
        technicianSkillRepository.deleteBySkillId(skillId);
        skillRepository.deleteById(skillId);
        
        log.info("Skill deleted successfully with ID: {}", skillId);
    }

    // ==================== TECHNICIAN SKILLS MANAGEMENT ====================

    @Transactional(readOnly = true)
    public List<SkillDTO> getTechnicianSkills(Long userId) {
        log.info("Fetching skills for technician user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        List<TechnicianSkill> technicianSkills = technicianSkillRepository.findByTechnicianProfile(profile);
        return technicianSkills.stream()
                .map(ts -> convertToSkillDTO(ts.getSkill()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SkillDTO> getMySkills() {
        log.info("Fetching skills for current authenticated technician");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        return getTechnicianSkills(user.getId());
    }

    @Transactional
    public List<SkillDTO> assignSkillsToTechnician(Long userId, AssignSkillsRequest request) {
        log.info("Assigning skills to technician user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));

        if (user.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("User is not a technician");
        }

        TechnicianProfile profile = technicianProfileRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("Technician profile not found"));

        // Remove existing skills
        technicianSkillRepository.deleteByTechnicianProfileId(profile.getId());

        // Add new skills
        for (Long skillId : request.getSkillIds()) {
            Skill skill = skillRepository.findById(skillId)
                    .orElseThrow(() -> new BadRequestException("Skill not found with id: " + skillId));

            TechnicianSkill technicianSkill = TechnicianSkill.builder()
                    .technicianProfile(profile)
                    .skill(skill)
                    .build();

            technicianSkillRepository.save(technicianSkill);
        }

        log.info("Skills assigned successfully to technician user ID: {}", userId);
        return getTechnicianSkills(userId);
    }

    @Transactional
    public List<SkillDTO> assignSkillsToMyself(AssignSkillsRequest request) {
        log.info("Assigning skills to current authenticated technician");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found"));

        return assignSkillsToTechnician(user.getId(), request);
    }

    // ==================== UTILITY METHODS ====================

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
                   .status(profile.getStatus());

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
} 