package com.crowdcheck.service;

import com.crowdcheck.dao.CrowdAlertDAO;
import com.crowdcheck.enums.AlertStatus;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.model.CrowdAlert;
import com.crowdcheck.model.Location;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Service managing alert generation rules, deduplication, and lifecycle transitions.
 */
public class AlertService {

    private final CrowdAlertDAO alertDAO;

    public AlertService() {
        this.alertDAO = new CrowdAlertDAO();
    }

    public AlertService(CrowdAlertDAO alertDAO) {
        this.alertDAO = alertDAO;
    }

    /**
     * Evaluates a location's crowd state within an active transaction.
     * Generates a new alert if level is HIGH or CRITICAL and no ACTIVE alert currently exists.
     * Prevents duplicate active alerts.
     *
     * @param location Updated location entity
     * @param conn     Active JDBC transaction connection
     * @return Generated CrowdAlert if created, or null if no new alert was triggered
     */
    public CrowdAlert evaluateAndGenerateAlert(Location location, Connection conn) throws SQLException {
        CrowdLevel level = location.getCrowdLevel();

        // Alerts are only triggered for HIGH and CRITICAL levels
        if (!level.isAlertWorthy()) {
            return null;
        }

        // Check for existing ACTIVE alert for this location to prevent duplicates
        CrowdAlert existingActive = alertDAO.findActiveAlertByLocation(location.getLocationId(), conn);
        if (existingActive != null) {
            // Duplicate active alert suppressed
            return null;
        }

        // Compose meaningful alert message
        double occupancy = location.getOccupancyPercentage();
        String message = buildAlertMessage(location.getLocationName(), level, occupancy,
                location.getCurrentCrowdCount(), location.getCapacity());

        CrowdAlert alert = new CrowdAlert(
                location.getLocationId(),
                message,
                level,
                AlertStatus.ACTIVE
        );

        int generatedId = alertDAO.save(alert, conn);
        if (generatedId > 0) {
            alert.setAlertId(generatedId);
            return alert;
        }
        return null;
    }

    /**
     * Non-transactional evaluation helper.
     */
    public CrowdAlert evaluateAndGenerateAlert(Location location) throws SQLException {
        try (Connection conn = com.crowdcheck.config.DatabaseConnection.getConnection()) {
            return evaluateAndGenerateAlert(location, conn);
        }
    }

    /**
     * Builds standard alert message based on crowd metrics.
     */
    public static String buildAlertMessage(String locationName, CrowdLevel level, double occupancy, int count, int capacity) {
        if (level == CrowdLevel.CRITICAL) {
            return String.format("CRITICAL CROWD LEVEL: %s has reached %.1f%% capacity (%d/%d). Immediate access regulation required.",
                    locationName, occupancy, count, capacity);
        } else {
            return String.format("HIGH CROWD LEVEL: %s is approaching capacity at %.1f%% (%d/%d). Monitor facility entries.",
                    locationName, occupancy, count, capacity);
        }
    }

    public boolean acknowledgeAlert(int alertId) throws SQLException {
        CrowdAlert alert = alertDAO.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert ID " + alertId + " does not exist.");
        }
        if (alert.getStatus() == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Cannot acknowledge an already resolved alert.");
        }
        return alertDAO.updateStatus(alertId, AlertStatus.ACKNOWLEDGED);
    }

    public boolean resolveAlert(int alertId) throws SQLException {
        CrowdAlert alert = alertDAO.findById(alertId);
        if (alert == null) {
            throw new IllegalArgumentException("Alert ID " + alertId + " does not exist.");
        }
        return alertDAO.updateStatus(alertId, AlertStatus.RESOLVED);
    }

    public List<CrowdAlert> getActiveAlerts() throws SQLException {
        return alertDAO.findActiveAlerts();
    }

    public List<CrowdAlert> getAllAlerts() throws SQLException {
        return alertDAO.findAllAlerts();
    }

    public CrowdAlert getAlertById(int alertId) throws SQLException {
        return alertDAO.findById(alertId);
    }

    public Map<AlertStatus, Integer> getAlertStatistics() throws SQLException {
        return alertDAO.getAlertCountsByStatus();
    }

    public List<Map<String, Object>> getFrequentOvercrowdedLocations(int limit) throws SQLException {
        return alertDAO.getFrequentOvercrowdedLocations(limit);
    }
}
