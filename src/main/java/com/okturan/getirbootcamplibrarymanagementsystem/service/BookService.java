package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;

import java.util.List;

import reactor.core.publisher.Flux;

public interface BookService {

    BookResponseDTO createBook(BookRequestDTO bookRequestDTO);

    BookResponseDTO getBookById(Long id);

    BookResponseDTO getBookByIsbn(String isbn);

    List<BookResponseDTO> getAllBooks();

    BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO);

    void deleteBook(Long id);

    List<BookResponseDTO> findBooksByAuthor(String author);

    List<BookResponseDTO> findBooksByTitle(String title);

    List<BookResponseDTO> findBooksByGenre(String genre);

    List<BookResponseDTO> findBooksByAvailability(boolean available);

    /**
     * Stream real-time book availability updates
     * @return Flux of BookAvailabilityDTO containing availability updates
     */
    Flux<BookAvailabilityDTO> streamBookAvailabilityUpdates();

    public void emitAvailabilityUpdate(Book book);
}
