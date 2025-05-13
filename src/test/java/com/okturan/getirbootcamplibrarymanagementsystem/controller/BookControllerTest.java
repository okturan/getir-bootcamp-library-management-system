package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookSearchFilterDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    private ObjectMapper objectMapper;

    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;
    private BookSearchFilterDTO bookSearchFilterDTO;
    private BookAvailabilityDTO bookAvailabilityDTO;

    @BeforeEach
    void setUp() {
        // Initialize controller and MockMvc
        BookController bookController = new BookController(bookService);
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        // Configure ObjectMapper for Java records and LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test data
        bookRequestDTO = new BookRequestDTO(
            "Test Book",
            "Test Author",
            "978-3-16-148410-0",
            LocalDate.of(2020, 1, 1),
            "Fiction"
        );

        bookResponseDTO = new BookResponseDTO(
            1L,
            "Test Book",
            "Test Author",
            "978-3-16-148410-0",
            LocalDate.of(2020, 1, 1),
            "Fiction",
            true
        );

        bookSearchFilterDTO = new BookSearchFilterDTO(
            Optional.of("Test Author"),
            Optional.of("Test Book"),
            Optional.of("Fiction"),
            Optional.of(true)
        );

        bookAvailabilityDTO = new BookAvailabilityDTO(
            1L,
            "Test Book",
            "978-3-16-148410-0",
            true,
            "2023-05-15T14:30:45.123Z"
        );
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createBook_ShouldReturnCreatedBook() throws Exception {
        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(bookResponseDTO);

        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"))
                .andExpect(jsonPath("$.genre").value("Fiction"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getBookById_ShouldReturnBook() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(bookResponseDTO);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void getBookByIsbn_ShouldReturnBook() throws Exception {
        when(bookService.getBookByIsbn("978-3-16-148410-0")).thenReturn(bookResponseDTO);

        mockMvc.perform(get("/api/books/isbn/978-3-16-148410-0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"));
    }

    @Test
    void getAllBooks_ShouldReturnPageOfBooks() throws Exception {
        List<BookResponseDTO> books = List.of(bookResponseDTO);
        Page<BookResponseDTO> bookPage = new PageImpl<>(books);

        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    void searchBooks_ShouldReturnFilteredBooks() throws Exception {
        List<BookResponseDTO> books = List.of(bookResponseDTO);
        Page<BookResponseDTO> bookPage = new PageImpl<>(books);

        when(bookService.search(any(BookSearchFilterDTO.class), any(Pageable.class))).thenReturn(bookPage);

        mockMvc.perform(get("/api/books/search")
                .param("author", "Test Author")
                .param("title", "Test Book")
                .param("genre", "Fiction")
                .param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateBook_ShouldReturnUpdatedBook() throws Exception {
        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class))).thenReturn(bookResponseDTO);

        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBook_ShouldReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    // Note: Testing the streamBookAvailability endpoint would require a different approach
    // as it uses Server-Sent Events (SSE) which are not easily testable with MockMvc.
    // For a complete test, we would need to use WebTestClient or a similar tool.
}
