package com.okturan.getirbootcamplibrarymanagementsystem.mapper;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between User entity and DTOs
 */
@Component
public class UserMapper {

    /**
     * Map UserRegistrationDTO to User entity
     */
    public User mapToEntity(UserRegistrationDTO dto) {
        User user = new User(
                dto.getUsername(),
                dto.getPassword(),
                dto.getEmail()
        );
        
        // Set role if specified, otherwise default to PATRON (handled in User constructor)
        if (dto.getRole() != null) {
            // Clear default role and set the specified one
            user.getRoles().clear();
            user.addRole(dto.getRole());
        }
        
        return user;
    }
}