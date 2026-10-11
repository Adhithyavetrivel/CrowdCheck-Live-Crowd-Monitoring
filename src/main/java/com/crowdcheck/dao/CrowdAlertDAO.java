package com.crowdcheck.dao;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.enums.AlertStatus;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.model.CrowdAlert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for CROWD_ALERTS table.
 * Supports transactional creation and alert lifecycle management.
 */
public class CrowdAlertDAO {

    public int save(CrowdAlert alert, Connection conn) throws SQLException {
        String sql = "INSERT INTO CROWD_ALERTS (location_id, alert_message, crowd_level, status) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, alert.getLocationId());
            stmt.setString(2, alert.getAlertMessage());
            stmt.setString(3, alert.getCrowdLevel().name());
            stmt.setString(4, alert.getStatus().name());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        alert.setAlertId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public int save(CrowdAlert alert) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return save(alert, conn);
        }
    }

    public CrowdAlert findActiveAlertByLocation(int locationId, Connection conn) throws SQLException {
        String sql = "SELECT a.alert_id, a.location_id, l.location_name, a.alert_message, " +
                     "a.alert_time, a.crowd_level, a.status " +
                     "FROM CROWD_ALERTS a " +
                     "JOIN LOCATIONS l ON a.location_id = l.location_id " +
                     "WHERE a.location_id = ? AND a.status = 'ACTIVE' " +
                     "ORDER BY a.alert_time DESC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public CrowdAlert findActiveAlertByLocation(int locationId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return findActiveAlertByLocation(locationId, conn);
        }
    }

    public List<CrowdAlert> findActiveAlerts() throws SQLException {
        List<CrowdAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.location_id, l.location_name, a.alert_message, " +
                     "a.alert_time, a.crowd_level, a.status " +
                     "FROM CROWD_ALERTS a " +
                     "JOIN LOCATIONS l ON a.location_id = l.location_id " +
                     "WHERE a.status = 'ACTIVE' " +
                     "ORDER BY a.alert_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<CrowdAlert> findAllAlerts() throws SQLException {
        List<CrowdAlert> list = new ArrayList<>();
        String sql = "SELECT a.alert_id, a.location_id, l.location_name, a.alert_message, " +
                     "a.alert_time, a.crowd_level, a.status " +
                     "FROM CROWD_ALERTS a " +
                     "JOIN LOCATIONS l ON a.location_id = l.location_id " +
                     "ORDER BY a.alert_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public CrowdAlert findById(int alertId) throws SQLException {
        String sql = "SELECT a.alert_id, a.location_id, l.location_name, a.alert_message, " +
                     "a.alert_time, a.crowd_level, a.status " +
                     "FROM CROWD_ALERTS a " +
                     "JOIN LOCATIONS l ON a.location_id = l.location_id " +
                     "WHERE a.alert_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, alertId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateStatus(int alertId, AlertStatus status) throws SQLException {
        String sql = "UPDATE CROWD_ALERTS SET status = ? WHERE alert_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, alertId);
            return stmt.executeUpdate() > 0;
        }
    }

    public int resolveActiveAlertsForLocation(int locationId, Connection conn) throws SQLException {
        String sql = "UPDATE CROWD_ALERTS SET status = 'RESOLVED' " +
                     "WHERE location_id = ? AND status IN ('ACTIVE', 'ACKNOWLEDGED')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, locationId);
            return stmt.executeUpdate();
        }
    }

    public Map<AlertStatus, Integer> getAlertCountsByStatus() throws SQLException {
        Map<AlertStatus, Integer> counts = new LinkedHashMap<>();
        for (AlertStatus s : AlertStatus.values()) {
            counts.put(s, 0);
        }
        String sql = "SELECT status, COUNT(*) AS cnt FROM CROWD_ALERTS GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                AlertStatus status = AlertStatus.valueOf(rs.getString("status"));
                counts.put(status, rs.getInt("cnt"));
            }
        }
        return counts;
    }

    /**
     * Analytical query: identify locations with the highest number of safety alerts triggered.
     */
    public List<Map<String, Object>> getFrequentOvercrowdedLocations(int limit) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();
        String sql = "SELECT l.location_id, l.location_name, l.capacity, COUNT(a.alert_id) AS total_alerts, " +
                     "SUM(CASE WHEN a.crowd_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_alerts, " +
                     "SUM(CASE WHEN a.crowd_level = 'HIGH' THEN 1 ELSE 0 END) AS high_alerts " +
                     "FROM LOCATIONS l " +
                     "JOIN CROWD_ALERTS a ON l.location_id = a.location_id " +
                     "GROUP BY l.location_id, l.location_name, l.capacity " +
                     "ORDER BY total_alerts DESC, critical_alerts DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("locationId", rs.getInt("location_id"));
                    row.put("locationName", rs.getString("location_name"));
                    row.put("capacity", rs.getInt("capacity"));
                    row.put("totalAlerts", rs.getInt("total_alerts"));
                    row.put("criticalAlerts", rs.getInt("critical_alerts"));
                    row.put("highAlerts", rs.getInt("high_alerts"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    private CrowdAlert mapRow(ResultSet rs) throws SQLException {
        return new CrowdAlert(
                rs.getInt("alert_id"),
                rs.getInt("location_id"),
                rs.getString("location_name"),
                rs.getString("alert_message"),
                rs.getTimestamp("alert_time"),
                CrowdLevel.valueOf(rs.getString("crowd_level")),
                AlertStatus.valueOf(rs.getString("status"))
        );
    }
}
