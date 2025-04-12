package com.javarepowizards.portfoliomanager.services;



import com.javarepowizards.portfoliomanager.dao.UserDAO;
import com.javarepowizards.portfoliomanager.models.User;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class AuthService {
    public User registerUser(String username, String password) throws Exception {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);

        User newUser = new User(username, hashedPassword, salt);
        UserDAO.createUser(newUser);
        return newUser;
    }

    public User loginUser(String username, String password) throws Exception {
        User user = UserDAO.findUserByUsername(username);
        if (user == null) {
            throw new Exception("User not found");
        }

        String hashedPassword = BCrypt.hashpw(password, user.getSalt());
        if (hashedPassword.equals(user.getPasswordHash())) {
            return user;
        } else {
            throw new Exception("Invalid credentials");
        }
    }
}