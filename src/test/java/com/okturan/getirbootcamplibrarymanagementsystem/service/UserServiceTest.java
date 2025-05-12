package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.UserMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDetailsDTO userDetailsDTO;
    private UserUpdateDTO userUpdateDTO;
    private AdminUserUpdateDTO adminUserUpdateDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAddress("123 Test St");
        user.setPhoneNumber("+1-555-123-4567");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRoles(Set.of(Role.PATRON));

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
    void registerUser_ShouldRegisterAndReturnUser() {
        // Arrange
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.registerUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        verify(userRepository).save(user);
    }

    @Test
    void findByUsername_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userMapper.mapToDetailsDTO(any(User.class))).thenReturn(userDetailsDTO);

        // Act
        UserDetailsDTO result = userService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());

        verify(userRepository).findByUsername("testuser");
        verify(userMapper).mapToDetailsDTO(user);
    }

    @Test
    void findByUsername_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.findByUsername("nonexistentuser"));

        verify(userRepository).findByUsername("nonexistentuser");
        verifyNoInteractions(userMapper);
    }

    @Test
    void getAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        List<User> users = List.of(user);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Pageable.class))).thenReturn(userPage);
        when(userMapper.mapToDetailsDTO(any(User.class))).thenReturn(userDetailsDTO);

        // Act
        Page<UserDetailsDTO> result = userService.getAllUsers(Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(userDetailsDTO, result.getContent().get(0));

        verify(userRepository).findAll(Pageable.unpaged());
        verify(userMapper).mapToDetailsDTO(user);
    }

    @Test
    void findById_ShouldReturnUserDetails() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userMapper.mapToDetailsDTO(any(User.class))).thenReturn(userDetailsDTO);

        // Act
        UserDetailsDTO result = userService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());

        verify(userRepository).findById(1L);
        verify(userMapper).mapToDetailsDTO(user);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> userService.findById(999L));

        verify(userRepository).findById(999L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUserDetails() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromAdminDto(any(AdminUserUpdateDTO.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDetailsDTO(any(User.class))).thenReturn(userDetailsDTO);

        // Act
        UserDetailsDTO result = userService.updateUser(1L, adminUserUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());

        verify(userRepository).findById(1L);
        verify(userMapper).updateUserFromAdminDto(adminUserUpdateDTO, user);
        verify(userRepository).save(user);
        verify(userMapper).mapToDetailsDTO(user);
    }

    @Test
    void updateCurrentUser_ShouldUpdateAndReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromDto(any(UserUpdateDTO.class), any(User.class));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToDetailsDTO(any(User.class))).thenReturn(userDetailsDTO);

        // Act
        UserDetailsDTO result = userService.updateCurrentUser("testuser", userUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());

        verify(userRepository).findByUsername("testuser");
        verify(userMapper).updateUserFromDto(userUpdateDTO, user);
        verify(userRepository).save(user);
        verify(userMapper).mapToDetailsDTO(user);
    }
}
