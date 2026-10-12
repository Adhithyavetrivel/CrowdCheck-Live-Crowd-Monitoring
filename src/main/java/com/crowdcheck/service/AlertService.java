package com.crowdcheck.service;

import com.crowdcheck.entity.CrowdAlert;
import com.crowdcheck.entity.Location;
import com.crowdcheck.repository.CrowdAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service managing crowd safety alerts, deduplication, and lifecycle transitions.
 */
@Service
public class AlertService {

    private final CrowdAlertRepository alertRepository;

    public AlertService(CrowdAlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /**
     * Evaluates a location's crowd level. If HIGH or CRITICAL, generates a safety alert
     * unless an ACTIVE alert already exists for this facility (deduplication).
     */
    @Transactional
    public Optional<CrowdAlert> evaluateAndGenerateAlert(Location location) {
        String level = location.getCrowdLevel();

        // Only HIGH and CRITICAL trigger alerts
        if (!"HIGH".equalsIgnoreCase(level) && !"CRITICAL".equalsIgnoreCase(level)) {
            return Optional.empty();
        }

        // Avoid unnecessary duplicate active alerts
        Optional<CrowdAlert> existingActive = alertRepository.findFirstByLocationAndStatus(location, "ACTIVE");
        if (existingActive.isPresent()) {
            return Optional.empty();
        }

        double occ = location.getOccupancyPercentage();
        String message;
        if ("CRITICAL".equalsIgnoreCase(level)) {
            message = String.format("CRITICAL alert: %s has reached %.1f%% capacity (%d/%d). Immediate crowd regulation advised.",
                    location.getLocationName(), occ, location.getCurrentCrowdCount(), location.getCapacity());
        } else {
            message = String.format("HIGH alert: %s is at %.1f%% capacity (%d/%d). Monitor facility entrances.",
                    location.getLocationName(), occ, location.getCurrentCrowdCount(), location.getCapacity());
        }

        CrowdAlert alert = new CrowdAlert(location, message, level.toUpperCase(), "ACTIVE");
        return Optional.of(alertRepository.save(alert));
    }

    public List<CrowdAlert> getAllAlerts() {
        return alertRepository.findAllByOrderByAlertTimeDesc();
    }

    public List<CrowdAlert> getActiveAlerts() {
        return alertRepository.findByStatusOrderByAlertTimeDesc("ACTIVE");
    }

    public CrowdAlert getAlertById(Integer alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));
    }

    @Transactional
    public CrowdAlert acknowledgeAlert(Integer alertId, String userRole) {
        verifyAlertManagementRole(userRole);
        CrowdAlert alert = getAlertById(alertId);
        if ("RESOLVED".equalsIgnoreCase(alert.getStatus())) {
            throw new IllegalStateException("Cannot acknowledge an already resolved alert.");
        }
        alert.setStatus("ACKNOWLEDGED");
        return alertRepository.save(alert);
    }

    @Transactional
    public CrowdAlert resolveAlert(Integer alertId, String userRole) {
        verifyAlertManagementRole(userRole);
        CrowdAlert alert = getAlertById(alertId);
        alert.setStatus("RESOLVED");
        return alertRepository.save(alert);
    }

    private void verifyAlertManagementRole(String userRole) {
        if (userRole == null || (!userRole.equalsIgnoreCase("ADMIN") &&
                !userRole.equalsIgnoreCase("ORGANIZER") &&
                !userRole.equalsIgnoreCase("STAFF"))) {
            throw new SecurityException("Access Denied: Only ADMIN, ORGANIZER, or STAFF can manage alerts.");
        }
    }
}
