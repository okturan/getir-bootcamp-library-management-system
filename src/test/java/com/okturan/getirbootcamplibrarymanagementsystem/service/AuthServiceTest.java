package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.UserMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtTokenProvider;
import com.okturan.getirbootcamplibrarymanagementsystem.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserRegistrationDTO userRegistrationDTO;
    private AdminUserRegistrationDTO adminUserRegistrationDTO;
    private LoginDTO loginDTO;
    private User user;
    private UserDetailsDTO userDetailsDTO;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test data
        userRegistrationDTO = new UserRegistrationDTO(
                "testuser",
                "password123",
                "test@example.com"
        );

        adminUserRegistrationDTO = new AdminUserRegistrationDTO(
                "adminuser",
                "admin123",
                "admin@example.com",
                Role.ADMIN
        );

        loginDTO = new LoginDTO("testuser", "password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
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

        authentication = mock(Authentication.class);
    }

    @Test
    void registerPatron_ShouldRegisterAndReturnAuthResult() {
        // Arrange
        when(userMapper.mapToEntity(userRegistrationDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.registerUser(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn("jwt-token");
        when(userService.findByUsername("testuser")).thenReturn(userDetailsDTO);

        // Act
        AuthResultDTO result = authService.registerPatron(userRegistrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals(1L, result.userId());
        assertEquals("jwt-token", result.token());
        assertTrue(result.roles().contains(Role.PATRON));

        verify(userMapper).mapToEntity(userRegistrationDTO);
        verify(passwordEncoder).encode("password123");
        verify(userService).registerUser(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).createToken(authentication);
        verify(userService).findByUsername("testuser");
    }

    @Test
    void registerWithRole_ShouldRegisterAndReturnAuthResult() {
        // Arrange
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("adminuser");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedPassword");
        adminUser.setRoles(Set.of(Role.ADMIN));

        UserDetailsDTO adminDetailsDTO = new UserDetailsDTO(
                2L,
                "adminuser",
                "admin@example.com",
                "Admin",
                "User",
                "123 Admin St",
                "+1-555-123-4567",
                LocalDate.of(1990, 1, 1),
                Set.of(Role.ADMIN)
        );

        when(userMapper.mapToEntity(adminUserRegistrationDTO)).thenReturn(adminUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.registerUser(any(User.class))).thenReturn(adminUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn("admin-jwt-token");
        when(userService.findByUsername("adminuser")).thenReturn(adminDetailsDTO);

        // Act
        AuthResultDTO result = authService.registerWithRole(adminUserRegistrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals("adminuser", result.username());
        assertEquals(2L, result.userId());
        assertEquals("admin-jwt-token", result.token());
        assertTrue(result.roles().contains(Role.ADMIN));

        verify(userMapper).mapToEntity(adminUserRegistrationDTO);
        verify(passwordEncoder).encode("admin123");
        verify(userService).registerUser(adminUser);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).createToken(authentication);
        verify(userService).findByUsername("adminuser");
    }

    @Test
    void login_ShouldAuthenticateAndReturnAuthResult() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.createToken(authentication)).thenReturn("jwt-token");
        when(userService.findByUsername("testuser")).thenReturn(userDetailsDTO);

        // Act
        AuthResultDTO result = authService.login(loginDTO);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals(1L, result.userId());
        assertEquals("jwt-token", result.token());
        assertTrue(result.roles().contains(Role.PATRON));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).createToken(authentication);
        verify(userService).findByUsername("testuser");
    }

    @Test
    void login_ShouldThrowException_WhenAuthenticationFails() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(mock(AuthenticationException.class));

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> authService.login(loginDTO));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(tokenProvider);
        verifyNoInteractions(userService);
    }

    @Test
    void registerPatron_ShouldThrowException_WhenDuplicateUser() {
        // Arrange
        when(userMapper.mapToEntity(userRegistrationDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.registerUser(any(User.class))).thenThrow(DataIntegrityViolationException.class);

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> authService.registerPatron(userRegistrationDTO));

        verify(userMapper).mapToEntity(userRegistrationDTO);
        verify(passwordEncoder).encode("password123");
        verify(userService).registerUser(user);
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(tokenProvider);
    }
}
