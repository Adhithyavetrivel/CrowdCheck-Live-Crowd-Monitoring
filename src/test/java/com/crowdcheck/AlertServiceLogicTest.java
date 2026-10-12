package com.crowdcheck;

import com.crowdcheck.entity.CrowdAlert;
import com.crowdcheck.entity.Location;
import com.crowdcheck.repository.CrowdAlertRepository;
import com.crowdcheck.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AlertServiceLogicTest {

    @Mock
    private CrowdAlertRepository alertRepository;

    @InjectMocks
    private AlertService alertService;

    private Location location;

    @BeforeEach
    void setUp() {
        location = new Location("Grand Auditorium", "AUDITORIUM", 500);
        location.setLocationId(1);
    }

    @Test
    @DisplayName("Generates new alert when level is CRITICAL and no active alert exists")
    void testCriticalAlertGenerated() {
        location.setCurrentCrowdCount(475); // 95%
        location.setCrowdLevel("CRITICAL");

        when(alertRepository.findFirstByLocationAndStatus(location, "ACTIVE"))
                .thenReturn(Optional.empty());
        when(alertRepository.save(any(CrowdAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Optional<CrowdAlert> result = alertService.evaluateAndGenerateAlert(location);

        assertTrue(result.isPresent());
        assertEquals("CRITICAL", result.get().getCrowdLevel());
        assertEquals("ACTIVE", result.get().getStatus());
        assertTrue(result.get().getAlertMessage().contains("Grand Auditorium"));
        verify(alertRepository).save(any(CrowdAlert.class));
    }

    @Test
    @DisplayName("Avoids duplicate alert when active alert already exists for the facility")
    void testDuplicateActiveAlertSuppressed() {
        location.setCurrentCrowdCount(480);
        location.setCrowdLevel("CRITICAL");

        CrowdAlert existing = new CrowdAlert(location, "Existing alert", "CRITICAL", "ACTIVE");
        when(alertRepository.findFirstByLocationAndStatus(location, "ACTIVE"))
                .thenReturn(Optional.of(existing));

        Optional<CrowdAlert> result = alertService.evaluateAndGenerateAlert(location);

        assertFalse(result.isPresent());
        verify(alertRepository, never()).save(any(CrowdAlert.class));
    }

    @Test
    @DisplayName("No alert generated when crowd level is LOW or MODERATE")
    void testSafeLevelsDoNotTriggerAlert() {
        location.setCurrentCrowdCount(150); // 30%
        location.setCrowdLevel("LOW");

        Optional<CrowdAlert> result = alertService.evaluateAndGenerateAlert(location);

        assertFalse(result.isPresent());
        verify(alertRepository, never()).save(any(CrowdAlert.class));
    }

    @Test
    @DisplayName("Role permission enforcement: VISITOR cannot manage alerts")
    void testVisitorCannotManageAlerts() {
        assertThrows(SecurityException.class, () ->
                alertService.acknowledgeAlert(1, "VISITOR"));
        assertThrows(SecurityException.class, () ->
                alertService.resolveAlert(1, "VISITOR"));
    }
}
