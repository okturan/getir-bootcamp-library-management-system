package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import java.util.Optional;

public record BookSearchFilterDTO(
        Optional<String> author,
        Optional<String> title,
        Optional<String> genre,
        Optional<Boolean> available
) {}