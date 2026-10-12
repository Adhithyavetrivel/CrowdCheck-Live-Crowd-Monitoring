package com.crowdcheck.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for requesting a crowd count update.
 */
public class CrowdUpdateRequest {

    @NotNull(message = "Location ID is required")
    private Integer locationId;

    @NotNull(message = "New crowd count is required")
    @Min(value = 0, message = "Crowd count cannot be negative")
    private Integer newCount;

    public CrowdUpdateRequest() {}

    public CrowdUpdateRequest(Integer locationId, Integer newCount) {
        this.locationId = locationId;
        this.newCount = newCount;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Integer getNewCount() {
        return newCount;
    }

    public void setNewCount(Integer newCount) {
        this.newCount = newCount;
    }
}
