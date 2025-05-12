package com.okturan.getirbootcamplibrarymanagementsystem.repository;

import com.okturan.getirbootcamplibrarymanagementsystem.model.Role;
import com.okturan.getirbootcamplibrarymanagementsystem.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setAddress("123 Test St");
        user.setPhoneNumber("+1-555-123-4567");
        user.setDateOfBirth(LocalDate.of(1990, 1, 1));
        user.setRoles(Set.of(Role.PATRON));
        
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        // Act
        Optional<User> found = userRepository.findByUsername("nonexistent");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRoles(Set.of(Role.PATRON));
        
        entityManager.persist(user);
        entityManager.flush();

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailDoesNotExist() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByRolesContaining_ShouldReturnTrue_WhenRoleExists() {
        // Arrange
        User user = new User();
        user.setUsername("admin");
        user.setPassword("password");
        user.setEmail("admin@example.com");
        user.setRoles(Set.of(Role.ADMIN));
        
        entityManager.persist(user);
        entityManager.flush();

        // Act
        boolean exists = userRepository.existsByRolesContaining(Role.ADMIN);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByRolesContaining_ShouldReturnFalse_WhenRoleDoesNotExist() {
        // Arrange - No users with LIBRARIAN role
        
        // Act
        boolean exists = userRepository.existsByRolesContaining(Role.LIBRARIAN);

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_ShouldPersistUser() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setPassword("password");
        user.setEmail("new@example.com");
        user.setFirstName("New");
        user.setLastName("User");
        user.setRoles(Set.of(Role.PATRON));

        // Act
        User saved = userRepository.save(user);
        
        // Assert
        assertNotNull(saved.getId());
        
        User found = entityManager.find(User.class, saved.getId());
        assertNotNull(found);
        assertEquals("newuser", found.getUsername());
        assertEquals("new@example.com", found.getEmail());
        assertTrue(found.getRoles().contains(Role.PATRON));
    }

    @Test
    void delete_ShouldRemoveUser() {
        // Arrange
        User user = new User();
        user.setUsername("userToDelete");
        user.setPassword("password");
        user.setEmail("delete@example.com");
        user.setRoles(Set.of(Role.PATRON));
        
        User persistedUser = entityManager.persist(user);
        entityManager.flush();
        
        // Act
        userRepository.delete(persistedUser);
        entityManager.flush();
        
        // Assert
        User found = entityManager.find(User.class, persistedUser.getId());
        assertNull(found);
    }
}