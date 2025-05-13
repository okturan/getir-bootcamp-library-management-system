package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AuthResultDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    private ObjectMapper objectMapper;

    private UserRegistrationDTO userRegistrationDTO;
    private AdminUserRegistrationDTO adminUserRegistrationDTO;
    private LoginDTO loginDTO;
    private AuthResultDTO authResultDTO;

    @BeforeEach
    void setUp() {
        // Initialize controller and MockMvc
        AuthController authController = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        
        // Configure ObjectMapper for Java records
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        
        // Setup test data
        userRegistrationDTO = new UserRegistrationDTO(
            "testuser",
            "password123",
            "test@example.com"
        );
        
        adminUserRegistrationDTO = new AdminUserRegistrationDTO(
            "adminuser",
            "password123",
            "admin@example.com",
            Role.LIBRARIAN
        );
        
        loginDTO = new LoginDTO(
            "testuser",
            "password123"
        );
        
        authResultDTO = new AuthResultDTO(
            "test-jwt-token",
            "testuser",
            1L,
            Set.of(Role.PATRON)
        );
    }

    @Test
    void registerPatron_ShouldReturnJwtResponse() throws Exception {
        when(authService.registerPatron(any(UserRegistrationDTO.class))).thenReturn(authResultDTO);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("PATRON"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void registerWithRole_ShouldReturnJwtResponse() throws Exception {
        // Create an auth result with LIBRARIAN role
        AuthResultDTO librarianAuthResult = new AuthResultDTO(
            "test-jwt-token",
            "adminuser",
            2L,
            Set.of(Role.LIBRARIAN)
        );
        
        when(authService.registerWithRole(any(AdminUserRegistrationDTO.class))).thenReturn(librarianAuthResult);

        mockMvc.perform(post("/api/auth/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.roles[0]").value("LIBRARIAN"));
    }

    @Test
    void login_ShouldReturnJwtResponse() throws Exception {
        when(authService.login(any(LoginDTO.class))).thenReturn(authResultDTO);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("PATRON"));
    }
}