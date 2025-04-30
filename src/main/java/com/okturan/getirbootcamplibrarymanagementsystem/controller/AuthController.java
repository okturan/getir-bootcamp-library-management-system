package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.JwtResponseDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.LoginDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserRegistrationDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtTokenProvider;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
public class AuthController {

    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(JwtTokenProvider tokenProvider, 
                         AuthenticationManager authenticationManager,
                         UserService userService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = JwtResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        // Check if username already exists
        if (userService.existsByUsername(registrationDTO.getUsername())) {
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
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Email is already in use",
                    java.time.LocalDateTime.now()
            );
            return ResponseEntity
                    .badRequest()
                    .body(errorResponse);
        }

        // Create new user
        User user = new User(
                registrationDTO.getUsername(),
                registrationDTO.getPassword(),
                registrationDTO.getEmail()
        );

        // Save user
        User savedUser = userService.registerUser(user);

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registrationDTO.getUsername(),
                        registrationDTO.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication);

        // Return JWT
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new JwtResponseDTO(jwt, savedUser.getUsername()));
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
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate token
            String jwt = tokenProvider.createToken(authentication);

            // Return the token
            return ResponseEntity.ok(new JwtResponseDTO(jwt, loginDTO.getUsername()));
        } catch (AuthenticationException e) {
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
