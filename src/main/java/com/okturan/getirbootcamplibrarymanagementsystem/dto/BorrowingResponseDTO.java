package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

/**
 * DTO for returning borrowing information.
 */
public record BorrowingResponseDTO(
    Long id,
    Long bookId,
    String bookTitle,
    String bookIsbn,
    Long userId,
    String username,
    LocalDate borrowDate,
    LocalDate dueDate,
    LocalDate returnDate,
    boolean returned,
    boolean overdue
) {}
