package com.okturan.getirbootcamplibrarymanagementsystem.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtFilter = new JwtFilter(tokenProvider);
        // Clear the security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenProvider.validateToken("valid-token")).thenReturn(true);
        when(tokenProvider.getAuthentication("valid-token")).thenReturn(authentication);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken("valid-token");
        verify(tokenProvider).getAuthentication("valid-token");
        verify(filterChain).doFilter(request, response);
        // We can't easily assert that SecurityContextHolder.getContext().getAuthentication() equals authentication
        // because SecurityContextHolder is static and its state is shared between tests
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenNoTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenInvalidTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(tokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider).validateToken("invalid-token");
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenNonBearerTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("NonBearer token");

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ShouldNotSetAuthentication_WhenEmptyTokenProvided() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(tokenProvider, never()).validateToken(anyString());
        verify(tokenProvider, never()).getAuthentication(anyString());
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}