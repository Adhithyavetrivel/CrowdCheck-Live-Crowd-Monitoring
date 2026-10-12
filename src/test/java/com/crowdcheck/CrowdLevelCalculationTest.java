package com.crowdcheck;

import com.crowdcheck.service.CrowdMonitoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests verifying exact crowd level calculation rules and boundaries.
 */
public class CrowdLevelCalculationTest {

    @ParameterizedTest(name = "Count: {0}, Capacity: {1} -> Expected Level: {2}")
    @CsvSource({
            // LOW: below 40%
            "0, 100, LOW",
            "20, 100, LOW",
            "39, 100, LOW",

            // MODERATE: 40% to below 70%
            "40, 100, MODERATE",
            "55, 100, MODERATE",
            "69, 100, MODERATE",

            // HIGH: 70% to below 90%
            "70, 100, HIGH",
            "80, 100, HIGH",
            "89, 100, HIGH",

            // CRITICAL: 90% or above
            "90, 100, CRITICAL",
            "95, 100, CRITICAL",
            "100, 100, CRITICAL",
            "150, 100, CRITICAL" // Overflow capacity
    })
    @DisplayName("Discrete Threshold Boundary Tests")
    void testBoundaryCalculations(int count, int capacity, String expectedLevel) {
        String level = CrowdMonitoringService.calculateCrowdLevel(count, capacity);
        assertEquals(expectedLevel, level);
    }

    @Test
    @DisplayName("Negative crowd count must be rejected with IllegalArgumentException")
    void testNegativeCountThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                CrowdMonitoringService.calculateCrowdLevel(-5, 100));
        assertTrue(ex.getMessage().contains("negative"));
    }

    @Test
    @DisplayName("Zero or negative capacity must be rejected with IllegalArgumentException")
    void testInvalidCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                CrowdMonitoringService.calculateCrowdLevel(50, 0));
        assertThrows(IllegalArgumentException.class, () ->
                CrowdMonitoringService.calculateCrowdLevel(50, -100));
    }
}
