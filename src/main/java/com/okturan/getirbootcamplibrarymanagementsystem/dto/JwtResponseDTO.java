package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "JWT response")
public record JwtResponseDTO(
		@Schema(description = "JWT token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token,

		@Schema(description = "Token type", example = "Bearer") String tokenType,

		@Schema(description = "Username", example = "johndoe") String username,

		@Schema(description = "User roles", example = "[PATRON]") Set<Role> roles) {
	/**
	 * Constructor with token, username, and roles (tokenType defaults to "Bearer")
	 */
	public JwtResponseDTO(String token, String username, Set<Role> roles) {
		this(token, "Bearer", username, roles);
	}
}
