package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO {
    
    private Long id;
    private String title;
    private String author;
    private String isbn;
    private LocalDate publicationDate;
    private String genre;
    private boolean available;
}