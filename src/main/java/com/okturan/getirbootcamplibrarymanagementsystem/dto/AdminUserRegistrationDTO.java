package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin user registration request")
public record AdminUserRegistrationDTO(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username", example = "johndoe")
    String username,

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Schema(description = "Password", example = "password123")
    String password,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email", example = "john.doe@example.com")
    String email,

    @Schema(description = "User role (ADMIN, LIBRARIAN, PATRON). Defaults to PATRON if not specified.", example = "PATRON")
    Role role
) {
    // Factory method to create AdminUserRegistrationDTO from UserRegistrationDTO
    public static AdminUserRegistrationDTO from(UserRegistrationDTO dto, Role role) {
        return new AdminUserRegistrationDTO(dto.username(), dto.password(), dto.email(), role);
    }
}
