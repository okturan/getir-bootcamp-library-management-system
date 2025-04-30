package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing book information")
public class BookResponseDTO {

    @Schema(description = "Unique identifier of the book", example = "1")
    private Long id;

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    private String title;

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    private String author;

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    private String isbn;

    @Schema(description = "Publication date of the book", example = "2020-01-01")
    private LocalDate publicationDate;

    @Schema(description = "Genre of the book", example = "Fiction")
    private String genre;

    @Schema(description = "Availability status of the book", example = "true")
    private boolean available;
}
