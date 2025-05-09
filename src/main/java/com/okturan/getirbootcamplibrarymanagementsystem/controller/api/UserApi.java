package com.okturan.getirbootcamplibrarymanagementsystem.controller.api;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.AdminUserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.PageDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserDetailsDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.dto.UserUpdateDTO;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.GlobalExceptionHandler.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "User Management", description = "User management API")
public interface UserApi {

    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailsDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    ResponseEntity<UserDetailsDTO> getCurrentUser();

    @Operation(
            summary = "Update current user profile",
            description = "Updates the profile of the currently authenticated user"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User profile updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailsDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input or email already exists",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    ResponseEntity<UserDetailsDTO> updateCurrentUser(UserUpdateDTO userUpdateDTO);

    @Operation(
            summary = "Get all users",
            description = "Returns all users with pagination (admin/librarian only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageDTO.class)
            )
    )
    ResponseEntity<PageDTO<UserDetailsDTO>> getAllUsers(Pageable pageable);

    @Operation(
            summary = "Get user by ID",
            description = "Returns a user by ID (admin/librarian only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailsDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    ResponseEntity<UserDetailsDTO> getUserById(Long id);

    @Operation(
            summary = "Update user by ID",
            description = "Updates a user by ID (admin/librarian only)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDetailsDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid input or username/email already exists",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)
            )
    )
    ResponseEntity<UserDetailsDTO> updateUser(Long id, AdminUserUpdateDTO adminUserUpdateDTO);
}
