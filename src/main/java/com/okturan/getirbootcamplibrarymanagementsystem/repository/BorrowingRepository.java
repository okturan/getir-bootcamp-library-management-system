package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

	Page<Borrowing> findByUser(User user, Pageable pageable);

	Page<Borrowing> findByReturned(boolean returned, Pageable pageable);

	Page<Borrowing> findByDueDateBeforeAndReturned(LocalDate currentDate, boolean returned, Pageable pageable);

	boolean existsByBookAndReturnedFalse(Book book);

	long countByUser(User user);

	long countByUserAndReturnedFalse(User user);

	long countByUserAndReturnedFalseAndDueDateBefore(User user, LocalDate date);

	long countByDueDateBeforeAndReturned(LocalDate date, boolean returned);

	@Query("SELECT COUNT(DISTINCT b.user.id) FROM Borrowing b WHERE b.dueDate < :date AND b.returned = false")
	long countDistinctUsersByDueDateBeforeAndReturnedFalse(@Param("date") LocalDate date);

	@Query("SELECT COUNT(DISTINCT b.book.id) FROM Borrowing b WHERE b.dueDate < :date AND b.returned = false")
	long countDistinctBooksByDueDateBeforeAndReturnedFalse(@Param("date") LocalDate date);

	@Query("SELECT b.book.id FROM Borrowing b WHERE b.book.id IN :bookIds AND b.returned = false")
	Set<Long> findBorrowedBookIdsByBookIds(@Param("bookIds") List<Long> bookIds);

}
