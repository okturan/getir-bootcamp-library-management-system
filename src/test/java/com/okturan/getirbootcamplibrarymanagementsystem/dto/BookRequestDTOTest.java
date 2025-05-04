package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BookRequestDTOTest {

    @Test
    void testAvailableFieldRespectsFalseValue() {
        // Create a BookRequestDTO with available=false
        BookRequestDTO dto = new BookRequestDTO(
                "Test Title",
                "Test Author",
                "978-3-16-148410-0",
                LocalDate.now(),
                "Test Genre",
                false
        );
        
        // Verify that available is false
        assertFalse(dto.available(), "available should be false when explicitly set to false");
        
        // Create a BookRequestDTO with available=true
        BookRequestDTO dto2 = new BookRequestDTO(
                "Test Title 2",
                "Test Author 2",
                "978-3-16-148410-1",
                LocalDate.now(),
                "Test Genre 2",
                true
        );
        
        // Verify that available is true
        assertTrue(dto2.available(), "available should be true when explicitly set to true");
    }
}