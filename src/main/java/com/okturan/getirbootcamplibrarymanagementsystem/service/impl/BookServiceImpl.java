package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final BookMapper bookMapper;
    private final Sinks.Many<BookAvailabilityDTO> availabilitySink =
            Sinks.many().multicast().onBackpressureBuffer();

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
    public List<BookResponseDTO> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        calculateBatchAvailability(books);
        return books.stream()
                .map(bookMapper::mapToDTO)
                .toList();
    }

    /**
     * Calculates if a book is available based on whether it's currently borrowed out
     *
     * @param book the book to check
     */
    private void calculateAvailability(Book book) {
        boolean isBorrowed = borrowingRepository.existsByBookAndReturnedFalse(book);
        book.setAvailable(!isBorrowed);
    }

    /**
     * Optimized method to calculate availability for a list of books in a single database query
     *
     * @param books the list of books to check
     */
    private void calculateBatchAvailability(List<Book> books) {
        if (books.isEmpty()) {
            return;
        }

        // Extract all book IDs
        List<Long> bookIds = books.stream()
                .map(Book::getId)
                .toList();

        // Get all book IDs that are currently borrowed in a single query
        Set<Long> borrowedBookIds = borrowingRepository.findBorrowedBookIdsByBookIds(bookIds);

        // Set availability flag for each book
        books.forEach(book -> book.setAvailable(!borrowedBookIds.contains(book.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> search(BookSearchFilterDTO f) {

        Specification<Book> spec = Specification.where(null);

        if (f.author().isPresent()) {
            spec = spec.and((root, q, cb) ->
                                    cb.like(cb.lower(root.get("author")), "%" + f.author().get().toLowerCase() + "%"));
        }

        if (f.title().isPresent()) {
            spec = spec.and((root, q, cb) ->
                                    cb.like(cb.lower(root.get("title")), "%" + f.title().get().toLowerCase() + "%"));
        }

        if (f.genre().isPresent()) {
            spec = spec.and((root, q, cb) ->
                                    cb.equal(root.get("genre"), f.genre().get()));
        }

        // Get all books matching the criteria
        List<Book> books = bookRepository.findAll(spec);

        // Fetch and Calculate availability for all books in a single query
        calculateBatchAvailability(books);

        // If availability filter is present, filter the books in memory
        if (f.available().isPresent()) {
            boolean availableFilter = f.available().get();
            books = books.stream()
                    .filter(book -> book.getAvailable() == availableFilter)
                    .toList();
        }

        // Map to DTOs and return
        return books.stream()
                .map(bookMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO dto) {
        Book book = findByIdOrThrow(id);

        if (!book.getIsbn().equals(dto.isbn()) && bookRepository.existsByIsbn(dto.isbn())) {
            throw new IllegalArgumentException("Book with ISBN " + dto.isbn() + " already exists");
        }

        // Store the current availability status
        boolean wasAvailable = book.getAvailable();

        // Update the book with the DTO data
        bookMapper.updateEntityFromDto(dto, book);

        // Save the updated book
        Book updated = bookRepository.save(book);
        log.info("Updated book {} ({})", updated.getTitle(), updated.getId());

        // Calculate the current availability based on borrowing status
        calculateAvailability(updated);

        // Check if availability has changed
        if (wasAvailable != updated.getAvailable()) {
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

        BookAvailabilityDTO dto = bookMapper.createAvailabilityDTO(book);
        availabilitySink.tryEmitNext(dto);
        log.info("Availability changed → emitted update for book {}", book.getId());
    }
}
