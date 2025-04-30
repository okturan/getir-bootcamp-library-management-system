package com.okturan.getirbootcamplibrarymanagementsystem.controller;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Book Management", description = "Operations for managing books in the library system")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
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
        BookResponseDTO createdBook = bookService.createBook(bookRequestDTO);
        return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
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
        BookResponseDTO book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
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
        BookResponseDTO book = bookService.getBookByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Returns a list of all books in the library")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        List<BookResponseDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    @PutMapping("/{id}")
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
        BookResponseDTO updatedBook = bookService.updateBook(id, bookRequestDTO);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Deletes a book by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/author")
    @Operation(summary = "Find books by author", description = "Returns a list of books by a specific author")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAuthor(
            @Parameter(description = "Author name to search for", required = true)
            @RequestParam String author) {
        List<BookResponseDTO> books = bookService.findBooksByAuthor(author);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/title")
    @Operation(summary = "Find books by title", description = "Returns a list of books containing the specified title")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByTitle(
            @Parameter(description = "Title to search for", required = true)
            @RequestParam String title) {
        List<BookResponseDTO> books = bookService.findBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/genre")
    @Operation(summary = "Find books by genre", description = "Returns a list of books in a specific genre")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByGenre(
            @Parameter(description = "Genre to search for", required = true)
            @RequestParam String genre) {
        List<BookResponseDTO> books = bookService.findBooksByGenre(genre);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/available")
    @Operation(summary = "Find books by availability", description = "Returns a list of books based on their availability status")
    @ApiResponse(responseCode = "200", description = "List of books retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = BookResponseDTO.class)))
    public ResponseEntity<List<BookResponseDTO>> findBooksByAvailability(
            @Parameter(description = "Availability status to filter by", required = true)
            @RequestParam boolean available) {
        List<BookResponseDTO> books = bookService.findBooksByAvailability(available);
        return ResponseEntity.ok(books);
    }
}
