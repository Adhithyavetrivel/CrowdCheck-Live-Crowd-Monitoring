package com.crowdcheck.model;

import com.crowdcheck.enums.UserRole;
import java.sql.Timestamp;

/**
 * Model representing a user in the USERS table.
 */
public class User {
    private int userId;
    private String userName;
    private String email;
    private String contactInfo;
    private UserRole userRole;
    private String passwordHash;
    private Timestamp createdAt;

    public User() {}

    public User(int userId, String userName, String email, String contactInfo, UserRole userRole, String passwordHash, Timestamp createdAt) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.contactInfo = contactInfo;
        this.userRole = userRole;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public User(String userName, String email, String contactInfo, UserRole userRole, String passwordHash) {
        this.userName = userName;
        this.email = email;
        this.contactInfo = contactInfo;
        this.userRole = userRole;
        this.passwordHash = passwordHash;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("User[id=%d, name='%s', email='%s', role=%s, contact='%s']",
                userId, userName, email, userRole, contactInfo);
    }
}
