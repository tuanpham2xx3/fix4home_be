package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.BadRequestException;
import com.fix4home.fix4home.exception.ResourceAlreadyExistsException;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.model.entity.Service;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        log.info("Fetching all services");
        List<Service> services = serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getActiveServices() {
        log.info("Fetching active services");
        List<Service> services = serviceRepository.findByStatus(UserStatus.ACTIVE);
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServiceDTO> getAllServicesWithPagination(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching services with pagination - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                 page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Service> servicePage = serviceRepository.findAll(pageable);
        
        return servicePage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(Long id) {
        log.info("Fetching service with id: {}", id);
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + id));
        return convertToDTO(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> searchServices(String keyword) {
        log.info("Searching services with keyword: {}", keyword);
        
        if (!StringUtils.hasText(keyword)) {
            return getActiveServices();
        }
        
        List<Service> services = serviceRepository.findByNameContainingIgnoreCase(keyword);
        return services.stream()
                .filter(service -> service.getStatus() == UserStatus.ACTIVE)
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public ServiceDTO createService(CreateServiceRequest request) {
        log.info("Creating new service: {}", request.getName());

        // Check for duplicate name
        if (serviceRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Service with name '" + request.getName() + "' already exists");
        }

        Service service = Service.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(UserStatus.ACTIVE)
                .build();

        Service savedService = serviceRepository.save(service);
        log.info("Service created successfully with id: {}", savedService.getId());
        
        return convertToDTO(savedService);
    }

    @Transactional
    public ServiceDTO updateService(Long id, UpdateServiceRequest request) {
        log.info("Updating service with id: {}", id);

        Service existingService = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + id));

        // Check for duplicate name if name is being updated
        if (StringUtils.hasText(request.getName()) && 
            !existingService.getName().equals(request.getName()) &&
            serviceRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Service with name '" + request.getName() + "' already exists");
        }

        // Update fields if provided
        if (StringUtils.hasText(request.getName())) {
            existingService.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            existingService.setDescription(request.getDescription());
        }
        
        if (request.getBasePrice() != null) {
            existingService.setBasePrice(request.getBasePrice());
        }
        
        if (request.getStatus() != null) {
            existingService.setStatus(request.getStatus());
        }

        Service updatedService = serviceRepository.save(existingService);
        log.info("Service updated successfully with id: {}", updatedService.getId());
        
        return convertToDTO(updatedService);
    }

    @Transactional
    public void deleteService(Long id) {
        log.info("Deleting service with id: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + id));

        // Soft delete by setting status to INACTIVE
        service.setStatus(UserStatus.INACTIVE);
        serviceRepository.save(service);
        
        log.info("Service soft deleted successfully with id: {}", id);
    }

    @Transactional
    public void hardDeleteService(Long id) {
        log.info("Hard deleting service with id: {}", id);

        if (!serviceRepository.existsById(id)) {
            throw new BadRequestException("Service not found with id: " + id);
        }

        serviceRepository.deleteById(id);
        log.info("Service hard deleted successfully with id: {}", id);
    }

    @Transactional
    public ServiceDTO toggleServiceStatus(Long id) {
        log.info("Toggling status for service with id: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Service not found with id: " + id));

        UserStatus newStatus = service.getStatus() == UserStatus.ACTIVE ? 
                              UserStatus.INACTIVE : UserStatus.ACTIVE;
        
        service.setStatus(newStatus);
        Service updatedService = serviceRepository.save(service);
        
        log.info("Service status toggled to {} for id: {}", newStatus, id);
        return convertToDTO(updatedService);
    }

    // Helper method to convert Entity to DTO
    private ServiceDTO convertToDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .status(service.getStatus())
                .build();
    }

    // Helper method to convert DTO to Entity (if needed)
    private Service convertToEntity(ServiceDTO serviceDTO) {
        return Service.builder()
                .id(serviceDTO.getId())
                .name(serviceDTO.getName())
                .description(serviceDTO.getDescription())
                .basePrice(serviceDTO.getBasePrice())
                .status(serviceDTO.getStatus())
                .build();
    }
} 