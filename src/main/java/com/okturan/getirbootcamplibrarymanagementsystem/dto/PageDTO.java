package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A DTO for representing paginated data with a stable JSON structure.
 * This addresses the warning: "Serializing PageImpl instances as-is is not supported"
 * by providing a custom serialization structure for Page objects.
 *
 * @param <T> the type of elements in the page
 */
public record PageDTO<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last,
    boolean empty) {

    /**
     * Creates a PageDTO from a Spring Data Page.
     *
     * @param page the Spring Data Page
     * @param <T> the type of elements in the page
     * @return a PageDTO with the same data as the Page
     */
    public static <T> PageDTO<T> from(Page<T> page) {
        return new PageDTO<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
