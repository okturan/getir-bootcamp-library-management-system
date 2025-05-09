package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.controller.api.BorrowingApi;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController implements BorrowingApi {

	private final BorrowingService borrowingService;

	@Override
	@PostMapping("/borrow")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BorrowingResponseDTO> borrowBook(
			@Valid @RequestBody BorrowingRequestDTO borrowingRequestDTO) {
		BorrowingResponseDTO response = borrowingService.borrowBook(borrowingRequestDTO);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Override
	@PostMapping("/{borrowingId}/return")
	@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @borrowingService.isOwner(#borrowingId, authentication.principal.username)")
	public ResponseEntity<BorrowingResponseDTO> returnBook(@PathVariable Long borrowingId) {
		BorrowingResponseDTO response = borrowingService.returnBook(borrowingId);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/{borrowingId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN') or @borrowingService.isOwner(#borrowingId, authentication.principal.username)")
	public ResponseEntity<BorrowingResponseDTO> getBorrowingById(@PathVariable Long borrowingId) {
		BorrowingResponseDTO response = borrowingService.getBorrowingById(borrowingId);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/history")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BorrowingHistoryDTO> getCurrentUserBorrowingHistory() {
		BorrowingHistoryDTO response = borrowingService.getCurrentUserBorrowingHistory();
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/users/{userId}/history")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<BorrowingHistoryDTO> getUserBorrowingHistory(@PathVariable Long userId) {
		BorrowingHistoryDTO response = borrowingService.getUserBorrowingHistory(userId);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/active")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<List<BorrowingResponseDTO>> getAllActiveBorrowings() {
		List<BorrowingResponseDTO> response = borrowingService.getAllActiveBorrowings();
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/overdue")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<List<BorrowingResponseDTO>> getAllOverdueBorrowings() {
		List<BorrowingResponseDTO> response = borrowingService.getAllOverdueBorrowings();
		return ResponseEntity.ok(response);
	}

}
