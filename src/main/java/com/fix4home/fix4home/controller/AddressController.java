package com.fix4home.fix4home.controller;

import com.fix4home.fix4home.model.dto.common.*;
import com.fix4home.fix4home.service.AddressApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Address Helper", description = "Vietnam Administrative Address Helper APIs")
public class AddressController {

    private final AddressApiService addressApiService;

    // ==================== PROVINCES ====================

    @GetMapping("/provinces")
    @Operation(summary = "Get all provinces", description = "Get list of all provinces in Vietnam")
    public ResponseEntity<ApiResponse<List<ProvinceDTO>>> getAllProvinces() {
        log.info("GET /api/v1/addresses/provinces - Get all provinces");
        List<ProvinceDTO> provinces = addressApiService.getAllProvinces();
        return ResponseEntity.ok(ApiResponse.success("Provinces retrieved successfully", provinces));
    }

    @GetMapping("/provinces/search")
    @Operation(summary = "Search provinces", description = "Search provinces by keyword")
    public ResponseEntity<ApiResponse<List<ProvinceDTO>>> searchProvinces(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "20") Integer limit) {
        log.info("GET /api/v1/addresses/provinces/search - Search provinces with keyword: {}", keyword);
        List<ProvinceDTO> provinces = addressApiService.searchProvinces(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success("Province search completed successfully", provinces));
    }

    @GetMapping("/provinces/{code}")
    @Operation(summary = "Get province by code", description = "Get province details by province code")
    public ResponseEntity<ApiResponse<ProvinceDTO>> getProvinceByCode(
            @Parameter(description = "Province code") @PathVariable String code) {
        log.info("GET /api/v1/addresses/provinces/{} - Get province by code", code);
        ProvinceDTO province = addressApiService.getProvinceByCode(code);
        
        if (province != null) {
            return ResponseEntity.ok(ApiResponse.success("Province retrieved successfully", province));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== WARDS ====================

    @GetMapping("/provinces/{provinceCode}/wards")
    @Operation(summary = "Get wards by province", description = "Get list of wards/districts for a specific province")
    public ResponseEntity<ApiResponse<List<WardDTO>>> getWardsByProvince(
            @Parameter(description = "Province code") @PathVariable String provinceCode,
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "50") Integer limit) {
        log.info("GET /api/v1/addresses/provinces/{}/wards - Get wards for province", provinceCode);
        List<WardDTO> wards = addressApiService.getWardsByProvince(provinceCode, keyword, limit);
        return ResponseEntity.ok(ApiResponse.success("Wards retrieved successfully", wards));
    }

    @GetMapping("/wards/search")
    @Operation(summary = "Search wards", description = "Search wards/districts globally or within a province")
    public ResponseEntity<ApiResponse<List<WardDTO>>> searchWards(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Province code filter") @RequestParam(required = false) String provinceCode,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "20") Integer limit) {
        log.info("GET /api/v1/addresses/wards/search - Search wards with keyword: {} in province: {}", keyword, provinceCode);
        List<WardDTO> wards = addressApiService.searchWards(keyword, provinceCode, limit);
        return ResponseEntity.ok(ApiResponse.success("Ward search completed successfully", wards));
    }

    @GetMapping("/wards/{code}")
    @Operation(summary = "Get ward by code", description = "Get ward details by ward code")
    public ResponseEntity<ApiResponse<WardDTO>> getWardByCode(
            @Parameter(description = "Ward code") @PathVariable String code) {
        log.info("GET /api/v1/addresses/wards/{} - Get ward by code", code);
        WardDTO ward = addressApiService.getWardByCode(code);
        
        if (ward != null) {
            return ResponseEntity.ok(ApiResponse.success("Ward retrieved successfully", ward));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== VALIDATION ====================

    @PostMapping("/validate")
    @Operation(summary = "Validate address", description = "Validate address using province and ward codes")
    public ResponseEntity<ApiResponse<AddressValidationResult>> validateAddress(
            @Valid @RequestBody AddressValidationRequest request) {
        log.info("POST /api/v1/addresses/validate - Validate address with province: {} and ward: {}", 
                request.getProvinceCode(), request.getWardCode());
        
        AddressValidationResult result = addressApiService.validateAddress(
                request.getProvinceCode(), request.getWardCode());
        
        if (result.getValid()) {
            return ResponseEntity.ok(ApiResponse.success("Address is valid", result));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(result.getMessage()));
        }
    }

    // ==================== SEARCH ====================

    @GetMapping("/search")
    @Operation(summary = "Global address search", description = "Search both provinces and wards with a keyword")
    public ResponseEntity<ApiResponse<List<Object>>> globalSearch(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "20") Integer limit) {
        log.info("GET /api/v1/addresses/search - Global search with keyword: {}", keyword);
        List<Object> results = addressApiService.globalSearch(keyword, limit);
        return ResponseEntity.ok(ApiResponse.success("Global search completed successfully", results));
    }

    // ==================== UTILITY ====================

    @GetMapping("/health")
    @Operation(summary = "Check API health", description = "Check if Vietnam Administrative API is healthy")
    public ResponseEntity<ApiResponse<Boolean>> checkApiHealth() {
        log.info("GET /api/v1/addresses/health - Check API health");
        boolean isHealthy = addressApiService.isApiHealthy();
        
        if (isHealthy) {
            return ResponseEntity.ok(ApiResponse.success("Address API is healthy", true));
        } else {
            return ResponseEntity.status(503)
                    .body(ApiResponse.error("Address API is not available"));
        }
    }

    // ==================== HELPER ENDPOINTS ====================

    @GetMapping("/provinces/types")
    @Operation(summary = "Get province types", description = "Get list of province types (tỉnh, thành phố)")
    public ResponseEntity<ApiResponse<List<String>>> getProvinceTypes() {
        log.info("GET /api/v1/addresses/provinces/types - Get province types");
        List<String> types = List.of("tinh", "thanh-pho");
        return ResponseEntity.ok(ApiResponse.success("Province types retrieved successfully", types));
    }

    @GetMapping("/wards/types")
    @Operation(summary = "Get ward types", description = "Get list of ward types (phường, xã, thị trấn)")
    public ResponseEntity<ApiResponse<List<String>>> getWardTypes() {
        log.info("GET /api/v1/addresses/wards/types - Get ward types");
        List<String> types = List.of("phuong", "xa", "thi-tran", "dac-khu");
        return ResponseEntity.ok(ApiResponse.success("Ward types retrieved successfully", types));
    }

    // ==================== CONVENIENCE METHODS ====================

    @GetMapping("/provinces/major-cities")
    @Operation(summary = "Get major cities", description = "Get list of major cities (thành phố)")
    public ResponseEntity<ApiResponse<List<ProvinceDTO>>> getMajorCities() {
        log.info("GET /api/v1/addresses/provinces/major-cities - Get major cities");
        List<ProvinceDTO> provinces = addressApiService.getAllProvinces();
        List<ProvinceDTO> cities = provinces.stream()
                .filter(p -> "thanh-pho".equals(p.getType()))
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Major cities retrieved successfully", cities));
    }

    @GetMapping("/quick-search")
    @Operation(summary = "Quick address search", description = "Quick search for addresses with minimal parameters")
    public ResponseEntity<ApiResponse<Object>> quickSearch(
            @Parameter(description = "Search keyword") @RequestParam String q) {
        log.info("GET /api/v1/addresses/quick-search - Quick search with keyword: {}", q);
        
        // Search both provinces and wards
        List<ProvinceDTO> provinces = addressApiService.searchProvinces(q, 5);
        List<WardDTO> wards = addressApiService.searchWards(q, null, 10);
        
        QuickSearchResult result = QuickSearchResult.builder()
                .provinces(provinces)
                .wards(wards)
                .keyword(q)
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Quick search completed successfully", result));
    }

    // Quick search result DTO
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QuickSearchResult {
        private List<ProvinceDTO> provinces;
        private List<WardDTO> wards;
        private String keyword;
    }
} 