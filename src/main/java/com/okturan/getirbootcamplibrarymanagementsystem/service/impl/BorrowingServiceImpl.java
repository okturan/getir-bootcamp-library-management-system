package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service("borrowingService")
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingServiceImpl.class);

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BorrowingResponseDTO borrowBook(BorrowingRequestDTO borrowingRequestDTO) {
        logger.info("Borrowing book with ID: {}", borrowingRequestDTO.getBookId());

        // Get the current user
        User currentUser = getCurrentUser();

        // Determine which user will be the borrower
        User borrower;

        if (borrowingRequestDTO.getUserId() != null) {
            // If userId is provided, check if current user is admin or librarian
            if (!currentUser.hasRole(Role.ADMIN) && !currentUser.hasRole(Role.LIBRARIAN)) {
                throw new AccessDeniedException("Only admins and librarians can borrow books for other users");
            }

            // Get the user to borrow for
            borrower = userRepository.findById(borrowingRequestDTO.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + borrowingRequestDTO.getUserId()));

            // Check if the target user is a librarian or admin
            if (borrower.hasRole(Role.LIBRARIAN) || borrower.hasRole(Role.ADMIN)) {
                throw new IllegalArgumentException("Books can only be borrowed for patrons, not for librarians or admins");
            }

            logger.info("Admin/Librarian {} is borrowing a book for user {}", currentUser.getUsername(), borrower.getUsername());
        } else {
            // If userId is not provided, the current user wants to borrow for themselves

            // Check if current user is admin or librarian
            if (currentUser.hasRole(Role.ADMIN) || currentUser.hasRole(Role.LIBRARIAN)) {
                throw new AccessDeniedException("Admins and librarians cannot borrow books for themselves. Please specify a patron userId.");
            }

            borrower = currentUser;
        }

        // Get the book
        Book book = bookRepository.findById(borrowingRequestDTO.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found with ID: " + borrowingRequestDTO.getBookId()));

        // Check if the book is available
        if (!book.isAvailable()) {
            throw new IllegalStateException("Book is not available for borrowing");
        }

        // Create a new borrowing
        Borrowing borrowing = new Borrowing();
        borrowing.setBook(book);
        borrowing.setUser(borrower);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(borrowingRequestDTO.getDueDate());
        borrowing.setReturned(false);

        // Save the borrowing
        borrowing = borrowingRepository.save(borrowing);

        // Update book availability
        book.setAvailable(false);
        bookRepository.save(book);

        return mapToDTO(borrowing);
    }

    @Override
    @Transactional
    public BorrowingResponseDTO returnBook(Long borrowingId) {
        logger.info("Returning book with borrowing ID: {}", borrowingId);

        // Get the current user
        User user = getCurrentUser();

        // Get the borrowing
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with ID: " + borrowingId));

        // Check if the book is already returned
        if (borrowing.isReturned()) {
            throw new IllegalStateException("Book is already returned");
        }

        // Update the borrowing
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());
        borrowing = borrowingRepository.save(borrowing);

        // Update book availability
        Book book = borrowing.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        return mapToDTO(borrowing);
    }

    @Override
    public BorrowingResponseDTO getBorrowingById(Long borrowingId) {
        logger.info("Getting borrowing with ID: {}", borrowingId);

        // Get the current user
        User user = getCurrentUser();

        // Get the borrowing
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with ID: " + borrowingId));

        return mapToDTO(borrowing);
    }

    @Override
    public BorrowingHistoryDTO getCurrentUserBorrowingHistory() {
        logger.info("Getting borrowing history for current user");

        // Get the current user
        User user = getCurrentUser();

        return getUserBorrowingHistoryInternal(user);
    }

    @Override
    public BorrowingHistoryDTO getUserBorrowingHistory(Long userId) {
        logger.info("Getting borrowing history for user with ID: {}", userId);

        // Get the current user
        User currentUser = getCurrentUser();

        // Get the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        return getUserBorrowingHistoryInternal(user);
    }

    @Override
    public List<BorrowingResponseDTO> getAllActiveBorrowings() {
        logger.info("Getting all active borrowings");

        // Get the current user
        User currentUser = getCurrentUser();

        // Get all active borrowings
        List<Borrowing> borrowings = borrowingRepository.findByReturned(false);

        return borrowings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingResponseDTO> getAllOverdueBorrowings() {
        logger.info("Getting all overdue borrowings");

        // Get the current user
        User currentUser = getCurrentUser();

        // Get all overdue borrowings
        List<Borrowing> borrowings = borrowingRepository.findByDueDateBeforeAndReturned(LocalDate.now(), false);

        return borrowings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isOwner(Long borrowingId, String username) {
        logger.info("Checking if user {} is the owner of borrowing {}", username, borrowingId);

        // Find the borrowing
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found with ID: " + borrowingId));

        // Check if the username matches the borrowing's user's username
        return borrowing.getUser().getUsername().equals(username);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
    }

    private BorrowingHistoryDTO getUserBorrowingHistoryInternal(User user) {
        // Get all borrowings for the user
        List<Borrowing> borrowings = borrowingRepository.findByUser(user);

        // Map borrowings to DTOs
        List<BorrowingResponseDTO> borrowingDTOs = borrowings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Count current and overdue borrowings
        int currentBorrowings = (int) borrowings.stream()
                .filter(b -> !b.isReturned())
                .count();

        int overdueBorrowings = (int) borrowings.stream()
                .filter(b -> !b.isReturned() && b.isOverdue())
                .count();

        // Create and return the history DTO
        BorrowingHistoryDTO historyDTO = new BorrowingHistoryDTO();
        historyDTO.setUserId(user.getId());
        historyDTO.setUsername(user.getUsername());
        historyDTO.setBorrowings(borrowingDTOs);
        historyDTO.setTotalBorrowings(borrowings.size());
        historyDTO.setCurrentBorrowings(currentBorrowings);
        historyDTO.setOverdueBorrowings(overdueBorrowings);

        return historyDTO;
    }

    private BorrowingResponseDTO mapToDTO(Borrowing borrowing) {
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(borrowing.getId());
        dto.setBookId(borrowing.getBook().getId());
        dto.setBookTitle(borrowing.getBook().getTitle());
        dto.setBookIsbn(borrowing.getBook().getIsbn());
        dto.setUserId(borrowing.getUser().getId());
        dto.setUsername(borrowing.getUser().getUsername());
        dto.setBorrowDate(borrowing.getBorrowDate());
        dto.setDueDate(borrowing.getDueDate());
        dto.setReturnDate(borrowing.getReturnDate());
        dto.setReturned(borrowing.isReturned());
        dto.setOverdue(borrowing.isOverdue());
        return dto;
    }
}
