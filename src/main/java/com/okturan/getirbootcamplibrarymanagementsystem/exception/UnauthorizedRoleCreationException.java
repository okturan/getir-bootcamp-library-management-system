package com.okturan.getirbootcamplibrarymanagementsystem.exception;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;

/**
 * Exception thrown when a non-admin user attempts to create an account with admin or
 * librarian role.
 */
public class UnauthorizedRoleCreationException extends AuthenticationException {

	public UnauthorizedRoleCreationException(Role role) {
		super("Only administrators can create " + role.name().toLowerCase() + " accounts");
	}

}