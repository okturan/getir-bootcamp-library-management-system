package com.okturan.getirbootcamplibrarymanagementsystem.model;

/**
 * Enum representing the roles in the library management system.
 * Each role has a specific authority level and permissions.
 */
public enum Role {
    ADMIN("ROLE_ADMIN"),
    LIBRARIAN("ROLE_LIBRARIAN"),
    PATRON("ROLE_PATRON");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return authority;
    }
}