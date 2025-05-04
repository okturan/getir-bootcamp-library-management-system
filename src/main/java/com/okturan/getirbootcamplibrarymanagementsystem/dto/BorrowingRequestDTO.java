package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BorrowingRequestDTO(
    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    Long bookId,

    @NotNull(message = "Due date is required")
    LocalDate dueDate,

    Long userId
) {}
