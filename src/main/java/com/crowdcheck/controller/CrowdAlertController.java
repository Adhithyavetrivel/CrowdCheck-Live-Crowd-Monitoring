package com.crowdcheck.controller;

import com.crowdcheck.entity.CrowdAlert;
import com.crowdcheck.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class CrowdAlertController {

    private final AlertService alertService;

    public CrowdAlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<List<CrowdAlert>> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/active")
    public ResponseEntity<List<CrowdAlert>> getActiveAlerts() {
        return ResponseEntity.ok(alertService.getActiveAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrowdAlert> getAlertById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(alertService.getAlertById(id));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<CrowdAlert> acknowledgeAlert(
            @PathVariable("id") Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        return ResponseEntity.ok(alertService.acknowledgeAlert(id, role));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<CrowdAlert> resolveAlert(
            @PathVariable("id") Integer id,
            @RequestHeader(value = "X-User-Role", required = false) String headerRole,
            @RequestParam(value = "role", required = false) String paramRole) {
        String role = resolveRole(headerRole, paramRole);
        return ResponseEntity.ok(alertService.resolveAlert(id, role));
    }

    private String resolveRole(String headerRole, String paramRole) {
        if (paramRole != null && !paramRole.isBlank()) return paramRole;
        if (headerRole != null && !headerRole.isBlank()) return headerRole;
        return "STAFF";
    }
}
