package com.crowdcheck.model;

import com.crowdcheck.enums.AlertStatus;
import com.crowdcheck.enums.CrowdLevel;
import java.sql.Timestamp;

/**
 * Model representing a crowd alert in the CROWD_ALERTS table.
 */
public class CrowdAlert {
    private int alertId;
    private int locationId;
    private String locationName; // Joined for readable reporting
    private String alertMessage;
    private Timestamp alertTime;
    private CrowdLevel crowdLevel;
    private AlertStatus status;

    public CrowdAlert() {}

    public CrowdAlert(int alertId, int locationId, String locationName, String alertMessage,
                      Timestamp alertTime, CrowdLevel crowdLevel, AlertStatus status) {
        this.alertId = alertId;
        this.locationId = locationId;
        this.locationName = locationName;
        this.alertMessage = alertMessage;
        this.alertTime = alertTime;
        this.crowdLevel = crowdLevel;
        this.status = status;
    }

    public CrowdAlert(int locationId, String alertMessage, CrowdLevel crowdLevel, AlertStatus status) {
        this.locationId = locationId;
        this.alertMessage = alertMessage;
        this.crowdLevel = crowdLevel;
        this.status = status;
    }

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
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

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public Timestamp getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Timestamp alertTime) {
        this.alertTime = alertTime;
    }

    public CrowdLevel getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("CrowdAlert[id=%d, locId=%d ('%s'), level=%s, status=%s, msg='%s', time=%s]",
                alertId, locationId, locationName != null ? locationName : "ID:" + locationId,
                crowdLevel, status, alertMessage, alertTime);
    }
}
