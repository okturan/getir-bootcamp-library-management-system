package com.okturan.getirbootcamplibrarymanagementsystem.dto;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;

import java.util.Set;

/**
 * DTO containing authentication result data.
 */
public record AuthResultDTO(String token, String username, Long userId, Set<Role> roles) {
	/**
	 * Constructor with token, username, and roles (userId will be null)
	 */
	public AuthResultDTO(String token, String username, Set<Role> roles) {
		this(token, username, null, roles);
	}
}
