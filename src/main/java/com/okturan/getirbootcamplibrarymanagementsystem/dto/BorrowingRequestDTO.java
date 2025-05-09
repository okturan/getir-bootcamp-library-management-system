package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record BorrowingRequestDTO(
		@NotNull(message = "Book ID is required") @Positive(message = "Book ID must be positive") Long bookId,

		@NotNull(message = "Due date is required") LocalDate dueDate,

		Long userId) {
}
