package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;

/**
 * Mapper interface for converting between Borrowing entity and DTOs
 */
@Mapper(componentModel = "spring")
public interface BorrowingMapper {

    /**
     * Map Borrowing entity to BorrowingResponseDTO
     */
    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookIsbn", source = "book.isbn")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    BorrowingResponseDTO mapToDTO(Borrowing borrowing);

    /**
     * Initialize a Borrowing entity for a new borrowing
     */
    @Named("initBorrowing")
    default void initBorrowing(Borrowing borrowing, Book book, User user, BorrowingRequestDTO request) {
        borrowing.setBook(book);
        borrowing.setUser(user);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(request.dueDate());
        borrowing.setReturned(false);
    }

    /**
     * Update a Borrowing entity for a book return
     */
    @Named("returnBook")
    default void returnBook(Borrowing borrowing) {
        borrowing.setReturned(true);
        borrowing.setReturnDate(LocalDate.now());
    }
}
