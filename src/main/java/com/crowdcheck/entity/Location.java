package com.crowdcheck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * Entity representing a monitored campus location in the LOCATIONS table.
 * Types: ENTRANCE, FOOD_COURT, AUDITORIUM, PARKING.
 * Levels: LOW, MODERATE, HIGH, CRITICAL.
 */
@Entity
@Table(name = "LOCATIONS")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_id")
    private Integer locationId;

    @NotBlank(message = "Location name is required")
    @Column(name = "location_name", nullable = false, unique = true, length = 100)
    private String locationName;

    @NotBlank(message = "Location type is required")
    @Pattern(regexp = "^(ENTRANCE|FOOD_COURT|AUDITORIUM|PARKING)$",
            message = "Location type must be ENTRANCE, FOOD_COURT, AUDITORIUM, or PARKING")
    @Column(name = "location_type", nullable = false, length = 50)
    private String locationType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be greater than zero")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull(message = "Current crowd count is required")
    @Min(value = 0, message = "Crowd count cannot be negative")
    @Column(name = "current_crowd_count", nullable = false)
    private Integer currentCrowdCount = 0;

    @NotBlank(message = "Crowd level is required")
    @Pattern(regexp = "^(LOW|MODERATE|HIGH|CRITICAL)$",
            message = "Crowd level must be LOW, MODERATE, HIGH, or CRITICAL")
    @Column(name = "crowd_level", nullable = false, length = 20)
    private String crowdLevel = "LOW";

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    public void onTimestampUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Location() {
        this.lastUpdated = LocalDateTime.now();
    }

    public Location(String locationName, String locationType, Integer capacity) {
        this.locationName = locationName;
        this.locationType = locationType;
        this.capacity = capacity;
        this.currentCrowdCount = 0;
        this.crowdLevel = "LOW";
        this.lastUpdated = LocalDateTime.now();
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

    public double getOccupancyPercentage() {
        if (capacity == null || capacity <= 0) {
            return 0.0;
        }
        int count = currentCrowdCount != null ? currentCrowdCount : 0;
        return ((double) count / (double) capacity) * 100.0;
    }
}
