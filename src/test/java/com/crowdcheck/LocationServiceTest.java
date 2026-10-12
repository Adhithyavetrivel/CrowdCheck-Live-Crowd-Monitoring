package com.crowdcheck;

import com.crowdcheck.entity.Location;
import com.crowdcheck.repository.CrowdAlertRepository;
import com.crowdcheck.repository.CrowdUpdateRepository;
import com.crowdcheck.repository.LocationRepository;
import com.crowdcheck.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CrowdUpdateRepository crowdUpdateRepository;

    @Mock
    private CrowdAlertRepository crowdAlertRepository;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;

    @BeforeEach
    void setUp() {
        testLocation = new Location("Central Food Court", "FOOD_COURT", 300);
        testLocation.setLocationId(1);
    }

    @Test
    @DisplayName("Reject location creation with non-positive capacity")
    void testInvalidCapacityRejected() {
        Location invalid = new Location("Test Entrance", "ENTRANCE", 0);
        assertThrows(IllegalArgumentException.class, () ->
                locationService.addLocation(invalid, "ADMIN"));
    }

    @Test
    @DisplayName("Reject invalid location types outside the permitted set")
    void testInvalidLocationTypeRejected() {
        Location invalid = new Location("Gym", "SPORTS_ARENA", 200);
        assertThrows(IllegalArgumentException.class, () ->
                locationService.addLocation(invalid, "ADMIN"));
    }

    @Test
    @DisplayName("Role permission enforcement: STAFF and VISITOR cannot add locations")
    void testUnauthorizedRoleCannotAddLocation() {
        assertThrows(SecurityException.class, () ->
                locationService.addLocation(testLocation, "STAFF"));
        assertThrows(SecurityException.class, () ->
                locationService.addLocation(testLocation, "VISITOR"));
    }

    @Test
    @DisplayName("Safe deletion prevents deleting location when crowd update history exists")
    void testSafeDeletionRejectsWhenHistoryExists() {
        when(locationRepository.findById(1)).thenReturn(Optional.of(testLocation));
        when(crowdUpdateRepository.countByLocation(testLocation)).thenReturn(5L);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                locationService.deleteLocation(1, "ADMIN"));

        assertTrue(ex.getMessage().contains("historical crowd update records"));
    }
}
