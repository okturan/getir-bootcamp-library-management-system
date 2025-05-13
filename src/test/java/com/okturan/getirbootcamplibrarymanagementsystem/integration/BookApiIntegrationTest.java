package com.okturan.getirbootcamplibrarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
// Import UserDetails and CustomUserDetailsService for creating a proper Authentication object
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.security.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
// Import Spring Security Test utility
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional; // Keep @Transactional

import java.time.LocalDate;
import java.util.UUID;

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

    // BookRepository and UserRepository are not strictly needed for @AfterEach with @Transactional
    // but kept if you decide to remove @Transactional later and need manual cleanup.
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    private String adminToken;
    private String patronToken;
    private Long createdBookId; // Book created in setUp for general use in tests
    private String patronUsernameForSetup; // Username for patron created in setUp
    private String patronEmailForSetup;    // Email for patron created in setUp
    private Authentication adminAuthentication;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.findAndRegisterModules();

        // Use System.nanoTime() or UUID for truly unique names if tests run in parallel
        // or if @Transactional is ever removed without proper @DirtiesContext.
        // With @Transactional on class, these are effectively reset per test method.
        patronUsernameForSetup = "testpatron_" + System.nanoTime();
        patronEmailForSetup = patronUsernameForSetup + "@example.com";

        adminToken = loginAsAdminAndGetToken();
        patronToken = registerAndLoginPatron(patronUsernameForSetup, patronEmailForSetup);

        UserDetails adminUserDetails = customUserDetailsService.loadUserByUsername("admin");
        adminAuthentication = new UsernamePasswordAuthenticationToken(
                adminUserDetails, null, adminUserDetails.getAuthorities());

        createTestBookWithDirectAuth();
    }

    private String loginAsAdminAndGetToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO("admin", "admin123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    // Parameterized for flexibility, though in @BeforeEach it uses instance fields
    private String registerAndLoginPatron(String username, String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO(
                                        username,
                                        "password123",
                                        email
                                ))))
                .andExpect(status().isCreated());

        LoginDTO loginDTO = new LoginDTO(username, "password123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    private void createTestBookWithDirectAuth() throws Exception {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
                "Test Book",
                "Test Author",
                "978-1-56619-909-4", // Valid ISBN for setup book
                LocalDate.of(2020, 1, 1),
                "Test Genre"
        );

        MvcResult result = mockMvc.perform(post("/api/books")
                                                   .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuthentication))
                                                   .contentType(MediaType.APPLICATION_JSON)
                                                   .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdBookId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createBook_ShouldCreateBook_WhenCalledByAdmin_UsingDirectAuth() throws Exception {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
                "New Test Book Direct Auth",
                "New Test Author Direct Auth",
                "978-0-306-40615-7", // Valid ISBN
                LocalDate.of(2021, 2, 2),
                "New Test Genre Direct Auth"
        );
        mockMvc.perform(post("/api/books")
                                .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuthentication))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test Book Direct Auth"));
    }

    @Test
    void createBook_ShouldCreateBook_WhenCalledByAdmin_UsingToken() throws Exception {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
                "New Test Book Token",
                "New Test Author Token",
                "978-3-16-148410-0", // Changed to a known valid ISBN (from BookRepositoryTest)
                LocalDate.of(2021, 2, 3),
                "New Test Genre Token"
        );

        mockMvc.perform(post("/api/books")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Test Book Token"));
    }


    @Test
    void createBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        BookRequestDTO bookRequestDTO = new BookRequestDTO(
                "Patron Book",
                "Patron Author",
                "978-0-7432-7356-5", // Another valid ISBN
                LocalDate.of(2021, 3, 3),
                "Patron Genre"
        );
        mockMvc.perform(post("/api/books")
                                .header("Authorization", patronToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBookById_ShouldReturnBook_WhenBookExists() throws Exception {
        mockMvc.perform(get("/api/books/" + createdBookId)
                                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdBookId))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("978-1-56619-909-4")); // Check specific ISBN from setup
    }

    // ... (other tests: getBookById_ShouldReturnNotFound_WhenBookDoesNotExist, getAllBooks_ShouldReturnBooks, searchBooks_ShouldReturnMatchingBooks)
    // These should be fine as they don't involve creating books with new ISBNs.

    @Test
    void getBookById_ShouldReturnNotFound_WhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/books/999999")
                                .header("Authorization", patronToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllBooks_ShouldReturnBooks() throws Exception {
        mockMvc.perform(get("/api/books")
                                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title").exists());
    }

    @Test
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        mockMvc.perform(get("/api/books/search?title=Test Book")
                                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].title", containsString("Test Book")));
    }
    // ---

    @Test
    void updateBook_ShouldUpdateBook_WhenCalledByAdmin_UsingToken() throws Exception {
        BookRequestDTO updateDTO = new BookRequestDTO(
                "Updated Book Token",
                "Updated Author Token",
                "978-1-56619-909-4", // This is the ISBN of the book created in setUp
                LocalDate.of(2022, 4, 4),
                "Updated Genre Token"
        );
        mockMvc.perform(put("/api/books/" + createdBookId)
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book Token"));
    }

    @Test
    void updateBook_ShouldUpdateBook_WhenCalledByAdmin_UsingDirectAuth() throws Exception {
        BookRequestDTO updateDTO = new BookRequestDTO(
                "Updated Book Direct Auth",
                "Updated Author Direct Auth",
                "978-1-56619-909-4", // This is the ISBN of the book created in setUp
                LocalDate.of(2022, 4, 5),
                "Updated Genre Direct Auth"
        );
        mockMvc.perform(put("/api/books/" + createdBookId)
                                .with(SecurityMockMvcRequestPostProcessors.authentication(adminAuthentication))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book Direct Auth"));
    }

    @Test
    void updateBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        BookRequestDTO updateDTO = new BookRequestDTO(
                "Patron Update",
                "Patron Update",
                "978-1-56619-909-4", // This is the ISBN of the book created in setUp
                LocalDate.of(2022, 5, 5),
                "Patron Update"
        );
        mockMvc.perform(put("/api/books/" + createdBookId)
                                .header("Authorization", patronToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBook_ShouldDeleteBook_WhenCalledByAdmin_UsingToken() throws Exception {
        mockMvc.perform(delete("/api/books/" + createdBookId)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/books/" + createdBookId)
                                .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
        // No need to set createdBookId to null, @Transactional will roll back the deletion.
        // If @Transactional were off, setting to null would be important for subsequent tests' @AfterEach.
    }

    @Test
    void deleteBook_ShouldReturnForbidden_WhenCalledByPatron() throws Exception {
        mockMvc.perform(delete("/api/books/" + createdBookId)
                                .header("Authorization", patronToken))
                .andExpect(status().isForbidden());
    }
}