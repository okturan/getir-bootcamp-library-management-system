package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BorrowingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BorrowingService borrowingService;

    private ObjectMapper objectMapper;

    private BorrowingRequestDTO borrowingRequestDTO;
    private BorrowingResponseDTO borrowingResponseDTO;
    private BorrowingHistoryDTO borrowingHistoryDTO;

    @BeforeEach
    void setUp() {
        // Initialize controller and MockMvc
        BorrowingController borrowingController = new BorrowingController(borrowingService);
        mockMvc = MockMvcBuilders.standaloneSetup(borrowingController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        // Configure ObjectMapper for Java records and LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test data
        borrowingRequestDTO = new BorrowingRequestDTO(1L, null);

        borrowingResponseDTO = new BorrowingResponseDTO(
            1L,
            1L,
            "Test Book",
            "978-3-16-148410-0",
            1L,
            "testuser",
            LocalDate.now(),
            LocalDate.now().plusDays(14),
            null,
            false,
            false
        );

        List<BorrowingResponseDTO> borrowings = List.of(borrowingResponseDTO);
        Page<BorrowingResponseDTO> borrowingsPage = new PageImpl<>(borrowings);

        borrowingHistoryDTO = new BorrowingHistoryDTO(
            1L,
            "testuser",
            PageDTO.from(borrowingsPage),
            1,
            1,
            0
        );
    }

    @Test
    @WithMockUser(username = "testuser")
    void borrowBook_ShouldReturnCreatedBorrowing() throws Exception {
        when(borrowingService.borrowBook(any(BorrowingRequestDTO.class))).thenReturn(borrowingResponseDTO);

        mockMvc.perform(post("/api/borrowings/borrow")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(borrowingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.returned").value(false));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void returnBook_ShouldReturnUpdatedBorrowing() throws Exception {
        // Create a returned borrowing response
        BorrowingResponseDTO returnedBorrowing = new BorrowingResponseDTO(
            1L,
            1L,
            "Test Book",
            "978-3-16-148410-0",
            1L,
            "testuser",
            LocalDate.now(),
            LocalDate.now().plusDays(14),
            LocalDate.now(),
            true,
            false
        );

        when(borrowingService.returnBook(1L)).thenReturn(returnedBorrowing);

        mockMvc.perform(post("/api/borrowings/1/return"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"ADMIN"})
    void getBorrowingById_ShouldReturnBorrowing() throws Exception {
        when(borrowingService.getBorrowingById(1L)).thenReturn(borrowingResponseDTO);

        mockMvc.perform(get("/api/borrowings/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUserBorrowingHistory_ShouldReturnHistory() throws Exception {
        when(borrowingService.getCurrentUserBorrowingHistory(any(Pageable.class))).thenReturn(borrowingHistoryDTO);

        mockMvc.perform(get("/api/borrowings/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.totalBorrowings").value(1))
                .andExpect(jsonPath("$.currentBorrowings").value(1))
                .andExpect(jsonPath("$.overdueBorrowings").value(0))
                .andExpect(jsonPath("$.borrowingsPage.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserBorrowingHistory_ShouldReturnHistory() throws Exception {
        when(borrowingService.getUserBorrowingHistory(eq(1L), any(Pageable.class))).thenReturn(borrowingHistoryDTO);

        mockMvc.perform(get("/api/borrowings/users/1/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.totalBorrowings").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllActiveBorrowings_ShouldReturnActiveBorrowings() throws Exception {
        List<BorrowingResponseDTO> borrowings = List.of(borrowingResponseDTO);
        Page<BorrowingResponseDTO> borrowingsPage = new PageImpl<>(borrowings);

        when(borrowingService.getAllActiveBorrowings(any(Pageable.class))).thenReturn(borrowingsPage);

        mockMvc.perform(get("/api/borrowings/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].returned").value(false));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllOverdueBorrowings_ShouldReturnOverdueBorrowings() throws Exception {
        // Create an overdue borrowing
        BorrowingResponseDTO overdueBorrowing = new BorrowingResponseDTO(
            2L,
            1L,
            "Test Book",
            "978-3-16-148410-0",
            1L,
            "testuser",
            LocalDate.now().minusDays(30),
            LocalDate.now().minusDays(16),
            null,
            false,
            true
        );

        List<BorrowingResponseDTO> borrowings = List.of(overdueBorrowing);
        Page<BorrowingResponseDTO> borrowingsPage = new PageImpl<>(borrowings);

        when(borrowingService.getAllOverdueBorrowings(any(Pageable.class))).thenReturn(borrowingsPage);

        mockMvc.perform(get("/api/borrowings/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].overdue").value(true));
    }
}
