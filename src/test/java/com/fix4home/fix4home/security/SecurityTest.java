package com.fix4home.fix4home.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.entity.User;
import com.fix4home.fix4home.model.enums.Role;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security and Authorization Tests")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User customerUser;
    private User technicianUser;
    private CreateServiceRequest serviceRequest;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@fix4home.com")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        customerUser = User.builder()
                .id(2L)
                .username("customer")
                .email("customer@fix4home.com")
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        technicianUser = User.builder()
                .id(3L)
                .username("technician")
                .email("technician@fix4home.com")
                .role(Role.TECHNICIAN)
                .status(UserStatus.ACTIVE)
                .build();

        serviceRequest = CreateServiceRequest.builder()
                .name("Test Service")
                .description("Test Description")
                .basePrice(new BigDecimal("100.00"))
                .build();

        // Mock user repository responses
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUsername("customer")).thenReturn(Optional.of(customerUser));
        when(userRepository.findByUsername("technician")).thenReturn(Optional.of(technicianUser));
    }

    // ==================== ADMIN-ONLY ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Admin should access admin-only endpoints")
    void adminUser_ShouldAccessAdminOnlyEndpoints() throws Exception {
        // Test service creation (admin-only)
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isCreated());

        // Test getting all services including inactive (admin-only)
        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isOk());

        // Test service update (admin-only)
        mockMvc.perform(put("/api/v1/services/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isOk());

        // Test service deletion (admin-only)
        mockMvc.perform(delete("/api/v1/services/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("Customer should be denied access to admin-only endpoints")
    void customerUser_ShouldBeDeniedAdminOnlyEndpoints() throws Exception {
        // Test service creation (should be denied)
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());

        // Test getting all services including inactive (should be denied)
        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isForbidden());

        // Test service update (should be denied)
        mockMvc.perform(put("/api/v1/services/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());

        // Test service deletion (should be denied)
        mockMvc.perform(delete("/api/v1/services/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_TECHNICIAN")
    @DisplayName("Technician should be denied access to admin-only endpoints")
    void technicianUser_ShouldBeDeniedAdminOnlyEndpoints() throws Exception {
        // Test service creation (should be denied)
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());

        // Test getting all services including inactive (should be denied)
        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isForbidden());
    }

    // ==================== PUBLIC ENDPOINTS TESTS ====================

    @Test
    @WithAnonymousUser
    @DisplayName("Anonymous users should access public endpoints")
    void anonymousUser_ShouldAccessPublicEndpoints() throws Exception {
        // Test getting active services (public)
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk());

        // Test getting service by ID (public)
        mockMvc.perform(get("/api/v1/services/1"))
                .andExpect(status().isOk());

        // Test searching services (public)
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", "test"))
                .andExpect(status().isOk());

        // Test service health check (public)
        mockMvc.perform(get("/api/v1/services/health"))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Anonymous users should be denied access to protected endpoints")
    void anonymousUser_ShouldBeDeniedProtectedEndpoints() throws Exception {
        // Test service creation (should be denied)
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isUnauthorized());

        // Test customer profile access (should be denied)
        mockMvc.perform(get("/api/v1/customers/profile"))
                .andExpect(status().isUnauthorized());

        // Test technician profile access (should be denied)
        mockMvc.perform(get("/api/v1/technicians/me"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== CUSTOMER-SPECIFIC ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("Customer should access customer-specific endpoints")
    void customerUser_ShouldAccessCustomerEndpoints() throws Exception {
        // Test customer profile access
        mockMvc.perform(get("/api/v1/customers/profile"))
                .andExpect(status().isOk());

        // Test creating service request
        mockMvc.perform(post("/api/v1/service-requests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest()); // Bad request due to validation, but authorized

        // Test getting my service requests
        mockMvc.perform(get("/api/v1/service-requests/my"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_TECHNICIAN")
    @DisplayName("Technician should be denied access to customer-only endpoints")
    void technicianUser_ShouldBeDeniedCustomerOnlyEndpoints() throws Exception {
        // Test customer profile access (should be denied)
        mockMvc.perform(get("/api/v1/customers/profile"))
                .andExpect(status().isForbidden());

        // Test creating service request (should be denied)
        mockMvc.perform(post("/api/v1/service-requests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ==================== TECHNICIAN-SPECIFIC ENDPOINTS TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_TECHNICIAN")
    @DisplayName("Technician should access technician-specific endpoints")
    void technicianUser_ShouldAccessTechnicianEndpoints() throws Exception {
        // Test technician profile access
        mockMvc.perform(get("/api/v1/technicians/me"))
                .andExpect(status().isOk());

        // Test getting available service requests
        mockMvc.perform(get("/api/v1/service-requests/available"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    @DisplayName("Customer should be denied access to technician-only endpoints")
    void customerUser_ShouldBeDeniedTechnicianOnlyEndpoints() throws Exception {
        // Test technician profile access (should be denied)
        mockMvc.perform(get("/api/v1/technicians/me"))
                .andExpect(status().isForbidden());

        // Test getting available service requests (should be denied)
        mockMvc.perform(get("/api/v1/service-requests/available"))
                .andExpect(status().isForbidden());
    }

    // ==================== ROLE HIERARCHY TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Admin should have access to multi-role endpoints")
    void adminUser_ShouldAccessMultiRoleEndpoints() throws Exception {
        // Admin should access customer endpoints
        mockMvc.perform(get("/api/v1/customers/profile"))
                .andExpect(status().isOk());

        // Admin should access technician endpoints
        mockMvc.perform(get("/api/v1/technicians/me"))
                .andExpect(status().isOk());
    }

    // ==================== CSRF PROTECTION TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("State-changing operations should require CSRF token")
    void stateChangingOperations_ShouldRequireCSRF() throws Exception {
        // Test POST without CSRF (should be denied)
        mockMvc.perform(post("/api/v1/services")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());

        // Test PUT without CSRF (should be denied)
        mockMvc.perform(put("/api/v1/services/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());

        // Test DELETE without CSRF (should be denied)
        mockMvc.perform(delete("/api/v1/services/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("GET operations should not require CSRF token")
    void getOperations_ShouldNotRequireCSRF() throws Exception {
        // Test GET operations (should work without CSRF)
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/services/admin/all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/services/1"))
                .andExpect(status().isOk());
    }

    // ==================== CONTENT TYPE SECURITY TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Should reject requests with invalid content type")
    void invalidContentType_ShouldBeRejected() throws Exception {
        // Test with text/plain instead of application/json
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    // ==================== HTTP METHOD SECURITY TESTS ====================

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("Should only allow specified HTTP methods")
    void unsupportedHttpMethods_ShouldBeRejected() throws Exception {
        // Test PATCH on endpoint that doesn't support it
        mockMvc.perform(patch("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== AUTHORITY VALIDATION TESTS ====================

    @Test
    @WithMockUser(authorities = {"ROLE_INVALID"})
    @DisplayName("Should reject users with invalid roles")
    void invalidRole_ShouldBeRejected() throws Exception {
        // Test with invalid role
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    @DisplayName("Should reject users with no roles")
    void noRoles_ShouldBeRejected() throws Exception {
        // Test with no roles
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());
    }

    // ==================== MIXED ROLE SCENARIOS ====================

    @Test
    @WithMockUser(authorities = {"ROLE_CUSTOMER", "ROLE_TECHNICIAN"})
    @DisplayName("Should handle users with multiple roles")
    void multipleRoles_ShouldBeHandledCorrectly() throws Exception {
        // User with both CUSTOMER and TECHNICIAN roles should access both endpoints
        mockMvc.perform(get("/api/v1/customers/profile"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/technicians/me"))
                .andExpect(status().isOk());

        // But still should not access admin endpoints
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(serviceRequest)))
                .andExpect(status().isForbidden());
    }
}
