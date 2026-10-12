package com.crowdcheck.service;

import com.crowdcheck.entity.Location;
import com.crowdcheck.repository.CrowdAlertRepository;
import com.crowdcheck.repository.CrowdUpdateRepository;
import com.crowdcheck.repository.LocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service managing campus location catalog, search filters, and safe deletion.
 */
@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final CrowdUpdateRepository crowdUpdateRepository;
    private final CrowdAlertRepository crowdAlertRepository;

    public LocationService(LocationRepository locationRepository,
                           CrowdUpdateRepository crowdUpdateRepository,
                           CrowdAlertRepository crowdAlertRepository) {
        this.locationRepository = locationRepository;
        this.crowdUpdateRepository = crowdUpdateRepository;
        this.crowdAlertRepository = crowdAlertRepository;
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Integer locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
    }

    public List<Location> searchLocations(String name, String type, String level) {
        String cleanName = (name != null && !name.trim().isEmpty()) ? name.trim() : null;
        String cleanType = (type != null && !type.trim().isEmpty() && !type.equalsIgnoreCase("ALL")) ? type.trim().toUpperCase() : null;
        String cleanLevel = (level != null && !level.trim().isEmpty() && !level.equalsIgnoreCase("ALL")) ? level.trim().toUpperCase() : null;

        return locationRepository.searchLocations(cleanName, cleanType, cleanLevel);
    }

    @Transactional
    public Location addLocation(Location location, String userRole) {
        verifyLocationManagementRole(userRole);

        if (location.getCapacity() == null || location.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly greater than zero");
        }

        if (locationRepository.existsByLocationName(location.getLocationName())) {
            throw new IllegalArgumentException("A location with name '" + location.getLocationName() + "' already exists");
        }

        validateLocationType(location.getLocationType());

        location.setCurrentCrowdCount(0);
        location.setCrowdLevel("LOW");
        return locationRepository.save(location);
    }

    @Transactional
    public Location updateLocation(Integer locationId, Location details, String userRole) {
        verifyLocationManagementRole(userRole);

        Location existing = getLocationById(locationId);

        if (details.getCapacity() == null || details.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly greater than zero");
        }

        validateLocationType(details.getLocationType());

        // Check name uniqueness if changed
        if (!existing.getLocationName().equalsIgnoreCase(details.getLocationName())) {
            if (locationRepository.existsByLocationName(details.getLocationName())) {
                throw new IllegalArgumentException("A location with name '" + details.getLocationName() + "' already exists");
            }
            existing.setLocationName(details.getLocationName());
        }

        existing.setLocationType(details.getLocationType().toUpperCase());
        existing.setCapacity(details.getCapacity());

        // Recompute crowd level if capacity changed
        String updatedLevel = CrowdMonitoringService.calculateCrowdLevel(existing.getCurrentCrowdCount(), existing.getCapacity());
        existing.setCrowdLevel(updatedLevel);

        return locationRepository.save(existing);
    }

    /**
     * Safely deletes a location. Prevents deletion if historical crowd updates or alerts exist.
     */
    @Transactional
    public void deleteLocation(Integer locationId, String userRole) {
        verifyLocationManagementRole(userRole);

        Location existing = getLocationById(locationId);

        long updatesCount = crowdUpdateRepository.countByLocation(existing);
        if (updatesCount > 0) {
            throw new IllegalStateException("Cannot safely delete location '" + existing.getLocationName() +
                    "'. It has " + updatesCount + " historical crowd update records.");
        }

        long alertsCount = crowdAlertRepository.countByLocation(existing);
        if (alertsCount > 0) {
            throw new IllegalStateException("Cannot safely delete location '" + existing.getLocationName() +
                    "'. It has " + alertsCount + " associated crowd alerts.");
        }

        locationRepository.delete(existing);
    }

    private void verifyLocationManagementRole(String userRole) {
        if (userRole == null || (!userRole.equalsIgnoreCase("ADMIN") && !userRole.equalsIgnoreCase("ORGANIZER"))) {
            throw new SecurityException("Access Denied: Only ADMIN and ORGANIZER roles can manage locations.");
        }
    }

    private void validateLocationType(String type) {
        if (type == null || (!type.equalsIgnoreCase("ENTRANCE") &&
                !type.equalsIgnoreCase("FOOD_COURT") &&
                !type.equalsIgnoreCase("AUDITORIUM") &&
                !type.equalsIgnoreCase("PARKING"))) {
            throw new IllegalArgumentException("Location type must be ENTRANCE, FOOD_COURT, AUDITORIUM, or PARKING");
        }
    }
}
