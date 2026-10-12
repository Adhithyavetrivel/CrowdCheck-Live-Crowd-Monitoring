package com.crowdcheck.dto;

import com.crowdcheck.entity.Location;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the live crowd status of a location.
 * Contains location details, capacities, calculated occupancy percentage, and crowd level.
 */
public class LocationResponse {

    private Integer locationId;
    private String locationName;
    private String locationType;
    private Integer capacity;
    private Integer currentCrowdCount;
    private Double occupancyPercentage;
    private String crowdLevel;
    private LocalDateTime lastUpdated;

    public LocationResponse() {}

    public LocationResponse(Integer locationId, String locationName, String locationType,
                            Integer capacity, Integer currentCrowdCount,
                            Double occupancyPercentage, String crowdLevel,
                            LocalDateTime lastUpdated) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.locationType = locationType;
        this.capacity = capacity;
        this.currentCrowdCount = currentCrowdCount;
        this.occupancyPercentage = occupancyPercentage;
        this.crowdLevel = crowdLevel;
        this.lastUpdated = lastUpdated;
    }

    public static LocationResponse fromEntity(Location location) {
        if (location == null) return null;
        double occ = location.getOccupancyPercentage();
        // Round to 1 decimal place
        double roundedOcc = Math.round(occ * 10.0) / 10.0;
        return new LocationResponse(
                location.getLocationId(),
                location.getLocationName(),
                location.getLocationType(),
                location.getCapacity(),
                location.getCurrentCrowdCount(),
                roundedOcc,
                location.getCrowdLevel(),
                location.getLastUpdated()
        );
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCurrentCrowdCount() {
        return currentCrowdCount;
    }

    public void setCurrentCrowdCount(Integer currentCrowdCount) {
        this.currentCrowdCount = currentCrowdCount;
    }

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(Double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public String getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(String crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
