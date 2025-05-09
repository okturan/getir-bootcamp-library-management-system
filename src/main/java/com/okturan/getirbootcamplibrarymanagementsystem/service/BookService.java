package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

import java.util.List;

public interface BookService {


	BookResponseDTO createBook(BookRequestDTO bookRequestDTO);

	BookResponseDTO getBookById(Long id);

	BookResponseDTO getBookByIsbn(String isbn);

	Page<BookResponseDTO> getAllBooks(Pageable pageable);

	Page<BookResponseDTO> search(BookSearchFilterDTO filter, Pageable pageable);

	BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO);

	void deleteBook(Long id);

	/**
	 * Stream real-time book availability updates
	 * @return Flux of BookAvailabilityDTO containing availability updates
	 */
	Flux<BookAvailabilityDTO> streamBookAvailabilityUpdates();

	void emitAvailabilityUpdate(Book book);

}
