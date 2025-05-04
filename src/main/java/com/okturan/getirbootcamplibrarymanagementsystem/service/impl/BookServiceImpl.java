package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
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
        Book saved = bookRepository.save(bookMapper.mapToEntity(dto));
        log.info("Created book {} ({})", saved.getTitle(), saved.getId());
        return bookMapper.mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        return bookMapper.mapToDTO(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookByIsbn(String isbn) {
        return bookMapper.mapToDTO(
                bookRepository.findByIsbn(isbn)
                        .orElseThrow(() -> new EntityNotFoundException("Book not found: " + isbn)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
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

        boolean availabilityChanged = book.isAvailable() != dto.available();

        bookMapper.updateEntityFromDto(dto, book);

        Book updated = bookRepository.save(book);
        log.info("Updated book {} ({})", updated.getTitle(), updated.getId());

        if (availabilityChanged) emitAvailabilityUpdate(updated);

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

    /* ---------- Search helpers ---------- */

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author)
                .stream()
                .map(bookMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(bookMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByGenre(String genre) {
        return bookRepository.findByGenreContainingIgnoreCase(genre)
                .stream()
                .map(bookMapper::mapToDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByAvailability(boolean available) {
        return bookRepository.findByAvailable(available)
                .stream()
                .map(bookMapper::mapToDTO)
                .toList();
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
        BookAvailabilityDTO dto = bookMapper.createAvailabilityDTO(book);
        availabilitySink.tryEmitNext(dto);
        log.info("Availability changed → emitted update for book {}", book.getId());
    }
}
