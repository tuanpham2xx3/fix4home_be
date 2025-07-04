package com.fix4home.fix4home.service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;

/**
 * Interface for DTO conversion patterns
 * Provides standardized methods for converting entities to DTOs
 * 
 * @param <E> Entity type
 * @param <D> DTO type
 */
public interface DTOConverter<E, D> {

    /**
     * Convert single entity to DTO
     */
    D convertToDTO(E entity);

    /**
     * Convert list of entities to list of DTOs
     */
    default List<D> convertToDTO(List<E> entities) {
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert page of entities to page of DTOs
     */
    default Page<D> convertToDTO(Page<E> entityPage) {
        return entityPage.map(this::convertToDTO);
    }
} 