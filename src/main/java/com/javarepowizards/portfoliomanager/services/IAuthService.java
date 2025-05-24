package com.javarepowizards.portfoliomanager.services;

/**
 * Service interface for password hashing and verification.
 */
public interface IAuthService {

    /**
     * Hashes the given plaintext password.
     *
     * @param password the plaintext password to hash
     * @return the hashed password string
     */
    String hashPassword(String password);

    /**
     * Verifies a plaintext password against a stored password hash.
     *
     * @param password   the plaintext password to verify
     * @param storedHash the stored hashed password to compare against
     * @return {@code true} if the password matches the stored hash, {@code false} otherwise
     */
    boolean verifyPassword(String password, String storedHash);
}
