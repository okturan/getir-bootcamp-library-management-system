package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.ISBN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating or updating a book")
public class BookRequestDTO {

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author name cannot exceed 255 characters")
    private String author;

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    @NotBlank(message = "ISBN is required")
    @ISBN(message = "ISBN is invalid")
    private String isbn;

    @Schema(description = "Publication date of the book", example = "2020-01-01")
    @NotNull(message = "Publication date is required")
    private LocalDate publicationDate;

    @Schema(description = "Genre of the book", example = "Fiction")
    @NotBlank(message = "Genre is required")
    @Size(max = 100, message = "Genre cannot exceed 100 characters")
    private String genre;

    @Schema(description = "Availability status of the book", example = "true", defaultValue = "true")
    private boolean available = true;
}
