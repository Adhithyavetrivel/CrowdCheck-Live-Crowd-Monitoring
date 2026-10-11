package com.crowdcheck.enums;

/**
 * Lifecycle status of crowd alerts in CrowdCheck.
 * - ACTIVE: Freshly generated alert, requiring staff attention.
 * - ACKNOWLEDGED: Operational personnel noted the alert and are responding.
 * - RESOLVED: Crowd situation returned to safe bounds or handled.
 */
public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED;

    public static AlertStatus fromString(String text) {
        if (text == null || text.isBlank()) {
            return ACTIVE;
        }
        for (AlertStatus status : AlertStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown AlertStatus: " + text);
    }
}
