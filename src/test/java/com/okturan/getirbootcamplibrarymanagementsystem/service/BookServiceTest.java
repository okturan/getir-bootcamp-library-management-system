package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;
    private BookSearchFilterDTO searchFilterDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setGenre("Fiction");
        book.setAvailable(true);

        bookRequestDTO = new BookRequestDTO(
                "Test Book",
                "Test Author",
                "1234567890",
                LocalDate.of(2020, 1, 1),
                "Fiction"
        );

        bookResponseDTO = new BookResponseDTO(
                1L,
                "Test Book",
                "Test Author",
                "1234567890",
                LocalDate.of(2020, 1, 1),
                "Fiction",
                true
        );

        searchFilterDTO = new BookSearchFilterDTO(
                Optional.of("Test Author"),
                Optional.of("Test Book"),
                Optional.of("Fiction"),
                Optional.of(true)
        );
    }

    @Test
    void createBook_ShouldCreateAndReturnBook() {
        // Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        when(bookMapper.mapToEntity(any(BookRequestDTO.class))).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        BookResponseDTO result = bookService.createBook(bookRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Book", result.title());
        assertEquals("Test Author", result.author());
        assertEquals("1234567890", result.isbn());

        verify(bookRepository).existsByIsbn("1234567890");
        verify(bookMapper).mapToEntity(bookRequestDTO);
        verify(bookRepository).save(book);
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void createBook_ShouldThrowException_WhenIsbnExists() {
        // Arrange
        when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> bookService.createBook(bookRequestDTO));

        verify(bookRepository).existsByIsbn("1234567890");
        verifyNoInteractions(bookMapper);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getBookById_ShouldReturnBook() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(any(Book.class))).thenReturn(false);
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        BookResponseDTO result = bookService.getBookById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Book", result.title());

        verify(bookRepository).findById(1L);
        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void getBookById_ShouldThrowException_WhenBookNotFound() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(1L));

        verify(bookRepository).findById(1L);
        verifyNoInteractions(borrowingRepository);
        verifyNoInteractions(bookMapper);
    }

    @Test
    void getBookByIsbn_ShouldReturnBook() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(any(Book.class))).thenReturn(false);
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        BookResponseDTO result = bookService.getBookByIsbn("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Book", result.title());

        verify(bookRepository).findByIsbn("1234567890");
        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void getAllBooks_ShouldReturnPageOfBooks() {
        // Arrange
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(bookPage);
        when(borrowingRepository.findBorrowedBookIdsByBookIds(anyList())).thenReturn(Set.of());
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        Page<BookResponseDTO> result = bookService.getAllBooks(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookResponseDTO, result.getContent().get(0));

        verify(bookRepository).findAll(Pageable.unpaged());
        verify(borrowingRepository).findBorrowedBookIdsByBookIds(List.of(1L));
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void search_ShouldReturnFilteredBooks() {
        // Arrange
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(bookPage);
        when(borrowingRepository.findBorrowedBookIdsByBookIds(anyList())).thenReturn(Set.of());
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        Page<BookResponseDTO> result = bookService.search(searchFilterDTO, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookResponseDTO, result.getContent().get(0));

        verify(bookRepository).findAll(any(Specification.class), eq(Pageable.unpaged()));
        verify(borrowingRepository).findBorrowedBookIdsByBookIds(List.of(1L));
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void updateBook_ShouldUpdateAndReturnBook() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(any(Book.class))).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.mapToDTO(any(Book.class))).thenReturn(bookResponseDTO);

        // Act
        BookResponseDTO result = bookService.updateBook(1L, bookRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test Book", result.title());

        verify(bookRepository).findById(1L);
        verify(borrowingRepository, times(2)).existsByBookAndReturnedFalse(book);
        verify(bookMapper).updateEntityFromDto(bookRequestDTO, book);
        verify(bookRepository).save(book);
        verify(bookMapper).mapToDTO(book);
    }

    @Test
    void deleteBook_ShouldDeleteBook() {
        // Arrange
        when(bookRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(anyLong());

        // Act
        bookService.deleteBook(1L);

        // Assert
        verify(bookRepository).existsById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_ShouldThrowException_WhenBookNotFound() {
        // Arrange
        when(bookRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> bookService.deleteBook(1L));

        verify(bookRepository).existsById(1L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void streamBookAvailabilityUpdates_ShouldReturnFlux() {
        // Arrange
        BookAvailabilityDTO availabilityDTO = new BookAvailabilityDTO(1L, "Test Book", "1234567890", true, "2023-05-15T14:30:45.123Z");
        when(borrowingRepository.existsByBookAndReturnedFalse(any(Book.class))).thenReturn(false);
        when(bookMapper.createAvailabilityDTO(any(Book.class), anyString())).thenReturn(availabilityDTO);

        // Act
        Flux<BookAvailabilityDTO> result = bookService.streamBookAvailabilityUpdates();

        // Assert
        assertNotNull(result);

        // Emit an update
        bookService.emitAvailabilityUpdate(book);

        // Verify the flux emits the update
        StepVerifier.create(result.take(1))
            .expectNext(availabilityDTO)
            .verifyComplete();

        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(bookMapper).createAvailabilityDTO(eq(book), anyString());
    }
}
