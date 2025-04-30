package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for requesting to borrow a book.
 * If userId is provided, the book will be borrowed for that user (admin/librarian only).
 * If userId is not provided, the book will be borrowed for the current user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRequestDTO {

    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    private Long bookId;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    /**
     * Optional user ID for whom the book is being borrowed.
     * Only used by admins and librarians to borrow books for other users.
     */
    private Long userId;
}
