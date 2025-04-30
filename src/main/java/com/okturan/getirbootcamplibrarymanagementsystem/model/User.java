package com.okturan.getirbootcamplibrarymanagementsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    private boolean active = true;

    // Constructor with required fields
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles.add(Role.PATRON); // Default role is patron
    }

    // Add a role to the user
    public void addRole(Role role) {
        this.roles.add(role);
    }

    // Check if user has a specific role
    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

}
