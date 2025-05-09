package com.okturan.getirbootcamplibrarymanagementsystem.bootstrap;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bootstrap component that generates mock data for development and testing purposes.
 * This initializer runs after the AdminUserInitializer to ensure the admin user exists.
 */
@Component
@Order(2) // Run after AdminUserInitializer
public class MockDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MockDataInitializer.class);
    private static final Random random = new Random();

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${mock.data.enabled:false}")
    private boolean mockDataEnabled;

    public MockDataInitializer(
            UserRepository userRepository,
            BookRepository bookRepository,
            BorrowingRepository borrowingRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!mockDataEnabled) {
            logger.info("Mock data generation is disabled. Skipping...");
            return;
        }

        logger.info("Generating mock data...");

        // Create users
        List<User> users = createMockUsers();
        
        // Create books
        List<Book> books = createMockBooks();
        
        // Create borrowings
        createMockBorrowings(users, books);

        logger.info("Mock data generation completed successfully.");
    }

    private List<User> createMockUsers() {
        logger.info("Creating mock users...");
        List<User> users = new ArrayList<>();

        // Create librarians
        for (int i = 1; i <= 3; i++) {
            User librarian = new User(
                    "librarian" + i,
                    passwordEncoder.encode("password"),
                    "librarian" + i + "@example.com",
                    "Librarian",
                    "User " + i,
                    "123 Library St, Apt " + i,
                    "555-123-" + (1000 + i),
                    LocalDate.now().minusYears(30 + i)
            );
            librarian.addRole(Role.LIBRARIAN);
            users.add(userRepository.save(librarian));
            logger.info("Created librarian: {}", librarian.getUsername());
        }

        // Create patrons
        for (int i = 1; i <= 20; i++) {
            User patron = new User(
                    "patron" + i,
                    passwordEncoder.encode("password"),
                    "patron" + i + "@example.com",
                    "Patron",
                    "User " + i,
                    i + " Reader Ave, Suite " + (100 + i),
                    "555-456-" + (1000 + i),
                    LocalDate.now().minusYears(20 + i % 40)
            );
            patron.addRole(Role.PATRON);
            users.add(userRepository.save(patron));
            logger.info("Created patron: {}", patron.getUsername());
        }

        return users;
    }

    private List<Book> createMockBooks() {
        logger.info("Creating mock books...");
        List<Book> books = new ArrayList<>();

        // Fiction books
        createFictionBooks(books);
        
        // Non-fiction books
        createNonFictionBooks(books);
        
        // Technical books
        createTechnicalBooks(books);

        return books;
    }

    private void createFictionBooks(List<Book> books) {
        // Fantasy
        books.add(createBook("The Lord of the Rings", "J.R.R. Tolkien", "978-0618640157", 
                LocalDate.of(1954, 7, 29), "Fantasy"));
        books.add(createBook("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "978-0747532743", 
                LocalDate.of(1997, 6, 26), "Fantasy"));
        books.add(createBook("A Game of Thrones", "George R.R. Martin", "978-0553573404", 
                LocalDate.of(1996, 8, 1), "Fantasy"));
        
        // Science Fiction
        books.add(createBook("Dune", "Frank Herbert", "978-0441172719", 
                LocalDate.of(1965, 8, 1), "Science Fiction"));
        books.add(createBook("Foundation", "Isaac Asimov", "978-0553293357", 
                LocalDate.of(1951, 5, 1), "Science Fiction"));
        books.add(createBook("Neuromancer", "William Gibson", "978-0441569595", 
                LocalDate.of(1984, 7, 1), "Science Fiction"));
        
        // Mystery
        books.add(createBook("The Hound of the Baskervilles", "Arthur Conan Doyle", "978-0451528018", 
                LocalDate.of(1902, 4, 1), "Mystery"));
        books.add(createBook("Murder on the Orient Express", "Agatha Christie", "978-0062693662", 
                LocalDate.of(1934, 1, 1), "Mystery"));
        books.add(createBook("The Girl with the Dragon Tattoo", "Stieg Larsson", "978-0307454546", 
                LocalDate.of(2005, 8, 1), "Mystery"));
    }

    private void createNonFictionBooks(List<Book> books) {
        // Biography
        books.add(createBook("Steve Jobs", "Walter Isaacson", "978-1451648539", 
                LocalDate.of(2011, 10, 24), "Biography"));
        books.add(createBook("Becoming", "Michelle Obama", "978-1524763138", 
                LocalDate.of(2018, 11, 13), "Biography"));
        books.add(createBook("The Diary of a Young Girl", "Anne Frank", "978-0553577129", 
                LocalDate.of(1947, 6, 25), "Biography"));
        
        // History
        books.add(createBook("Sapiens: A Brief History of Humankind", "Yuval Noah Harari", "978-0062316097", 
                LocalDate.of(2014, 2, 10), "History"));
        books.add(createBook("Guns, Germs, and Steel", "Jared Diamond", "978-0393317558", 
                LocalDate.of(1997, 3, 1), "History"));
        books.add(createBook("A People's History of the United States", "Howard Zinn", "978-0062397348", 
                LocalDate.of(1980, 11, 17), "History"));
        
        // Science
        books.add(createBook("A Brief History of Time", "Stephen Hawking", "978-0553380163", 
                LocalDate.of(1988, 4, 1), "Science"));
        books.add(createBook("The Selfish Gene", "Richard Dawkins", "978-0198788607", 
                LocalDate.of(1976, 5, 1), "Science"));
        books.add(createBook("Silent Spring", "Rachel Carson", "978-0618249060", 
                LocalDate.of(1962, 9, 27), "Science"));
    }

    private void createTechnicalBooks(List<Book> books) {
        // Programming
        books.add(createBook("Clean Code", "Robert C. Martin", "978-0132350884", 
                LocalDate.of(2008, 8, 1), "Programming"));
        books.add(createBook("Design Patterns", "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "978-0201633610", 
                LocalDate.of(1994, 10, 31), "Programming"));
        books.add(createBook("Effective Java", "Joshua Bloch", "978-0134685991", 
                LocalDate.of(2017, 12, 27), "Programming"));
        
        // Data Science
        books.add(createBook("Python for Data Analysis", "Wes McKinney", "978-1491957660", 
                LocalDate.of(2017, 10, 31), "Data Science"));
        books.add(createBook("The Elements of Statistical Learning", "Trevor Hastie, Robert Tibshirani, Jerome Friedman", "978-0387848570", 
                LocalDate.of(2009, 2, 1), "Data Science"));
        books.add(createBook("Deep Learning", "Ian Goodfellow, Yoshua Bengio, Aaron Courville", "978-0262035613", 
                LocalDate.of(2016, 11, 18), "Data Science"));
    }

    private Book createBook(String title, String author, String isbn, LocalDate publicationDate, String genre) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublicationDate(publicationDate);
        book.setGenre(genre);
        
        return bookRepository.save(book);
    }

    private void createMockBorrowings(List<User> users, List<Book> books) {
        logger.info("Creating mock borrowings...");
        
        // Get only patron users
        List<User> patrons = users.stream()
                .filter(user -> user.hasRole(Role.PATRON))
                .toList();
        
        // Create active borrowings (not returned)
        createActiveBorrowings(patrons, books);
        
        // Create returned borrowings
        createReturnedBorrowings(patrons, books);
        
        // Create overdue borrowings
        createOverdueBorrowings(patrons, books);
    }

    private void createActiveBorrowings(List<User> patrons, List<Book> books) {
        // Create 10 active borrowings
        for (int i = 0; i < 10; i++) {
            User patron = patrons.get(random.nextInt(patrons.size()));
            Book book = books.get(random.nextInt(books.size()));
            
            // Skip if book is already borrowed
            if (borrowingRepository.existsByBookAndReturnedFalse(book)) {
                continue;
            }
            
            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(10) + 1);
            LocalDate dueDate = borrowDate.plusDays(14); // 2 weeks loan period
            
            Borrowing borrowing = new Borrowing();
            borrowing.setUser(patron);
            borrowing.setBook(book);
            borrowing.setBorrowDate(borrowDate);
            borrowing.setDueDate(dueDate);
            borrowing.setReturned(false);
            
            borrowingRepository.save(borrowing);
            logger.info("Created active borrowing: {} borrowed by {}", book.getTitle(), patron.getUsername());
        }
    }

    private void createReturnedBorrowings(List<User> patrons, List<Book> books) {
        // Create 15 returned borrowings
        for (int i = 0; i < 15; i++) {
            User patron = patrons.get(random.nextInt(patrons.size()));
            Book book = books.get(random.nextInt(books.size()));
            
            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(30) + 15);
            LocalDate dueDate = borrowDate.plusDays(14); // 2 weeks loan period
            LocalDate returnDate = borrowDate.plusDays(random.nextInt(14) + 1); // Return within loan period
            
            Borrowing borrowing = new Borrowing();
            borrowing.setUser(patron);
            borrowing.setBook(book);
            borrowing.setBorrowDate(borrowDate);
            borrowing.setDueDate(dueDate);
            borrowing.setReturnDate(returnDate);
            borrowing.setReturned(true);
            
            borrowingRepository.save(borrowing);
            logger.info("Created returned borrowing: {} borrowed by {}", book.getTitle(), patron.getUsername());
        }
    }

    private void createOverdueBorrowings(List<User> patrons, List<Book> books) {
        // Create 5 overdue borrowings
        for (int i = 0; i < 5; i++) {
            User patron = patrons.get(random.nextInt(patrons.size()));
            Book book = books.get(random.nextInt(books.size()));
            
            // Skip if book is already borrowed
            if (borrowingRepository.existsByBookAndReturnedFalse(book)) {
                continue;
            }
            
            LocalDate borrowDate = LocalDate.now().minusDays(random.nextInt(30) + 20);
            LocalDate dueDate = borrowDate.plusDays(14); // 2 weeks loan period, now overdue
            
            Borrowing borrowing = new Borrowing();
            borrowing.setUser(patron);
            borrowing.setBook(book);
            borrowing.setBorrowDate(borrowDate);
            borrowing.setDueDate(dueDate);
            borrowing.setReturned(false);
            
            borrowingRepository.save(borrowing);
            logger.info("Created overdue borrowing: {} borrowed by {}", book.getTitle(), patron.getUsername());
        }
    }
}