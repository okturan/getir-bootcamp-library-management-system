package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;

import java.time.LocalDate;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Admin user update request")
public record AdminUserUpdateDTO(
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username", example = "johndoe")
    String username,

    @Email(message = "Email should be valid")
    @Schema(description = "Email", example = "john.doe@example.com")
    String email,

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Schema(description = "First name", example = "John")
    String firstName,

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Schema(description = "Last name", example = "Doe")
    String lastName,

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Address", example = "123 Main St, Anytown, USA")
    String address,

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(description = "Phone number", example = "+1-555-123-4567")
    String phoneNumber,

    @Schema(description = "Date of birth", example = "1990-01-01")
    LocalDate dateOfBirth,

    @Schema(description = "User roles", example = "[PATRON]")
    Set<Role> roles
) {}
