package com.okturan.getirbootcamplibrarymanagementsystem.exception;

/**
 * Base class for all authentication-related exceptions.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}