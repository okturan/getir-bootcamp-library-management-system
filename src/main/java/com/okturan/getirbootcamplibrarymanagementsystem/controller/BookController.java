package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Book created successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    })
    public ResponseEntity<BookResponseDTO> createBook(
            @Parameter(description = "Book data to create", required = true)
            @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        logger.info("Creating new book with title: '{}', ISBN: {}", bookRequestDTO.getTitle(), bookRequestDTO.getIsbn());
        try {
            BookResponseDTO createdBook = bookService.createBook(bookRequestDTO);
            logger.info("Book created successfully with ID: {}, title: '{}'", createdBook.getId(), createdBook.getTitle());
            return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Failed to create book with ISBN: {}", bookRequestDTO.getIsbn(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID", description = "Returns a book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponseDTO> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true)
            @PathVariable Long id) {
        logger.info("Retrieving book with ID: {}", id);
        try {
            BookResponseDTO book = bookService.getBookById(id);
            logger.debug("Book found: ID: {}, title: '{}'", book.getId(), book.getTitle());
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            logger.error("Failed to retrieve book with ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get a book by ISBN", description = "Returns a book by its ISBN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponseDTO> getBookByIsbn(
            @Parameter(description = "ISBN of the book to retrieve", required = true)
            @PathVariable String isbn) {
        logger.info("Retrieving book with ISBN: {}", isbn);
        try {
            BookResponseDTO book = bookService.getBookByIsbn(isbn);
            logger.debug("Book found: ID: {}, title: '{}', ISBN: {}", book.getId(), book.getTitle(), book.getIsbn());
            return ResponseEntity.ok(book);
        } catch (Exception e) {
            logger.error("Failed to retrieve book with ISBN: {}", isbn, e);
            throw e;
        }
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a list of all books in the library")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        logger.info("Retrieving all books");
        try {
            List<BookResponseDTO> books = bookService.getAllBooks();
            logger.debug("Retrieved {} books", books.size());
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Failed to retrieve all books", e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update a book", description = "Updates an existing book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Book updated successfully",
                content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = BookResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Book not found"),
        @ApiResponse(responseCode = "409", description = "Book with the same ISBN already exists")
    })
    public ResponseEntity<BookResponseDTO> updateBook(
            @Parameter(description = "ID of the book to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated book data", required = true)
            @Valid @RequestBody BookRequestDTO bookRequestDTO) {
        logger.info("Updating book with ID: {}, new title: '{}'", id, bookRequestDTO.getTitle());
        try {
            BookResponseDTO updatedBook = bookService.updateBook(id, bookRequestDTO);
            logger.info("Book updated successfully: ID: {}, title: '{}'", updatedBook.getId(), updatedBook.getTitle());
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            logger.error("Failed to update book with ID: {}", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Delete a book", description = "Deletes a book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathVariable Long id) {
        logger.info("Deleting book with ID: {}", id);
        try {
            bookService.deleteBook(id);
            logger.info("Book deleted successfully: ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete book with ID: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/search/author")
    @Operation(summary = "Find books by author", description = "Returns a list of books by a specific author")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAuthor(
            @Parameter(description = "Author name to search for", required = true)
            @RequestParam String author) {
        logger.info("Searching for books by author: '{}'", author);
        try {
            List<BookResponseDTO> books = bookService.findBooksByAuthor(author);
            logger.debug("Found {} books by author: '{}'", books.size(), author);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Failed to search books by author: '{}'", author, e);
            throw e;
        }
    }

    @GetMapping("/search/title")
    @Operation(summary = "Find books by title", description = "Returns a list of books containing the specified title")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByTitle(
            @Parameter(description = "Title to search for", required = true)
            @RequestParam String title) {
        logger.info("Searching for books by title: '{}'", title);
        try {
            List<BookResponseDTO> books = bookService.findBooksByTitle(title);
            logger.debug("Found {} books with title containing: '{}'", books.size(), title);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Failed to search books by title: '{}'", title, e);
            throw e;
        }
    }

    @GetMapping("/search/genre")
    @Operation(summary = "Find books by genre", description = "Returns a list of books in a specific genre")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByGenre(
            @Parameter(description = "Genre to search for", required = true)
            @RequestParam String genre) {
        logger.info("Searching for books by genre: '{}'", genre);
        try {
            List<BookResponseDTO> books = bookService.findBooksByGenre(genre);
            logger.debug("Found {} books in genre: '{}'", books.size(), genre);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Failed to search books by genre: '{}'", genre, e);
            throw e;
        }
    }

    @GetMapping("/search/available")
    @Operation(summary = "Find books by availability", description = "Returns a list of books based on their availability status")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAvailability(
            @Parameter(description = "Availability status to filter by", required = true)
            @RequestParam boolean available) {
        logger.info("Searching for books by availability: {}", available);
        try {
            List<BookResponseDTO> books = bookService.findBooksByAvailability(available);
            logger.debug("Found {} books with availability status: {}", books.size(), available);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            logger.error("Failed to search books by availability: {}", available, e);
            throw e;
        }
    }

    @GetMapping(path = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream real-time book availability updates", 
               description = "Returns a stream of Server-Sent Events with real-time book availability updates")
    @ApiResponse(responseCode = "200", description = "Stream of book availability updates",
                 content = @Content(mediaType = MediaType.TEXT_EVENT_STREAM_VALUE, 
                 schema = @Schema(implementation = BookAvailabilityDTO.class)))
    public Flux<ServerSentEvent<BookAvailabilityDTO>> streamBookAvailability() {
        logger.info("Client connected to book availability stream");
        return bookService.streamBookAvailabilityUpdates()
                .map(availabilityDTO -> ServerSentEvent.<BookAvailabilityDTO>builder()
                        .id(String.valueOf(availabilityDTO.getId()))
                        .event("book-availability-update")
                        .data(availabilityDTO)
                        .build())
                .doOnCancel(() -> logger.info("Client disconnected from book availability stream"));
    }

    // Admin-only endpoints
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get library statistics", description = "Returns statistics about the library (Admin only)")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<String> getLibraryStats() {
        logger.info("Admin retrieving library statistics");
        try {
            // This is a placeholder for actual statistics logic
            // In a real implementation, this would call a service method to get statistics
            String stats = "Total books: " + bookService.getAllBooks().size();
            logger.debug("Library statistics retrieved successfully");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to retrieve library statistics", e);
            throw e;
        }
    }
}
