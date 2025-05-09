package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;

import java.util.List;

import reactor.core.publisher.Flux;

public interface BookService {

    List<BookResponseDTO> search(BookSearchFilterDTO filter);

    BookResponseDTO createBook(BookRequestDTO bookRequestDTO);

    BookResponseDTO getBookById(Long id);

    BookResponseDTO getBookByIsbn(String isbn);

    List<BookResponseDTO> getAllBooks();

    BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO);

    void deleteBook(Long id);

    /**
     * Stream real-time book availability updates
     * @return Flux of BookAvailabilityDTO containing availability updates
     */
    Flux<BookAvailabilityDTO> streamBookAvailabilityUpdates();

    void emitAvailabilityUpdate(Book book);
}
