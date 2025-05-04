package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.util.List;

/**
 * DTO for returning a user's borrowing history.
 */
public record BorrowingHistoryDTO(
    Long userId,
    String username,
    List<BorrowingResponseDTO> borrowings,
    int totalBorrowings,
    int currentBorrowings,
    int overdueBorrowings
) {}
