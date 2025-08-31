package com.fix4home.fix4home.validation;

import com.fix4home.fix4home.model.dto.service.CreateServiceRequest;
import com.fix4home.fix4home.model.dto.service.UpdateServiceRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Service Request Validation Tests")
class ServiceValidationTest {

    @Autowired
    private Validator validator;

    private CreateServiceRequest validCreateRequest;
    private UpdateServiceRequest validUpdateRequest;

    @BeforeEach
    void setUp() {
        validCreateRequest = CreateServiceRequest.builder()
                .name("Valid Service Name")
                .description("Valid service description")
                .basePrice(new BigDecimal("100.00"))
                .build();

        validUpdateRequest = UpdateServiceRequest.builder()
                .name("Updated Service Name")
                .description("Updated description")
                .basePrice(new BigDecimal("150.00"))
                .build();
    }

    @Test
    @DisplayName("Valid CreateServiceRequest should pass validation")
    void validCreateServiceRequest_ShouldPassValidation() {
        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }

    @Test
    @DisplayName("CreateServiceRequest with null name should fail validation")
    void createServiceRequest_WithNullName_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setName(null);

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Service name is required", violation.getMessage());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    @DisplayName("CreateServiceRequest with blank name should fail validation")
    void createServiceRequest_WithBlankName_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setName("   ");

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Service name is required", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with too short name should fail validation")
    void createServiceRequest_WithTooShortName_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setName("A"); // Only 1 character, minimum is 2

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Service name must be between 2 and 100 characters", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with too long name should fail validation")
    void createServiceRequest_WithTooLongName_ShouldFailValidation() {
        // Arrange
        String longName = "A".repeat(101); // 101 characters, maximum is 100
        validCreateRequest.setName(longName);

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Service name must be between 2 and 100 characters", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with too long description should fail validation")
    void createServiceRequest_WithTooLongDescription_ShouldFailValidation() {
        // Arrange
        String longDescription = "A".repeat(1001); // 1001 characters, maximum is 1000
        validCreateRequest.setDescription(longDescription);

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Description cannot exceed 1000 characters", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with null base price should fail validation")
    void createServiceRequest_WithNullBasePrice_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setBasePrice(null);

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Base price is required", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with negative base price should fail validation")
    void createServiceRequest_WithNegativeBasePrice_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setBasePrice(new BigDecimal("-50.00"));

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Base price must be positive or zero", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with zero base price should pass validation")
    void createServiceRequest_WithZeroBasePrice_ShouldPassValidation() {
        // Arrange
        validCreateRequest.setBasePrice(BigDecimal.ZERO);

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Zero base price should be allowed");
    }

    @Test
    @DisplayName("CreateServiceRequest with too many decimal places should fail validation")
    void createServiceRequest_WithTooManyDecimalPlaces_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setBasePrice(new BigDecimal("100.123")); // 3 decimal places, maximum is 2

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Base price must have at most 10 integer digits and 2 decimal places", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with too many integer digits should fail validation")
    void createServiceRequest_WithTooManyIntegerDigits_ShouldFailValidation() {
        // Arrange
        validCreateRequest.setBasePrice(new BigDecimal("12345678901.00")); // 11 integer digits, maximum is 10

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(validCreateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<CreateServiceRequest> violation = violations.iterator().next();
        assertEquals("Base price must have at most 10 integer digits and 2 decimal places", violation.getMessage());
    }

    @Test
    @DisplayName("CreateServiceRequest with multiple validation errors should return all violations")
    void createServiceRequest_WithMultipleErrors_ShouldReturnAllViolations() {
        // Arrange
        CreateServiceRequest invalidRequest = CreateServiceRequest.builder()
                .name("") // Blank name
                .description("A".repeat(1001)) // Too long description
                .basePrice(new BigDecimal("-100.123")) // Negative with too many decimals
                .build();

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(invalidRequest);

        // Assert
        assertEquals(3, violations.size(), "Should have 3 validation errors");
        
        // Check that all expected error messages are present
        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
        
        assertTrue(messages.contains("Service name is required"));
        assertTrue(messages.contains("Description cannot exceed 1000 characters"));
        assertTrue(messages.contains("Base price must be positive or zero"));
    }

    @Test
    @DisplayName("Valid UpdateServiceRequest should pass validation")
    void validUpdateServiceRequest_ShouldPassValidation() {
        // Act
        Set<ConstraintViolation<UpdateServiceRequest>> violations = validator.validate(validUpdateRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Valid update request should have no violations");
    }

    @Test
    @DisplayName("UpdateServiceRequest with null values should pass validation")
    void updateServiceRequest_WithNullValues_ShouldPassValidation() {
        // Arrange - All fields are optional in update request
        UpdateServiceRequest nullRequest = UpdateServiceRequest.builder().build();

        // Act
        Set<ConstraintViolation<UpdateServiceRequest>> violations = validator.validate(nullRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Update request with null values should be valid");
    }

    @Test
    @DisplayName("UpdateServiceRequest with invalid name should fail validation")
    void updateServiceRequest_WithInvalidName_ShouldFailValidation() {
        // Arrange
        validUpdateRequest.setName("A"); // Too short

        // Act
        Set<ConstraintViolation<UpdateServiceRequest>> violations = validator.validate(validUpdateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UpdateServiceRequest> violation = violations.iterator().next();
        assertTrue(violation.getMessage().contains("between 2 and 100 characters"));
    }

    @Test
    @DisplayName("UpdateServiceRequest with invalid description should fail validation")
    void updateServiceRequest_WithInvalidDescription_ShouldFailValidation() {
        // Arrange
        validUpdateRequest.setDescription("A".repeat(1001)); // Too long

        // Act
        Set<ConstraintViolation<UpdateServiceRequest>> violations = validator.validate(validUpdateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UpdateServiceRequest> violation = violations.iterator().next();
        assertTrue(violation.getMessage().contains("exceed 1000 characters"));
    }

    @Test
    @DisplayName("UpdateServiceRequest with invalid base price should fail validation")
    void updateServiceRequest_WithInvalidBasePrice_ShouldFailValidation() {
        // Arrange
        validUpdateRequest.setBasePrice(new BigDecimal("-50.00")); // Negative

        // Act
        Set<ConstraintViolation<UpdateServiceRequest>> violations = validator.validate(validUpdateRequest);

        // Assert
        assertEquals(1, violations.size());
        ConstraintViolation<UpdateServiceRequest> violation = violations.iterator().next();
        assertTrue(violation.getMessage().contains("positive or zero"));
    }

    @Test
    @DisplayName("Edge case: CreateServiceRequest with minimum valid values should pass")
    void createServiceRequest_WithMinimumValidValues_ShouldPassValidation() {
        // Arrange
        CreateServiceRequest minRequest = CreateServiceRequest.builder()
                .name("AB") // Minimum 2 characters
                .description("") // Empty description is allowed
                .basePrice(BigDecimal.ZERO) // Zero is allowed
                .build();

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(minRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Minimum valid values should pass validation");
    }

    @Test
    @DisplayName("Edge case: CreateServiceRequest with maximum valid values should pass")
    void createServiceRequest_WithMaximumValidValues_ShouldPassValidation() {
        // Arrange
        CreateServiceRequest maxRequest = CreateServiceRequest.builder()
                .name("A".repeat(100)) // Maximum 100 characters
                .description("B".repeat(1000)) // Maximum 1000 characters
                .basePrice(new BigDecimal("9999999999.99")) // Maximum 10 integer digits, 2 decimal places
                .build();

        // Act
        Set<ConstraintViolation<CreateServiceRequest>> violations = validator.validate(maxRequest);

        // Assert
        assertTrue(violations.isEmpty(), "Maximum valid values should pass validation");
    }
}
