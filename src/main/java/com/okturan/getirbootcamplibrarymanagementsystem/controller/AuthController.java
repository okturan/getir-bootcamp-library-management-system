package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.JwtResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtTokenProvider;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication API")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden - Only administrators can create librarian or admin accounts",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        logger.info("Registration attempt for username: {}", registrationDTO.getUsername());

        // Check if username already exists
        if (userService.existsByUsername(registrationDTO.getUsername())) {
            logger.warn("Registration failed: Username '{}' is already taken", registrationDTO.getUsername());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Username is already taken",
                    java.time.LocalDateTime.now()
            );
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse);
        }

        // Check if email already exists
        if (userService.existsByEmail(registrationDTO.getEmail())) {
            logger.warn("Registration failed: Email '{}' is already in use", registrationDTO.getEmail());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Email is already in use",
                    java.time.LocalDateTime.now()
            );
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse);
        }

        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Check if trying to create a LIBRARIAN or ADMIN account
        if (registrationDTO.getRole() != null && 
            (registrationDTO.getRole() == Role.LIBRARIAN || registrationDTO.getRole() == Role.ADMIN) && 
            !isAdmin) {
            logger.warn("Unauthorized attempt to create {} account by: {}", 
                    registrationDTO.getRole(), 
                    authentication != null ? authentication.getName() : "unauthenticated user");

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Only administrators can create librarian or admin accounts",
                    java.time.LocalDateTime.now()
            );
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(errorResponse);
        }

        logger.debug("Creating new user with username: {}", registrationDTO.getUsername());
        // Create new user
        User user = new User(
                registrationDTO.getUsername(),
                registrationDTO.getPassword(),
                registrationDTO.getEmail()
        );

        // Set role if specified, otherwise default to PATRON (handled in User constructor)
        if (registrationDTO.getRole() != null) {
            // Clear default role and set the specified one
            user.getRoles().clear();
            user.addRole(registrationDTO.getRole());
            logger.debug("Setting user role to: {}", registrationDTO.getRole());
        }

        // Save user
        User savedUser = userService.registerUser(user);
        logger.info("User registered successfully: {}", savedUser.getUsername());

        try {
            // Authenticate user
            logger.debug("Authenticating newly registered user: {}", savedUser.getUsername());
            Authentication newAuthentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registrationDTO.getUsername(),
                            registrationDTO.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            String jwt = tokenProvider.createToken(newAuthentication);
            logger.debug("JWT token generated for user: {}", savedUser.getUsername());

            // Return JWT
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new JwtResponseDTO(jwt, savedUser.getUsername()));
        } catch (AuthenticationException e) {
            logger.error("Failed to authenticate newly registered user: {}", savedUser.getUsername(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login to get JWT token", description = "Provides a JWT token for API access")
    @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        logger.info("Login attempt for username: {}", loginDTO.getUsername());
        try {
            // Authenticate user
            logger.debug("Attempting to authenticate user: {}", loginDTO.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Authentication set in security context for user: {}", loginDTO.getUsername());

            // Generate token
            String jwt = tokenProvider.createToken(authentication);
            logger.debug("JWT token generated for user: {}", loginDTO.getUsername());

            // Return the token
            logger.info("Login successful for user: {}", loginDTO.getUsername());
            return ResponseEntity.ok(new JwtResponseDTO(jwt, loginDTO.getUsername()));
        } catch (AuthenticationException e) {
            logger.warn("Login failed for username: {} - Invalid credentials", loginDTO.getUsername());
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid username or password",
                    java.time.LocalDateTime.now()
            );
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(errorResponse);
        }
    }
}
