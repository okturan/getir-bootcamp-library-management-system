package com.okturan.getirbootcamplibrarymanagementsystem.controller.api;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.JwtResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Authentication API")
public interface AuthApi {

	@Operation(summary = "Register a new patron", description = "Creates a new user account with PATRON role")
	@ApiResponse(responseCode = "201", description = "User registered successfully",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
	@ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	ResponseEntity<JwtResponseDTO> registerPatron(UserRegistrationDTO registrationDTO);

	@Operation(summary = "Register a user with any role (Admin only)",
			description = "Creates a new user account with any role (requires ADMIN privileges)")
	@ApiResponse(responseCode = "201", description = "User registered successfully",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
	@ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden - Only administrators can access this endpoint",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	ResponseEntity<JwtResponseDTO> registerWithRole(AdminUserRegistrationDTO registrationDTO);

	@Operation(summary = "Login to get JWT token", description = "Provides a JWT token for API access")
	@ApiResponse(responseCode = "200", description = "Login successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
	@ApiResponse(responseCode = "401", description = "Invalid credentials",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
	ResponseEntity<JwtResponseDTO> login(LoginDTO loginDTO);

}
