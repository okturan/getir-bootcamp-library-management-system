package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Book availability information for real-time updates")
public class BookAvailabilityDTO {

    @Schema(description = "Unique identifier of the book", example = "1")
    private Long id;

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    private String isbn;

    @Schema(description = "Availability status of the book", example = "true")
    private boolean available;

    @Schema(description = "Timestamp of the availability update", example = "2023-05-15T14:30:45.123Z")
    private String timestamp;
}