package com.javarepowizards.portfoliomanager.services.Auth;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Service responsible for hashing and verifying user passwords.
 * Uses the provided PasswordEncoder implementation for all cryptographic operations.
 */
public class AuthService implements IAuthService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs an AuthService with the specified PasswordEncoder.
     *
     * @param passwordEncoder the PasswordEncoder used for hashing and verifying passwords
     */
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hashes the provided plaintext password using the configured PasswordEncoder.
     *
     * @param password the plaintext password to hash
     * @return the hashed password string
     */
    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Verifies a plaintext password against a stored password hash.
     *
     * @param password   the plaintext password to verify
     * @param storedHash the stored password hash to compare against
     * @return {@code true} if the password matches the hash, {@code false} otherwise
     */
    @Override
    public boolean verifyPassword(String password, String storedHash) {
        return passwordEncoder.matches(password, storedHash);
    }
}
