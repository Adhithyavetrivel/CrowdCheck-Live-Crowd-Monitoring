package com.crowdcheck;

import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.service.AlertService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for AlertService business rules:
 * - Alert-worthy level detection (HIGH and CRITICAL)
 * - Safe level non-alerting (LOW and MODERATE)
 * - Alert message composition with exact occupancy percentages
 */
public class AlertServiceTest {

    @Test
    @DisplayName("HIGH and CRITICAL levels are alert-worthy")
    void testAlertWorthyLevels() {
        assertTrue(CrowdLevel.HIGH.isAlertWorthy());
        assertTrue(CrowdLevel.CRITICAL.isAlertWorthy());
    }

    @Test
    @DisplayName("LOW and MODERATE levels do not trigger alerts")
    void testSafeLevelsNotAlertWorthy() {
        assertFalse(CrowdLevel.LOW.isAlertWorthy());
        assertFalse(CrowdLevel.MODERATE.isAlertWorthy());
    }

    @Test
    @DisplayName("Build alert message for CRITICAL crowd")
    void testBuildCriticalAlertMessage() {
        String msg = AlertService.buildAlertMessage("Main Auditorium", CrowdLevel.CRITICAL, 95.0, 475, 500);
        assertNotNull(msg);
        assertTrue(msg.contains("CRITICAL CROWD LEVEL"));
        assertTrue(msg.contains("Main Auditorium"));
        assertTrue(msg.contains("95.0%"));
        assertTrue(msg.contains("475/500"));
    }

    @Test
    @DisplayName("Build alert message for HIGH crowd")
    void testBuildHighAlertMessage() {
        String msg = AlertService.buildAlertMessage("Central Cafeteria", CrowdLevel.HIGH, 78.5, 157, 200);
        assertNotNull(msg);
        assertTrue(msg.contains("HIGH CROWD LEVEL"));
        assertTrue(msg.contains("Central Cafeteria"));
        assertTrue(msg.contains("78.5%"));
    }
}
