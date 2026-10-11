package com.crowdcheck.service;

import com.crowdcheck.enums.CrowdLevel;

/**
 * Service responsible for computing capacity occupancy and crowd classification.
 *
 * Rules:
 * - LOW: Occupancy strictly below 40% (< 40.0)
 * - MODERATE: Occupancy from 40% to below 70% (>= 40.0 and < 70.0)
 * - HIGH: Occupancy from 70% to below 90% (>= 70.0 and < 90.0)
 * - CRITICAL: Occupancy at least 90% (>= 90.0), including counts exceeding capacity
 *
 * Formula:
 * Occupancy % = (crowd count / capacity) * 100.0
 */
public class CrowdLevelCalculator {

    /**
     * Calculates the occupancy percentage with validation.
     *
     * @param crowdCount Current count of individuals present (must be >= 0)
     * @param capacity   Maximum designated capacity (must be > 0)
     * @return Occupancy percentage (e.g. 75.5 for 75.5%)
     * @throws IllegalArgumentException if crowdCount < 0 or capacity <= 0
     */
    public static double calculateOccupancyPercentage(int crowdCount, int capacity) {
        if (crowdCount < 0) {
            throw new IllegalArgumentException("Crowd count cannot be negative: " + crowdCount);
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly greater than zero: " + capacity);
        }
        return ((double) crowdCount / (double) capacity) * 100.0;
    }

    /**
     * Classifies crowd level based on occupancy percentage.
     *
     * @param crowdCount Current crowd count (>= 0)
     * @param capacity   Designated capacity (> 0)
     * @return CrowdLevel enum (LOW, MODERATE, HIGH, CRITICAL)
     */
    public static CrowdLevel determineCrowdLevel(int crowdCount, int capacity) {
        double occupancy = calculateOccupancyPercentage(crowdCount, capacity);
        return classifyOccupancy(occupancy);
    }

    /**
     * Helper to classify a pre-computed occupancy percentage.
     */
    public static CrowdLevel classifyOccupancy(double occupancyPercentage) {
        if (occupancyPercentage < 40.0) {
            return CrowdLevel.LOW;
        } else if (occupancyPercentage < 70.0) {
            return CrowdLevel.MODERATE;
        } else if (occupancyPercentage < 90.0) {
            return CrowdLevel.HIGH;
        } else {
            return CrowdLevel.CRITICAL;
        }
    }

    /**
     * Returns a visual ASCII progress bar representing current occupancy.
     * E.g.: [████████░░░░░░░░░░░░] 40.0%
     */
    public static String getOccupancyBar(double occupancyPercentage, int totalBlocks) {
        int cappedPercent = Math.min(100, Math.max(0, (int) Math.round(occupancyPercentage)));
        int filled = (int) Math.round((cappedPercent / 100.0) * totalBlocks);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < totalBlocks; i++) {
            if (i < filled) {
                sb.append("█");
            } else {
                sb.append("░");
            }
        }
        sb.append(String.format("] %.1f%%", occupancyPercentage));
        return sb.toString();
    }
}
