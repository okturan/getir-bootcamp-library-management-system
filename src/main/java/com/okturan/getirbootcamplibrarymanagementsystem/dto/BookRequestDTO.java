package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import org.hibernate.validator.constraints.ISBN;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating or updating a book")
public record BookRequestDTO(
    @Schema(description = "Title of the book", example = "The Great Gatsby")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    String author,

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    @NotBlank(message = "ISBN is required")
    @ISBN(message = "ISBN is invalid")
    String isbn,

    @Schema(description = "Publication date of the book", example = "2020-01-01")
    @NotNull(message = "Publication date is required")
    LocalDate publicationDate,

    @Schema(description = "Genre of the book", example = "Fiction")
    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    String genre
) {
}
