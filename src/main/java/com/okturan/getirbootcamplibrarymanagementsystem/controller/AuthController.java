package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.AuthResultDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.JwtResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import com.okturan.getirbootcamplibrarymanagementsystem.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new patron", description = "Creates a new user account with PATRON role")
    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<JwtResponseDTO> registerPatron(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        logger.info("Public registration request received for username: {}", registrationDTO.username());
        AuthResultDTO result = authService.registerPatron(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }

    @PostMapping("/admin/register")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Register a user with any role (Admin only)", description = "Creates a new user account with any role (requires ADMIN privileges)")
    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Only administrators can access this endpoint", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<JwtResponseDTO> registerWithRole(@Valid @RequestBody AdminUserRegistrationDTO registrationDTO) {
        logger.info("Admin registration request received for username: {} with role: {}", 
                registrationDTO.username(), registrationDTO.role());
        AuthResultDTO result = authService.registerWithRole(registrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get JWT token", description = "Provides a JWT token for API access")
    @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        AuthResultDTO result = authService.login(loginDTO);
        return ResponseEntity.ok(new JwtResponseDTO(result.token(), result.username(), result.roles()));
    }
}
