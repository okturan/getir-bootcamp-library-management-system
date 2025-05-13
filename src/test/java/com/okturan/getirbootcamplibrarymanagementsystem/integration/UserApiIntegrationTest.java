package com.okturan.getirbootcamplibrarymanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String adminToken;
    private String patronToken;

    @BeforeEach
    void setUp() throws Exception {
        // Configure ObjectMapper for Java records and LocalDate
        objectMapper.findAndRegisterModules();
        
        // Register and login as admin
        adminToken = registerAndLoginAdmin();
        
        // Register and login as patron
        patronToken = registerAndLoginPatron();
    }

    private String registerAndLoginAdmin() throws Exception {
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

    @Test
    void getCurrentUser_ShouldReturnCurrentUserDetails() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("testpatron"))
                .andExpect(jsonPath("$.email").value("patron@example.com"));
    }

    @Test
    void updateCurrentUser_ShouldUpdateAndReturnUserDetails() throws Exception {
        UserUpdateDTO updateDTO = new UserUpdateDTO(
            "Updated",
            "Patron",
            "123 Main St",
            "+1-555-123-4567",
            "updated.patron@example.com"
        );
        
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", patronToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Patron"))
                .andExpect(jsonPath("$.email").value("updated.patron@example.com"));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(org.hamcrest.Matchers.greaterThanOrEqualTo(2))));
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenPatron() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", patronToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnUserDetails_WhenAdmin() throws Exception {
        // First get the user ID of the patron
        MvcResult result = mockMvc.perform(get("/api/users/me")
                .header("Authorization", patronToken))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(response).get("id").asLong();
        
        // Then get the user details as admin
        mockMvc.perform(get("/api/users/" + userId)
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("testpatron"));
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenPatron() throws Exception {
        // First get the user ID of the admin
        MvcResult result = mockMvc.perform(get("/api/users/me")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        Long adminUserId = objectMapper.readTree(response).get("id").asLong();
        
        // Then try to get the admin details as patron
        mockMvc.perform(get("/api/users/" + adminUserId)
                .header("Authorization", patronToken))
                .andExpect(status().isForbidden());
    }
}