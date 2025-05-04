package com.okturan.getirbootcamplibrarymanagementsystem.controller;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User management API")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", description = "Returns the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailsDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserDetailsDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetailsDTO userDetailsDTO = userService.findByUsername(username);
        return ResponseEntity.ok(userDetailsDTO);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = "Updates the profile of the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "User profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailsDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserDetailsDTO> updateCurrentUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDetailsDTO updatedUser = userService.updateCurrentUser(username, userUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all users", description = "Returns all users with pagination (admin/librarian only)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PageDTO.class)))
    public ResponseEntity<PageDTO<UserDetailsDTO>> getAllUsers(
            @ParameterObject
            @PageableDefault(page = 0, size = 20, sort = "id,asc")
            Pageable pageable) {
        logger.info("Getting all users with pagination");

        Page<UserDetailsDTO> userDTOs = userService.findAllUsers(pageable);
        logger.debug("Retrieved {} users", userDTOs.getTotalElements());

        // Convert Page to PageDTO to ensure stable JSON structure
        PageDTO<UserDetailsDTO> pageDTO = PageDTO.from(userDTOs);
        return ResponseEntity.ok(pageDTO);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get user by ID", description = "Returns a user by ID (admin/librarian only)")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailsDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserDetailsDTO> getUserById(@PathVariable Long id) {
        logger.info("Getting user by ID: {}", id);
        UserDetailsDTO userDetailsDTO = userService.findById(id);
        logger.debug("User found: ID: {}", userDetailsDTO.id());
        return ResponseEntity.ok(userDetailsDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update user by ID", description = "Updates a user by ID (admin/librarian only)")
    @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailsDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input or username/email already exists", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<UserDetailsDTO> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateDTO adminUserUpdateDTO) {
        logger.info("Updating user with ID: {}", id);
        UserDetailsDTO updatedUser = userService.updateUser(id, adminUserUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }

}
