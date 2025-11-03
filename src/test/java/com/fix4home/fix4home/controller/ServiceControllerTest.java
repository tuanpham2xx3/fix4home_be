package com.fix4home.fix4home.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.exception.ServiceNotFoundException;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@DisplayName("ServiceController Unit Tests")
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceDTO testServiceDTO;
    private CreateServiceRequest createRequest;
    private UpdateServiceRequest updateRequest;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    @DisplayName("GET /api/v1/services - Should return all active services")
    void getAllServices_ShouldReturnActiveServices() throws Exception {
        // Arrange
        List<ServiceDTO> services = List.of(testServiceDTO);
        when(serviceService.getActiveServices()).thenReturn(services);

        // Act & Assert
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Services retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Plumbing Service"))
                .andExpect(jsonPath("$.data[0].basePrice").value(150.00));

        verify(serviceService).getActiveServices();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("GET /api/v1/services/admin/all - Should return all services for admin")
    void getAllServicesForAdmin_WithAdminRole_ShouldReturnAllServices() throws Exception {
        // Arrange
        List<ServiceDTO> services = List.of(testServiceDTO);
        when(serviceService.getAllServices()).thenReturn(services);

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All services retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));

        verify(serviceService).getAllServices();
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("GET /api/v1/services/admin/all - Should deny access for non-admin")
    void getAllServicesForAdmin_WithCustomerRole_ShouldDenyAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).getAllServices();
    }

    @Test
    @DisplayName("GET /api/v1/services/paginated - Should return paginated services")
    void getServicesWithPagination_ShouldReturnPagedResults() throws Exception {
        // Arrange
        Page<ServiceDTO> servicePage = new PageImpl<>(List.of(testServiceDTO), 
                PageRequest.of(0, 5), 1);
        when(serviceService.getAllServicesWithPagination(0, 5, "name", "asc"))
                .thenReturn(servicePage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/paginated")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(serviceService).getAllServicesWithPagination(0, 5, "name", "asc");
    }

    @Test
    @DisplayName("GET /api/v1/services/{id} - Should return service by ID")
    void getServiceById_WithValidId_ShouldReturnService() throws Exception {
        // Arrange
        when(serviceService.getServiceById(1L)).thenReturn(testServiceDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Service retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Plumbing Service"));

        verify(serviceService).getServiceById(1L);
    }

    @Test
    @DisplayName("GET /api/v1/services/{id} - Should return 404 when service not found")
    void getServiceById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(serviceService.getServiceById(999L))
                .thenThrow(new ServiceNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/999"))
                .andExpect(status().isNotFound());

        verify(serviceService).getServiceById(999L);
    }

    @Test
    @DisplayName("GET /api/v1/services/search - Should return search results")
    void searchServices_WithKeyword_ShouldReturnMatchingServices() throws Exception {
        // Arrange
        List<ServiceDTO> searchResults = List.of(testServiceDTO);
        when(serviceService.searchServices("plumbing")).thenReturn(searchResults);

        // Act & Assert
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", "plumbing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Search completed successfully"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Plumbing Service"));

        verify(serviceService).searchServices("plumbing");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/services - Should create service successfully")
    void createService_WithValidRequest_ShouldCreateService() throws Exception {
        // Arrange
        when(serviceService.createService(any(CreateServiceRequest.class)))
                .thenReturn(testServiceDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Service created successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(serviceService).createService(any(CreateServiceRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("POST /api/v1/services - Should deny access for non-admin")
    void createService_WithCustomerRole_ShouldDenyAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).createService(any(CreateServiceRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("POST /api/v1/services - Should return 400 for invalid request")
    void createService_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateServiceRequest invalidRequest = CreateServiceRequest.builder()
                .name("") // Invalid empty name
                .basePrice(new BigDecimal("-100")) // Invalid negative price
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(serviceService, never()).createService(any(CreateServiceRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("PUT /api/v1/services/{id} - Should update service successfully")
    void updateService_WithValidRequest_ShouldUpdateService() throws Exception {
        // Arrange
        when(serviceService.updateService(eq(1L), any(UpdateServiceRequest.class)))
                .thenReturn(testServiceDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/services/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Service updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(serviceService).updateService(eq(1L), any(UpdateServiceRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("DELETE /api/v1/services/{id} - Should delete service successfully")
    void deleteService_WithValidId_ShouldDeleteService() throws Exception {
        // Arrange
        doNothing().when(serviceService).deleteService(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/services/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Service deleted successfully"));

        verify(serviceService).deleteService(1L);
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("DELETE /api/v1/services/{id} - Should deny access for non-admin")
    void deleteService_WithCustomerRole_ShouldDenyAccess() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/services/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(serviceService, never()).deleteService(anyLong());
    }

    @Test
    @DisplayName("GET /api/v1/services/health - Should return service health status")
    void getServiceHealth_ShouldReturnHealthStatus() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/services/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Service Controller is working"));
    }
}
