package com.javarepowizards.portfoliomanager.dao;

import com.javarepowizards.portfoliomanager.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GetUserByUsernameTest {

    private UserDAO userDAO;

    @BeforeEach
    public void setUp() throws SQLException{
        // Set up an in-memory database and UserDAO for isolated testing
        Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        IDatabaseConnection testDb = () -> connection;

        userDAO = new UserDAO(testDb);

        // Insert a known user into the test database
        User testUser = new User ("johnsmith", "john@gmail.com", "hashpassword123");
        userDAO.createUser(testUser, 10000);
    }

    @Test
    public void testGetUserByUsername_notFound() throws SQLException{
        // Tests that the DAO returns an empty Optional when the username doesn't exist
        Optional<User> result = userDAO.getUserByUsername("notrealuser");

        assertFalse(result.isPresent());
    }

}
