package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * Mapper interface for converting between User entity and DTOs
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    /**
     * Map UserRegistrationDTO to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User mapToEntity(UserRegistrationDTO dto);

    /**
     * Map AdminUserRegistrationDTO to User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    User mapToEntity(AdminUserRegistrationDTO dto);

    /**
     * Map User entity to UserDetailsDTO
     */
    UserDetailsDTO mapToDetailsDTO(User user);

    /**
     * Update User entity from UserUpdateDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDTO dto, @MappingTarget User user);

    /**
     * Update User entity from AdminUserUpdateDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromAdminDto(AdminUserUpdateDTO dto, @MappingTarget User user);

    /**
     * After mapping from AdminUserRegistrationDTO to User, set the role
     */
    @AfterMapping
    default void setRoleAfterMapping(AdminUserRegistrationDTO dto, @MappingTarget User user) {
        // Set role if specified, otherwise default to PATRON
        user.getRoles().clear();
        if (dto.role() != null) {
            user.addRole(dto.role());
        } else {
            user.addRole(Role.PATRON);
        }
    }

    /**
     * After mapping from UserRegistrationDTO to User, clear roles (will be set by service layer)
     */
    @AfterMapping
    default void clearRolesAfterMapping(UserRegistrationDTO dto, @MappingTarget User user) {
        user.getRoles().clear();
    }
}
