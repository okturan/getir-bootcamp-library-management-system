package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mapper class for converting between Book entity and DTOs
 */
@Component
public class BookMapper {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Map Book entity to BookResponseDTO
     */
    public BookResponseDTO mapToDTO(Book book) {
        return new BookResponseDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationDate(),
                book.getGenre(),
                book.isAvailable()
        );
    }

    /**
     * Map BookRequestDTO to Book entity
     */
    public Book mapToEntity(BookRequestDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setGenre(dto.getGenre());
        book.setAvailable(dto.isAvailable());
        return book;
    }

    /**
     * Create a BookAvailabilityDTO from a Book entity
     */
    public BookAvailabilityDTO createAvailabilityDTO(Book book) {
        return new BookAvailabilityDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.isAvailable(),
                LocalDateTime.now().format(DATE_TIME_FORMATTER)
        );
    }
}