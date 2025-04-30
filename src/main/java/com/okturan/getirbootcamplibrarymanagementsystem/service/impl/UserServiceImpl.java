package com.okturan.getirbootcamplibrarymanagementsystem.service.impl;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;
import com.okturan.getirbootcamplibrarymanagementsystem.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        logger.info("Registering new user with username: {}, email: {}", user.getUsername(), user.getEmail());
        try {
            // Encode the password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            logger.debug("Password encoded for user: {}", user.getUsername());

            // Ensure the user has the PATRON role by default
            user.addRole(Role.PATRON);
            logger.debug("Added PATRON role to user: {}", user.getUsername());

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully: {}, ID: {}", savedUser.getUsername(), savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            logger.error("Failed to register user: {}", user.getUsername(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        logger.debug("Finding user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("User found by username: {}", username);
        } else {
            logger.debug("User not found by username: {}", username);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            logger.debug("User found by email: {}", email);
        } else {
            logger.debug("User not found by email: {}", email);
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        logger.debug("Checking if username exists: {}", username);
        boolean exists = userRepository.existsByUsername(username);
        logger.debug("Username {} exists: {}", username, exists);
        return exists;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        logger.debug("Checking if email exists: {}", email);
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("Email {} exists: {}", email, exists);
        return exists;
    }
}
