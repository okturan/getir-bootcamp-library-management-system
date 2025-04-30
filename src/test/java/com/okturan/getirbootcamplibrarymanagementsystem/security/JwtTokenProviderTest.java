package com.okturan.getirbootcamplibrarymanagementsystem.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @Mock
    private UserDetailsService userDetailsService;

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Initialize the JwtTokenProvider with the mocked UserDetailsService
        jwtTokenProvider = new JwtTokenProvider(userDetailsService);

        // Create a UserDetails object for testing
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        userDetails = new User("testuser", "password", authorities);

        // Create an Authentication object for testing
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    }

    @Test
    void createToken_ShouldCreateValidToken() {
        // Act
        String token = jwtTokenProvider.createToken(authentication);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_ShouldReturnTrue_ForValidToken() {
        // Arrange
        String token = jwtTokenProvider.createToken(authentication);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_ForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.string";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getAuthentication_ShouldReturnAuthentication_ForValidToken() {
        // Arrange
        String token = jwtTokenProvider.createToken(authentication);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Act
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // Assert
        assertNotNull(resultAuth);
        assertEquals(userDetails, resultAuth.getPrincipal());
        assertEquals(1, resultAuth.getAuthorities().size());
        assertTrue(resultAuth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void getAuthentication_ShouldContainCorrectUsername() {
        // Arrange
        String token = jwtTokenProvider.createToken(authentication);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        // Act
        Authentication resultAuth = jwtTokenProvider.getAuthentication(token);

        // Assert
        assertNotNull(resultAuth);
        UserDetails resultUserDetails = (UserDetails) resultAuth.getPrincipal();
        assertEquals("testuser", resultUserDetails.getUsername());
    }
}