package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.persistence.EntityNotFoundException;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final BookRepository bookRepository;
    private final Sinks.Many<BookAvailabilityDTO> bookAvailabilitySink;
    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.bookAvailabilitySink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    @Transactional
    public BookResponseDTO createBook(BookRequestDTO bookRequestDTO) {
        logger.info("Creating new book with title: '{}', ISBN: {}", bookRequestDTO.getTitle(), bookRequestDTO.getIsbn());

        // Check if book with same ISBN already exists
        if (bookRepository.findByIsbn(bookRequestDTO.getIsbn()).isPresent()) {
            logger.warn("Failed to create book: Book with ISBN {} already exists", bookRequestDTO.getIsbn());
            throw new IllegalArgumentException("Book with ISBN " + bookRequestDTO.getIsbn() + " already exists");
        }

        try {
            Book book = bookMapper.mapToEntity(bookRequestDTO);
            logger.debug("Mapped DTO to entity for book: '{}'", bookRequestDTO.getTitle());

            Book savedBook = bookRepository.save(book);
            logger.info("Book created successfully: ID: {}, title: '{}', ISBN: {}", 
                    savedBook.getId(), savedBook.getTitle(), savedBook.getIsbn());

            return bookMapper.mapToDTO(savedBook);
        } catch (Exception e) {
            logger.error("Error creating book with title: '{}', ISBN: {}", 
                    bookRequestDTO.getTitle(), bookRequestDTO.getIsbn(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookById(Long id) {
        logger.debug("Retrieving book with ID: {}", id);
        try {
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Book not found with ID: {}", id);
                        return new EntityNotFoundException("Book not found with id: " + id);
                    });

            logger.debug("Book found: ID: {}, title: '{}', ISBN: {}", 
                    book.getId(), book.getTitle(), book.getIsbn());
            return bookMapper.mapToDTO(book);
        } catch (EntityNotFoundException e) {
            // Already logged in the orElseThrow
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving book with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO getBookByIsbn(String isbn) {
        logger.debug("Retrieving book with ISBN: {}", isbn);
        try {
            Book book = bookRepository.findByIsbn(isbn)
                    .orElseThrow(() -> {
                        logger.warn("Book not found with ISBN: {}", isbn);
                        return new EntityNotFoundException("Book not found with ISBN: " + isbn);
                    });

            logger.debug("Book found: ID: {}, title: '{}', ISBN: {}", 
                    book.getId(), book.getTitle(), book.getIsbn());
            return bookMapper.mapToDTO(book);
        } catch (EntityNotFoundException e) {
            // Already logged in the orElseThrow
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving book with ISBN: {}", isbn, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> getAllBooks() {
        logger.debug("Retrieving all books");
        try {
            List<BookResponseDTO> books = bookRepository.findAll().stream()
                    .map(bookMapper::mapToDTO)
                    .collect(Collectors.toList());

            logger.debug("Retrieved {} books", books.size());
            return books;
        } catch (Exception e) {
            logger.error("Error retrieving all books", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO) {
        logger.info("Updating book with ID: {}, new title: '{}'", id, bookRequestDTO.getTitle());

        try {
            // Find the book
            Book book = bookRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Failed to update: Book not found with ID: {}", id);
                        return new EntityNotFoundException("Book not found with id: " + id);
                    });

            logger.debug("Found book to update: ID: {}, current title: '{}', ISBN: {}", 
                    book.getId(), book.getTitle(), book.getIsbn());

            // Check if ISBN is being changed and if the new ISBN already exists
            if (!book.getIsbn().equals(bookRequestDTO.getIsbn())) {
                logger.debug("ISBN change detected: {} -> {}", book.getIsbn(), bookRequestDTO.getIsbn());

                if (bookRepository.findByIsbn(bookRequestDTO.getIsbn()).isPresent()) {
                    logger.warn("Failed to update: Book with ISBN {} already exists", bookRequestDTO.getIsbn());
                    throw new IllegalArgumentException("Book with ISBN " + bookRequestDTO.getIsbn() + " already exists");
                }
            }

            // Check if availability is changing
            boolean availabilityChanged = book.isAvailable() != bookRequestDTO.isAvailable();

            // Update book properties
            book.setTitle(bookRequestDTO.getTitle());
            book.setAuthor(bookRequestDTO.getAuthor());
            book.setIsbn(bookRequestDTO.getIsbn());
            book.setPublicationDate(bookRequestDTO.getPublicationDate());
            book.setGenre(bookRequestDTO.getGenre());
            book.setAvailable(bookRequestDTO.isAvailable());

            Book updatedBook = bookRepository.save(book);
            logger.info("Book updated successfully: ID: {}, title: '{}', ISBN: {}", 
                    updatedBook.getId(), updatedBook.getTitle(), updatedBook.getIsbn());

            // If availability changed, emit an update to the sink
            if (availabilityChanged) {
                BookAvailabilityDTO availabilityUpdate = bookMapper.createAvailabilityDTO(updatedBook);
                logger.info("Emitting book availability update: Book ID: {}, Title: '{}', Available: {}", 
                        availabilityUpdate.getId(), availabilityUpdate.getTitle(), availabilityUpdate.isAvailable());
                bookAvailabilitySink.tryEmitNext(availabilityUpdate);
            }

            return bookMapper.mapToDTO(updatedBook);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // Already logged above
            throw e;
        } catch (Exception e) {
            logger.error("Error updating book with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        logger.info("Deleting book with ID: {}", id);

        try {
            if (!bookRepository.existsById(id)) {
                logger.warn("Failed to delete: Book not found with ID: {}", id);
                throw new EntityNotFoundException("Book not found with id: " + id);
            }

            bookRepository.deleteById(id);
            logger.info("Book deleted successfully: ID: {}", id);
        } catch (EntityNotFoundException e) {
            // Already logged above
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting book with ID: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByAuthor(String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author).stream()
                .map(bookMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(bookMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByGenre(String genre) {
        return bookRepository.findByGenreContainingIgnoreCase(genre).stream()
                .map(bookMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponseDTO> findBooksByAvailability(boolean available) {
        return bookRepository.findByAvailable(available).stream()
                .map(bookMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Flux<BookAvailabilityDTO> streamBookAvailabilityUpdates() {
        logger.info("Client subscribed to book availability updates stream");
        return bookAvailabilitySink.asFlux()
                .publishOn(Schedulers.boundedElastic())
                .doOnCancel(() -> logger.info("Client unsubscribed from book availability updates stream"));
    }

}
