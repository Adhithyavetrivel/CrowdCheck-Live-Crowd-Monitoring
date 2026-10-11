package com.crowdcheck.model;

import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.enums.LocationType;
import java.sql.Timestamp;

/**
 * Model representing a campus location in the LOCATIONS table.
 */
public class Location {
    private int locationId;
    private String locationName;
    private LocationType locationType;
    private int capacity;
    private int currentCrowdCount;
    private CrowdLevel crowdLevel;
    private Timestamp lastUpdated;

    public Location() {}

    public Location(int locationId, String locationName, LocationType locationType, int capacity,
                    int currentCrowdCount, CrowdLevel crowdLevel, Timestamp lastUpdated) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.locationType = locationType;
        this.capacity = capacity;
        this.currentCrowdCount = currentCrowdCount;
        this.crowdLevel = crowdLevel;
        this.lastUpdated = lastUpdated;
    }

    public Location(String locationName, LocationType locationType, int capacity) {
        this.locationName = locationName;
        this.locationType = locationType;
        this.capacity = capacity;
        this.currentCrowdCount = 0;
        this.crowdLevel = CrowdLevel.LOW;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCurrentCrowdCount() {
        return currentCrowdCount;
    }

    public void setCurrentCrowdCount(int currentCrowdCount) {
        this.currentCrowdCount = currentCrowdCount;
    }

    public CrowdLevel getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getOccupancyPercentage() {
        if (capacity <= 0) {
            return 0.0;
        }
        return (currentCrowdCount / (double) capacity) * 100.0;
    }

    @Override
    public String toString() {
        return String.format("Location[id=%d, name='%s', type=%s, capacity=%d, count=%d (%.1f%%), level=%s]",
                locationId, locationName, locationType, capacity, currentCrowdCount,
                getOccupancyPercentage(), crowdLevel);
    }
}
