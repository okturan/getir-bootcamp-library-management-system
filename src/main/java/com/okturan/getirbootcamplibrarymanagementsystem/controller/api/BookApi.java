package com.okturan.getirbootcamplibrarymanagementsystem.controller.api;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

@Tag(name = "Book Management", description = "Operations for managing books in the library system")
public interface BookApi {

	@Operation(summary = "Create a new book", description = "Creates a new book in the library system")
	@ApiResponse(responseCode = "201", description = "Book created successfully",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = BookResponseDTO.class)))
	@ApiResponse(responseCode = "400", description = "Invalid input data")
	@ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
	ResponseEntity<BookResponseDTO> createBook(BookRequestDTO bookRequestDTO);

	@Operation(summary = "Get a book by ID", description = "Returns a book by its ID")
	@ApiResponse(responseCode = "200", description = "Book found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = BookResponseDTO.class)))
	@ApiResponse(responseCode = "404", description = "Book not found")
	ResponseEntity<BookResponseDTO> getBookById(Long id);

	@Operation(summary = "Get a book by ISBN", description = "Returns a book by its ISBN")
	@ApiResponse(responseCode = "200", description = "Book found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = BookResponseDTO.class)))
	@ApiResponse(responseCode = "404", description = "Book not found")
	ResponseEntity<BookResponseDTO> getBookByIsbn(String isbn);

	@Operation(summary = "Get all books", description = "Returns a list of all books in the library")
	@ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = BookResponseDTO.class)))
	ResponseEntity<PageDTO<BookResponseDTO>> getAllBooks(Pageable pageable);

	@Operation(summary = "Search books by any combination of filters")
	ResponseEntity<PageDTO<BookResponseDTO>> searchBooks(BookSearchFilterDTO filter, Pageable pageable);

	@Operation(summary = "Update a book", description = "Updates an existing book by its ID")
	@ApiResponse(responseCode = "200", description = "Book updated successfully",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = BookResponseDTO.class)))
	@ApiResponse(responseCode = "400", description = "Invalid input data")
	@ApiResponse(responseCode = "404", description = "Book not found")
	@ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
	ResponseEntity<BookResponseDTO> updateBook(Long id, BookRequestDTO bookRequestDTO);

	@Operation(summary = "Delete a book", description = "Deletes a book by its ID")
	@ApiResponse(responseCode = "204", description = "Book deleted successfully")
	@ApiResponse(responseCode = "404", description = "Book not found")
	ResponseEntity<Void> deleteBook(Long id);

	@Operation(summary = "Stream real-time book availability updates",
			description = "Returns a stream of Server-Sent Events with real-time book availability updates")
	@ApiResponse(responseCode = "200", description = "Stream of book availability updates",
			content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
					schema = @Schema(implementation = BookAvailabilityDTO.class)))
	Flux<ServerSentEvent<BookAvailabilityDTO>> streamBookAvailability();

}
