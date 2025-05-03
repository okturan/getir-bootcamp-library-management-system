package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BookMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.BookServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private BookRequestDTO bookRequestDTO;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublicationDate(LocalDate.of(2023, 1, 1));
        testBook.setGenre("Test Genre");
        testBook.setAvailable(true);

        // Setup book request DTO
        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Test Book");
        bookRequestDTO.setAuthor("Test Author");
        bookRequestDTO.setIsbn("1234567890");
        bookRequestDTO.setPublicationDate(LocalDate.of(2023, 1, 1));
        bookRequestDTO.setGenre("Test Genre");
        bookRequestDTO.setAvailable(true);

        // Configure BookMapper mock with lenient stubbings
        lenient().when(bookMapper.mapToEntity(any(BookRequestDTO.class))).thenReturn(testBook);
        lenient().when(bookMapper.mapToDTO(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationDate(),
                book.getGenre(),
                book.isAvailable()
            );
        });
        lenient().when(bookMapper.createAvailabilityDTO(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return new BookAvailabilityDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.isAvailable(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            );
        });
    }

    @Test
    void createBook_ShouldCreateBook_WhenIsbnDoesNotExist() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        BookResponseDTO result = bookService.createBook(bookRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testBook.getId(), result.getId());
        assertEquals(testBook.getTitle(), result.getTitle());
        assertEquals(testBook.getAuthor(), result.getAuthor());
        assertEquals(testBook.getIsbn(), result.getIsbn());
        assertEquals(testBook.getPublicationDate(), result.getPublicationDate());
        assertEquals(testBook.getGenre(), result.getGenre());
        assertEquals(testBook.isAvailable(), result.isAvailable());
        verify(bookRepository).findByIsbn(bookRequestDTO.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_ShouldThrowException_WhenIsbnExists() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.createBook(bookRequestDTO);
        });
        assertEquals("Book with ISBN 1234567890 already exists", exception.getMessage());
        verify(bookRepository).findByIsbn(bookRequestDTO.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));

        // Act
        BookResponseDTO result = bookService.getBookById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testBook.getId(), result.getId());
        assertEquals(testBook.getTitle(), result.getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_ShouldThrowException_WhenBookDoesNotExist() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookService.getBookById(1L);
        });
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookByIsbn_ShouldReturnBook_WhenBookExists() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(testBook));

        // Act
        BookResponseDTO result = bookService.getBookByIsbn("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals(testBook.getId(), result.getId());
        assertEquals(testBook.getTitle(), result.getTitle());
        verify(bookRepository).findByIsbn("1234567890");
    }

    @Test
    void getBookByIsbn_ShouldThrowException_WhenBookDoesNotExist() {
        // Arrange
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookService.getBookByIsbn("1234567890");
        });
        assertEquals("Book not found with ISBN: 1234567890", exception.getMessage());
        verify(bookRepository).findByIsbn("1234567890");
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Arrange
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Test Book 2");
        book2.setAuthor("Test Author 2");
        book2.setIsbn("0987654321");
        book2.setPublicationDate(LocalDate.of(2023, 2, 2));
        book2.setGenre("Test Genre 2");
        book2.setAvailable(false);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(testBook, book2));

        // Act
        List<BookResponseDTO> result = bookService.getAllBooks();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testBook.getId(), result.get(0).getId());
        assertEquals(testBook.getTitle(), result.get(0).getTitle());
        assertEquals(book2.getId(), result.get(1).getId());
        assertEquals(book2.getTitle(), result.get(1).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void updateBook_ShouldUpdateBook_WhenBookExistsAndIsbnNotChanged() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        BookResponseDTO result = bookService.updateBook(1L, bookRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testBook.getId(), result.getId());
        assertEquals(testBook.getTitle(), result.getTitle());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(testBook);
    }

    @Test
    void updateBook_ShouldUpdateBook_WhenBookExistsAndIsbnChangedAndNewIsbnDoesNotExist() {
        // Arrange
        BookRequestDTO updatedDTO = new BookRequestDTO();
        updatedDTO.setTitle("Updated Book");
        updatedDTO.setAuthor("Updated Author");
        updatedDTO.setIsbn("9876543210");
        updatedDTO.setPublicationDate(LocalDate.of(2023, 3, 3));
        updatedDTO.setGenre("Updated Genre");
        updatedDTO.setAvailable(false);

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn("9876543210")).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // Act
        BookResponseDTO result = bookService.updateBook(1L, updatedDTO);

        // Assert
        assertNotNull(result);
        verify(bookRepository).findById(1L);
        verify(bookRepository).findByIsbn("9876543210");
        verify(bookRepository).save(testBook);
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookExistsAndIsbnChangedAndNewIsbnExists() {
        // Arrange
        Book existingBook = new Book();
        existingBook.setId(2L);
        existingBook.setIsbn("9876543210");

        BookRequestDTO updatedDTO = new BookRequestDTO();
        updatedDTO.setTitle("Updated Book");
        updatedDTO.setAuthor("Updated Author");
        updatedDTO.setIsbn("9876543210");
        updatedDTO.setPublicationDate(LocalDate.of(2023, 3, 3));
        updatedDTO.setGenre("Updated Genre");
        updatedDTO.setAvailable(false);

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(testBook));
        when(bookRepository.findByIsbn("9876543210")).thenReturn(Optional.of(existingBook));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookService.updateBook(1L, updatedDTO);
        });
        assertEquals("Book with ISBN 9876543210 already exists", exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(bookRepository).findByIsbn("9876543210");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_ShouldThrowException_WhenBookDoesNotExist() {
        // Arrange
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookService.updateBook(1L, bookRequestDTO);
        });
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_ShouldDeleteBook_WhenBookExists() {
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
    void deleteBook_ShouldThrowException_WhenBookDoesNotExist() {
        // Arrange
        when(bookRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bookService.deleteBook(1L);
        });
        assertEquals("Book not found with id: 1", exception.getMessage());
        verify(bookRepository).existsById(1L);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    void findBooksByAuthor_ShouldReturnBooks() {
        // Arrange
        when(bookRepository.findByAuthorContainingIgnoreCase(anyString())).thenReturn(List.of(testBook));

        // Act
        List<BookResponseDTO> result = bookService.findBooksByAuthor("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook.getId(), result.get(0).getId());
        assertEquals(testBook.getTitle(), result.get(0).getTitle());
        verify(bookRepository).findByAuthorContainingIgnoreCase("Test");
    }

    @Test
    void findBooksByTitle_ShouldReturnBooks() {
        // Arrange
        when(bookRepository.findByTitleContainingIgnoreCase(anyString())).thenReturn(List.of(testBook));

        // Act
        List<BookResponseDTO> result = bookService.findBooksByTitle("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook.getId(), result.get(0).getId());
        assertEquals(testBook.getTitle(), result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("Test");
    }

    @Test
    void findBooksByGenre_ShouldReturnBooks() {
        // Arrange
        when(bookRepository.findByGenreContainingIgnoreCase(anyString())).thenReturn(List.of(testBook));

        // Act
        List<BookResponseDTO> result = bookService.findBooksByGenre("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook.getId(), result.get(0).getId());
        assertEquals(testBook.getTitle(), result.get(0).getTitle());
        verify(bookRepository).findByGenreContainingIgnoreCase("Test");
    }

    @Test
    void findBooksByAvailability_ShouldReturnBooks() {
        // Arrange
        when(bookRepository.findByAvailable(anyBoolean())).thenReturn(List.of(testBook));

        // Act
        List<BookResponseDTO> result = bookService.findBooksByAvailability(true);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBook.getId(), result.get(0).getId());
        assertEquals(testBook.getTitle(), result.get(0).getTitle());
        verify(bookRepository).findByAvailable(true);
    }
}
