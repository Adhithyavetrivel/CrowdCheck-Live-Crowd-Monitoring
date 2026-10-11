package com.crowdcheck.enums;

/**
 * Crowd classification levels based on capacity occupancy percentage:
 * - LOW: Occupancy strictly below 40%
 * - MODERATE: Occupancy from 40% to below 70%
 * - HIGH: Occupancy from 70% to below 90%
 * - CRITICAL: Occupancy 90% and above (including exceeding 100%)
 */
public enum CrowdLevel {
    LOW("Low Traffic (<40%)", "\u001B[32m"),           // Green
    MODERATE("Moderate Traffic (40%-69%)", "\u001B[33m"), // Yellow
    HIGH("High Traffic (70%-89%)", "\u001B[35m"),        // Magenta
    CRITICAL("Critical Capacity (>=90%)", "\u001B[31m");  // Red

    private final String description;
    private final String ansiColor;

    CrowdLevel(String description, String ansiColor) {
        this.description = description;
        this.ansiColor = ansiColor;
    }

    public String getDescription() {
        return description;
    }

    public String getAnsiColor() {
        return ansiColor;
    }

    public boolean isAlertWorthy() {
        return this == HIGH || this == CRITICAL;
    }

    public static CrowdLevel fromString(String text) {
        if (text == null || text.isBlank()) {
            return LOW;
        }
        for (CrowdLevel level : CrowdLevel.values()) {
            if (level.name().equalsIgnoreCase(text.trim())) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown CrowdLevel: " + text);
    }
}
