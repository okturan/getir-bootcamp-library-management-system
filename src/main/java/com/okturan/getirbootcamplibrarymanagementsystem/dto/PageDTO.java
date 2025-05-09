package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageDTO<T>(List<T> content, long totalElements, int totalPages, int number, int size, boolean first,
		boolean last, boolean empty) {

	public static <T> PageDTO<T> from(Page<T> page) {
		return new PageDTO<>(page.getContent(),
		                     page.getTotalElements(),
		                     page.getTotalPages(),
		                     page.getNumber(),
		                     page.getSize(),
		                     page.isFirst(),
		                     page.isLast(),
		                     page.isEmpty());
	}
}
