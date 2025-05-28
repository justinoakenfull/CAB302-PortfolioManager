package com.javarepowizards.portfoliomanager.services.Auth;

import com.javarepowizards.portfoliomanager.dao.user.IUserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import com.javarepowizards.portfoliomanager.services.utility.ValidationException;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class RegistrationService {

    private final IAuthService authService;
    private final IUserDAO userDAO;

    public RegistrationService(IAuthService authService, IUserDAO userDAO) {
        this.authService = authService;
        this.userDAO = userDAO;
    }

    public void register(String username,
                         String email,
                         String rawPassword,
                         String rawConfirmPassword,
                         String rawBalance)
            throws ValidationException, SQLException
    {
        validateAllFieldsNonEmpty(username, email, rawPassword, rawConfirmPassword, rawBalance);
        validatePasswordsMatch(rawPassword, rawConfirmPassword);
        validatePasswordLength(rawPassword);
        validateEmailFormat(email);
        double balance = parseAndValidateBalance(rawBalance);

        String hashed = authService.hashPassword(rawPassword);
        User user = new User(username, email, hashed);

        if (!userDAO.createUser(user, balance)) {
            throw new SQLException("Failed to create user in database");
        }
    }

    private void validateAllFieldsNonEmpty(String username,
                                           String email,
                                           String password,
                                           String confirmPassword,
                                           String balance)
            throws ValidationException
    {
        if (username.isBlank() ||
                email.isBlank()    ||
                password.isBlank() ||
                confirmPassword.isBlank() ||
                balance.isBlank())
        {
            throw new ValidationException("All fields are required");
        }
    }

    private void validatePasswordsMatch(String password, String confirmPassword)
            throws ValidationException
    {
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
    }

    private void validatePasswordLength(String password)
            throws ValidationException
    {
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }

    private void validateEmailFormat(String email)
            throws ValidationException
    {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    private double parseAndValidateBalance(String rawBalance)
            throws ValidationException
    {
        try {
            double bal = Double.parseDouble(rawBalance);
            if (bal < 0) {
                throw new ValidationException("Balance cannot be negative");
            }
            return bal;
        } catch (NumberFormatException e) {
            throw new ValidationException("Balance must be a valid number");
        }
    }
}
