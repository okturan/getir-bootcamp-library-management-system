package com.okturan.getirbootcamplibrarymanagementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing a book borrowing transaction.
 * Tracks who borrowed which book, when it was borrowed, when it's due, and when it was returned.
 */
@Entity
@Table(name = "borrowings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "borrow_date", nullable = false)
    private LocalDate borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "is_returned", nullable = false)
    private boolean returned = false;

    /**
     * Checks if the borrowing is overdue.
     * A borrowing is overdue if the due date has passed and the book hasn't been returned.
     *
     * @return true if the borrowing is overdue, false otherwise
     */
    @Transient
    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }
}