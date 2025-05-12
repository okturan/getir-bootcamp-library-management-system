package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void findByIsbn_ShouldReturnBook_WhenIsbnExists() {
        // Arrange
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setGenre("Fiction");
        
        entityManager.persist(book);
        entityManager.flush();

        // Act
        Optional<Book> found = bookRepository.findByIsbn("1234567890");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Test Book", found.get().getTitle());
        assertEquals("Test Author", found.get().getAuthor());
    }

    @Test
    void findByIsbn_ShouldReturnEmpty_WhenIsbnDoesNotExist() {
        // Act
        Optional<Book> found = bookRepository.findByIsbn("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByIsbn_ShouldReturnTrue_WhenIsbnExists() {
        // Arrange
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setGenre("Fiction");
        
        entityManager.persist(book);
        entityManager.flush();

        // Act
        boolean exists = bookRepository.existsByIsbn("1234567890");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByIsbn_ShouldReturnFalse_WhenIsbnDoesNotExist() {
        // Act
        boolean exists = bookRepository.existsByIsbn("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_ShouldPersistBook() {
        // Arrange
        Book book = new Book();
        book.setTitle("New Book");
        book.setAuthor("New Author");
        book.setIsbn("9876543210");
        book.setPublicationDate(LocalDate.of(2021, 1, 1));
        book.setGenre("Non-Fiction");

        // Act
        Book saved = bookRepository.save(book);
        
        // Assert
        assertNotNull(saved.getId());
        
        Book found = entityManager.find(Book.class, saved.getId());
        assertNotNull(found);
        assertEquals("New Book", found.getTitle());
        assertEquals("New Author", found.getAuthor());
        assertEquals("9876543210", found.getIsbn());
    }

    @Test
    void delete_ShouldRemoveBook() {
        // Arrange
        Book book = new Book();
        book.setTitle("Book to Delete");
        book.setAuthor("Author to Delete");
        book.setIsbn("5555555555");
        book.setPublicationDate(LocalDate.of(2019, 1, 1));
        book.setGenre("Mystery");
        
        Book persistedBook = entityManager.persist(book);
        entityManager.flush();
        
        // Act
        bookRepository.delete(persistedBook);
        entityManager.flush();
        
        // Assert
        Book found = entityManager.find(Book.class, persistedBook.getId());
        assertNull(found);
    }
}