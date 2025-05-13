package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

/**
 * DTO for returning overdue books report information.
 * This report provides a comprehensive view of all overdue books in the library,
 * including summary statistics and a paginated list of overdue borrowings.
 */
public record OverdueReportDTO(
        PageDTO<BorrowingResponseDTO> overdueBorrowings,
        int totalOverdueCount,
        int distinctUsersWithOverdueCount,
        int distinctBooksOverdueCount,
        LocalDate reportGeneratedDate
) {}