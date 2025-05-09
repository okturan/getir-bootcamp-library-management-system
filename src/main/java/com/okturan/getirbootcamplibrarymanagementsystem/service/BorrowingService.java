package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowingService {

	BorrowingResponseDTO borrowBook(BorrowingRequestDTO borrowingRequestDTO);

	BorrowingResponseDTO returnBook(Long borrowingId);

	BorrowingResponseDTO getBorrowingById(Long borrowingId);

	BorrowingHistoryDTO getCurrentUserBorrowingHistory(Pageable pageable);

	BorrowingHistoryDTO getUserBorrowingHistory(Long userId, Pageable pageable);

	Page<BorrowingResponseDTO> getAllActiveBorrowings(Pageable pageable);

	Page<BorrowingResponseDTO> getAllOverdueBorrowings(Pageable pageable);

	boolean isOwner(Long borrowingId, String username);

}
