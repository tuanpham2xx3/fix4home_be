package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.model.entity.Service;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
public class ServiceService extends BaseService {

    private final ServiceRepository serviceRepository;

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        logBusinessOperation("GET_ALL_SERVICES");
        requireRole(Role.ADMIN);

        List<Service> services = serviceRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "services", key = "'active'")
    public List<ServiceDTO> getActiveServices() {
        logBusinessOperation("GET_ACTIVE_SERVICES");

        List<Service> services = serviceRepository.findByStatus(UserStatus.ACTIVE);
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServiceDTO> getAllServicesWithPagination(int page, int size, String sortBy, String sortDir) {
        logBusinessOperation("GET_ALL_SERVICES_PAGINATED");
        requireRole(Role.ADMIN);

        validatePaginationParams(page, size);
        validateSortDirection(sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Service> servicePage = serviceRepository.findAll(pageable);
        
        return servicePage.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(Long id) {
        logBusinessOperation("GET_SERVICE_BY_ID", "id=" + id);

        validatePositiveId(id, "id");
        return convertToDTO(findServiceById(id));
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> searchServices(String keyword) {
        logBusinessOperation("SEARCH_SERVICES", "keyword=" + keyword);
        
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
        logBusinessOperation("CREATE_SERVICE", "name=" + request.getName());
        requireRole(Role.ADMIN);

        validateRequired(request, "request");
        validateRequired(request.getName(), "name");
        validateRequired(request.getDescription(), "description");
        validateRequired(request.getBasePrice(), "basePrice");

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
        return convertToDTO(savedService);
    }

    @Transactional
    public ServiceDTO updateService(Long id, UpdateServiceRequest request) {
        logBusinessOperation("UPDATE_SERVICE", "id=" + id);
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        validateRequired(request, "request");

        Service existingService = findServiceById(id);

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
        return convertToDTO(updatedService);
    }

    @Transactional
    public void deleteService(Long id) {
        logBusinessOperation("DELETE_SERVICE", "id=" + id);
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        Service service = findServiceById(id);

        // Soft delete by setting status to INACTIVE
        service.setStatus(UserStatus.INACTIVE);
        serviceRepository.save(service);
    }

    @Transactional
    public void hardDeleteService(Long id) {
        logBusinessOperation("HARD_DELETE_SERVICE", "id=" + id);
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        if (!serviceRepository.existsById(id)) {
            throw new ServiceNotFoundException(id);
        }

        serviceRepository.deleteById(id);
    }

    @Transactional
    public ServiceDTO toggleServiceStatus(Long id) {
        logBusinessOperation("TOGGLE_SERVICE_STATUS", "id=" + id);
        requireRole(Role.ADMIN);

        validatePositiveId(id, "id");
        Service service = findServiceById(id);

        UserStatus newStatus = service.getStatus() == UserStatus.ACTIVE ? 
                              UserStatus.INACTIVE : UserStatus.ACTIVE;
        
        service.setStatus(newStatus);
        Service updatedService = serviceRepository.save(service);
        
        return convertToDTO(updatedService);
    }

    // ==================== HELPER METHODS ====================

    private Service findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException(id));
    }

    private ServiceDTO convertToDTO(Service service) {
        return ServiceDTO.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .basePrice(service.getBasePrice())
                .status(service.getStatus())
                .build();
    }

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