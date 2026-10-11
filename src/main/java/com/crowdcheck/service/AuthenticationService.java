package com.crowdcheck.service;

import com.crowdcheck.dao.UserDAO;
import com.crowdcheck.enums.UserRole;
import com.crowdcheck.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Service handling user authentication, session state, and password hashing using SHA-256.
 */
public class AuthenticationService {

    private final UserDAO userDAO;
    private User currentUser;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user against MySQL database records.
     *
     * @param username    Input username
     * @param rawPassword Plain text password
     * @return Logged in User on success, null on failure
     * @throws SQLException on database error
     */
    public User login(String username, String rawPassword) throws SQLException {
        if (username == null || rawPassword == null) {
            return null;
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return null;
        }

        String inputHash = hashPassword(rawPassword);
        if (inputHash.equalsIgnoreCase(user.getPasswordHash())) {
            this.currentUser = user;
            return user;
        }
        return null;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasRole(UserRole... allowedRoles) {
        if (currentUser == null) return false;
        for (UserRole r : allowedRoles) {
            if (currentUser.getUserRole() == r) return true;
        }
        return false;
    }

    public boolean canManageLocations() {
        return currentUser != null && currentUser.getUserRole().canManageLocations();
    }

    public boolean canUpdateCrowd() {
        return currentUser != null && currentUser.getUserRole().canUpdateCrowd();
    }

    public boolean canManageAlerts() {
        return currentUser != null && currentUser.getUserRole().canManageAlerts();
    }

    public boolean canManageUsers() {
        return currentUser != null && currentUser.getUserRole().canManageUsers();
    }

    public boolean canViewReports() {
        return currentUser != null && currentUser.getUserRole().canViewReports();
    }

    /**
     * Registers a new user with hashed password.
     */
    public User registerUser(String username, String email, String contact, UserRole role, String rawPassword)
            throws SQLException {
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }

        String passwordHash = hashPassword(rawPassword);
        User user = new User(username, email, contact, role, passwordHash);
        int generatedId = userDAO.save(user);
        if (generatedId > 0) {
            user.setUserId(generatedId);
            return user;
        }
        return null;
    }

    /**
     * Hashes a raw password string using SHA-256.
     */
    public static String hashPassword(String rawPassword) {
        if (rawPassword == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available in Java runtime", e);
        }
    }
}
