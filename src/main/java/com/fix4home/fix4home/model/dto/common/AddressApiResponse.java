package com.fix4home.fix4home.model.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressApiResponse<T> {
    
    private Boolean success;
    private T data;
    private String message;
    private PaginationInfo pagination;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaginationInfo {
        private Integer total;
        private Integer limit;
        private Integer offset;
        private Integer pages;
    }
} 