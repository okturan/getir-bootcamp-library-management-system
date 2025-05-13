package com.okturan.getirbootcamplibrarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
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
public class BorrowingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    private String adminToken;
    private String patronToken;
    private Long createdBookId;
    private Long patronUserId;
    private Long borrowingId;

    @BeforeEach
    void setUp() throws Exception {
        // Configure ObjectMapper for Java records and LocalDate
        objectMapper.findAndRegisterModules();

        // Login as admin and patron
        adminToken = loginAsAdmin();
        patronToken = registerAndLoginPatron();

        // Create a test book
        createTestBook();
        
        // Get patron user ID
        getPatronUserId();
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
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO(
            "testpatron",
            "password123",
            "patron@example.com"
        );
        
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
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
            "Test Book for Borrowing",
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
    
    private void getPatronUserId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users/me")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andReturn();
                
        String response = result.getResponse().getContentAsString();
        patronUserId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void borrowBook_ShouldCreateBorrowing_WhenCalledByPatron() throws Exception {
        // Arrange
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null // Patron borrowing for themselves
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bookId").value(createdBookId))
                .andExpect(jsonPath("$.userId").value(patronUserId))
                .andExpect(jsonPath("$.returned").value(false))
                .andExpect(jsonPath("$.overdue").value(false))
                .andReturn();
                
        // Save borrowing ID for later tests
        String response = result.getResponse().getContentAsString();
        borrowingId = objectMapper.readTree(response).get("id").asLong();
    }
    
    @Test
    void borrowBook_ShouldCreateBorrowing_WhenCalledByAdminForPatron() throws Exception {
        // Arrange
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            patronUserId // Admin borrowing for a patron
        );

        // Act & Assert
        mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bookId").value(createdBookId))
                .andExpect(jsonPath("$.userId").value(patronUserId))
                .andExpect(jsonPath("$.returned").value(false))
                .andExpect(jsonPath("$.overdue").value(false));
    }
    
    @Test
    void getBorrowingById_ShouldReturnBorrowing_WhenCalledByOwner() throws Exception {
        // First borrow a book
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String response = result.getResponse().getContentAsString();
        Long borrowingId = objectMapper.readTree(response).get("id").asLong();
        
        // Now get the borrowing by ID
        mockMvc.perform(get("/api/borrowings/" + borrowingId)
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(borrowingId))
                .andExpect(jsonPath("$.bookId").value(createdBookId))
                .andExpect(jsonPath("$.userId").value(patronUserId));
    }
    
    @Test
    void getBorrowingById_ShouldReturnBorrowing_WhenCalledByAdmin() throws Exception {
        // First borrow a book as patron
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String response = result.getResponse().getContentAsString();
        Long borrowingId = objectMapper.readTree(response).get("id").asLong();
        
        // Now get the borrowing by ID as admin
        mockMvc.perform(get("/api/borrowings/" + borrowingId)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(borrowingId))
                .andExpect(jsonPath("$.bookId").value(createdBookId))
                .andExpect(jsonPath("$.userId").value(patronUserId));
    }
    
    @Test
    void returnBook_ShouldUpdateBorrowing_WhenCalledByOwner() throws Exception {
        // First borrow a book
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String response = result.getResponse().getContentAsString();
        Long borrowingId = objectMapper.readTree(response).get("id").asLong();
        
        // Now return the book
        mockMvc.perform(post("/api/borrowings/" + borrowingId + "/return")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(borrowingId))
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());
    }
    
    @Test
    void returnBook_ShouldUpdateBorrowing_WhenCalledByAdmin() throws Exception {
        // First borrow a book as patron
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andReturn();
                
        String response = result.getResponse().getContentAsString();
        Long borrowingId = objectMapper.readTree(response).get("id").asLong();
        
        // Now return the book as admin
        mockMvc.perform(post("/api/borrowings/" + borrowingId + "/return")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(borrowingId))
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());
    }
    
    @Test
    void getUserBorrowingHistory_ShouldReturnHistory_WhenCalledByAdmin() throws Exception {
        // First borrow a book as patron
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated());
                
        // Now get the borrowing history as admin
        mockMvc.perform(get("/api/borrowings/users/" + patronUserId + "/history")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(patronUserId))
                .andExpect(jsonPath("$.username").value("testpatron"))
                .andExpect(jsonPath("$.borrowingsPage.content").isArray())
                .andExpect(jsonPath("$.borrowingsPage.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalBorrowings").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.currentBorrowings").value(greaterThanOrEqualTo(1)));
    }
    
    @Test
    void getCurrentUserBorrowingHistory_ShouldReturnHistory_WhenCalledByPatron() throws Exception {
        // First borrow a book as patron
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated());
                
        // Now get the borrowing history
        mockMvc.perform(get("/api/borrowings/history")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(patronUserId))
                .andExpect(jsonPath("$.username").value("testpatron"))
                .andExpect(jsonPath("$.borrowingsPage.content").isArray())
                .andExpect(jsonPath("$.borrowingsPage.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.totalBorrowings").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.currentBorrowings").value(greaterThanOrEqualTo(1)));
    }
    
    @Test
    void getAllActiveBorrowings_ShouldReturnActiveBorrowings_WhenCalledByAdmin() throws Exception {
        // First borrow a book as patron
        BorrowingRequestDTO borrowingRequestDTO = new BorrowingRequestDTO(
            createdBookId,
            null
        );
        
        mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated());
                
        // Now get all active borrowings as admin
        mockMvc.perform(get("/api/borrowings/active")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].returned").value(false));
    }
    
    @Test
    void getAllOverdueBorrowings_ShouldReturnOverdueBorrowings_WhenCalledByAdmin() throws Exception {
        // This test is a bit tricky since we can't easily create overdue borrowings in a test
        // We'll just verify the endpoint works and returns the expected structure
        
        mockMvc.perform(get("/api/borrowings/overdue")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }
}