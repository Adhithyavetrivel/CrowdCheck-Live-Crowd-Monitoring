package com.crowdcheck.dao;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.model.CrowdUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CROWD_UPDATES table.
 * Supports transactional audit logging and analytical queries.
 */
public class CrowdUpdateDAO {

    /**
     * Transactional insert for crowd update audit logging.
     */
    public int save(CrowdUpdate update, Connection conn) throws SQLException {
        String sql = "INSERT INTO CROWD_UPDATES (location_id, user_id, previous_count, new_count, crowd_level) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, update.getLocationId());
            if (update.getUserId() != null) {
                stmt.setInt(2, update.getUserId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setInt(3, update.getPreviousCount());
            stmt.setInt(4, update.getNewCount());
            stmt.setString(5, update.getCrowdLevel().name());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        update.setUpdateId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public int save(CrowdUpdate update) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return save(update, conn);
        }
    }

    public List<CrowdUpdate> findByLocationId(int locationId, int limit) throws SQLException {
        List<CrowdUpdate> list = new ArrayList<>();
        String sql = "SELECT u.update_id, u.location_id, l.location_name, u.user_id, usr.user_name, " +
                     "u.previous_count, u.new_count, u.crowd_level, u.update_time " +
                     "FROM CROWD_UPDATES u " +
                     "JOIN LOCATIONS l ON u.location_id = l.location_id " +
                     "LEFT JOIN USERS usr ON u.user_id = usr.user_id " +
                     "WHERE u.location_id = ? " +
                     "ORDER BY u.update_time DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            stmt.setInt(2, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<CrowdUpdate> findAll(int limit) throws SQLException {
        List<CrowdUpdate> list = new ArrayList<>();
        String sql = "SELECT u.update_id, u.location_id, l.location_name, u.user_id, usr.user_name, " +
                     "u.previous_count, u.new_count, u.crowd_level, u.update_time " +
                     "FROM CROWD_UPDATES u " +
                     "JOIN LOCATIONS l ON u.location_id = l.location_id " +
                     "LEFT JOIN USERS usr ON u.user_id = usr.user_id " +
                     "ORDER BY u.update_time DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 50);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public double getAverageCount(int locationId) throws SQLException {
        String sql = "SELECT AVG(new_count) FROM CROWD_UPDATES WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public int getMaxCount(int locationId) throws SQLException {
        String sql = "SELECT MAX(new_count) FROM CROWD_UPDATES WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private CrowdUpdate mapRow(ResultSet rs) throws SQLException {
        Integer userId = rs.getObject("user_id") != null ? rs.getInt("user_id") : null;
        return new CrowdUpdate(
                rs.getInt("update_id"),
                rs.getInt("location_id"),
                rs.getString("location_name"),
                userId,
                rs.getString("user_name"),
                rs.getInt("previous_count"),
                rs.getInt("new_count"),
                CrowdLevel.valueOf(rs.getString("crowd_level")),
                rs.getTimestamp("update_time")
        );
    }
}
