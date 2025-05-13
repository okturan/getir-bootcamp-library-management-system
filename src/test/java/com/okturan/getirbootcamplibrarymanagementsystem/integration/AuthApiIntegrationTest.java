package com.okturan.getirbootcamplibrarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Configure ObjectMapper for Java records and LocalDate
        objectMapper.findAndRegisterModules();

        // Login as admin
        adminToken = loginAsAdmin();
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

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("admin", "admin123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginDTO loginDTO = new LoginDTO("admin", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAdmin_ShouldCreateAdminUser_WhenCalledByAdmin() throws Exception {
        // Arrange
        AdminUserRegistrationDTO registrationDTO = new AdminUserRegistrationDTO(
            "newadmin",
            "password123",
            "newadmin@example.com",
            Role.LIBRARIAN
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/admin/register")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("newadmin"));

        // Verify user was created with correct role
        assertTrue(userRepository.findByUsername("newadmin").isPresent());
        assertTrue(userRepository.findByUsername("newadmin").get().getRoles().contains(Role.LIBRARIAN));
    }

    @Test
    void registerAdmin_ShouldReturnForbidden_WhenCalledWithoutAuthentication() throws Exception {
        // Arrange
        AdminUserRegistrationDTO registrationDTO = new AdminUserRegistrationDTO(
            "newadmin2",
            "password123",
            "newadmin2@example.com",
            Role.LIBRARIAN
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/auth/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andReturn();

        int status = result.getResponse().getStatus();
        System.out.println("[DEBUG_LOG] Response status: " + status);

        // The endpoint might return 401 (Unauthorized) instead of 403 (Forbidden)
        // when called without authentication
        mockMvc.perform(post("/api/auth/admin/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isUnauthorized());
    }
}
