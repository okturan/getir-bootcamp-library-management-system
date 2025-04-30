package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for returning borrowing information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingResponseDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long userId;
    private String username;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean returned;
    private boolean overdue;
}