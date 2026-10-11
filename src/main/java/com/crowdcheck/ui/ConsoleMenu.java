package com.crowdcheck.ui;

import com.crowdcheck.dao.UserDAO;
import com.crowdcheck.enums.AlertStatus;
import com.crowdcheck.enums.CrowdLevel;
import com.crowdcheck.enums.LocationType;
import com.crowdcheck.enums.UserRole;
import com.crowdcheck.model.CrowdAlert;
import com.crowdcheck.model.CrowdUpdate;
import com.crowdcheck.model.Location;
import com.crowdcheck.model.User;
import com.crowdcheck.service.AlertService;
import com.crowdcheck.service.AuthenticationService;
import com.crowdcheck.service.CrowdLevelCalculator;
import com.crowdcheck.service.CrowdMonitoringService;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Console-based menu interface for CrowdCheck.
 * Implements role-aware operations, formatted data tables, and input handling.
 */
public class ConsoleMenu {

    private final Scanner scanner;
    private final AuthenticationService authService;
    private final CrowdMonitoringService monitoringService;
    private final AlertService alertService;
    private final UserDAO userDAO;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.authService = new AuthenticationService();
        this.monitoringService = new CrowdMonitoringService();
        this.alertService = monitoringService.getAlertService();
        this.userDAO = new UserDAO();
    }

    public void start() {
        printBanner();
        boolean running = true;

        while (running) {
            displayMainMenu();
            int choice = InputValidator.readInt(scanner, "Enter option [0-10]: ", 0, 10);
            System.out.println();

            try {
                switch (choice) {
                    case 1 -> handleLoginOrLogout();
                    case 2 -> handleViewLiveCrowdStatus();
                    case 3 -> handleSearchLocations();
                    case 4 -> handleUpdateCrowdCount();
                    case 5 -> handleManageLocations();
                    case 6 -> handleViewAlerts();
                    case 7 -> handleManageAlerts();
                    case 8 -> handleViewCrowdHistory();
                    case 9 -> handleCrowdAnalysisAndReports();
                    case 10 -> handleManageUsers();
                    case 0 -> {
                        System.out.println("===============================================================================");
                        System.out.println("  Exiting CrowdCheck. Thank you for maintaining campus safety and efficiency! ");
                        System.out.println("===============================================================================");
                        running = false;
                    }
                    default -> System.out.println("  [!] Invalid choice. Please choose from 0 to 10.");
                }
            } catch (SQLException e) {
                System.out.println("  [Database Error] " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("  [Access Denied] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  [Error] " + e.getMessage());
            }

            if (running) {
                System.out.println("\nPress [Enter] to return to the menu...");
                if (scanner.hasNextLine()) {
                    scanner.nextLine();
                } else {
                    running = false;
                }
            }
        }
    }

    private void printBanner() {
        System.out.println("===============================================================================");
        System.out.println("      ____                           _  ____ _               _    ");
        System.out.println("     / ___|_ __ _____      _____   | |/ ___| |__   ___  ___| | __ ");
        System.out.println("    | |   | '__/ _ \\ \\ /\\ / / _ \\  | | |   | '_ \\ / _ \\/ __| |/ / ");
        System.out.println("    | |___| | | (_) \\ V  V / (_| | | | |___| | | |  __/ (__|   <  ");
        System.out.println("     \\____|_|  \\___/ \\_/\\_/ \\__,_| |_|\\____|_| |_|\\___|\\___|_|\\_\\ ");
        System.out.println("                  LIVE CROWD MONITORING SYSTEM (DBMS)                 ");
        System.out.println("===============================================================================");
    }

    private void displayMainMenu() {
        System.out.println("\n-------------------------------------------------------------------------------");
        User user = authService.getCurrentUser();
        if (user != null) {
            System.out.printf("  Logged In: %s | Role: [%s] | Email: %s%n",
                    user.getUserName(), user.getUserRole(), user.getEmail());
        } else {
            System.out.println("  Session: Guest / Visitor Mode (Login for Staff/Admin capabilities)");
        }
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("  1. " + (user != null ? "Switch User / Logout" : "Login"));
        System.out.println("  2. View live crowd status");
        System.out.println("  3. Search locations");
        System.out.println("  4. Update crowd count" + (canUpdate() ? "" : " [Requires STAFF+]"));
        System.out.println("  5. Manage locations" + (canManageLoc() ? "" : " [Requires ORGANIZER+]"));
        System.out.println("  6. View alerts");
        System.out.println("  7. Manage alerts" + (canManageAlerts() ? "" : " [Requires STAFF+]"));
        System.out.println("  8. View crowd history");
        System.out.println("  9. Crowd analysis and reports");
        System.out.println("  10. Manage users" + (canManageUsers() ? "" : " [Requires ADMIN]"));
        System.out.println("  0. Exit");
        System.out.println("-------------------------------------------------------------------------------");
    }

    private boolean canUpdate() {
        return authService.getCurrentUser() != null && authService.getCurrentUser().getUserRole().canUpdateCrowd();
    }

    private boolean canManageLoc() {
        return authService.getCurrentUser() != null && authService.getCurrentUser().getUserRole().canManageLocations();
    }

    private boolean canManageAlerts() {
        return authService.getCurrentUser() != null && authService.getCurrentUser().getUserRole().canManageAlerts();
    }

    private boolean canManageUsers() {
        return authService.getCurrentUser() != null && authService.getCurrentUser().getUserRole().canManageUsers();
    }

    // =========================================================================
    // 1. LOGIN / LOGOUT
    // =========================================================================

    private void handleLoginOrLogout() throws SQLException {
        if (authService.getCurrentUser() != null) {
            System.out.println("  Currently logged in as: " + authService.getCurrentUser().getUserName());
            System.out.println("  1. Switch Account");
            System.out.println("  2. Logout to Guest Mode");
            int sub = InputValidator.readInt(scanner, "Choose option [1-2]: ", 1, 2);
            if (sub == 2) {
                authService.logout();
                System.out.println("  [+] Successfully logged out.");
                return;
            }
        }

        System.out.println("=== USER LOGIN ===");
        System.out.println("(Sample credentials: admin / Admin@123, staff_john / Staff@123, visitor_alex / Visitor@123)");
        String username = InputValidator.readString(scanner, "Username: ", false);
        String password = InputValidator.readString(scanner, "Password: ", false);

        User loggedIn = authService.login(username, password);
        if (loggedIn != null) {
            System.out.printf("  [+] Welcome back, %s! Logged in as [%s].%n",
                    loggedIn.getUserName(), loggedIn.getUserRole());
        } else {
            System.out.println("  [-] Invalid username or password.");
        }
    }

    // =========================================================================
    // 2. VIEW LIVE CROWD STATUS
    // =========================================================================

    private void handleViewLiveCrowdStatus() throws SQLException {
        List<Location> locations = monitoringService.getAllLocations();
        System.out.println("=======================================================================================================");
        System.out.println("                                       LIVE CAMPUS CROWD STATUS                                        ");
        System.out.println("=======================================================================================================");
        if (locations.isEmpty()) {
            System.out.println("  No locations registered in the database.");
            return;
        }

        printLocationTableHeader();
        for (Location loc : locations) {
            printLocationTableRow(loc);
        }
        System.out.println("-------------------------------------------------------------------------------------------------------");
        System.out.printf("  Total Locations: %d | Low: %d | Moderate: %d | High: %d | Critical: %d%n",
                locations.size(),
                locations.stream().filter(l -> l.getCrowdLevel() == CrowdLevel.LOW).count(),
                locations.stream().filter(l -> l.getCrowdLevel() == CrowdLevel.MODERATE).count(),
                locations.stream().filter(l -> l.getCrowdLevel() == CrowdLevel.HIGH).count(),
                locations.stream().filter(l -> l.getCrowdLevel() == CrowdLevel.CRITICAL).count()
        );
    }

    private void printLocationTableHeader() {
        System.out.printf("| %-4s | %-24s | %-14s | %-8s | %-7s | %-8s | %-10s | %-19s |%n",
                "ID", "Location Name", "Type", "Capacity", "Current", "Occup %", "Level", "Last Updated");
        System.out.println("|------|--------------------------|----------------|----------|---------|----------|------------|---------------------|");
    }

    private void printLocationTableRow(Location loc) {
        String formattedDate = loc.getLastUpdated() != null ? dateFormat.format(loc.getLastUpdated()) : "N/A";
        System.out.printf("| %-4d | %-24s | %-14s | %-8d | %-7d | %6.1f%%  | %-10s | %-19s |%n",
                loc.getLocationId(),
                truncate(loc.getLocationName(), 24),
                truncate(loc.getLocationType().name(), 14),
                loc.getCapacity(),
                loc.getCurrentCrowdCount(),
                loc.getOccupancyPercentage(),
                loc.getCrowdLevel().name(),
                formattedDate);
    }

    // =========================================================================
    // 3. SEARCH LOCATIONS
    // =========================================================================

    private void handleSearchLocations() throws SQLException {
        System.out.println("=== SEARCH LOCATIONS ===");
        System.out.println("  1. Search by Location ID");
        System.out.println("  2. Search by Name Keyword");
        System.out.println("  3. Filter by Location Type");
        System.out.println("  4. Filter by Crowd Level");
        int option = InputValidator.readInt(scanner, "Choose search mode [1-4]: ", 1, 4);

        List<Location> results;
        switch (option) {
            case 1 -> {
                int id = InputValidator.readInt(scanner, "Enter Location ID: ", 1, Integer.MAX_VALUE);
                Location loc = monitoringService.getLocationById(id);
                results = loc != null ? List.of(loc) : List.of();
            }
            case 2 -> {
                String kw = InputValidator.readString(scanner, "Enter Name Keyword: ", false);
                results = monitoringService.searchLocations(kw, null, null);
            }
            case 3 -> {
                LocationType type = InputValidator.readEnum(scanner, "Select Location Type", LocationType.class);
                results = monitoringService.searchLocations(null, type, null);
            }
            case 4 -> {
                CrowdLevel level = InputValidator.readEnum(scanner, "Select Crowd Level", CrowdLevel.class);
                results = monitoringService.searchLocations(null, null, level);
            }
            default -> results = List.of();
        }

        System.out.println("\nSearch Results (" + results.size() + " found):");
        if (results.isEmpty()) {
            System.out.println("  No matching locations found.");
        } else {
            printLocationTableHeader();
            for (Location loc : results) {
                printLocationTableRow(loc);
            }
            System.out.println("-------------------------------------------------------------------------------------------------------");
        }
    }

    // =========================================================================
    // 4. UPDATE CROWD COUNT
    // =========================================================================

    private void handleUpdateCrowdCount() throws SQLException {
        User user = authService.getCurrentUser();
        if (user == null || !user.getUserRole().canUpdateCrowd()) {
            System.out.println("  [Access Denied] Crowd count updates require STAFF, ORGANIZER, or ADMIN role.");
            System.out.println("  Please login first via option 1.");
            return;
        }

        System.out.println("=== UPDATE CROWD COUNT ===");
        int locationId = InputValidator.readInt(scanner, "Enter Location ID to update: ", 1, Integer.MAX_VALUE);
        Location loc = monitoringService.getLocationById(locationId);
        if (loc == null) {
            System.out.println("  [-] Location ID " + locationId + " does not exist.");
            return;
        }

        System.out.printf("  Selected: %s (Capacity: %d | Current: %d | Level: %s)%n",
                loc.getLocationName(), loc.getCapacity(), loc.getCurrentCrowdCount(), loc.getCrowdLevel());

        int newCount = InputValidator.readInt(scanner, "Enter new crowd count (>= 0): ", 0, 1000000);
        if (newCount > loc.getCapacity()) {
            System.out.printf("  [!] Notice: Count (%d) exceeds designated capacity (%d). This will trigger CRITICAL status.%n",
                    newCount, loc.getCapacity());
            if (!InputValidator.readBoolean(scanner, "Confirm exceeding capacity update?")) {
                System.out.println("  Update cancelled.");
                return;
            }
        }

        CrowdMonitoringService.UpdateResult result = monitoringService.updateCrowdCount(locationId, newCount, user);
        System.out.println("\n  [+] Crowd count successfully updated!");
        System.out.printf("      Previous Count : %d%n", result.getPreviousCount());
        System.out.printf("      New Count      : %d (%.1f%% occupancy)%n",
                result.getNewCount(), result.getLocation().getOccupancyPercentage());
        System.out.printf("      Crowd Level    : %s%n", result.getNewLevel());
        System.out.printf("      Occupancy Bar  : %s%n",
                CrowdLevelCalculator.getOccupancyBar(result.getLocation().getOccupancyPercentage(), 20));

        if (result.getGeneratedAlert() != null) {
            System.out.println("  [!] NEW SAFETY ALERT GENERATED:");
            System.out.println("      " + result.getGeneratedAlert().getAlertMessage());
        }
    }

    // =========================================================================
    // 5. MANAGE LOCATIONS
    // =========================================================================

    private void handleManageLocations() throws SQLException {
        User user = authService.getCurrentUser();
        if (user == null || !user.getUserRole().canManageLocations()) {
            System.out.println("  [Access Denied] Managing locations requires ORGANIZER or ADMIN role.");
            return;
        }

        System.out.println("=== MANAGE LOCATIONS ===");
        System.out.println("  1. Add New Location");
        System.out.println("  2. Edit Location Details (Name / Capacity / Type)");
        System.out.println("  3. Safely Delete Location");
        System.out.println("  4. Return to Main Menu");
        int sub = InputValidator.readInt(scanner, "Choose action [1-4]: ", 1, 4);

        switch (sub) {
            case 1 -> {
                System.out.println("\n--- Add New Location ---");
                String name = InputValidator.readString(scanner, "Location Name: ", false);
                LocationType type = InputValidator.readEnum(scanner, "Select Location Type", LocationType.class);
                int capacity = InputValidator.readInt(scanner, "Designated Capacity: ", 1, 100000);

                Location newLoc = new Location(name, type, capacity);
                int id = monitoringService.addLocation(newLoc, user);
                System.out.println("  [+] Location added successfully with ID: " + id);
            }
            case 2 -> {
                System.out.println("\n--- Edit Location ---");
                int id = InputValidator.readInt(scanner, "Enter Location ID to edit: ", 1, Integer.MAX_VALUE);
                Location loc = monitoringService.getLocationById(id);
                if (loc == null) {
                    System.out.println("  [-] Location ID " + id + " not found.");
                    return;
                }
                System.out.println("Editing: " + loc.getLocationName());
                String newName = InputValidator.readString(scanner, "New Name (leave blank to keep '" + loc.getLocationName() + "'): ", true);
                if (!newName.isBlank()) loc.setLocationName(newName);

                if (InputValidator.readBoolean(scanner, "Change Location Type (" + loc.getLocationType() + ")?")) {
                    loc.setLocationType(InputValidator.readEnum(scanner, "Select New Type", LocationType.class));
                }

                int newCap = InputValidator.readInt(scanner, "New Capacity (Current: " + loc.getCapacity() + "): ", 1, 100000);
                loc.setCapacity(newCap);

                monitoringService.updateLocationDetails(loc, user);
                System.out.println("  [+] Location updated successfully.");
            }
            case 3 -> {
                System.out.println("\n--- Delete Location ---");
                int id = InputValidator.readInt(scanner, "Enter Location ID to delete: ", 1, Integer.MAX_VALUE);
                Location loc = monitoringService.getLocationById(id);
                if (loc == null) {
                    System.out.println("  [-] Location ID " + id + " not found.");
                    return;
                }
                System.out.printf("  Are you sure you want to delete '%s' (ID: %d)?%n", loc.getLocationName(), id);
                if (!InputValidator.readBoolean(scanner, "Confirm deletion?")) {
                    System.out.println("  Deletion cancelled.");
                    return;
                }
                boolean force = InputValidator.readBoolean(scanner, "Force cascade deletion if historical updates exist?");
                boolean deleted = monitoringService.deleteLocation(id, user, force);
                if (deleted) {
                    System.out.println("  [+] Location deleted successfully.");
                } else {
                    System.out.println("  [-] Deletion could not be completed.");
                }
            }
        }
    }

    // =========================================================================
    // 6. VIEW ALERTS
    // =========================================================================

    private void handleViewAlerts() throws SQLException {
        System.out.println("=== VIEW ALERTS ===");
        System.out.println("  1. View Active Alerts Only");
        System.out.println("  2. View All Alerts (Active, Acknowledged, Resolved)");
        int sub = InputValidator.readInt(scanner, "Choose option [1-2]: ", 1, 2);

        List<CrowdAlert> alerts = (sub == 1) ? alertService.getActiveAlerts() : alertService.getAllAlerts();
        if (alerts.isEmpty()) {
            System.out.println("  No alerts found for the selected filter.");
            return;
        }

        System.out.println("================================================================================================================");
        System.out.printf("| %-4s | %-20s | %-8s | %-12s | %-19s | %-38s |%n",
                "ID", "Location", "Level", "Status", "Triggered Time", "Alert Message");
        System.out.println("|------|----------------------|----------|--------------|---------------------|----------------------------------------|");
        for (CrowdAlert a : alerts) {
            String timeStr = a.getAlertTime() != null ? dateFormat.format(a.getAlertTime()) : "N/A";
            System.out.printf("| %-4d | %-20s | %-8s | %-12s | %-19s | %-38s |%n",
                    a.getAlertId(),
                    truncate(a.getLocationName() != null ? a.getLocationName() : "ID:" + a.getLocationId(), 20),
                    a.getCrowdLevel(),
                    a.getStatus(),
                    timeStr,
                    truncate(a.getAlertMessage(), 38));
        }
        System.out.println("================================================================================================================");
    }

    // =========================================================================
    // 7. MANAGE ALERTS
    // =========================================================================

    private void handleManageAlerts() throws SQLException {
        User user = authService.getCurrentUser();
        if (user == null || !user.getUserRole().canManageAlerts()) {
            System.out.println("  [Access Denied] Managing alerts requires STAFF, ORGANIZER, or ADMIN role.");
            return;
        }

        System.out.println("=== MANAGE ALERTS ===");
        System.out.println("  1. Acknowledge Alert (ACTIVE -> ACKNOWLEDGED)");
        System.out.println("  2. Resolve Alert (-> RESOLVED)");
        int choice = InputValidator.readInt(scanner, "Choose action [1-2]: ", 1, 2);

        int alertId = InputValidator.readInt(scanner, "Enter Alert ID: ", 1, Integer.MAX_VALUE);
        CrowdAlert alert = alertService.getAlertById(alertId);
        if (alert == null) {
            System.out.println("  [-] Alert ID " + alertId + " does not exist.");
            return;
        }

        System.out.printf("  Alert for '%s' is currently [%s].%n", alert.getLocationName(), alert.getStatus());
        if (choice == 1) {
            boolean ack = alertService.acknowledgeAlert(alertId);
            if (ack) System.out.println("  [+] Alert " + alertId + " marked as ACKNOWLEDGED.");
        } else {
            boolean res = alertService.resolveAlert(alertId);
            if (res) System.out.println("  [+] Alert " + alertId + " marked as RESOLVED.");
        }
    }

    // =========================================================================
    // 8. VIEW CROWD HISTORY
    // =========================================================================

    private void handleViewCrowdHistory() throws SQLException {
        System.out.println("=== VIEW CROWD HISTORY ===");
        System.out.println("  1. View History for a Specific Location");
        System.out.println("  2. View Recent Campus-Wide Update Audit Log");
        int sub = InputValidator.readInt(scanner, "Choose option [1-2]: ", 1, 2);

        List<CrowdUpdate> history;
        if (sub == 1) {
            int locId = InputValidator.readInt(scanner, "Enter Location ID: ", 1, Integer.MAX_VALUE);
            history = monitoringService.getLocationHistory(locId, 30);
        } else {
            history = monitoringService.getRecentUpdates(30);
        }

        if (history.isEmpty()) {
            System.out.println("  No update records found.");
            return;
        }

        System.out.println("========================================================================================================");
        System.out.printf("| %-4s | %-22s | %-16s | %-8s | %-8s | %-9s | %-19s |%n",
                "ID", "Location", "Updated By", "Prev", "New", "Level", "Timestamp");
        System.out.println("|------|------------------------|------------------|----------|----------|-----------|---------------------|");
        for (CrowdUpdate u : history) {
            String timeStr = u.getUpdateTime() != null ? dateFormat.format(u.getUpdateTime()) : "N/A";
            String by = u.getUserName() != null ? u.getUserName() : (u.getUserId() != null ? "UID:" + u.getUserId() : "System");
            System.out.printf("| %-4d | %-22s | %-16s | %-8d | %-8d | %-9s | %-19s |%n",
                    u.getUpdateId(),
                    truncate(u.getLocationName() != null ? u.getLocationName() : "ID:" + u.getLocationId(), 22),
                    truncate(by, 16),
                    u.getPreviousCount(),
                    u.getNewCount(),
                    u.getCrowdLevel(),
                    timeStr);
        }
        System.out.println("========================================================================================================");
    }

    // =========================================================================
    // 9. CROWD ANALYSIS AND REPORTS
    // =========================================================================

    private void handleCrowdAnalysisAndReports() throws SQLException {
        System.out.println("=== CROWD ANALYSIS AND REPORTS ===");
        System.out.println("  1. Campus-wide Overcrowding Hotspots (High/Critical Frequency)");
        System.out.println("  2. Safety Alert Distribution Summary");
        System.out.println("  3. Detailed Statistical Profile for a Specific Location");
        int sub = InputValidator.readInt(scanner, "Choose report [1-3]: ", 1, 3);

        switch (sub) {
            case 1 -> {
                System.out.println("\n--- Top Overcrowded Campus Hotspots ---");
                List<Map<String, Object>> hotspots = alertService.getFrequentOvercrowdedLocations(10);
                if (hotspots.isEmpty()) {
                    System.out.println("  No overcrowding alert events recorded yet.");
                } else {
                    System.out.printf("| %-4s | %-24s | %-8s | %-12s | %-15s | %-12s |%n",
                            "ID", "Location Name", "Capacity", "Total Alerts", "Critical Events", "High Events");
                    System.out.println("|------|--------------------------|----------|--------------|-----------------|--------------|");
                    for (Map<String, Object> row : hotspots) {
                        System.out.printf("| %-4s | %-24s | %-8s | %-12s | %-15s | %-12s |%n",
                                row.get("locationId"),
                                truncate(row.get("locationName").toString(), 24),
                                row.get("capacity"),
                                row.get("totalAlerts"),
                                row.get("criticalAlerts"),
                                row.get("highAlerts"));
                    }
                }
            }
            case 2 -> {
                System.out.println("\n--- Safety Alert Lifecycle Status ---");
                Map<AlertStatus, Integer> counts = alertService.getAlertStatistics();
                int total = counts.values().stream().mapToInt(Integer::intValue).sum();
                System.out.printf("  Total Generated Alerts: %d%n", total);
                for (Map.Entry<AlertStatus, Integer> entry : counts.entrySet()) {
                    double pct = total > 0 ? (entry.getValue() * 100.0 / total) : 0.0;
                    System.out.printf("  - %-14s : %-4d (%5.1f%%)%n", entry.getKey(), entry.getValue(), pct);
                }
            }
            case 3 -> {
                int locId = InputValidator.readInt(scanner, "Enter Location ID: ", 1, Integer.MAX_VALUE);
                Map<String, Object> stats = monitoringService.getLocationAnalytics(locId);
                if (stats == null) {
                    System.out.println("  [-] Location ID " + locId + " not found.");
                    return;
                }
                Location loc = (Location) stats.get("location");
                System.out.println("\n--- Statistical Profile: " + loc.getLocationName() + " ---");
                System.out.printf("  Location Type              : %s%n", loc.getLocationType());
                System.out.printf("  Designated Capacity        : %d%n", loc.getCapacity());
                System.out.printf("  Current Crowd Count        : %d (%.1f%%, %s)%n",
                        loc.getCurrentCrowdCount(), loc.getOccupancyPercentage(), loc.getCrowdLevel());
                System.out.printf("  Total Audit Updates Stored : %s%n", stats.get("totalUpdatesRecorded"));
                System.out.printf("  Average Historical Count   : %.1f%n", (Double) stats.get("averageCount"));
                System.out.printf("  Peak Historical Count      : %d (%.1f%% of capacity)%n",
                        (Integer) stats.get("maxCount"), (Double) stats.get("peakOccupancyPercentage"));
            }
        }
    }

    // =========================================================================
    // 10. MANAGE USERS (ADMIN ONLY)
    // =========================================================================

    private void handleManageUsers() throws SQLException {
        User current = authService.getCurrentUser();
        if (current == null || !current.getUserRole().canManageUsers()) {
            System.out.println("  [Access Denied] User management requires ADMIN role.");
            return;
        }

        System.out.println("=== MANAGE USERS (ADMIN) ===");
        System.out.println("  1. List All Users");
        System.out.println("  2. Register New User");
        System.out.println("  3. Change User Role");
        System.out.println("  4. Delete User");
        int sub = InputValidator.readInt(scanner, "Choose action [1-4]: ", 1, 4);

        switch (sub) {
            case 1 -> {
                List<User> users = userDAO.findAll();
                System.out.println("=========================================================================================");
                System.out.printf("| %-4s | %-18s | %-26s | %-12s | %-14s |%n",
                        "ID", "Username", "Email", "Role", "Contact");
                System.out.println("|------|--------------------|----------------------------|--------------|----------------|");
                for (User u : users) {
                    System.out.printf("| %-4d | %-18s | %-26s | %-12s | %-14s |%n",
                            u.getUserId(),
                            truncate(u.getUserName(), 18),
                            truncate(u.getEmail(), 26),
                            u.getUserRole(),
                            truncate(u.getContactInfo() != null ? u.getContactInfo() : "-", 14));
                }
                System.out.println("=========================================================================================");
            }
            case 2 -> {
                System.out.println("\n--- Register User ---");
                String uname = InputValidator.readString(scanner, "Username: ", false);
                String email = InputValidator.readEmail(scanner, "Email: ");
                String contact = InputValidator.readPhone(scanner, "Contact Info: ");
                UserRole role = InputValidator.readEnum(scanner, "Select Role", UserRole.class);
                String rawPassword = InputValidator.readString(scanner, "Password: ", false);

                try {
                    User newUser = authService.registerUser(uname, email, contact, role, rawPassword);
                    System.out.println("  [+] User registered successfully with ID: " + newUser.getUserId());
                } catch (IllegalArgumentException e) {
                    System.out.println("  [-] Error: " + e.getMessage());
                }
            }
            case 3 -> {
                System.out.println("\n--- Change User Role ---");
                int uid = InputValidator.readInt(scanner, "Enter User ID: ", 1, Integer.MAX_VALUE);
                User u = userDAO.findById(uid);
                if (u == null) {
                    System.out.println("  [-] User ID " + uid + " not found.");
                    return;
                }
                System.out.printf("  Current Role for '%s': %s%n", u.getUserName(), u.getUserRole());
                UserRole newRole = InputValidator.readEnum(scanner, "Select New Role", UserRole.class);
                u.setUserRole(newRole);
                userDAO.update(u);
                System.out.println("  [+] Role updated successfully.");
            }
            case 4 -> {
                System.out.println("\n--- Delete User ---");
                int uid = InputValidator.readInt(scanner, "Enter User ID to delete: ", 1, Integer.MAX_VALUE);
                if (uid == current.getUserId()) {
                    System.out.println("  [-] You cannot delete your own currently active admin account.");
                    return;
                }
                User u = userDAO.findById(uid);
                if (u == null) {
                    System.out.println("  [-] User ID " + uid + " not found.");
                    return;
                }
                if (InputValidator.readBoolean(scanner, "Confirm deletion of user '" + u.getUserName() + "'?")) {
                    userDAO.delete(uid);
                    System.out.println("  [+] User deleted.");
                }
            }
        }
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 2) + "..";
    }
}
