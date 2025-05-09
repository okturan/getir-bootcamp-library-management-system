package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Book availability information for real-time updates")
public record BookAvailabilityDTO(
        @Schema(description = "Unique identifier of the book", example = "1")
        Long id,

        @Schema(description = "Title of the book", example = "The Great Gatsby")
        String title,

        @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
        String isbn,

        @Schema(description = "Availability status of the book", example = "true")
        boolean available,

        @Schema(description = "Timestamp of the availability update", example = "2023-05-15T14:30:45.123Z")
        String timestamp
) {
}
