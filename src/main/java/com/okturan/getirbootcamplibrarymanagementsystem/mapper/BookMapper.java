package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper interface for converting between Book entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface BookMapper {

    /**
     * Map Book entity to BookResponseDTO
     */
    BookResponseDTO mapToDTO(Book book);

    /**
     * Map BookRequestDTO to Book entity
     */
    @Mapping(target = "id", ignore = true)
    Book mapToEntity(BookRequestDTO dto);

    /**
     * Update an existing Book entity from BookRequestDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(BookRequestDTO dto, @MappingTarget Book book);

    /**
     * Create a BookAvailabilityDTO from a Book entity with current timestamp
     */
    @Mapping(target = "timestamp", expression = "java(getCurrentTimestamp())")
    BookAvailabilityDTO createAvailabilityDTO(Book book);

    /**
     * Helper method to get current timestamp formatted as ISO date time
     */
    default String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
