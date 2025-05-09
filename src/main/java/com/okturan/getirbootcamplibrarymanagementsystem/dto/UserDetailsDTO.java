package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "User details response")
public record UserDetailsDTO(@Schema(description = "User ID", example = "1") Long id,

		@Schema(description = "Username", example = "johndoe") String username,

		@Schema(description = "Email", example = "john.doe@example.com") String email,

		@Schema(description = "First name", example = "John") String firstName,

		@Schema(description = "Last name", example = "Doe") String lastName,

		@Schema(description = "Address", example = "123 Main St, Anytown, USA") String address,

		@Schema(description = "Phone number", example = "+1-555-123-4567") String phoneNumber,

		@Schema(description = "Date of birth", example = "1990-01-01") LocalDate dateOfBirth,

		@Schema(description = "User roles", example = "[PATRON]") Set<Role> roles) {
}
