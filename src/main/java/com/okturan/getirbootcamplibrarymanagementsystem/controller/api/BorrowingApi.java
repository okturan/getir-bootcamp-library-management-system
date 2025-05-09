package com.okturan.getirbootcamplibrarymanagementsystem.controller.api;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Borrowing", description = "Borrowing management APIs")
public interface BorrowingApi {

    @Operation(
            summary = "Borrow a book",
            description = "Borrow a book for the current user"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Book borrowed successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowingResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Book not found"
    )
    @ApiResponse(
            responseCode = "409",
            description = "Book not available"
    )
    ResponseEntity<BorrowingResponseDTO> borrowBook(BorrowingRequestDTO borrowingRequestDTO);

    @Operation(
            summary = "Return a book",
            description = "Return a borrowed book"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Book returned successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowingResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Borrowing not found"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Book already returned"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to return this book"
    )
    ResponseEntity<BorrowingResponseDTO> returnBook(Long borrowingId);

    @Operation(
            summary = "Get borrowing by ID",
            description = "Get a borrowing by its ID"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Borrowing found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowingResponseDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Borrowing not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to view this borrowing"
    )
    ResponseEntity<BorrowingResponseDTO> getBorrowingById(Long borrowingId);

    @Operation(
            summary = "Get current user's borrowing history",
            description = "Get borrowing history for the current user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Borrowing history retrieved",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowingHistoryDTO.class)
            )
    )
    ResponseEntity<BorrowingHistoryDTO> getCurrentUserBorrowingHistory();

    @Operation(
            summary = "Get user's borrowing history",
            description = "Get borrowing history for a specific user (librarians and admins only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Borrowing history retrieved",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BorrowingHistoryDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to view this user's borrowing history"
    )
    ResponseEntity<BorrowingHistoryDTO> getUserBorrowingHistory(Long userId);

    @Operation(
            summary = "Get all active borrowings",
            description = "Get all active borrowings (librarians and admins only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Active borrowings retrieved"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to view active borrowings"
    )
    ResponseEntity<List<BorrowingResponseDTO>> getAllActiveBorrowings();

    @Operation(
            summary = "Get all overdue borrowings",
            description = "Get all overdue borrowings (librarians and admins only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Overdue borrowings retrieved"
    )
    @ApiResponse(
            responseCode = "403",
            description = "Not authorized to view overdue borrowings"
    )
    ResponseEntity<List<BorrowingResponseDTO>> getAllOverdueBorrowings();
}
