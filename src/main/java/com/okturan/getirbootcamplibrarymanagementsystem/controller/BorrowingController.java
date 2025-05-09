package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.controller.api.BorrowingApi;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

	@GetMapping("/history")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BorrowingHistoryDTO> getCurrentUserBorrowingHistory(
			@ParameterObject @PageableDefault(page = 0, sort = "borrowDate", direction = Direction.DESC, size = 10) Pageable pageable) {
		BorrowingHistoryDTO response = borrowingService.getCurrentUserBorrowingHistory(pageable);
		return ResponseEntity.ok(response);
	}

	@Override
	@GetMapping("/users/{userId}/history")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<BorrowingHistoryDTO> getUserBorrowingHistory(
			@ParameterObject @PageableDefault(page = 0, sort = "borrowDate", direction = Direction.DESC, size = 10) Pageable pageable,
			@PathVariable Long userId) {
		BorrowingHistoryDTO response = borrowingService.getUserBorrowingHistory(userId, pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/active")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<PageDTO<BorrowingResponseDTO>> getAllActiveBorrowings(
			@ParameterObject @PageableDefault(page = 0, sort = "borrowDate", direction = Direction.DESC, size = 20) Pageable pageable) {
		Page<BorrowingResponseDTO> page = borrowingService.getAllActiveBorrowings(pageable);
		return ResponseEntity.ok(PageDTO.from(page));
	}

	@Override
	@GetMapping("/overdue")
	@PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
	public ResponseEntity<PageDTO<BorrowingResponseDTO>> getAllOverdueBorrowings(
			@ParameterObject @PageableDefault(page = 0, sort = "borrowDate", direction = Direction.DESC, size = 20) Pageable pageable) {
		Page<BorrowingResponseDTO> page = borrowingService.getAllOverdueBorrowings(pageable);
		return ResponseEntity.ok(PageDTO.from(page));
	}

}
