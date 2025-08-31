package com.fix4home.fix4home.service;

import com.fix4home.fix4home.model.dto.common.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressApiService extends BaseService {

    private final RestTemplate restTemplate;

    @Value("${vietnam.address.api.url}")
    private String apiBaseUrl;

    @Value("${vietnam.address.api.timeout:5000}")
    private int timeoutMs;

    // ==================== PROVINCES ====================

    /**
     * Get all provinces
     */
    @Cacheable(value = "provinces", key = "'all'")
    public List<ProvinceDTO> getAllProvinces() {
        try {
            log.info("Fetching all provinces from Vietnam Address API");
            
            String url = apiBaseUrl + "/provinces";
            ResponseEntity<AddressApiResponse<List<ProvinceDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<List<ProvinceDTO>>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Successfully fetched {} provinces", response.getBody().getData().size());
                return response.getBody().getData();
            } else {
                log.warn("API returned unsuccessful response");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching provinces from API: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Search provinces by keyword
     */
    public List<ProvinceDTO> searchProvinces(String keyword, Integer limit) {
        try {
            log.info("Searching provinces with keyword: {}", keyword);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/provinces");
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                builder.queryParam("search", keyword.trim());
            }
            if (limit != null && limit > 0) {
                builder.queryParam("limit", limit);
            }

            String url = builder.toUriString();
            ResponseEntity<AddressApiResponse<List<ProvinceDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<List<ProvinceDTO>>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Found {} provinces matching keyword: {}", response.getBody().getData().size(), keyword);
                return response.getBody().getData();
            } else {
                log.warn("API returned unsuccessful response for search: {}", keyword);
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error searching provinces with keyword '{}': {}", keyword, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get province by code
     */
    @Cacheable(value = "provinces", key = "#provinceCode")
    public ProvinceDTO getProvinceByCode(String provinceCode) {
        try {
            log.info("Fetching province by code: {}", provinceCode);
            
            String url = apiBaseUrl + "/provinces/" + provinceCode;
            ResponseEntity<AddressApiResponse<ProvinceDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<ProvinceDTO>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Successfully fetched province: {}", response.getBody().getData().getName());
                return response.getBody().getData();
            } else {
                log.warn("Province not found for code: {}", provinceCode);
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching province by code '{}': {}", provinceCode, e.getMessage(), e);
            return null;
        }
    }

    // ==================== WARDS ====================

    /**
     * Get wards by province code
     */
    public List<WardDTO> getWardsByProvince(String provinceCode, String keyword, Integer limit) {
        try {
            log.info("Fetching wards for province: {}", provinceCode);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/provinces/" + provinceCode + "/wards");
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                builder.queryParam("search", keyword.trim());
            }
            if (limit != null && limit > 0) {
                builder.queryParam("limit", limit);
            }

            String url = builder.toUriString();
            ResponseEntity<AddressApiResponse<List<WardDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<List<WardDTO>>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Found {} wards for province: {}", response.getBody().getData().size(), provinceCode);
                return response.getBody().getData();
            } else {
                log.warn("API returned unsuccessful response for province: {}", provinceCode);
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error fetching wards for province '{}': {}", provinceCode, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get ward by code
     */
    @Cacheable(value = "wards", key = "#wardCode")
    public WardDTO getWardByCode(String wardCode) {
        try {
            log.info("Fetching ward by code: {}", wardCode);
            
            String url = apiBaseUrl + "/wards/" + wardCode;
            ResponseEntity<AddressApiResponse<WardDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<WardDTO>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Successfully fetched ward: {}", response.getBody().getData().getName());
                return response.getBody().getData();
            } else {
                log.warn("Ward not found for code: {}", wardCode);
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching ward by code '{}': {}", wardCode, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Search wards globally
     */
    public List<WardDTO> searchWards(String keyword, String provinceCode, Integer limit) {
        try {
            log.info("Searching wards with keyword: {} in province: {}", keyword, provinceCode);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/wards");
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                builder.queryParam("search", keyword.trim());
            }
            if (provinceCode != null && !provinceCode.trim().isEmpty()) {
                builder.queryParam("province_code", provinceCode.trim());
            }
            if (limit != null && limit > 0) {
                builder.queryParam("limit", limit);
            }

            String url = builder.toUriString();
            ResponseEntity<AddressApiResponse<List<WardDTO>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<List<WardDTO>>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Found {} wards matching search criteria", response.getBody().getData().size());
                return response.getBody().getData();
            } else {
                log.warn("API returned unsuccessful response for ward search");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error searching wards: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Validate address using province and ward codes
     */
    public AddressValidationResult validateAddress(String provinceCode, String wardCode) {
        try {
            log.info("Validating address with province: {} and ward: {}", provinceCode, wardCode);
            
            // Validate ward exists and belongs to province
            WardDTO ward = getWardByCode(wardCode);
            if (ward == null) {
                return AddressValidationResult.invalid("Ward not found with code: " + wardCode);
            }

            // Check if ward belongs to the specified province
            if (ward.getProvince() != null && !provinceCode.equals(ward.getProvince().getCode())) {
                return AddressValidationResult.invalid(
                        String.format("Ward %s does not belong to province %s", wardCode, provinceCode));
            }

            log.info("Address validation successful for province: {} and ward: {}", provinceCode, wardCode);
            return AddressValidationResult.valid(ward);
            
        } catch (Exception e) {
            log.error("Error validating address: {}", e.getMessage(), e);
            return AddressValidationResult.invalid("Address validation failed: " + e.getMessage());
        }
    }

    // ==================== SEARCH ====================

    /**
     * Global search for provinces and wards
     */
    public List<Object> globalSearch(String keyword, Integer limit) {
        try {
            log.info("Performing global address search with keyword: {}", keyword);
            
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiBaseUrl + "/search");
            builder.queryParam("q", keyword);
            
            if (limit != null && limit > 0) {
                builder.queryParam("limit", limit);
            }

            String url = builder.toUriString();
            ResponseEntity<AddressApiResponse<Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<Object>>() {}
            );

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess())) {
                log.info("Global search completed successfully");
                return List.of(response.getBody().getData());
            } else {
                log.warn("API returned unsuccessful response for global search");
                return List.of();
            }
        } catch (Exception e) {
            log.error("Error performing global search: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Check if the API is healthy
     */
    public boolean isApiHealthy() {
        try {
            String url = apiBaseUrl + "/health";
            ResponseEntity<AddressApiResponse<Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<AddressApiResponse<Object>>() {}
            );

            return response.getBody() != null && Boolean.TRUE.equals(response.getBody().getSuccess());
        } catch (Exception e) {
            log.warn("Address API health check failed: {}", e.getMessage());
            return false;
        }
    }
} 