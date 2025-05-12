package com.okturan.getirbootcamplibrarymanagementsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomAccessDeniedHandlerTest {

    private CustomAccessDeniedHandler accessDeniedHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @BeforeEach
    void setUp() {
        accessDeniedHandler = new CustomAccessDeniedHandler();
    }

    @Test
    void handle_ShouldSetCorrectResponseStatusAndContentType() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(response.getOutputStream()).thenReturn(outputStream);

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void handle_ShouldWriteErrorResponseToOutputStream() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");
        when(response.getOutputStream()).thenReturn(outputStream);

        // Create a spy on the ObjectMapper to capture what's being written
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        ObjectMapper objectMapperSpy = spy(objectMapper);
        // Use reflection to replace the private objectMapper field
        try {
            java.lang.reflect.Field field = CustomAccessDeniedHandler.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(accessDeniedHandler, objectMapperSpy);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set objectMapper field", e);
        }

        // Act
        accessDeniedHandler.handle(request, response, accessDeniedException);

        // Assert
        // Capture the ErrorResponse that was written to the output stream
        ArgumentCaptor<Object> errorResponseCaptor = ArgumentCaptor.forClass(Object.class);
        verify(objectMapperSpy).writeValue(eq(outputStream), errorResponseCaptor.capture());

        // Verify the captured ErrorResponse has the expected values
        Object capturedValue = errorResponseCaptor.getValue();
        if (capturedValue instanceof ErrorResponse) {
            ErrorResponse errorResponse = (ErrorResponse) capturedValue;
            assertEquals(HttpStatus.FORBIDDEN.value(), errorResponse.status());
            assertEquals("Access denied: You don't have permission to access this resource", errorResponse.message());
        } else {
            throw new AssertionError("Expected ErrorResponse but got " + capturedValue.getClass().getName());
        }
    }
}
