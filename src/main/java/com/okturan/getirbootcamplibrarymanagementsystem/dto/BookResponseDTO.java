package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing book information")
public record BookResponseDTO(
    @Schema(description = "Unique identifier of the book", example = "1")
    Long id,

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    String title,

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    String author,

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    String isbn,

    @Schema(description = "Publication date of the book", example = "2020-01-01")
    LocalDate publicationDate,

    @Schema(description = "Genre of the book", example = "Fiction")
    String genre,

    @Schema(description = "Availability status of the book", example = "true")
    boolean available
) {}
