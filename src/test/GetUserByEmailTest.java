package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GetUserByEmailTest{

    private static final String USERNAME = "johnsmith";
    private static final String EMAIL = "john@smith.com";
    private static final String HASHED_PASS = "passwordhash123";

    private  UserDAO userDAO;

    @BeforeEach
    public void setUp() throws SQLException{
    // Create an in-memory database connection and initialise UserDAO
        Connection connection = DriverManager.getConnection(("jdbc:sqlite::memory:"));
        IDatabaseConnection testDb = () -> connection;
        userDAO = new UserDAO(testDb);

    // Insert a known user to retrieve in the tests
        User u = new User(USERNAME, EMAIL, HASHED_PASS);
        assertTrue(userDAO.createUser(u));
}

    @Test
    public void testGetUserByEmail_found() throws SQLException{
    // Check that a valid email returns the expected User object
        Optional<User> opt = userDAO.getUserByEmail(EMAIL);
        assertTrue(opt.isPresent());
        User found = opt.get();
        assertEquals(USERNAME, found.getUsername());
        assertEquals(EMAIL, found.getEmail());
        assertEquals(HASHED_PASS, found.getPasswordHash());
        assertTrue(found.getUserId() > 0);
}

    @Test
    public void testGetUserByEmail_notFound() throws SQLException{
    // Verifies that a non-existent email returns an empty result
        Optional<User> opt = userDAO.getUserByEmail("nonexistent@example.com");
        assertFalse(opt.isPresent());
}

    @Test
    public void testGetUserByEmail_nullInputReturnsEmpty() throws SQLException{
    // Ensures the method handles null input gracefully and returns Optional.empty()
        Optional<User> opt = userDAO.getUserByEmail(null);
        assertFalse(opt.isPresent());
}


}