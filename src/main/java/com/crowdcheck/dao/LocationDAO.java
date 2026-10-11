package com.crowdcheck.dao;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.enums.LocationType;
import com.crowdcheck.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for LOCATIONS table.
 * Supports standalone and transactional database operations.
 */
public class LocationDAO {

    public Location findById(int locationId) throws SQLException {
        String sql = "SELECT location_id, location_name, location_type, capacity, " +
                     "current_crowd_count, crowd_level, last_updated " +
                     "FROM LOCATIONS WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Location findByName(String locationName) throws SQLException {
        String sql = "SELECT location_id, location_name, location_type, capacity, " +
                     "current_crowd_count, crowd_level, last_updated " +
                     "FROM LOCATIONS WHERE location_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, locationName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Location> findAll() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT location_id, location_name, location_type, capacity, " +
                     "current_crowd_count, crowd_level, last_updated " +
                     "FROM LOCATIONS ORDER BY location_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Search locations flexibly by keyword (in name), type, and/or crowd level.
     */
    public List<Location> search(String keyword, LocationType type, CrowdLevel level) throws SQLException {
        List<Location> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT location_id, location_name, location_type, capacity, " +
                "current_crowd_count, crowd_level, last_updated FROM LOCATIONS WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND LOWER(location_name) LIKE ?");
            params.add("%" + keyword.trim().toLowerCase() + "%");
        }
        if (type != null) {
            sql.append(" AND location_type = ?");
            params.add(type.name());
        }
        if (level != null) {
            sql.append(" AND crowd_level = ?");
            params.add(level.name());
        }
        sql.append(" ORDER BY location_name ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public int save(Location location) throws SQLException {
        String sql = "INSERT INTO LOCATIONS (location_name, location_type, capacity, current_crowd_count, crowd_level) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, location.getLocationName());
            stmt.setString(2, location.getLocationType().name());
            stmt.setInt(3, location.getCapacity());
            stmt.setInt(4, location.getCurrentCrowdCount());
            stmt.setString(5, location.getCrowdLevel().name());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        location.setLocationId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public boolean update(Location location) throws SQLException {
        String sql = "UPDATE LOCATIONS SET location_name = ?, location_type = ?, capacity = ? " +
                     "WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, location.getLocationName());
            stmt.setString(2, location.getLocationType().name());
            stmt.setInt(3, location.getCapacity());
            stmt.setInt(4, location.getLocationId());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Transactional crowd count and level update.
     * Takes an external open Connection belonging to an active transaction.
     */
    public boolean updateCrowd(int locationId, int newCount, CrowdLevel newLevel, Connection conn) throws SQLException {
        String sql = "UPDATE LOCATIONS SET current_crowd_count = ?, crowd_level = ?, last_updated = CURRENT_TIMESTAMP " +
                     "WHERE location_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newCount);
            stmt.setString(2, newLevel.name());
            stmt.setInt(3, locationId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateCrowd(int locationId, int newCount, CrowdLevel newLevel) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return updateCrowd(locationId, newCount, newLevel, conn);
        }
    }

    public boolean delete(int locationId) throws SQLException {
        String sql = "DELETE FROM LOCATIONS WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int getAssociatedUpdateCount(int locationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CROWD_UPDATES WHERE location_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Location mapRow(ResultSet rs) throws SQLException {
        return new Location(
                rs.getInt("location_id"),
                rs.getString("location_name"),
                LocationType.valueOf(rs.getString("location_type")),
                rs.getInt("capacity"),
                rs.getInt("current_crowd_count"),
                CrowdLevel.valueOf(rs.getString("crowd_level")),
                rs.getTimestamp("last_updated")
        );
    }
}
