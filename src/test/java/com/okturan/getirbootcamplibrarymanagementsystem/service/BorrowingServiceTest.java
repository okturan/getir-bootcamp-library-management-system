package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BorrowingMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.BorrowingServiceImpl;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User patronUser;
    private User adminUser;
    private Book book;
    private Borrowing borrowing;
    private BorrowingRequestDTO borrowingRequestDTO;
    private BorrowingResponseDTO borrowingResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        patronUser = new User();
        patronUser.setId(1L);
        patronUser.setUsername("patron");
        patronUser.setEmail("patron@example.com");
        patronUser.setRoles(Set.of(Role.PATRON));

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setRoles(Set.of(Role.ADMIN));

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setGenre("Fiction");
        book.setAvailable(true);

        borrowing = new Borrowing();
        borrowing.setId(1L);
        borrowing.setBook(book);
        borrowing.setUser(patronUser);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(LocalDate.now().plusDays(14));
        borrowing.setReturned(false);

        borrowingRequestDTO = new BorrowingRequestDTO(1L, null);

        borrowingResponseDTO = new BorrowingResponseDTO(
                1L,
                1L,
                "Test Book",
                "1234567890",
                1L,
                "patron",
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                null,
                false,
                false
        );
    }

    @Test
    void borrowBook_ShouldBorrowBookForCurrentUser() {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("patron");

        // Mock repository and mapper
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patronUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(book)).thenReturn(false);
        doNothing().when(borrowingMapper).initBorrowing(any(Borrowing.class), eq(book), eq(patronUser));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        BorrowingResponseDTO result = borrowingService.borrowBook(borrowingRequestDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.bookId());
        assertEquals("Test Book", result.bookTitle());
        assertEquals(1L, result.userId());
        assertEquals("patron", result.username());
        
        verify(userRepository).findByUsername("patron");
        verify(bookRepository).findById(1L);
        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(borrowingMapper).initBorrowing(any(Borrowing.class), eq(book), eq(patronUser));
        verify(borrowingRepository).save(any(Borrowing.class));
        verify(borrowingMapper).mapToDTO(borrowing);
        verify(bookService).emitAvailabilityUpdate(book);
    }

    @Test
    void borrowBook_ShouldThrowException_WhenBookAlreadyBorrowed() {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("patron");

        // Mock repository
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patronUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(book)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> borrowingService.borrowBook(borrowingRequestDTO));
        
        verify(userRepository).findByUsername("patron");
        verify(bookRepository).findById(1L);
        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(borrowingMapper, never()).initBorrowing(any(Borrowing.class), any(Book.class), any(User.class));
        verify(borrowingRepository, never()).save(any(Borrowing.class));
    }

    @Test
    void borrowBook_ShouldBorrowBookForSpecifiedUser_WhenAdminUser() {
        // Mock authentication for admin
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("admin");

        // Create request with specified userId
        BorrowingRequestDTO requestWithUserId = new BorrowingRequestDTO(1L, 1L);

        // Mock repository and mapper
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(patronUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookAndReturnedFalse(book)).thenReturn(false);
        doNothing().when(borrowingMapper).initBorrowing(any(Borrowing.class), eq(book), eq(patronUser));
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(borrowing);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        BorrowingResponseDTO result = borrowingService.borrowBook(requestWithUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.bookId());
        assertEquals("Test Book", result.bookTitle());
        assertEquals(1L, result.userId());
        assertEquals("patron", result.username());
        
        verify(userRepository).findByUsername("admin");
        verify(userRepository).findById(1L);
        verify(bookRepository).findById(1L);
        verify(borrowingRepository).existsByBookAndReturnedFalse(book);
        verify(borrowingMapper).initBorrowing(any(Borrowing.class), eq(book), eq(patronUser));
        verify(borrowingRepository).save(any(Borrowing.class));
        verify(borrowingMapper).mapToDTO(borrowing);
        verify(bookService).emitAvailabilityUpdate(book);
    }

    @Test
    void borrowBook_ShouldThrowException_WhenPatronTriesToBorrowForOthers() {
        // Mock authentication for patron
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("patron");

        // Create request with specified userId
        BorrowingRequestDTO requestWithUserId = new BorrowingRequestDTO(1L, 3L);

        // Mock repository
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patronUser));

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> borrowingService.borrowBook(requestWithUserId));
        
        verify(userRepository).findByUsername("patron");
        verify(userRepository, never()).findById(anyLong());
        verify(bookRepository, never()).findById(anyLong());
    }

    @Test
    void returnBook_ShouldReturnBorrowedBook() {
        // Mock repository and mapper
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));
        doNothing().when(borrowingMapper).returnBook(borrowing);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        BorrowingResponseDTO result = borrowingService.returnBook(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.bookId());
        assertEquals("Test Book", result.bookTitle());
        
        verify(borrowingRepository).findById(1L);
        verify(borrowingMapper).returnBook(borrowing);
        verify(borrowingRepository).save(borrowing);
        verify(borrowingMapper).mapToDTO(borrowing);
        verify(bookService).emitAvailabilityUpdate(book);
    }

    @Test
    void returnBook_ShouldThrowException_WhenBookAlreadyReturned() {
        // Setup already returned borrowing
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());

        // Mock repository
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> borrowingService.returnBook(1L));
        
        verify(borrowingRepository).findById(1L);
        verify(borrowingMapper, never()).returnBook(any(Borrowing.class));
        verify(borrowingRepository, never()).save(any(Borrowing.class));
        verify(bookService, never()).emitAvailabilityUpdate(any(Book.class));
    }

    @Test
    void getBorrowingById_ShouldReturnBorrowing() {
        // Mock repository and mapper
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        BorrowingResponseDTO result = borrowingService.getBorrowingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(1L, result.bookId());
        assertEquals("Test Book", result.bookTitle());
        
        verify(borrowingRepository).findById(1L);
        verify(borrowingMapper).mapToDTO(borrowing);
    }

    @Test
    void getCurrentUserBorrowingHistory_ShouldReturnUserHistory() {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("patron");

        // Mock repository and mapper
        when(userRepository.findByUsername("patron")).thenReturn(Optional.of(patronUser));
        
        List<Borrowing> borrowings = List.of(borrowing);
        Page<Borrowing> borrowingsPage = new PageImpl<>(borrowings);
        when(borrowingRepository.findByUser(eq(patronUser), any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);
        when(borrowingRepository.countByUser(patronUser)).thenReturn(1L);
        when(borrowingRepository.countByUserAndReturnedFalse(patronUser)).thenReturn(1L);
        when(borrowingRepository.countByUserAndReturnedFalseAndDueDateBefore(eq(patronUser), any(LocalDate.class))).thenReturn(0L);

        // Act
        BorrowingHistoryDTO result = borrowingService.getCurrentUserBorrowingHistory(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("patron", result.username());
        assertEquals(1, result.totalBorrowings());
        assertEquals(1, result.currentBorrowings());
        assertEquals(0, result.overdueBorrowings());
        
        verify(userRepository).findByUsername("patron");
        verify(borrowingRepository).findByUser(eq(patronUser), any(Pageable.class));
        verify(borrowingMapper).mapToDTO(borrowing);
        verify(borrowingRepository).countByUser(patronUser);
        verify(borrowingRepository).countByUserAndReturnedFalse(patronUser);
        verify(borrowingRepository).countByUserAndReturnedFalseAndDueDateBefore(eq(patronUser), any(LocalDate.class));
    }

    @Test
    void getUserBorrowingHistory_ShouldReturnSpecifiedUserHistory() {
        // Mock repository and mapper
        when(userRepository.findById(1L)).thenReturn(Optional.of(patronUser));
        
        List<Borrowing> borrowings = List.of(borrowing);
        Page<Borrowing> borrowingsPage = new PageImpl<>(borrowings);
        when(borrowingRepository.findByUser(eq(patronUser), any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);
        when(borrowingRepository.countByUser(patronUser)).thenReturn(1L);
        when(borrowingRepository.countByUserAndReturnedFalse(patronUser)).thenReturn(1L);
        when(borrowingRepository.countByUserAndReturnedFalseAndDueDateBefore(eq(patronUser), any(LocalDate.class))).thenReturn(0L);

        // Act
        BorrowingHistoryDTO result = borrowingService.getUserBorrowingHistory(1L, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.userId());
        assertEquals("patron", result.username());
        assertEquals(1, result.totalBorrowings());
        assertEquals(1, result.currentBorrowings());
        assertEquals(0, result.overdueBorrowings());
        
        verify(userRepository).findById(1L);
        verify(borrowingRepository).findByUser(eq(patronUser), any(Pageable.class));
        verify(borrowingMapper).mapToDTO(borrowing);
        verify(borrowingRepository).countByUser(patronUser);
        verify(borrowingRepository).countByUserAndReturnedFalse(patronUser);
        verify(borrowingRepository).countByUserAndReturnedFalseAndDueDateBefore(eq(patronUser), any(LocalDate.class));
    }

    @Test
    void getAllActiveBorrowings_ShouldReturnActiveBorrowings() {
        // Mock repository and mapper
        List<Borrowing> borrowings = List.of(borrowing);
        Page<Borrowing> borrowingsPage = new PageImpl<>(borrowings);
        when(borrowingRepository.findByReturned(eq(false), any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        Page<BorrowingResponseDTO> result = borrowingService.getAllActiveBorrowings(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(borrowingResponseDTO, result.getContent().get(0));
        
        verify(borrowingRepository).findByReturned(eq(false), any(Pageable.class));
        verify(borrowingMapper).mapToDTO(borrowing);
    }

    @Test
    void getAllOverdueBorrowings_ShouldReturnOverdueBorrowings() {
        // Mock repository and mapper
        List<Borrowing> borrowings = List.of(borrowing);
        Page<Borrowing> borrowingsPage = new PageImpl<>(borrowings);
        when(borrowingRepository.findByDueDateBeforeAndReturned(any(LocalDate.class), eq(false), any(Pageable.class))).thenReturn(borrowingsPage);
        when(borrowingMapper.mapToDTO(borrowing)).thenReturn(borrowingResponseDTO);

        // Act
        Page<BorrowingResponseDTO> result = borrowingService.getAllOverdueBorrowings(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(borrowingResponseDTO, result.getContent().get(0));
        
        verify(borrowingRepository).findByDueDateBeforeAndReturned(any(LocalDate.class), eq(false), any(Pageable.class));
        verify(borrowingMapper).mapToDTO(borrowing);
    }

    @Test
    void isOwner_ShouldReturnTrue_WhenUserIsOwner() {
        // Mock repository
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));

        // Act
        boolean result = borrowingService.isOwner(1L, "patron");

        // Assert
        assertTrue(result);
        
        verify(borrowingRepository).findById(1L);
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenUserIsNotOwner() {
        // Mock repository
        when(borrowingRepository.findById(1L)).thenReturn(Optional.of(borrowing));

        // Act
        boolean result = borrowingService.isOwner(1L, "admin");

        // Assert
        assertFalse(result);
        
        verify(borrowingRepository).findById(1L);
    }
}