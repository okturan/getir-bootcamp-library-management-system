package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class BorrowingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BorrowingRepository borrowingRepository;

    private User user;
    private Book book1;
    private Book book2;
    private Borrowing activeBorrowing;
    private Borrowing returnedBorrowing;
    private Borrowing overdueBorrowing;

    @BeforeEach
    void setUp() {
        // Create a user
        user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(Role.PATRON));
        entityManager.persist(user);

        // Create books
        book1 = new Book();
        book1.setTitle("Book 1");
        book1.setAuthor("Author 1");
        book1.setIsbn("1111111111");
        book1.setPublicationDate(LocalDate.of(2020, 1, 1));
        book1.setGenre("Fiction");
        entityManager.persist(book1);

        book2 = new Book();
        book2.setTitle("Book 2");
        book2.setAuthor("Author 2");
        book2.setIsbn("2222222222");
        book2.setPublicationDate(LocalDate.of(2021, 1, 1));
        book2.setGenre("Non-Fiction");
        entityManager.persist(book2);

        // Create an active borrowing
        activeBorrowing = new Borrowing();
        activeBorrowing.setBook(book1);
        activeBorrowing.setUser(user);
        activeBorrowing.setBorrowDate(LocalDate.now().minusDays(5));
        activeBorrowing.setDueDate(LocalDate.now().plusDays(9));
        activeBorrowing.setReturned(false);
        entityManager.persist(activeBorrowing);

        // Create a returned borrowing
        returnedBorrowing = new Borrowing();
        returnedBorrowing.setBook(book2);
        returnedBorrowing.setUser(user);
        returnedBorrowing.setBorrowDate(LocalDate.now().minusDays(20));
        returnedBorrowing.setDueDate(LocalDate.now().minusDays(6));
        returnedBorrowing.setReturned(true);
        returnedBorrowing.setReturnDate(LocalDate.now().minusDays(7));
        entityManager.persist(returnedBorrowing);

        // Create an overdue borrowing
        overdueBorrowing = new Borrowing();
        overdueBorrowing.setBook(book2);
        overdueBorrowing.setUser(user);
        overdueBorrowing.setBorrowDate(LocalDate.now().minusDays(20));
        overdueBorrowing.setDueDate(LocalDate.now().minusDays(6));
        overdueBorrowing.setReturned(false);
        entityManager.persist(overdueBorrowing);

        entityManager.flush();
    }

    @Test
    void findByUser_ShouldReturnUserBorrowings() {
        // Act
        Page<Borrowing> borrowings = borrowingRepository.findByUser(user, PageRequest.of(0, 10));

        // Assert
        assertEquals(3, borrowings.getTotalElements());
    }

    @Test
    void findByReturned_ShouldReturnActiveBorrowings_WhenReturnedIsFalse() {
        // Act
        Page<Borrowing> activeBorrowings = borrowingRepository.findByReturned(false, PageRequest.of(0, 10));

        // Assert
        assertEquals(2, activeBorrowings.getTotalElements());
        assertTrue(activeBorrowings.getContent().contains(activeBorrowing));
        assertTrue(activeBorrowings.getContent().contains(overdueBorrowing));
    }

    @Test
    void findByReturned_ShouldReturnReturnedBorrowings_WhenReturnedIsTrue() {
        // Act
        Page<Borrowing> returnedBorrowings = borrowingRepository.findByReturned(true, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, returnedBorrowings.getTotalElements());
        assertTrue(returnedBorrowings.getContent().contains(returnedBorrowing));
    }

    @Test
    void findByDueDateBeforeAndReturned_ShouldReturnOverdueBorrowings() {
        // Act
        Page<Borrowing> overdueBorrowings = borrowingRepository.findByDueDateBeforeAndReturned(
                LocalDate.now(), false, PageRequest.of(0, 10));

        // Assert
        assertEquals(1, overdueBorrowings.getTotalElements());
        assertTrue(overdueBorrowings.getContent().contains(overdueBorrowing));
    }

    @Test
    void existsByBookAndReturnedFalse_ShouldReturnTrue_WhenBookIsBorrowed() {
        // Act
        boolean exists = borrowingRepository.existsByBookAndReturnedFalse(book1);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByBookAndReturnedFalse_ShouldReturnFalse_WhenBookIsNotBorrowed() {
        // Create a new book that is not borrowed
        Book unborrowed = new Book();
        unborrowed.setTitle("Unborrowed Book");
        unborrowed.setAuthor("Author");
        unborrowed.setIsbn("3333333333");
        unborrowed.setPublicationDate(LocalDate.of(2022, 1, 1));
        unborrowed.setGenre("Mystery");
        entityManager.persist(unborrowed);
        entityManager.flush();

        // Act
        boolean exists = borrowingRepository.existsByBookAndReturnedFalse(unborrowed);

        // Assert
        assertFalse(exists);
    }

    @Test
    void findBorrowedBookIdsByBookIds_ShouldReturnBorrowedBookIds() {
        // Act
        Set<Long> borrowedIds = borrowingRepository.findBorrowedBookIdsByBookIds(List.of(book1.getId(), book2.getId()));

        // Assert
        assertEquals(2, borrowedIds.size());
        assertTrue(borrowedIds.contains(book1.getId()));
        assertTrue(borrowedIds.contains(book2.getId()));
    }

    @Test
    void countByUser_ShouldReturnTotalBorrowingsCount() {
        // Act
        long count = borrowingRepository.countByUser(user);

        // Assert
        assertEquals(3, count);
    }

    @Test
    void countByUserAndReturnedFalse_ShouldReturnActiveBorrowingsCount() {
        // Act
        long count = borrowingRepository.countByUserAndReturnedFalse(user);

        // Assert
        assertEquals(2, count);
    }

    @Test
    void countByUserAndReturnedFalseAndDueDateBefore_ShouldReturnOverdueBorrowingsCount() {
        // Act
        long count = borrowingRepository.countByUserAndReturnedFalseAndDueDateBefore(user, LocalDate.now());

        // Assert
        assertEquals(1, count);
    }
}