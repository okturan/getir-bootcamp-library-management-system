package com.okturan.getirbootcamplibrarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class BookApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    private String adminToken;
    private String patronToken;
    private Long createdBookId;

    @BeforeEach
    void setUp() throws Exception {
        // Configure ObjectMapper for Java records and LocalDate
        objectMapper.findAndRegisterModules();

        // Login as admin and patron
        adminToken = loginAsAdmin();
        patronToken = registerAndLoginPatron();

        // Create a test book
        createTestBook();
    }

    private String loginAsAdmin() throws Exception {
        // Admin user should already exist from AdminUserInitializer
        LoginDTO loginDTO = new LoginDTO("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    private String registerAndLoginPatron() throws Exception {
        // Register a new patron
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO(
                    "testpatron",
                    "password123",
                    "patron@example.com"
                ))))
                .andExpect(status().isCreated());

        // Login as the new patron
        LoginDTO loginDTO = new LoginDTO("testpatron", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    private void createTestBook() throws Exception {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
            "Test Book",
            "Test Author",
            "978-1-56619-909-4",
            LocalDate.of(2020, 1, 1),
            "Test Genre"
        );

        MvcResult result = mockMvc.perform(post("/api/books")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdBookId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createBook_ShouldCreateBook_WhenCalledByAdmin() throws Exception {
        // Arrange
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
            "New Test Book",
            "New Test Author",
            "978-0-306-40615-7",
            LocalDate.of(2021, 2, 2),
            "New Test Genre"
        );

        // Act & Assert
        mockMvc.perform(post("/api/books")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Test Book"))
                .andExpect(jsonPath("$.author").value("New Test Author"));
    }

    @Test
    void createBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        // Arrange
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
            "Patron Book",
            "Patron Author",
            "978-0-306-40615-8",
            LocalDate.of(2021, 3, 3),
            "Patron Genre"
        );

        // Act & Assert
        mockMvc.perform(post("/api/books")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/" + createdBookId)
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdBookId))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.isbn").value("978-1-56619-909-4"))
                .andExpect(jsonPath("$.genre").value("Test Genre"));
    }

    @Test
    void getBookById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/999999")
                .header("Authorization", patronToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBooks_ShouldReturnBooks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].author").exists())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/books/search?title=Test")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", containsString("Test")));
    }

    @Test
    void updateBook_ShouldUpdateBook_WhenCalledByAdmin() throws Exception {
        // Arrange
        BookRequestDTO updateDTO = new BookRequestDTO(
            "Updated Book",
            "Updated Author",
            "978-1-56619-909-4", // Same ISBN
            LocalDate.of(2022, 4, 4),
            "Updated Genre"
        );

        // Act & Assert
        mockMvc.perform(put("/api/books/" + createdBookId)
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdBookId))
                .andExpect(jsonPath("$.title").value("Updated Book"))
                .andExpect(jsonPath("$.author").value("Updated Author"))
                .andExpect(jsonPath("$.genre").value("Updated Genre"));
    }

    @Test
    void updateBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        // Arrange
        BookRequestDTO updateDTO = new BookRequestDTO(
            "Patron Update",
            "Patron Update",
            "978-1-56619-909-4",
            LocalDate.of(2022, 5, 5),
            "Patron Update"
        );

        // Act & Assert
        mockMvc.perform(put("/api/books/" + createdBookId)
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_ShouldDeleteBook_WhenCalledByAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/books/" + createdBookId)
                .header("Authorization", adminToken))
                .andExpect(status().isNoContent());

        // Verify book is deleted
        mockMvc.perform(get("/api/books/" + createdBookId)
                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/books/" + createdBookId)
                .header("Authorization", patronToken))
                .andExpect(status().isForbidden());
    }
}
