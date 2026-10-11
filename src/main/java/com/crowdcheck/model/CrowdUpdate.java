package com.crowdcheck.model;

import com.crowdcheck.enums.CrowdLevel;
import java.sql.Timestamp;

/**
 * Model representing a crowd update entry in the CROWD_UPDATES audit table.
 */
public class CrowdUpdate {
    private int updateId;
    private int locationId;
    private String locationName; // Joined for readable reporting
    private Integer userId;       // Nullable if user was deleted
    private String userName;     // Joined for readable reporting
    private int previousCount;
    private int newCount;
    private CrowdLevel crowdLevel;
    private Timestamp updateTime;

    public CrowdUpdate() {}

    public CrowdUpdate(int updateId, int locationId, String locationName, Integer userId,
                       String userName, int previousCount, int newCount,
                       CrowdLevel crowdLevel, Timestamp updateTime) {
        this.updateId = updateId;
        this.locationId = locationId;
        this.locationName = locationName;
        this.userId = userId;
        this.userName = userName;
        this.previousCount = previousCount;
        this.newCount = newCount;
        this.crowdLevel = crowdLevel;
        this.updateTime = updateTime;
    }

    public CrowdUpdate(int locationId, Integer userId, int previousCount, int newCount, CrowdLevel crowdLevel) {
        this.locationId = locationId;
        this.userId = userId;
        this.previousCount = previousCount;
        this.newCount = newCount;
        this.crowdLevel = crowdLevel;
    }

    public int getUpdateId() {
        return updateId;
    }

    public void setUpdateId(int updateId) {
        this.updateId = updateId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getPreviousCount() {
        return previousCount;
    }

    public void setPreviousCount(int previousCount) {
        this.previousCount = previousCount;
    }

    public int getNewCount() {
        return newCount;
    }

    public void setNewCount(int newCount) {
        this.newCount = newCount;
    }

    public CrowdLevel getCrowdLevel() {
        return crowdLevel;
    }

    public void setCrowdLevel(CrowdLevel crowdLevel) {
        this.crowdLevel = crowdLevel;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return String.format("CrowdUpdate[id=%d, locId=%d ('%s'), by='%s', %d -> %d, level=%s, time=%s]",
                updateId, locationId, locationName != null ? locationName : "ID:" + locationId,
                userName != null ? userName : (userId != null ? "UID:" + userId : "System"),
                previousCount, newCount, crowdLevel, updateTime);
    }
}
