package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;

    @BeforeEach
    void setUp() {
        // Create test books
        book1 = new Book();
        book1.setTitle("The Great Gatsby");
        book1.setAuthor("F. Scott Fitzgerald");
        book1.setIsbn("978-3-16-148410-0");
        book1.setPublicationDate(LocalDate.of(1925, 4, 10));
        book1.setGenre("Classic");
        book1.setAvailable(true);

        book2 = new Book();
        book2.setTitle("To Kill a Mockingbird");
        book2.setAuthor("Harper Lee");
        book2.setIsbn("978-0-06-112008-4");
        book2.setPublicationDate(LocalDate.of(1960, 7, 11));
        book2.setGenre("Fiction");
        book2.setAvailable(true);

        book3 = new Book();
        book3.setTitle("1984");
        book3.setAuthor("George Orwell");
        book3.setIsbn("978-0-452-28423-4");
        book3.setPublicationDate(LocalDate.of(1949, 6, 8));
        book3.setGenre("Dystopian");
        book3.setAvailable(false);

        // Save the books using the entity manager
        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.persist(book3);
        entityManager.flush();
    }

    @Test
    void findByIsbn_ShouldReturnBook_WhenBookExists() {
        // Act
        Optional<Book> foundBook = bookRepository.findByIsbn("978-3-16-148410-0");

        // Assert
        assertTrue(foundBook.isPresent());
        assertEquals("The Great Gatsby", foundBook.get().getTitle());
        assertEquals("F. Scott Fitzgerald", foundBook.get().getAuthor());
    }

    @Test
    void findByIsbn_ShouldReturnEmpty_WhenBookDoesNotExist() {
        // Act
        Optional<Book> foundBook = bookRepository.findByIsbn("nonexistent");

        // Assert
        assertFalse(foundBook.isPresent());
    }

    @Test
    void findByAuthorContainingIgnoreCase_ShouldReturnBooks_WhenAuthorMatches() {
        // Act
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase("fitzgerald");

        // Assert
        assertEquals(1, books.size());
        assertEquals("The Great Gatsby", books.get(0).getTitle());
    }

    @Test
    void findByAuthorContainingIgnoreCase_ShouldReturnEmptyList_WhenNoAuthorMatches() {
        // Act
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase("nonexistent");

        // Assert
        assertTrue(books.isEmpty());
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnBooks_WhenTitleMatches() {
        // Act
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase("great");

        // Assert
        assertEquals(1, books.size());
        assertEquals("The Great Gatsby", books.get(0).getTitle());
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnEmptyList_WhenNoTitleMatches() {
        // Act
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase("nonexistent");

        // Assert
        assertTrue(books.isEmpty());
    }

    @Test
    void findByGenreContainingIgnoreCase_ShouldReturnBooks_WhenGenreMatches() {
        // Act
        List<Book> books = bookRepository.findByGenreContainingIgnoreCase("classic");

        // Assert
        assertEquals(1, books.size());
        assertEquals("The Great Gatsby", books.get(0).getTitle());
    }

    @Test
    void findByGenreContainingIgnoreCase_ShouldReturnEmptyList_WhenNoGenreMatches() {
        // Act
        List<Book> books = bookRepository.findByGenreContainingIgnoreCase("nonexistent");

        // Assert
        assertTrue(books.isEmpty());
    }

    @Test
    void findByAvailable_ShouldReturnBooks_WhenAvailabilityMatches() {
        // Act
        List<Book> availableBooks = bookRepository.findByAvailable(true);
        List<Book> unavailableBooks = bookRepository.findByAvailable(false);

        // Assert
        assertEquals(2, availableBooks.size());
        assertEquals(1, unavailableBooks.size());
        assertEquals("1984", unavailableBooks.get(0).getTitle());
    }
}
