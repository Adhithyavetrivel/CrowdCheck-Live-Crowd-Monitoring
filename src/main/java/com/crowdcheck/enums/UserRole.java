package com.crowdcheck.enums;

/**
 * Role-Based Access Control (RBAC) definitions for CrowdCheck.
 * Hierarchy / Permissions:
 * - ADMIN: Full access (Locations, Crowd Updates, Alerts, User Management, Reports)
 * - ORGANIZER: Event and facility control (Manage Locations, Update Crowd, Manage Alerts, Reports)
 * - STAFF: Operational reporting (Update Crowd, View & Manage Alerts, View Live Status)
 * - VISITOR: Read-only access (View Live Status, Search Locations, View Public Alerts)
 */
public enum UserRole {
    ADMIN,
    ORGANIZER,
    STAFF,
    VISITOR;

    public boolean canManageLocations() {
        return this == ADMIN || this == ORGANIZER;
    }

    public boolean canUpdateCrowd() {
        return this == ADMIN || this == ORGANIZER || this == STAFF;
    }

    public boolean canManageAlerts() {
        return this == ADMIN || this == ORGANIZER || this == STAFF;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }

    public boolean canViewReports() {
        return this == ADMIN || this == ORGANIZER || this == STAFF;
    }
}
