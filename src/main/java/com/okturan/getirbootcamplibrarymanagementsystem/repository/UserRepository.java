package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	/* ---------- single‑column look‑ups ---------- */

	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	/* ---------- role‑based helpers ---------- */

	/**
	 * True if at least one user holds the requested role.
	 */
	boolean existsByRolesContaining(Role role);

}
