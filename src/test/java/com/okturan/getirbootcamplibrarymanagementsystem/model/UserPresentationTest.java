package com.okturan.getirbootcamplibrarymanagementsystem.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserPresentationTest {

	@Test
	void toStringDoesNotExposePasswordMaterial() {
		User user = new User();
		user.setUsername("portfolio-patron");
		user.setPassword("password-material-sentinel");

		String rendered = user.toString();

		assertTrue(rendered.contains("portfolio-patron"));
		assertFalse(rendered.contains("password-material-sentinel"));
		assertFalse(rendered.contains("password="));
	}
}
