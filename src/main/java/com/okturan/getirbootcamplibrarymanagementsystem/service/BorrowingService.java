package com.okturan.getirbootcamplibrarymanagementsystem.service;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;

import java.util.List;

public interface BorrowingService {

    BorrowingResponseDTO borrowBook(BorrowingRequestDTO borrowingRequestDTO);

    BorrowingResponseDTO returnBook(Long borrowingId);

    BorrowingResponseDTO getBorrowingById(Long borrowingId);

    BorrowingHistoryDTO getCurrentUserBorrowingHistory();

    BorrowingHistoryDTO getUserBorrowingHistory(Long userId);

    List<BorrowingResponseDTO> getAllActiveBorrowings();

    List<BorrowingResponseDTO> getAllOverdueBorrowings();

    boolean isOwner(Long borrowingId, String username);

}
