package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for returning a user's borrowing history.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingHistoryDTO {

    private Long userId;
    private String username;
    private List<BorrowingResponseDTO> borrowings;
    private int totalBorrowings;
    private int currentBorrowings;
    private int overdueBorrowings;
}