package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowings")
@Tag(name = "Borrowing", description = "Borrowing management APIs")
@RequiredArgsConstructor
public class BorrowingController {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingController.class);

    private final BorrowingService borrowingService;

    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book", description = "Borrow a book for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book borrowed successfully",
                    content = @Content(schema = @Schema(implementation = BorrowingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "Book not available")
    })
    public ResponseEntity<BorrowingResponseDTO> borrowBook(
            @Valid @RequestBody BorrowingRequestDTO borrowingRequestDTO) {
        logger.info("Received request to borrow book with ID: {}", borrowingRequestDTO.getBookId());
        BorrowingResponseDTO response = borrowingService.borrowBook(borrowingRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{borrowingId}/return")
    @Operation(summary = "Return a book", description = "Return a borrowed book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book returned successfully",
                    content = @Content(schema = @Schema(implementation = BorrowingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Borrowing not found"),
            @ApiResponse(responseCode = "400", description = "Book already returned"),
            @ApiResponse(responseCode = "403", description = "Not authorized to return this book")
    })
    public ResponseEntity<BorrowingResponseDTO> returnBook(
            @Parameter(description = "Borrowing ID", required = true)
            @PathVariable Long borrowingId) {
        logger.info("Received request to return book with borrowing ID: {}", borrowingId);
        BorrowingResponseDTO response = borrowingService.returnBook(borrowingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{borrowingId}")
    @Operation(summary = "Get borrowing by ID", description = "Get a borrowing by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing found",
                    content = @Content(schema = @Schema(implementation = BorrowingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Borrowing not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this borrowing")
    })
    public ResponseEntity<BorrowingResponseDTO> getBorrowingById(
            @Parameter(description = "Borrowing ID", required = true)
            @PathVariable Long borrowingId) {
        logger.info("Received request to get borrowing with ID: {}", borrowingId);
        BorrowingResponseDTO response = borrowingService.getBorrowingById(borrowingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get current user's borrowing history", description = "Get borrowing history for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing history retrieved",
                    content = @Content(schema = @Schema(implementation = BorrowingHistoryDTO.class)))
    })
    public ResponseEntity<BorrowingHistoryDTO> getCurrentUserBorrowingHistory() {
        logger.info("Received request to get borrowing history for current user");
        BorrowingHistoryDTO response = borrowingService.getCurrentUserBorrowingHistory();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/history")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Get user's borrowing history", description = "Get borrowing history for a specific user (librarians and admins only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Borrowing history retrieved",
                    content = @Content(schema = @Schema(implementation = BorrowingHistoryDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this user's borrowing history")
    })
    public ResponseEntity<BorrowingHistoryDTO> getUserBorrowingHistory(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long userId) {
        logger.info("Received request to get borrowing history for user with ID: {}", userId);
        BorrowingHistoryDTO response = borrowingService.getUserBorrowingHistory(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Get all active borrowings", description = "Get all active borrowings (librarians and admins only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Active borrowings retrieved"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view active borrowings")
    })
    public ResponseEntity<List<BorrowingResponseDTO>> getAllActiveBorrowings() {
        logger.info("Received request to get all active borrowings");
        List<BorrowingResponseDTO> response = borrowingService.getAllActiveBorrowings();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    @Operation(summary = "Get all overdue borrowings", description = "Get all overdue borrowings (librarians and admins only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Overdue borrowings retrieved"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view overdue borrowings")
    })
    public ResponseEntity<List<BorrowingResponseDTO>> getAllOverdueBorrowings() {
        logger.info("Received request to get all overdue borrowings");
        List<BorrowingResponseDTO> response = borrowingService.getAllOverdueBorrowings();
        return ResponseEntity.ok(response);
    }
}