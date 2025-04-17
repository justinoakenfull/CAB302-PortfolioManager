package com.javarepowizards.portfoliomanager.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService() {
        this.passwordEncoder = new BCryptPasswordEncoder();
        // You can customize the strength (4-31, default is 10)
        // this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean verifyPassword(String password, String storedHash) {
        return passwordEncoder.matches(password, storedHash);
    }
}