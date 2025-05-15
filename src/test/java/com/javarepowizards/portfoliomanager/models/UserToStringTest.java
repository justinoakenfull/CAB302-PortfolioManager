package com.javarepowizards.portfoliomanager.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserToStringTest {
    private User user;

    @BeforeEach
    public void setUp(){
        // Initialise a user object with known values for testing
        user = new User("johnsmith", "john@smith.com", "hashedpassword123");
        user.setId(42);
    }

    @Test
    public void testToString_containsExpectedFields(){
        // Verifies that the toString() method includes expected fields
        String output = user.toString();

        assertTrue(output.contains("userId=42"));
        assertTrue(output.contains("username='johnsmith'"));
        assertTrue(output.contains("email='john@smith.com'"));
        assertTrue(output.contains("passwordHash='[PROTECTED]'"));
    }

    @Test
    public void testToString_doesNotExposeActualPassword(){
        // Ensures that the actual password hash is not leaked in toString()
        String output = user.toString();

        assertFalse(output.contains("hashedpassword123"));
    }
}