package com.crowdcheck.controller;

import com.crowdcheck.dto.CrowdUpdateRequest;
import com.crowdcheck.dto.LocationResponse;
import com.crowdcheck.entity.CrowdUpdate;
import com.crowdcheck.service.CrowdMonitoringService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crowd")
public class CrowdUpdateController {

    private final CrowdMonitoringService monitoringService;

    public CrowdUpdateController(CrowdMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @PostMapping("/update")
    public ResponseEntity<LocationResponse> updateCrowdCount(
            @Valid @RequestBody CrowdUpdateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        LocationResponse response = monitoringService.updateCrowdCount(request, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<CrowdUpdate>> getAllHistory() {
        return ResponseEntity.ok(monitoringService.getAllHistory());
    }

    @GetMapping("/history/{locationId}")
    public ResponseEntity<List<CrowdUpdate>> getLocationHistory(@PathVariable("locationId") Integer locationId) {
        return ResponseEntity.ok(monitoringService.getLocationHistory(locationId));
    }

    @GetMapping("/analysis/frequent-overcrowded")
    public ResponseEntity<List<Map<String, Object>>> getFrequentOvercrowdedLocations() {
        return ResponseEntity.ok(monitoringService.getFrequentOvercrowdedLocations());
    }

    private String resolveRole(String headerRole, String paramRole) {
        if (paramRole != null && !paramRole.isBlank()) return paramRole;
        if (headerRole != null && !headerRole.isBlank()) return headerRole;
        return "STAFF";
    }
}
