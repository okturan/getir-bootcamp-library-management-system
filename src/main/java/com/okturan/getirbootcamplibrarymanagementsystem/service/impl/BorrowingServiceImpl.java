package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingHistoryDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BorrowingResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.OverdueReportDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.BorrowingMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BookRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.BorrowingRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BookService;
import com.okturan.getirbootcamplibrarymanagementsystem.service.BorrowingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service("borrowingService")
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

	private final BorrowingRepository borrowingRepo;

	private final BookRepository bookRepo;

	private final UserRepository userRepo;

	private final BookService bookService; // used only to broadcast availability

	private final BorrowingMapper mapper;

	/* ─────────── borrow / return ─────────── */

	private static boolean hasAdminOrLibrarian(User user) {
		return user.hasRole(Role.ADMIN) || user.hasRole(Role.LIBRARIAN);
	}

	@Override
	@Transactional
	public BorrowingResponseDTO borrowBook(BorrowingRequestDTO req) {
		log.info("Borrow request – book {}", req.bookId());

		User borrower = resolveBorrower(req.userId());
		Book book = bookRepo.findById(req.bookId())
			.orElseThrow(() -> new EntityNotFoundException("Book not found " + req.bookId()));

		// Check if the book is already borrowed
		if (borrowingRepo.existsByBookAndReturnedFalse(book)) {
			throw new IllegalStateException("Book is currently borrowed by another patron and not available for borrowing. Please try again when the book has been returned.");
		}

		Borrowing borrowing = new Borrowing();
		mapper.initBorrowing(borrowing, book, borrower);

		borrowingRepo.save(borrowing);

		// Emit availability update (availability is determined by borrowing status)
		bookService.emitAvailabilityUpdate(book);

		return mapper.mapToDTO(borrowing);
	}

	/* ─────────── look‑ups ─────────── */

	@Override
	@Transactional
	public BorrowingResponseDTO returnBook(Long borrowingId) {
		log.info("Return request – borrowing {}", borrowingId);

		Borrowing borrowing = borrowingRepo.findById(borrowingId)
			.orElseThrow(() -> new EntityNotFoundException("Borrowing not found " + borrowingId));

		if (borrowing.isReturned()) {
			throw new IllegalStateException("Book already returned");
		}

		mapper.returnBook(borrowing);
		borrowingRepo.save(borrowing);

		Book book = borrowing.getBook();
		// Emit availability update (availability is determined by borrowing status)
		bookService.emitAvailabilityUpdate(book);

		return mapper.mapToDTO(borrowing);
	}

	@Override
	@Transactional(readOnly = true)
	public BorrowingResponseDTO getBorrowingById(Long id) {
		return mapper.mapToDTO(
				borrowingRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Borrowing not found " + id)));
	}

	@Override
	@Transactional(readOnly = true)
	public BorrowingHistoryDTO getCurrentUserBorrowingHistory(Pageable pageable) {
		return historyForUser(currentUser(), pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public BorrowingHistoryDTO getUserBorrowingHistory(Long userId, Pageable pageable) {
		User user = userRepo.findById(userId)
				.orElseThrow(() -> new EntityNotFoundException("User not found " + userId));
		return historyForUser(user, pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BorrowingResponseDTO> getAllActiveBorrowings(Pageable pageable) {
		return borrowingRepo.findByReturned(false, pageable).map(mapper::mapToDTO);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<BorrowingResponseDTO> getAllOverdueBorrowings(Pageable pageable) {
		return borrowingRepo.findByDueDateBeforeAndReturned(LocalDate.now(), false, pageable)
				.map(mapper::mapToDTO);
	}

	/* ─────────── helpers ─────────── */

	@Override
	@Transactional(readOnly = true)
	public boolean isOwner(Long borrowingId, String username) {
		return borrowingRepo.findById(borrowingId)
			.orElseThrow(() -> new EntityNotFoundException("Borrowing not found " + borrowingId))
			.getUser()
			.getUsername()
			.equals(username);
	}

	private User resolveBorrower(Long targetUserId) {
		User current = currentUser();

		// When an explicit userId is supplied
		if (targetUserId != null) {
			if (!hasAdminOrLibrarian(current)) {
				throw new AccessDeniedException("Only admins or librarians can borrow for other users");
			}

			User target = userRepo.findById(targetUserId)
				.orElseThrow(() -> new EntityNotFoundException("User not found " + targetUserId));

			if (hasAdminOrLibrarian(target)) {
				throw new IllegalArgumentException("Cannot borrow books for admins or librarians");
			}

			log.info("{} borrows for {}", current.getUsername(), target.getUsername());
			return target;
		}

		// Self‑borrow
		if (hasAdminOrLibrarian(current)) {
			throw new AccessDeniedException("Admins/Librarians must specify a patron userId");
		}

		return current;
	}

	private BorrowingHistoryDTO historyForUser(User user, Pageable pageable) {
		Page<Borrowing> borrowingsPageEntity = borrowingRepo.findByUser(user, pageable);
		PageDTO<BorrowingResponseDTO> borrowingsPageDTO = PageDTO.from(borrowingsPageEntity.map(mapper::mapToDTO));

		// Fetch overall stats for the user
		long totalUserBorrowings = borrowingRepo.countByUser(user);
		long currentUserBorrowings = borrowingRepo.countByUserAndReturnedFalse(user);
		long overdueUserBorrowings = borrowingRepo.countByUserAndReturnedFalseAndDueDateBefore(user, LocalDate.now());

		return new BorrowingHistoryDTO(
				user.getId(), user.getUsername(),
				borrowingsPageDTO,
				(int) totalUserBorrowings,
				(int) currentUserBorrowings,
				(int) overdueUserBorrowings
		);
	}

	private User currentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return userRepo.findByUsername(auth.getName())
			.orElseThrow(() -> new EntityNotFoundException("User not found " + auth.getName()));
	}

	@Override
	@Transactional(readOnly = true)
	public OverdueReportDTO generateOverdueReport(Pageable pageable) {
		log.info("Generating overdue books report");

		// Get paginated list of overdue borrowings
		Page<BorrowingResponseDTO> overdueBorrowingsPage = getAllOverdueBorrowings(pageable);

		// Calculate summary statistics
		LocalDate today = LocalDate.now();
		long totalOverdueCount = borrowingRepo.countByDueDateBeforeAndReturned(today, false);
		long distinctUsersWithOverdueCount = borrowingRepo.countDistinctUsersByDueDateBeforeAndReturnedFalse(today);
		long distinctBooksOverdueCount = borrowingRepo.countDistinctBooksByDueDateBeforeAndReturnedFalse(today);

		// Create and return the report
		return new OverdueReportDTO(
				PageDTO.from(overdueBorrowingsPage),
				(int) totalOverdueCount,
				(int) distinctUsersWithOverdueCount,
				(int) distinctBooksOverdueCount,
				today
		);
	}

}
