package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Borrowing;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    List<Borrowing> findByUser(User user);

    List<Borrowing> findByBook(Book book);

    List<Borrowing> findByUserAndReturned(User user, boolean returned);

    List<Borrowing> findByBookAndReturned(Book book, boolean returned);

    List<Borrowing> findByReturned(boolean returned);

    List<Borrowing> findByDueDateBeforeAndReturned(LocalDate currentDate, boolean returned);

    Optional<Borrowing> findFirstByBookAndReturned(Book book, boolean returned);
}
