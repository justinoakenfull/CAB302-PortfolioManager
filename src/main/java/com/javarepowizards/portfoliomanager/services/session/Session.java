package com.javarepowizards.portfoliomanager.services.session;

import com.javarepowizards.portfoliomanager.models.User;

/**
 * Holds the current user session state.
 * Provides static methods to set and retrieve the authenticated user.
 */
public class Session {
    private static User currentUser;
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    public static User getCurrentUser() {
        return currentUser;
    }
}
