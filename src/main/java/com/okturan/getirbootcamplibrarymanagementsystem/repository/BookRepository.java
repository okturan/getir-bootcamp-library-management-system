package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

	Optional<Book> findByIsbn(String isbn);

	boolean existsByIsbn(String isbn);

}