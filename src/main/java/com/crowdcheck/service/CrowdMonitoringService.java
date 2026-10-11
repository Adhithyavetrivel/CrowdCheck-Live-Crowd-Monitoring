package com.crowdcheck.service;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.dao.CrowdAlertDAO;
import com.crowdcheck.dao.CrowdUpdateDAO;
import com.crowdcheck.dao.LocationDAO;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.enums.LocationType;
import com.crowdcheck.model.CrowdAlert;
import com.crowdcheck.model.CrowdUpdate;
import com.crowdcheck.model.Location;
import com.crowdcheck.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business service for crowd monitoring, transactional updates,
 * facility management, and analytical metrics.
 */
public class CrowdMonitoringService {

    private final LocationDAO locationDAO;
    private final CrowdUpdateDAO updateDAO;
    private final CrowdAlertDAO alertDAO;
    private final AlertService alertService;

    public CrowdMonitoringService() {
        this.locationDAO = new LocationDAO();
        this.updateDAO = new CrowdUpdateDAO();
        this.alertDAO = new CrowdAlertDAO();
        this.alertService = new AlertService(this.alertDAO);
    }

    public CrowdMonitoringService(LocationDAO locationDAO, CrowdUpdateDAO updateDAO,
                                  CrowdAlertDAO alertDAO, AlertService alertService) {
        this.locationDAO = locationDAO;
        this.updateDAO = updateDAO;
        this.alertDAO = alertDAO;
        this.alertService = alertService;
    }

    // =========================================================================
    // 1. LOCATION MANAGEMENT (CRUD & Search)
    // =========================================================================

    public Location getLocationById(int locationId) throws SQLException {
        return locationDAO.findById(locationId);
    }

    public List<Location> getAllLocations() throws SQLException {
        return locationDAO.findAll();
    }

    public List<Location> searchLocations(String keyword, LocationType type, CrowdLevel level) throws SQLException {
        return locationDAO.search(keyword, type, level);
    }

    public int addLocation(Location location, User currentUser) throws SQLException {
        if (currentUser == null || !currentUser.getUserRole().canManageLocations()) {
            throw new SecurityException("Access Denied: Only ADMIN and ORGANIZER roles can add new locations.");
        }
        if (location.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Location name cannot be blank.");
        }
        if (locationDAO.findByName(location.getLocationName().trim()) != null) {
            throw new IllegalArgumentException("A location with name '" + location.getLocationName() + "' already exists.");
        }

        location.setCurrentCrowdCount(0);
        location.setCrowdLevel(CrowdLevel.LOW);
        return locationDAO.save(location);
    }

    public boolean updateLocationDetails(Location location, User currentUser) throws SQLException {
        if (currentUser == null || !currentUser.getUserRole().canManageLocations()) {
            throw new SecurityException("Access Denied: Only ADMIN and ORGANIZER roles can update locations.");
        }
        Location existing = locationDAO.findById(location.getLocationId());
        if (existing == null) {
            throw new IllegalArgumentException("Location ID " + location.getLocationId() + " not found.");
        }
        if (location.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }

        return locationDAO.update(location);
    }

    /**
     * Safely deletes a location.
     * If checkReferences is true and historical updates exist, throws an exception
     * to protect data integrity unless force deletion is confirmed.
     */
    public boolean deleteLocation(int locationId, User currentUser, boolean force) throws SQLException {
        if (currentUser == null || !currentUser.getUserRole().canManageLocations()) {
            throw new SecurityException("Access Denied: Only ADMIN and ORGANIZER roles can delete locations.");
        }
        Location existing = locationDAO.findById(locationId);
        if (existing == null) {
            throw new IllegalArgumentException("Location ID " + locationId + " not found.");
        }

        int updatesCount = locationDAO.getAssociatedUpdateCount(locationId);
        if (updatesCount > 0 && !force) {
            throw new IllegalStateException("Location has " + updatesCount +
                    " recorded crowd updates. Use force deletion if cascade removal is intended.");
        }

        return locationDAO.delete(locationId);
    }

    // =========================================================================
    // 2. TRANSACTIONAL CROWD COUNT UPDATE & ALERT EVALUATION
    // =========================================================================

    /**
     * DTO representing the outcome of a transactional crowd update.
     */
    public static class UpdateResult {
        private final Location location;
        private final int previousCount;
        private final int newCount;
        private final CrowdLevel newLevel;
        private final CrowdAlert generatedAlert;

