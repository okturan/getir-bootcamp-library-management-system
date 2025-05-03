package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BorrowingMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.BorrowingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class BorrowingServiceTest {

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookService bookService;

    @Mock
    private BorrowingMapper borrowingMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User patron;
    private User librarian;
    private User admin;
    private User anotherPatron;
    private Book book;
    private Borrowing borrowing;
    private BorrowingRequestDTO borrowingRequestDTO;

    @BeforeEach
    void setUp() {
        // Set up security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Create test data
        patron = new User("patron", "password", "patron@example.com");
        patron.setId(1L);

        anotherPatron = new User("anotherPatron", "password", "anotherpatron@example.com");
        anotherPatron.setId(3L);

        librarian = new User("librarian", "password", "librarian@example.com");
        librarian.setId(2L);
        librarian.addRole(Role.LIBRARIAN);

        admin = new User("admin", "password", "admin@example.com");
        admin.setId(4L);
        admin.addRole(Role.ADMIN);

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("978-3-16-148410-0");
        book.setGenre("Test Genre");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setAvailable(true);

        borrowing = new Borrowing();
        borrowing.setId(1L);
        borrowing.setBook(book);
        borrowing.setUser(patron);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(LocalDate.now().plusDays(14));
        borrowing.setReturned(false);

        borrowingRequestDTO = new BorrowingRequestDTO();
        borrowingRequestDTO.setBookId(1L);
        borrowingRequestDTO.setDueDate(LocalDate.now().plusDays(14));

        // Configure BorrowingMapper mock
        lenient().when(borrowingMapper.mapToDTO(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing b = invocation.getArgument(0);
            BorrowingResponseDTO dto = new BorrowingResponseDTO();
            dto.setId(b.getId());
            if (b.getBook() != null) {
                dto.setBookId(b.getBook().getId());
                dto.setBookTitle(b.getBook().getTitle());
                dto.setBookIsbn(b.getBook().getIsbn());
            }
            if (b.getUser() != null) {
                dto.setUserId(b.getUser().getId());
                dto.setUsername(b.getUser().getUsername());
            }
            dto.setBorrowDate(b.getBorrowDate());
            dto.setDueDate(b.getDueDate());
            dto.setReturnDate(b.getReturnDate());
            dto.setReturned(b.isReturned());
            dto.setOverdue(b.isOverdue());
            return dto;
        });

        // Configure BookService mock
        lenient().when(bookService.updateBook(anyLong(), any())).thenAnswer(invocation -> {
            Long bookId = invocation.getArgument(0);
            Book updatedBook = new Book();
            updatedBook.setId(bookId);
            updatedBook.setTitle("Test Book");
            updatedBook.setAuthor("Test Author");
            updatedBook.setIsbn("978-3-16-148410-0");
            updatedBook.setGenre("Test Genre");
            updatedBook.setPublicationDate(LocalDate.of(2020, 1, 1));
            updatedBook.setAvailable(true);
            return new BookResponseDTO(
                updatedBook.getId(),
                updatedBook.getTitle(),
                updatedBook.getAuthor(),
                updatedBook.getIsbn(),
                updatedBook.getPublicationDate(),
                updatedBook.getGenre(),
                updatedBook.isAvailable()
            );
        });
    }

    @Test
    void borrowBook_Success() {
        // Arrange
        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        BorrowingResponseDTO result = borrowingService.borrowBook(borrowingRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals("Test Book", result.getBookTitle());
        assertEquals(1L, result.getUserId());
        assertEquals("patron", result.getUsername());
        assertFalse(result.isReturned());

        verify(bookRepository).save(book);
        assertFalse(book.isAvailable());
    }

    @Test
    void borrowBook_BookNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void borrowBook_BookNotAvailable() {
        // Arrange
        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        book.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void borrowBook_AdminCannotBorrowForSelf() {
        // Arrange
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void borrowBook_LibrarianCannotBorrowForSelf() {
        // Arrange
        when(authentication.getName()).thenReturn("librarian");
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(librarian));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void borrowBook_AdminCanBorrowForPatron() {
        // Arrange
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Set userId in the request
        borrowingRequestDTO.setUserId(1L);

        // Act
        BorrowingResponseDTO result = borrowingService.borrowBook(borrowingRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals("Test Book", result.getBookTitle());
        assertEquals(1L, result.getUserId());
        assertEquals("patron", result.getUsername());
        assertFalse(result.isReturned());

        verify(bookRepository).save(book);
        assertFalse(book.isAvailable());
    }

    @Test
    void borrowBook_LibrarianCanBorrowForPatron() {
        // Arrange
        when(authentication.getName()).thenReturn("librarian");
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(librarian));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Set userId in the request
        borrowingRequestDTO.setUserId(1L);

        // Act
        BorrowingResponseDTO result = borrowingService.borrowBook(borrowingRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getBookId());
        assertEquals("Test Book", result.getBookTitle());
        assertEquals(1L, result.getUserId());
        assertEquals("patron", result.getUsername());
        assertFalse(result.isReturned());

        verify(bookRepository).save(book);
        assertFalse(book.isAvailable());
    }

    @Test
    void borrowBook_PatronCannotBorrowForOthers() {
        // Arrange
        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));

        // Set userId in the request
        borrowingRequestDTO.setUserId(3L); // anotherPatron's ID

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void borrowBook_AdminCannotBorrowForNonPatron() {
        // Arrange
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(librarian)); // librarian is not a patron

        // Set userId in the request
        borrowingRequestDTO.setUserId(2L); // librarian's ID

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
    }

    @Test
    void returnBook_Success() {
        // Arrange
        // Ensure borrowing has a book
        borrowing.setBook(book);

        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        BorrowingResponseDTO result = borrowingService.returnBook(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isReturned());
        assertNotNull(result.getReturnDate());

        verify(borrowingRepository).save(borrowing);
        verify(bookRepository).save(book);
        assertTrue(book.isAvailable());
    }

    @Test
    void returnBook_BorrowingNotFound() {
        // Arrange
        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> borrowingService.returnBook(1L));
    }

    // This test is no longer needed as authorization is now handled at the controller level
    // with @PreAuthorize annotations

    @Test
    void returnBook_AlreadyReturned() {
        // Arrange
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now().minusDays(1));

        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> borrowingService.returnBook(1L));
    }

    @Test
    void getCurrentUserBorrowingHistory_Success() {
        // Arrange
        List<Borrowing> borrowings = Arrays.asList(borrowing);
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(1L);
        dto.setBookId(1L);
        dto.setBookTitle("Test Book");
        dto.setBookIsbn("978-3-16-148410-0");
        dto.setUserId(1L);
        dto.setUsername("patron");
        dto.setBorrowDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(14));
        dto.setReturned(false);

        when(authentication.getName()).thenReturn("patron");
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patron));
        when(borrowingRepository.findByUser(patron)).thenReturn(borrowings);
        when(borrowingMapper.mapToDTO(any(Borrowing.class))).thenReturn(dto);

        // Act
        BorrowingHistoryDTO result = borrowingService.getCurrentUserBorrowingHistory();

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("patron", result.getUsername());
        assertEquals(1, result.getTotalBorrowings());
        assertEquals(1, result.getCurrentBorrowings());
        assertEquals(0, result.getOverdueBorrowings());
        assertEquals(1, result.getBorrowings().size());
    }

    @Test
    void getUserBorrowingHistory_Success() {
        // Arrange
        List<Borrowing> borrowings = Arrays.asList(borrowing);
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(1L);
        dto.setBookId(1L);
        dto.setBookTitle("Test Book");
        dto.setBookIsbn("978-3-16-148410-0");
        dto.setUserId(1L);
        dto.setUsername("patron");
        dto.setBorrowDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(14));
        dto.setReturned(false);

        when(authentication.getName()).thenReturn("librarian");
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(librarian));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRepository.findByUser(patron)).thenReturn(borrowings);
        when(borrowingMapper.mapToDTO(any(Borrowing.class))).thenReturn(dto);

        // Act
        BorrowingHistoryDTO result = borrowingService.getUserBorrowingHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("patron", result.getUsername());
        assertEquals(1, result.getTotalBorrowings());
        assertEquals(1, result.getCurrentBorrowings());
        assertEquals(0, result.getOverdueBorrowings());
        assertEquals(1, result.getBorrowings().size());
    }

    // This test is no longer needed as authorization is now handled at the controller level
    // with @PreAuthorize annotations

    @Test
    void getAllActiveBorrowings_Success() {
        // Arrange
        List<Borrowing> borrowings = Arrays.asList(borrowing);
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(1L);
        dto.setBookId(1L);
        dto.setBookTitle("Test Book");
        dto.setBookIsbn("978-3-16-148410-0");
        dto.setUserId(1L);
        dto.setUsername("patron");
        dto.setBorrowDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(14));
        dto.setReturned(false);

        when(authentication.getName()).thenReturn("librarian");
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(librarian));
        when(borrowingRepository.findByReturned(false)).thenReturn(borrowings);
        when(borrowingMapper.mapToDTO(any(Borrowing.class))).thenReturn(dto);

        // Act
        List<BorrowingResponseDTO> result = borrowingService.getAllActiveBorrowings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBookId());
        assertEquals("Test Book", result.get(0).getBookTitle());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("patron", result.get(0).getUsername());
        assertFalse(result.get(0).isReturned());
    }

    // This test is no longer needed as authorization is now handled at the controller level
    // with @PreAuthorize annotations

    @Test
    void getAllOverdueBorrowings_Success() {
        // Arrange
        borrowing.setDueDate(LocalDate.now().minusDays(1));
        List<Borrowing> borrowings = Arrays.asList(borrowing);
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(1L);
        dto.setBookId(1L);
        dto.setBookTitle("Test Book");
        dto.setBookIsbn("978-3-16-148410-0");
        dto.setUserId(1L);
        dto.setUsername("patron");
        dto.setBorrowDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().minusDays(1));
        dto.setReturned(false);
        dto.setOverdue(true);

        when(authentication.getName()).thenReturn("librarian");
        when(userRepository.findByUsername("librarian")).thenReturn(Optional.of(librarian));
        when(borrowingRepository.findByDueDateBeforeAndReturned(any(LocalDate.class), eq(false))).thenReturn(borrowings);
        when(borrowingMapper.mapToDTO(any(Borrowing.class))).thenReturn(dto);

        // Act
        List<BorrowingResponseDTO> result = borrowingService.getAllOverdueBorrowings();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getBookId());
        assertEquals("Test Book", result.get(0).getBookTitle());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("patron", result.get(0).getUsername());
        assertFalse(result.get(0).isReturned());
        assertTrue(result.get(0).isOverdue());
    }

    // This test is no longer needed as authorization is now handled at the controller level
    // with @PreAuthorize annotations
}
