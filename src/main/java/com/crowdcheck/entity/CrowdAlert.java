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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

/**
 * Entity representing a crowd alert in CROWD_ALERTS.
 * Statuses: ACTIVE, ACKNOWLEDGED, RESOLVED.
 */
@Entity
@Table(name = "CROWD_ALERTS")
public class CrowdAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Integer alertId;

    @NotNull(message = "Location is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @NotBlank(message = "Alert message is required")
    @Column(name = "alert_message", nullable = false, length = 255)
    private String alertMessage;

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @NotBlank(message = "Crowd level is required")
    @Column(name = "crowd_level", nullable = false, length = 20)
    private String crowdLevel;

    @NotBlank(message = "Alert status is required")
    @Pattern(regexp = "^(ACTIVE|ACKNOWLEDGED|RESOLVED)$",
            message = "Status must be ACTIVE, ACKNOWLEDGED, or RESOLVED")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @PrePersist
    public void onCreate() {
        if (this.alertTime == null) {
            this.alertTime = LocalDateTime.now();
        }
    }

    public CrowdAlert() {
        this.alertTime = LocalDateTime.now();
    }

    public CrowdAlert(Location location, String alertMessage, String crowdLevel, String status) {
        this.location = location;
        this.alertMessage = alertMessage;
        this.crowdLevel = crowdLevel;
        this.status = status;
        this.alertTime = LocalDateTime.now();
    }

    public Integer getAlertId() {
        return alertId;
    }

    public void setAlertId(Integer alertId) {
        this.alertId = alertId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public LocalDateTime getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(LocalDateTime alertTime) {
        this.alertTime = alertTime;
    }

    public String getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(String crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
