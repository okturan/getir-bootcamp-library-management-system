package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginDTO(
    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "johndoe")
    String username,

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "password123")
    String password
) {}
