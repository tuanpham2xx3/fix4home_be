package com.fix4home.fix4home.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.ServiceDTO;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import com.fix4home.fix4home.model.entity.Service;
import com.fix4home.fix4home.model.enums.UserStatus;
import com.fix4home.fix4home.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Transactional
@DisplayName("Service Integration Tests with TestContainers")
class ServiceIntegrationTest {

    @Container
    @SuppressWarnings("resource") // TestContainers handles resource lifecycle
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("fix4home_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQLDialect");
        
        // Disable Redis for testing
        registry.add("spring.cache.type", () -> "none");
        registry.add("spring.data.redis.host", () -> "disabled");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Service testService;

    @BeforeEach
    void setUp() {
        serviceRepository.deleteAll();
        
        testService = Service.builder()
                .name("Integration Test Service")
                .description("Service for integration testing")
                .basePrice(new BigDecimal("100.00"))
                .status(UserStatus.ACTIVE)
                .build();
        
        testService = serviceRepository.save(testService);
    }

    @Test
    @DisplayName("Should perform complete service lifecycle operations")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void serviceLifecycle_ShouldWorkEndToEnd() throws Exception {
        // 1. Create a new service
        CreateServiceRequest createRequest = CreateServiceRequest.builder()
                .name("New Integration Service")
                .description("Description for new service")
                .basePrice(new BigDecimal("150.00"))
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Integration Service"))
                .andReturn().getResponse().getContentAsString();

        // Extract the created service ID
        ServiceDTO createdService = objectMapper.readTree(createResponse)
                .get("data").traverse(objectMapper).readValueAs(ServiceDTO.class);
        Long serviceId = createdService.getId();

        // 2. Get the created service
        mockMvc.perform(get("/api/v1/services/" + serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Integration Service"))
                .andExpect(jsonPath("$.data.basePrice").value(150.00));

        // 3. Update the service
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .name("Updated Integration Service")
                .description("Updated description")
                .basePrice(new BigDecimal("175.00"))
                .build();

        mockMvc.perform(put("/api/v1/services/" + serviceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Integration Service"))
                .andExpect(jsonPath("$.data.basePrice").value(175.00));

        // 4. Search for the service
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", "Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Updated Integration Service"));

        // 5. Get all services (should include both existing and new service)
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)); // testService + created service

        // 6. Delete the service (soft delete)
        mockMvc.perform(delete("/api/v1/services/" + serviceId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 7. Verify service is not in active services anymore
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1)); // Only testService should remain
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void pagination_ShouldWorkCorrectly() throws Exception {
        // Create multiple services for pagination test
        for (int i = 1; i <= 15; i++) {
            Service service = Service.builder()
                    .name("Service " + i)
                    .description("Description " + i)
                    .basePrice(new BigDecimal("50.00"))
                    .status(UserStatus.ACTIVE)
                    .build();
            serviceRepository.save(service);
        }

        // Test first page
        mockMvc.perform(get("/api/v1/services/paginated")
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(16)) // 15 + testService
                .andExpect(jsonPath("$.data.totalPages").value(4))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(false));

        // Test last page
        mockMvc.perform(get("/api/v1/services/paginated")
                .param("page", "3")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.first").value(false))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    @DisplayName("Should handle database constraints and validations")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void databaseConstraints_ShouldBeEnforced() throws Exception {
        // Try to create service with duplicate name
        CreateServiceRequest duplicateRequest = CreateServiceRequest.builder()
                .name("Integration Test Service") // Same as testService
                .description("Duplicate service")
                .basePrice(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict()); // Should conflict due to duplicate name
    }

    @Test
    @DisplayName("Should handle search functionality correctly")
    void search_ShouldFindServicesCorrectly() throws Exception {
        // Create services with different names for search testing
        Service plumbingService = Service.builder()
                .name("Plumbing Repair Service")
                .description("Fix pipes and faucets")
                .basePrice(new BigDecimal("120.00"))
                .status(UserStatus.ACTIVE)
                .build();
        
        Service electricalService = Service.builder()
                .name("Electrical Installation")
                .description("Install electrical fixtures")
                .basePrice(new BigDecimal("200.00"))
                .status(UserStatus.ACTIVE)
                .build();
        
        serviceRepository.save(plumbingService);
        serviceRepository.save(electricalService);

        // Search for "plumbing"
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", "plumbing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Plumbing Repair Service"));

        // Search for "service" (should match multiple)
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", "service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2)); // plumbingService + testService

        // Search with empty keyword should return all active services
        mockMvc.perform(get("/api/v1/services/search")
                .param("keyword", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3)); // All three services
    }

    @Test
    @DisplayName("Should handle service status toggling")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void statusToggling_ShouldWorkCorrectly() throws Exception {
        Long serviceId = testService.getId();

        // Initially service should be ACTIVE
        mockMvc.perform(get("/api/v1/services/" + serviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        // Toggle to INACTIVE (using PUT to update status instead)
        UpdateServiceRequest statusUpdate = UpdateServiceRequest.builder()
                .status(UserStatus.INACTIVE)
                .build();
        
        mockMvc.perform(put("/api/v1/services/" + serviceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk());

        // Service should not appear in active services list
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        // Toggle back to ACTIVE
        UpdateServiceRequest activeUpdate = UpdateServiceRequest.builder()
                .status(UserStatus.ACTIVE)
                .build();
        
        mockMvc.perform(put("/api/v1/services/" + serviceId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activeUpdate)))
                .andExpect(status().isOk());

        // Service should appear in active services list again
        mockMvc.perform(get("/api/v1/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Should enforce security for admin-only operations")
    @WithMockUser(authorities = "ROLE_CUSTOMER")
    void adminOperations_ShouldRequireAdminRole() throws Exception {
        CreateServiceRequest createRequest = CreateServiceRequest.builder()
                .name("Unauthorized Service")
                .description("Should not be created")
                .basePrice(new BigDecimal("100.00"))
                .build();

        // Customer should not be able to create service
        mockMvc.perform(post("/api/v1/services")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        // Customer should not be able to update service
        UpdateServiceRequest updateRequest = UpdateServiceRequest.builder()
                .name("Updated name")
                .build();

        mockMvc.perform(put("/api/v1/services/" + testService.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        // Customer should not be able to delete service
        mockMvc.perform(delete("/api/v1/services/" + testService.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
