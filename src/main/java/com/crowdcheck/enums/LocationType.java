package com.crowdcheck.enums;

/**
 * Categorization of campus facilities monitored by CrowdCheck.
 */
public enum LocationType {
    AUDITORIUM,
    CAFETERIA,
    LIBRARY,
    SPORTS_COMPLEX,
    LABORATORY,
    GATE,
    CAMPUS_GROUND,
    PARKING,
    OTHER;

    /**
     * Case-insensitive lookup helper with fallback.
     */
    public static LocationType fromString(String text) {
        if (text == null || text.isBlank()) {
            return OTHER;
        }
        for (LocationType type : LocationType.values()) {
            if (type.name().equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown LocationType: " + text);
    }
}
