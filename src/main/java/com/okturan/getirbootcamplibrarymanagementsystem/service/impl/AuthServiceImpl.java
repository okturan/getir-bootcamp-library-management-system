package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.dto.*;
import com.okturan.getirbootcamplibrarymanagementsystem.exception.UnauthorizedRoleCreationException;
import com.okturan.getirbootcamplibrarymanagementsystem.mapper.UserMapper;
import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.security.JwtTokenProvider;
import com.okturan.getirbootcamplibrarymanagementsystem.service.AuthService;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {

	private final JwtTokenProvider tokenProvider;

	private final AuthenticationManager authenticationManager;

	private final UserService userService;

	private final UserMapper userMapper;

	private final PasswordEncoder passwordEncoder;

	/* ───────── registration ───────── */

	@Override
	public AuthResultDTO registerPatron(UserRegistrationDTO dto) {
		log.info("Public registration attempt for username: {}", dto.username());

		// For public registration, we always create a PATRON user
		User user = userMapper.mapToEntity(dto);
		user.addRole(Role.PATRON);

		return saveAndLogin(user, dto.password());
	}

	@Override
	public AuthResultDTO registerWithRole(AdminUserRegistrationDTO dto) {
		log.info("Admin registration: {} ({})", dto.username(), dto.role());

		// extra guard – keep it, but move it to a one‑liner
		if (dto.role() != null && dto.role() != Role.PATRON
				&& SecurityContextHolder.getContext()
					.getAuthentication()
					.getAuthorities()
					.stream()
					.noneMatch(a -> a.getAuthority().equals(Role.ADMIN.getAuthority()))) {
			throw new UnauthorizedRoleCreationException(dto.role());
		}

		return saveAndLogin(userMapper.mapToEntity(dto), dto.password());
	}

	/* ───────── login ───────── */

	@Override
	public AuthResultDTO login(LoginDTO dto) {
		log.info("Login attempt for username: {}", dto.username());
		return authenticateUser(dto.username(), dto.password());
	}

	/* ───────── core helper ───────── */

	private AuthResultDTO saveAndLogin(User user, String rawPassword) {
		user.setPassword(passwordEncoder.encode(rawPassword));
		try {
			User saved = userService.registerUser(user);
			// Get authentication result without userId
			AuthResultDTO tempDto = authenticateUser(saved.getUsername(), rawPassword);
			// Create a new AuthResultDTO with the userId
			return new AuthResultDTO(tempDto.token(), tempDto.username(), saved.getId(), tempDto.roles());
		}
		catch (DataIntegrityViolationException dup) {
			log.warn("Duplicate username/email – {}", dup.getMostSpecificCause().getMessage());
			throw dup;
		}
	}

	private AuthResultDTO authenticateUser(String username, String password) {
		try {
			log.debug("Attempting authentication for {}", username);
			Authentication auth = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));

			SecurityContextHolder.getContext().setAuthentication(auth);
			String jwt = tokenProvider.createToken(auth);
			UserDetailsDTO userDetails = userService.findByUsername(username);
			Long userId = userDetails.id();
			Set<Role> roles = userDetails.roles();

			log.info("Authentication successful for user ID {}", userId);
			return new AuthResultDTO(jwt, username, userId, roles);

		}
		catch (AuthenticationException ex) {
			log.warn("Authentication failed for {}", username);
			throw ex; // 401 handled globally
		}
	}

}