        public UpdateResult(Location location, int previousCount, int newCount, CrowdLevel newLevel, CrowdAlert generatedAlert) {
            this.location = location;
            this.previousCount = previousCount;
            this.newCount = newCount;
            this.newLevel = newLevel;
            this.generatedAlert = generatedAlert;
        }

        public Location getLocation() { return location; }
        public int getPreviousCount() { return previousCount; }
        public int getNewCount() { return newCount; }
        public CrowdLevel getNewLevel() { return newLevel; }
        public CrowdAlert getGeneratedAlert() { return generatedAlert; }
    }

    /**
     * Atomically updates a location's crowd count:
     * 1. Validates user permissions (ADMIN, ORGANIZER, STAFF).
     * 2. Validates new crowd count (>= 0).
     * 3. Calculates the new CrowdLevel automatically.
     * 4. Updates the LOCATIONS table.
     * 5. Appends an entry to CROWD_UPDATES history.
     * 6. Evaluates and creates a CROWD_ALERTS entry if HIGH/CRITICAL and not duplicate.
     * 7. Commits transaction or rolls back on any failure.
     */
    public UpdateResult updateCrowdCount(int locationId, int newCount, User currentUser) throws SQLException {
        // Authorization check
        if (currentUser == null || !currentUser.getUserRole().canUpdateCrowd()) {
            throw new SecurityException("Access Denied: You do not have permission to update crowd counts.");
        }

        if (newCount < 0) {
            throw new IllegalArgumentException("Crowd count cannot be negative: " + newCount);
        }

        Location location = locationDAO.findById(locationId);
        if (location == null) {
            throw new IllegalArgumentException("Location with ID " + locationId + " does not exist.");
        }

        int previousCount = location.getCurrentCrowdCount();
        int capacity = location.getCapacity();

        // Automatic crowd level calculation
        CrowdLevel newLevel = CrowdLevelCalculator.determineCrowdLevel(newCount, capacity);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            // Step 1: Update LOCATIONS table
            boolean updatedLoc = locationDAO.updateCrowd(locationId, newCount, newLevel, conn);
            if (!updatedLoc) {
                throw new SQLException("Failed to update LOCATIONS table for ID: " + locationId);
            }

            // Step 2: Record in CROWD_UPDATES audit table
            Integer userId = currentUser.getUserId();
            CrowdUpdate updateRecord = new CrowdUpdate(locationId, userId, previousCount, newCount, newLevel);
            int updateId = updateDAO.save(updateRecord, conn);
            if (updateId <= 0) {
                throw new SQLException("Failed to record update in CROWD_UPDATES table.");
            }

            // Update in-memory location state
            location.setCurrentCrowdCount(newCount);
            location.setCrowdLevel(newLevel);

            // Step 3: Evaluate alert generation within the same transaction
            CrowdAlert generatedAlert = alertService.evaluateAndGenerateAlert(location, conn);

            // If level dropped to safe (LOW or MODERATE), auto-resolve existing active alerts if any
            if (!newLevel.isAlertWorthy()) {
                alertDAO.resolveActiveAlertsForLocation(locationId, conn);
            }

            // Commit atomic transaction
            conn.commit();

            return new UpdateResult(location, previousCount, newCount, newLevel, generatedAlert);

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("[CrowdMonitoringService] Rollback error: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[CrowdMonitoringService] Connection close error: " + e.getMessage());
                }
            }
        }
    }

    // =========================================================================
    // 3. HISTORY AND ANALYTICS
    // =========================================================================

    public List<CrowdUpdate> getLocationHistory(int locationId, int limit) throws SQLException {
        return updateDAO.findByLocationId(locationId, limit);
    }

    public List<CrowdUpdate> getRecentUpdates(int limit) throws SQLException {
        return updateDAO.findAll(limit);
    }

    public Map<String, Object> getLocationAnalytics(int locationId) throws SQLException {
        Location loc = locationDAO.findById(locationId);
        if (loc == null) return null;

        double avgCount = updateDAO.getAverageCount(locationId);
        int maxCount = updateDAO.getMaxCount(locationId);
        List<CrowdUpdate> updates = updateDAO.findByLocationId(locationId, 500);

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("location", loc);
        analytics.put("totalUpdatesRecorded", updates.size());
        analytics.put("averageCount", avgCount);
        analytics.put("maxCount", maxCount);
        analytics.put("peakOccupancyPercentage", (loc.getCapacity() > 0) ? (maxCount * 100.0 / loc.getCapacity()) : 0.0);
        return analytics;
    }

    public AlertService getAlertService() {
        return alertService;
    }
}
