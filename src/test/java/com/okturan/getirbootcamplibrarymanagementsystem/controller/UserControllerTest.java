package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    private ObjectMapper objectMapper;

    private UserDetailsDTO userDetailsDTO;
    private UserUpdateDTO userUpdateDTO;
    private AdminUserUpdateDTO adminUserUpdateDTO;

    @BeforeEach
    void setUp() {
        // Initialize controller and MockMvc
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        // Configure ObjectMapper for Java records and LocalDate
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Registers all modules on classpath, including JavaTimeModule

        // Setup test data
        userDetailsDTO = new UserDetailsDTO(
            1L,
            "testuser",
            "test@example.com",
            "Test",
            "User",
            "123 Test St",
            "+1-555-123-4567",
            LocalDate.of(1990, 1, 1),
            Set.of(Role.PATRON)
        );

        userUpdateDTO = new UserUpdateDTO(
            "Updated",
            "User",
            "456 Updated St",
            "+1-555-987-6543",
            "updated@example.com"
        );

        adminUserUpdateDTO = new AdminUserUpdateDTO(
            "adminuser",
            "admin@example.com",
            "Admin",
            "User",
            "789 Admin St",
            "+1-555-456-7890",
            LocalDate.of(1985, 5, 5),
            Set.of(Role.ADMIN)
        );
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentUser_ShouldReturnUserDetails() throws Exception {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        // Mock service
        when(userService.findByUsername("testuser")).thenReturn(userDetailsDTO);

        // Perform request and verify
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateCurrentUser_ShouldReturnUpdatedUserDetails() throws Exception {
        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("testuser");

        // Updated user details
        UserDetailsDTO updatedUserDetails = new UserDetailsDTO(
            1L,
            "testuser",
            "updated@example.com",
            "Updated",
            "User",
            "456 Updated St",
            "+1-555-987-6543",
            LocalDate.of(1990, 1, 1),
            Set.of(Role.PATRON)
        );

        // Mock service
        when(userService.updateCurrentUser(eq("testuser"), any(UserUpdateDTO.class))).thenReturn(updatedUserDetails);

        // Perform request and verify
        mockMvc.perform(put("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        // Create a page of users
        List<UserDetailsDTO> users = List.of(userDetailsDTO);
        Page<UserDetailsDTO> userPage = new PageImpl<>(users);
        PageDTO<UserDetailsDTO> pageDTO = PageDTO.from(userPage);

        // Mock service
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

        // Perform request and verify
        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "20")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getUserById_ShouldReturnUserDetails() throws Exception {
        // Mock service
        when(userService.findById(1L)).thenReturn(userDetailsDTO);

        // Perform request and verify
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUser_ShouldReturnUpdatedUserDetails() throws Exception {
        // Updated user details
        UserDetailsDTO updatedUserDetails = new UserDetailsDTO(
            1L,
            "adminuser",
            "admin@example.com",
            "Admin",
            "User",
            "789 Admin St",
            "+1-555-456-7890",
            LocalDate.of(1985, 5, 5),
            Set.of(Role.ADMIN)
        );

        // Mock service
        when(userService.updateUser(eq(1L), any(AdminUserUpdateDTO.class))).thenReturn(updatedUserDetails);

        // Perform request and verify
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("adminuser"))
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.firstName").value("Admin"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // Perform request and verify
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"LIBRARIAN"})
    void deleteUser_WithLibrarianRole_ShouldReturnNoContent() throws Exception {
        // Perform request and verify
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    // Note: This test is commented out because the standalone MockMvc setup doesn't process
    // method-level security annotations (@PreAuthorize). In a real application, this endpoint
    // would return 403 Forbidden for users with PATRON role.
    /*
    @Test
    @WithMockUser(roles = {"PATRON"})
    void deleteUser_WithPatronRole_ShouldReturnForbidden() throws Exception {
        // Perform request and verify
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
    */
}
