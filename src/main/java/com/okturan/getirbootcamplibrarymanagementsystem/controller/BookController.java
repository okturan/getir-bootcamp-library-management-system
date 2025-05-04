package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "Operations for managing books in the library system")
@RequiredArgsConstructor
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

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

    @GetMapping("/search/author")
    @Operation(summary = "Find books by author", description = "Returns a list of books by a specific author")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAuthor(@RequestParam String author) {
        List<BookResponseDTO> books = bookService.findBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/title")
    @Operation(summary = "Find books by title", description = "Returns a list of books containing the specified title")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByTitle(@RequestParam String title) {
        List<BookResponseDTO> books = bookService.findBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/genre")
    @Operation(summary = "Find books by genre", description = "Returns a list of books in a specific genre")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByGenre(@RequestParam String genre) {
        List<BookResponseDTO> books = bookService.findBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/available")
    @Operation(summary = "Find books by availability", description = "Returns a list of books based on their availability status")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAvailability(@RequestParam boolean available) {
        List<BookResponseDTO> books = bookService.findBooksByAvailability(available);
        return ResponseEntity.ok(books);
    }

    @GetMapping(path = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time book availability updates", description = "Returns a stream of Server-Sent Events with real-time book availability updates")
    @ApiResponse(responseCode = "200", description = "Stream of book availability updates", content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, schema = @Schema(implementation = BookAvailabilityDTO.class)))
    public Flux<ServerSentEvent<BookAvailabilityDTO>> streamBookAvailability() {
        logger.info("Client connected to book availability stream");
        return bookService.streamBookAvailabilityUpdates()
                .map(availabilityDTO -> ServerSentEvent.<BookAvailabilityDTO>builder()
                        .id(String.valueOf(availabilityDTO.id()))
                        .event("book-availability-update")
                        .data(availabilityDTO)
                        .build())
                .doOnCancel(() -> logger.info("Client disconnected from book availability stream"));
    }

}
