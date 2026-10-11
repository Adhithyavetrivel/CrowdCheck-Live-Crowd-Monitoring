package com.crowdcheck.dao;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.enums.UserRole;
import com.crowdcheck.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for USERS table.
 * All operations use PreparedStatements and try-with-resources.
 */
public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, user_name, email, contact_info, user_role, password_hash, created_at " +
                     "FROM USERS WHERE user_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, user_name, email, contact_info, user_role, password_hash, created_at " +
                     "FROM USERS WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public User findById(int userId) throws SQLException {
        String sql = "SELECT user_id, user_name, email, contact_info, user_role, password_hash, created_at " +
                     "FROM USERS WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, user_name, email, contact_info, user_role, password_hash, created_at " +
                     "FROM USERS ORDER BY user_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    public int save(User user) throws SQLException {
        String sql = "INSERT INTO USERS (user_name, email, contact_info, user_role, password_hash) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getContactInfo());
            stmt.setString(4, user.getUserRole().name());
            stmt.setString(5, user.getPasswordHash());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int generatedId = keys.getInt(1);
                        user.setUserId(generatedId);
                        return generatedId;
                    }
                }
            }
        }
        return -1;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE USERS SET user_name = ?, email = ?, contact_info = ?, user_role = ? " +
                     "WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUserName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getContactInfo());
            stmt.setString(4, user.getUserRole().name());
            stmt.setInt(5, user.getUserId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE USERS SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(int userId) throws SQLException {
        String sql = "DELETE FROM USERS WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("user_name"),
                rs.getString("email"),
                rs.getString("contact_info"),
                UserRole.valueOf(rs.getString("user_role")),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at")
        );
    }
}
