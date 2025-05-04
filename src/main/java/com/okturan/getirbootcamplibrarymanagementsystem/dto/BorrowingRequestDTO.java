package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO for requesting to borrow a book.
 * If userId is provided, the book will be borrowed for that user (admin/librarian only).
 * If userId is not provided, the book will be borrowed for the current user.
 */
public record BorrowingRequestDTO(
    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    Long bookId,

    @NotNull(message = "Due date is required")
    LocalDate dueDate,

    /**
     * Optional user ID for whom the book is being borrowed.
     * Only used by admins and librarians to borrow books for other users.
     */
    Long userId
) {}
