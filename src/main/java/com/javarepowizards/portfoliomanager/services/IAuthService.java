package com.javarepowizards.portfoliomanager.services;

public interface IAuthService {
    String hashPassword(String password);
    boolean verifyPassword(String password, String storedHash);
}