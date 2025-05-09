package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "Operations for managing books in the library system")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Create a new book", description = "Creates a new book in the library system")
    @ApiResponse(responseCode = "201", description = "Book created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO createdBook = bookService.createBook(bookRequestDTO);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Returns a book by its ID")
    @ApiResponse(responseCode = "200", description = "Book found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get a book by ISBN", description = "Returns a book by its ISBN")
    @ApiResponse(responseCode = "200", description = "Book found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<BookResponseDTO> getBookByIsbn(@PathVariable String isbn) {
        BookResponseDTO book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a list of all books in the library")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        List<BookResponseDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update a book", description = "Updates an existing book by its ID")
    @ApiResponse(responseCode = "200", description = "Book updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Book not found")
    @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        BookResponseDTO updatedBook = bookService.updateBook(id, bookRequestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Delete a book", description = "Deletes a book by its ID")
    @ApiResponse(responseCode = "204", description = "Book deleted successfully")
    @ApiResponse(responseCode = "404", description = "Book not found")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by any combination of filters")
    public List<BookResponseDTO> searchBooks(@ModelAttribute BookSearchFilterDTO filter) {
        return bookService.search(filter);
    }
    
    @GetMapping(path = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time book availability updates", description = "Returns a stream of Server-Sent Events with real-time book availability updates")
    @ApiResponse(responseCode = "200", description = "Stream of book availability updates", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = BookAvailabilityDTO.class)))
    public Flux<ServerSentEvent<BookAvailabilityDTO>> streamBookAvailability() {
        log.info("Client connected to book availability stream");
        return bookService.streamBookAvailabilityUpdates()
                .map(availabilityDTO -> ServerSentEvent.<BookAvailabilityDTO>builder()
                        .id(String.valueOf(availabilityDTO.id()))
                        .event("book-availability-update")
                        .data(availabilityDTO)
                        .build())
                .doOnCancel(() -> log.info("Client disconnected from book availability stream"));
    }

}
