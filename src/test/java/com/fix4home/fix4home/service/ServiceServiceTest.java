package com.fix4home.fix4home.service;

import com.fix4home.fix4home.exception.*;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.model.entity.Service;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ServiceRepository;
import com.fix4home.fix4home.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceService Unit Tests")
class ServiceServiceTest {

    @Mock
    private ServiceRepository serviceRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ServiceService serviceService;

    private Service testService;
    private ServiceDTO testServiceDTO;
    private CreateServiceRequest createRequest;
    private UpdateServiceRequest updateRequest;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        testService = Service.builder()
                .id(1L)
                .name("Plumbing Service")
                .description("Professional plumbing repair service")
                .basePrice(new BigDecimal("150.00"))
                .status(UserStatus.ACTIVE)
                .build();

        testServiceDTO = ServiceDTO.builder()
                .id(1L)
                .name("Plumbing Service")
                .description("Professional plumbing repair service")
                .basePrice(new BigDecimal("150.00"))
                .status(UserStatus.ACTIVE)
                .build();

        createRequest = CreateServiceRequest.builder()
                .name("New Service")
                .description("New service description")
                .basePrice(new BigDecimal("200.00"))
                .build();

        updateRequest = UpdateServiceRequest.builder()
                .name("Updated Service")
                .description("Updated description")
                .basePrice(new BigDecimal("175.00"))
                .build();

        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        // Setup security context
        setupSecurityContext(adminUser);
    }

    private void setupSecurityContext(User user) {
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        var authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, authorities);
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(userRepository.findByUsername(user.getUsername()))
                .thenReturn(Optional.of(user));
    }

    @Test
    @DisplayName("Should get all services for admin")
    void getAllServices_WhenAdminRole_ShouldReturnAllServices() {
        // Arrange
        List<Service> services = List.of(testService);
        when(serviceRepository.findAll(any(Sort.class))).thenReturn(services);

        // Act
        List<ServiceDTO> result = serviceService.getAllServices();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testServiceDTO.getName(), result.get(0).getName());
        verify(serviceRepository).findAll(any(Sort.class));
    }

    @Test
    @DisplayName("Should get active services without authentication")
    void getActiveServices_ShouldReturnActiveServices() {
        // Arrange
        List<Service> activeServices = List.of(testService);
        when(serviceRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = serviceService.getActiveServices();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserStatus.ACTIVE, result.get(0).getStatus());
        verify(serviceRepository).findByStatus(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should get services with pagination for admin")
    void getAllServicesWithPagination_WhenValidParams_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Service> servicePage = new PageImpl<>(List.of(testService), pageable, 1);
        when(serviceRepository.findAll(any(Pageable.class))).thenReturn(servicePage);

        // Act
        Page<ServiceDTO> result = serviceService.getAllServicesWithPagination(0, 10, "name", "asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(serviceRepository).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Should throw exception for invalid pagination params")
    void getAllServicesWithPagination_WhenInvalidParams_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessValidationException.class, 
            () -> serviceService.getAllServicesWithPagination(-1, 10, "name", "asc"));
        
        assertThrows(BusinessValidationException.class, 
            () -> serviceService.getAllServicesWithPagination(0, 0, "name", "asc"));
    }

    @Test
    @DisplayName("Should get service by ID")
    void getServiceById_WhenValidId_ShouldReturnService() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));

        // Act
        ServiceDTO result = serviceService.getServiceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testServiceDTO.getId(), result.getId());
        assertEquals(testServiceDTO.getName(), result.getName());
        verify(serviceRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when service not found")
    void getServiceById_WhenServiceNotFound_ShouldThrowException() {
        // Arrange
        when(serviceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ServiceNotFoundException.class, 
            () -> serviceService.getServiceById(999L));
        verify(serviceRepository).findById(999L);
    }

    @Test
    @DisplayName("Should search services by keyword")
    void searchServices_WhenValidKeyword_ShouldReturnMatchingServices() {
        // Arrange
        String keyword = "plumbing";
        List<Service> matchingServices = List.of(testService);
        when(serviceRepository.findByNameContainingIgnoreCase(keyword))
                .thenReturn(matchingServices);

        // Act
        List<ServiceDTO> result = serviceService.searchServices(keyword);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(UserStatus.ACTIVE, result.get(0).getStatus());
        verify(serviceRepository).findByNameContainingIgnoreCase(keyword);
    }

    @Test
    @DisplayName("Should return active services when keyword is empty")
    void searchServices_WhenEmptyKeyword_ShouldReturnActiveServices() {
        // Arrange
        List<Service> activeServices = List.of(testService);
        when(serviceRepository.findByStatus(UserStatus.ACTIVE)).thenReturn(activeServices);

        // Act
        List<ServiceDTO> result = serviceService.searchServices("");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceRepository).findByStatus(UserStatus.ACTIVE);
        verify(serviceRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Should create service successfully")
    void createService_WhenValidRequest_ShouldCreateService() {
        // Arrange
        when(serviceRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        ServiceDTO result = serviceService.createService(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testService.getName(), result.getName());
        verify(serviceRepository).existsByName(createRequest.getName());
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Should throw exception when creating service with existing name")
    void createService_WhenDuplicateName_ShouldThrowException() {
        // Arrange
        when(serviceRepository.existsByName(createRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, 
            () -> serviceService.createService(createRequest));
        verify(serviceRepository).existsByName(createRequest.getName());
        verify(serviceRepository, never()).save(any(Service.class));
    }

    @Test
    @DisplayName("Should update service successfully")
    void updateService_WhenValidRequest_ShouldUpdateService() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.existsByName(updateRequest.getName())).thenReturn(false);
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        ServiceDTO result = serviceService.updateService(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Should soft delete service")
    void deleteService_WhenValidId_ShouldSoftDeleteService() {
        // Arrange
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        serviceService.deleteService(1L);

        // Assert
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Should hard delete service")
    void hardDeleteService_WhenValidId_ShouldHardDeleteService() {
        // Arrange
        when(serviceRepository.existsById(1L)).thenReturn(true);

        // Act
        serviceService.hardDeleteService(1L);

        // Assert
        verify(serviceRepository).existsById(1L);
        verify(serviceRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should toggle service status")
    void toggleServiceStatus_WhenValidId_ShouldToggleStatus() {
        // Arrange
        testService.setStatus(UserStatus.ACTIVE);
        when(serviceRepository.findById(1L)).thenReturn(Optional.of(testService));
        when(serviceRepository.save(any(Service.class))).thenReturn(testService);

        // Act
        ServiceDTO result = serviceService.toggleServiceStatus(1L);

        // Assert
        assertNotNull(result);
        verify(serviceRepository).findById(1L);
        verify(serviceRepository).save(any(Service.class));
    }

    @Test
    @DisplayName("Should throw exception for null request in createService")
    void createService_WhenNullRequest_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessValidationException.class, 
            () -> serviceService.createService(null));
    }

    @Test
    @DisplayName("Should throw exception for invalid ID")
    void getServiceById_WhenInvalidId_ShouldThrowException() {
        // Act & Assert
        assertThrows(BusinessValidationException.class, 
            () -> serviceService.getServiceById(-1L));
        
        assertThrows(BusinessValidationException.class, 
            () -> serviceService.getServiceById(null));
    }
}
