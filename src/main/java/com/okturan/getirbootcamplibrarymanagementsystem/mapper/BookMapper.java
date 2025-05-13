package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookAvailabilityDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookRequestDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.BookResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {

	BookResponseDTO mapToDTO(Book book);

	@Mapping(target = "id", ignore = true)
	Book mapToEntity(BookRequestDTO dto);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void updateEntityFromDto(BookRequestDTO dto, @MappingTarget Book book);

	BookAvailabilityDTO createAvailabilityDTO(Book book, String timestamp);

}
