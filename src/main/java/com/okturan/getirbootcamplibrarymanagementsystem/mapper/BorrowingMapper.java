package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Borrowing entity and DTOs
 */
@Component
public class BorrowingMapper {

    /**
     * Map Borrowing entity to BorrowingResponseDTO
     */
    public BorrowingResponseDTO mapToDTO(Borrowing borrowing) {
        BorrowingResponseDTO dto = new BorrowingResponseDTO();
        dto.setId(borrowing.getId());
        dto.setBookId(borrowing.getBook().getId());
        dto.setBookTitle(borrowing.getBook().getTitle());
        dto.setBookIsbn(borrowing.getBook().getIsbn());
        dto.setUserId(borrowing.getUser().getId());
        dto.setUsername(borrowing.getUser().getUsername());
        dto.setBorrowDate(borrowing.getBorrowDate());
        dto.setDueDate(borrowing.getDueDate());
        dto.setReturnDate(borrowing.getReturnDate());
        dto.setReturned(borrowing.isReturned());
        dto.setOverdue(borrowing.isOverdue());
        return dto;
    }
}