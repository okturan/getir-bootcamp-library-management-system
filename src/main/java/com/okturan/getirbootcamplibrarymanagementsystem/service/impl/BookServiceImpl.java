package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

	private final BookRepository bookRepository;

	private final BorrowingRepository borrowingRepository;

	private final BookMapper bookMapper;

	private final Sinks.Many<BookAvailabilityDTO> availabilitySink = Sinks.many().multicast().onBackpressureBuffer();

	/* ---------- CRUD ---------- */

	@Override
	@Transactional
	public BookResponseDTO createBook(BookRequestDTO dto) {
		if (bookRepository.existsByIsbn(dto.isbn())) {
			throw new IllegalArgumentException("Book with ISBN " + dto.isbn() + " already exists");
		}
		Book savedBook = bookRepository.save(bookMapper.mapToEntity(dto));
		log.info("Created book {} ({})", savedBook.getTitle(), savedBook.getId());
		return bookMapper.mapToDTO(savedBook);
	}

	@Override
	@Transactional(readOnly = true)
	public BookResponseDTO getBookById(Long id) {
		Book book = findByIdOrThrow(id);
		calculateAvailability(book);
		return bookMapper.mapToDTO(book);
	}

	@Override
	@Transactional(readOnly = true)
	public BookResponseDTO getBookByIsbn(String isbn) {
		Book book = bookRepository.findByIsbn(isbn)
			.orElseThrow(() -> new EntityNotFoundException("Book not found: " + isbn));
		calculateAvailability(book);
		return bookMapper.mapToDTO(book);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BookResponseDTO> getAllBooks(Pageable pageable) {
		Page<Book> booksPage = bookRepository.findAll(pageable);
		calculateBatchAvailability(booksPage.getContent()); // Essential for DTO mapping
		return booksPage.map(bookMapper::mapToDTO);
	}

	/**
	 * Calculates if a book is available based on whether it's currently borrowed out
	 * @param book the book to check
	 */
	private void calculateAvailability(Book book) {
		boolean isBorrowed = borrowingRepository.existsByBookAndReturnedFalse(book);
		book.setAvailable(!isBorrowed);
	}

	/**
	 * Optimized method to calculate availability for a list of books in a single database
	 * query
	 * @param books the list of books to check
	 */
	private void calculateBatchAvailability(List<Book> books) {
		if (books.isEmpty()) {
			return;
		}

		// Extract all book IDs
		List<Long> bookIds = books.stream().map(Book::getId).toList();

		// Get all book IDs that are currently borrowed in a single query
		Set<Long> borrowedBookIds = borrowingRepository.findBorrowedBookIdsByBookIds(bookIds);

		// Set availability flag for each book
		books.forEach(book -> book.setAvailable(!borrowedBookIds.contains(book.getId())));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BookResponseDTO> search(BookSearchFilterDTO filter, Pageable pageable) {
		Specification<Book> spec = createBookSpecification(filter); // New helper method
		Page<Book> booksPage = bookRepository.findAll(spec, pageable);
		calculateBatchAvailability(booksPage.getContent()); // Set transient field for DTO mapping
		return booksPage.map(bookMapper::mapToDTO);
	}

	private Specification<Book> createBookSpecification(BookSearchFilterDTO f) {
		return (root, query, cb) -> {
			java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
			if (f.author().isPresent() && !f.author().get().isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("author")), "%" + f.author().get().toLowerCase() + "%"));
			}
			if (f.title().isPresent() && !f.title().get().isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("title")), "%" + f.title().get().toLowerCase() + "%"));
			}
			if (f.genre().isPresent() && !f.genre().get().isBlank()) {
				predicates.add(cb.equal(cb.lower(root.get("genre")), f.genre().get().toLowerCase()));
			}

			if (f.available().isPresent()) {
				jakarta.persistence.criteria.Subquery<Long> subquery = query.subquery(Long.class);
				jakarta.persistence.criteria.Root<Borrowing> borrowingRoot = subquery.from(Borrowing.class);
				subquery.select(borrowingRoot.get("book").get("id"));
				subquery.where(
						cb.equal(borrowingRoot.get("book").get("id"), root.get("id")),
						cb.isFalse(borrowingRoot.get("returned"))
				);

				if (f.available().get()) { // We want available books (book ID NOT IN subquery)
					predicates.add(cb.not(cb.exists(subquery)));
				} else { // We want unavailable books (book ID IN subquery)
					predicates.add(cb.exists(subquery));
				}
			}
			return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
		};
	}

	@Override
	@Transactional
	public BookResponseDTO updateBook(Long id, BookRequestDTO dto) {
		Book book = findByIdOrThrow(id);

		calculateAvailability(book); // Ensure book.getAvailable() is populated
		Boolean wasAvailable = book.getAvailable(); // Now it's correctly true/false

		if (!book.getIsbn().equals(dto.isbn()) && bookRepository.existsByIsbn(dto.isbn())) {
			throw new IllegalArgumentException("Book with ISBN " + dto.isbn() + " already exists");
		}

		bookMapper.updateEntityFromDto(dto, book);
		Book updated = bookRepository.save(book);
		log.info("Updated book {} ({})", updated.getTitle(), updated.getId());

		calculateAvailability(updated); // Recalculate for the DTO and for comparison

		if (!wasAvailable.equals(updated.getAvailable())) { // Use .equals for Boolean comparison
			emitAvailabilityUpdate(updated);
		}

		return bookMapper.mapToDTO(updated);
	}

	@Override
	@Transactional
	public void deleteBook(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new EntityNotFoundException("Book not found with id: " + id);
		}
		bookRepository.deleteById(id);
		log.info("Deleted book {}", id);
	}

	/* ---------- Streaming ---------- */

	@Override
	public Flux<BookAvailabilityDTO> streamBookAvailabilityUpdates() {
		log.info("Subscribed to book availability updates");
		return availabilitySink.asFlux()
			.publishOn(Schedulers.boundedElastic())
			.doOnCancel(() -> log.info("Unsubscribed from book availability updates"));
	}

	/* ---------- Internal helpers ---------- */

	private Book findByIdOrThrow(Long id) {
		return bookRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
	}

	public void emitAvailabilityUpdate(Book book) {
		// Calculate availability based on borrowing status
		calculateAvailability(book);

		// Generate timestamp in the service layer
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

		// Pass the timestamp to the mapper
		BookAvailabilityDTO dto = bookMapper.createAvailabilityDTO(book, timestamp);
		availabilitySink.tryEmitNext(dto);
		log.info("Availability changed → emitted update for book {}", book.getId());
	}

}
