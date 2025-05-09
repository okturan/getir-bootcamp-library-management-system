package com.okturan.getirbootcamplibrarymanagementsystem.dto;

public record BorrowingHistoryDTO(
		Long userId, String username,
		PageDTO<BorrowingResponseDTO> borrowingsPage, // Changed from List to PageDTO
		int totalBorrowings, // These stats should reflect ALL user borrowings
		int currentBorrowings,
		int overdueBorrowings
) {}