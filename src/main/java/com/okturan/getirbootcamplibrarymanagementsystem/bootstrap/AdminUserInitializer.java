package com.okturan.getirbootcamplibrarymanagementsystem.bootstrap;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import com.okturan.getirbootcamplibrarymanagementsystem.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstrap component that ensures at least one admin user exists in the system.
 * This is necessary for the "admin-only" rule to work properly.
 */
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${admin.username:admin}")
    private String adminUsername;
    
    @Value("${admin.email:admin@example.com}")
    private String adminEmail;
    
    @Value("${admin.password}")
    private String adminPassword;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Check if any admin user exists
        if (!userRepository.existsByRolesContaining(Role.ADMIN)) {
            logger.info("No admin user found. Creating initial admin user.");
            
            // Validate that admin password is set
            if (adminPassword == null || adminPassword.trim().isEmpty()) {
                logger.error("Admin password environment variable not set. Cannot create admin user.");
                logger.error("Please set the 'admin.password' environment variable and restart the application.");
                return;
            }
            
            // Create admin user
            User adminUser = new User(
                    adminUsername,
                    passwordEncoder.encode(adminPassword),
                    adminEmail
            );
            
            // Clear default PATRON role and set ADMIN role
            adminUser.getRoles().clear();
            adminUser.addRole(Role.ADMIN);
            
            // Save admin user
            User savedAdmin = userRepository.save(adminUser);
            logger.info("Initial admin user created with ID: {}", savedAdmin.getId());
        } else {
            logger.info("Admin user already exists. Skipping initialization.");
        }
    }
}