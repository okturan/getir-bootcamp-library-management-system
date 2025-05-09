package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.controller.api.BookApi;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequiredArgsConstructor
@Slf4j
public class BookController implements BookApi {

	private final BookService bookService;

	@Override
	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
	public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO bookRequestDTO) {
		BookResponseDTO createdBook = bookService.createBook(bookRequestDTO);
		return new ResponseEntity<>(createdBook, HttpStatus.CREATED);
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
		BookResponseDTO book = bookService.getBookById(id);
		return ResponseEntity.ok(book);
	}

	@GetMapping("/isbn/{isbn}")
	public ResponseEntity<BookResponseDTO> getBookByIsbn(@PathVariable String isbn) {
		BookResponseDTO book = bookService.getBookByIsbn(isbn);
		return ResponseEntity.ok(book);
	}

	@GetMapping
	public ResponseEntity<PageDTO<BookResponseDTO>> getAllBooks(
			@ParameterObject @PageableDefault(sort = "title,asc", size = 20) Pageable pageable) {
		Page<BookResponseDTO> booksPage = bookService.getAllBooks(pageable);
		return ResponseEntity.ok(PageDTO.from(booksPage));
	}

	@GetMapping("/search")
	public ResponseEntity<PageDTO<BookResponseDTO>> searchBooks(
			@ModelAttribute BookSearchFilterDTO filter,
			@ParameterObject @PageableDefault(sort = "title,asc", size = 20) Pageable pageable) {
		Page<BookResponseDTO> booksPage = bookService.search(filter, pageable);
		return ResponseEntity.ok(PageDTO.from(booksPage));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
	public ResponseEntity<BookResponseDTO> updateBook(
			@PathVariable Long id,
			@Valid @RequestBody BookRequestDTO bookRequestDTO) {
		BookResponseDTO updatedBook = bookService.updateBook(id, bookRequestDTO);
		return ResponseEntity.ok(updatedBook);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
	public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
		bookService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping(path = "/availability/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
