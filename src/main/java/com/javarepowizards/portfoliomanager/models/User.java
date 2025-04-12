
package com.javarepowizards.portfoliomanager.models;

public class User {
    private int userId;
    private String username;

    private String passwordHash;
    private String salt;

    public User() { }

    // Constructors


    public User(String username, String passwordHash, String salt) {
        this.username = username;

        this.passwordHash = passwordHash;
        this.salt = salt;

    }

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    // Setters
    public void setId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    // Optional: toString() for debugging
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", passwordHash='[PROTECTED]'" +
                ", salt='[PROTECTED]'" +
                '}';
    }
}
