package com.crowdcheck.controller;

import com.crowdcheck.dto.LocationResponse;
import com.crowdcheck.entity.Location;
import com.crowdcheck.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> list = locationService.getAllLocations().stream()
                .map(LocationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable("id") Integer id) {
        Location loc = locationService.getLocationById(id);
        return ResponseEntity.ok(LocationResponse.fromEntity(loc));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocationResponse>> searchLocations(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "level", required = false) String level) {
        List<LocationResponse> list = locationService.searchLocations(name, type, level).stream()
                .map(LocationResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<LocationResponse> addLocation(
            @Valid @RequestBody Location location,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        Location created = locationService.addLocation(location, role);
        return new ResponseEntity<>(LocationResponse.fromEntity(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable("id") Integer id,
            @Valid @RequestBody Location details,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        Location updated = locationService.updateLocation(id, details, role);
        return ResponseEntity.ok(LocationResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(
            @PathVariable("id") Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        locationService.deleteLocation(id, role);
        return ResponseEntity.noContent().build();
    }

    private String resolveRole(String headerRole, String paramRole) {
        if (paramRole != null && !paramRole.isBlank()) return paramRole;
        if (headerRole != null && !headerRole.isBlank()) return headerRole;
        return "ADMIN";
    }
}
