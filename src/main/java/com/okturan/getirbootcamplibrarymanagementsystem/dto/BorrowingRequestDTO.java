package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

@Schema(description = "Request object for borrowing a book")
public record BorrowingRequestDTO(
		@Schema(description = "ID of the book to borrow", example = "1", required = true)
		@NotNull(message = "Book ID is required")
		@Positive(message = "Book ID must be positive")
		Long bookId,

		@Schema(description = "ID of the user who will borrow the book. Optional for patrons (who borrow for themselves), but required for admins and librarians (who must specify which patron they're borrowing for).", example = "5", required = false)
		Long userId) {
}
