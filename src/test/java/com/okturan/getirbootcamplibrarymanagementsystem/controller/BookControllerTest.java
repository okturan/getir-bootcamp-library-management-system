package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;
    private List<BookResponseDTO> bookResponseDTOList;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with exception handler
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler())
                .build();

        // Configure ObjectMapper for LocalDate serialization
        objectMapper.findAndRegisterModules();

        // Setup book request DTO
        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Test Book");
        bookRequestDTO.setAuthor("Test Author");
        bookRequestDTO.setIsbn("978-3-16-148410-0");
        bookRequestDTO.setPublicationDate(LocalDate.of(2023, 1, 1));
        bookRequestDTO.setGenre("Test Genre");
        bookRequestDTO.setAvailable(true);

        // Setup book response DTO
        bookResponseDTO = new BookResponseDTO(
                1L,
                "Test Book",
                "Test Author",
                "978-3-16-148410-0",
                LocalDate.of(2023, 1, 1),
                "Test Genre",
                true
        );

        // Setup book response DTO list
        BookResponseDTO book2 = new BookResponseDTO(
                2L,
                "Test Book 2",
                "Test Author 2",
                "0987654321",
                LocalDate.of(2023, 2, 2),
                "Test Genre 2",
                false
        );
        bookResponseDTOList = Arrays.asList(bookResponseDTO, book2);
    }

    @Test
    void createBook_ShouldReturnCreated_WhenValidInput() throws Exception {
        // Arrange
        when(bookService.createBook(any(BookRequestDTO.class))).thenReturn(bookResponseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"));
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Arrange
        when(bookService.getBookById(1L)).thenReturn(bookResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void getBookById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Arrange
        when(bookService.getBookById(99L)).thenThrow(new EntityNotFoundException("Book not found with id: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBookByIsbn_ShouldReturnBook_WhenBookExists() throws Exception {
        // Arrange
        when(bookService.getBookByIsbn("978-3-16-148410-0")).thenReturn(bookResponseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/books/isbn/978-3-16-148410-0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-3-16-148410-0"));
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() throws Exception {
        // Arrange
        when(bookService.getAllBooks()).thenReturn(bookResponseDTOList);

        // Act & Assert
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Test Book 2"));
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook_WhenBookExists() throws Exception {
        // Arrange
        when(bookService.updateBook(eq(1L), any(BookRequestDTO.class))).thenReturn(bookResponseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void deleteBook_ShouldReturnNoContent_WhenBookExists() throws Exception {
        // Arrange
        doNothing().when(bookService).deleteBook(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void findBooksByAuthor_ShouldReturnBooks() throws Exception {
        // Arrange
        when(bookService.findBooksByAuthor("Test Author")).thenReturn(List.of(bookResponseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/books/search/author").param("author", "Test Author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].author").value("Test Author"));
    }

    @Test
    void findBooksByTitle_ShouldReturnBooks() throws Exception {
        // Arrange
        when(bookService.findBooksByTitle("Test Book")).thenReturn(List.of(bookResponseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/books/search/title").param("title", "Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }

    @Test
    void findBooksByGenre_ShouldReturnBooks() throws Exception {
        // Arrange
        when(bookService.findBooksByGenre("Test Genre")).thenReturn(List.of(bookResponseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/books/search/genre").param("genre", "Test Genre"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].genre").value("Test Genre"));
    }

    @Test
    void findBooksByAvailability_ShouldReturnBooks() throws Exception {
        // Arrange
        when(bookService.findBooksByAvailability(true)).thenReturn(List.of(bookResponseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/books/search/available").param("available", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].available").value(true));
    }
}
