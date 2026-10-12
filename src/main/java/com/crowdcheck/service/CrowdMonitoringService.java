package com.crowdcheck.service;

import com.crowdcheck.dto.CrowdUpdateRequest;
import com.crowdcheck.dto.LocationResponse;
import com.crowdcheck.entity.CrowdUpdate;
import com.crowdcheck.entity.Location;
import com.crowdcheck.repository.CrowdUpdateRepository;
import com.crowdcheck.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business service for crowd level calculation, transactional headcount updates,
 * audit trail recording, and overcrowding frequency analytics.
 */
@Service
public class CrowdMonitoringService {

    private final LocationRepository locationRepository;
    private final CrowdUpdateRepository crowdUpdateRepository;
    private final AlertService alertService;

    public CrowdMonitoringService(LocationRepository locationRepository,
                                  CrowdUpdateRepository crowdUpdateRepository,
                                  AlertService alertService) {
        this.locationRepository = locationRepository;
        this.crowdUpdateRepository = crowdUpdateRepository;
        this.alertService = alertService;
    }

    /**
     * Calculates the crowd classification level based on capacity occupancy:
     * - LOW: Occupancy strictly below 40%
     * - MODERATE: Occupancy from 40% to below 70%
     * - HIGH: Occupancy from 70% to below 90%
     * - CRITICAL: Occupancy 90% or above (including counts exceeding capacity)
     *
     * @param crowdCount Current crowd count (must be >= 0)
     * @param capacity   Maximum capacity (must be > 0)
     * @return Classification level: LOW, MODERATE, HIGH, CRITICAL
     */
    public static String calculateCrowdLevel(int crowdCount, int capacity) {
        if (crowdCount < 0) {
            throw new IllegalArgumentException("Crowd count cannot be negative: " + crowdCount);
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly greater than zero: " + capacity);
        }

        double occupancy = ((double) crowdCount / (double) capacity) * 100.0;

        if (occupancy < 40.0) {
            return "LOW";
        } else if (occupancy < 70.0) {
            return "MODERATE";
        } else if (occupancy < 90.0) {
            return "HIGH";
        } else {
            return "CRITICAL";
        }
    }

    /**
     * Transactionally updates a location's live crowd count:
     * 1. Validates user role (ADMIN, ORGANIZER, STAFF).
     * 2. Rejects negative counts or invalid capacities.
     * 3. Calculates the new crowd level.
     * 4. Updates the LOCATIONS record.
     * 5. Saves historical record into CROWD_UPDATES.
     * 6. Generates safety alert if level is HIGH or CRITICAL without active duplicate.
     */
    @Transactional
    public LocationResponse updateCrowdCount(CrowdUpdateRequest request, String userRole) {
        verifyCrowdUpdateRole(userRole);

        if (request.getNewCount() == null || request.getNewCount() < 0) {
            throw new IllegalArgumentException("Crowd count cannot be negative");
        }

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + request.getLocationId()));

        if (location.getCapacity() <= 0) {
            throw new IllegalArgumentException("Invalid location capacity: " + location.getCapacity());
        }

        int previousCount = location.getCurrentCrowdCount();
        int newCount = request.getNewCount();

        // Calculate new level
        String newLevel = calculateCrowdLevel(newCount, location.getCapacity());

        // Update location entity
        location.setCurrentCrowdCount(newCount);
        location.setCrowdLevel(newLevel);
        location = locationRepository.save(location);

        // Record in CROWD_UPDATES audit table
        CrowdUpdate history = new CrowdUpdate(location, previousCount, newCount, newLevel);
        crowdUpdateRepository.save(history);

        // Check and generate alert if HIGH or CRITICAL
        alertService.evaluateAndGenerateAlert(location);

        return LocationResponse.fromEntity(location);
    }

    public List<CrowdUpdate> getLocationHistory(Integer locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        return crowdUpdateRepository.findByLocationOrderByUpdateTimeDesc(location);
    }

    public List<CrowdUpdate> getAllHistory() {
        return crowdUpdateRepository.findAllByOrderByUpdateTimeDesc();
    }

    /**
     * Identifies locations that frequently experience HIGH or CRITICAL crowd levels.
     */
    public List<Map<String, Object>> getFrequentOvercrowdedLocations() {
        List<Object[]> rawList = crowdUpdateRepository.findFrequentOvercrowdedLocations();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawList) {
            Map<String, Object> map = new HashMap<>();
            map.put("locationId", row[0]);
            map.put("locationName", row[1]);
            map.put("locationType", row[2]);
            map.put("capacity", row[3]);
            map.put("highCriticalCount", row[4]);
            result.add(map);
        }
        return result;
    }

    private void verifyCrowdUpdateRole(String userRole) {
        if (userRole == null || (!userRole.equalsIgnoreCase("ADMIN") &&
                !userRole.equalsIgnoreCase("ORGANIZER") &&
                !userRole.equalsIgnoreCase("STAFF"))) {
            throw new SecurityException("Access Denied: Only ADMIN, ORGANIZER, or STAFF can update crowd details.");
        }
    }
}
