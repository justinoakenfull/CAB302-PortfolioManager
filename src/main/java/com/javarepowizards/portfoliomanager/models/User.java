package com.javarepowizards.portfoliomanager.models;

public class User {
    private int userId;
    private String username;
    private String email;
    private String passwordHash; // BCrypt includes salt in the hash

    // Remove salt field since BCrypt handles it internally
    // Add other profile fields as needed

    public User() { }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters and setters (remove getSalt/setSalt)
    public int getUserId() { return userId; }
    public void setId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", passwordHash='[PROTECTED]'" +
                '}';
    }
}