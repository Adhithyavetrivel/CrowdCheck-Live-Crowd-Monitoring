package com.crowdcheck.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Entity representing an immutable crowd update event in CROWD_UPDATES.
 */
@Entity
@Table(name = "CROWD_UPDATES")
public class CrowdUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "update_id")
    private Integer updateId;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotNull(message = "Previous count is required")
    @Min(value = 0, message = "Previous count cannot be negative")
    @Column(name = "previous_count", nullable = false)
    private Integer previousCount;

    @NotNull(message = "New count is required")
    @Min(value = 0, message = "New count cannot be negative")
    @Column(name = "new_count", nullable = false)
    private Integer newCount;

    @NotBlank(message = "Crowd level is required")
    @Column(name = "crowd_level", nullable = false, length = 20)
    private String crowdLevel;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    public void onCreate() {
        if (this.updateTime == null) {
            this.updateTime = LocalDateTime.now();
        }
    }

    public CrowdUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public CrowdUpdate(Location location, Integer previousCount, Integer newCount, String crowdLevel) {
        this.location = location;
        this.previousCount = previousCount;
        this.newCount = newCount;
        this.crowdLevel = crowdLevel;
        this.updateTime = LocalDateTime.now();
    }

    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getPreviousCount() {
        return previousCount;
    }

    public void setPreviousCount(Integer previousCount) {
        this.previousCount = previousCount;
    }

    public Integer getNewCount() {
        return newCount;
    }

    public void setNewCount(Integer newCount) {
        this.newCount = newCount;
    }

    public String getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(String crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
