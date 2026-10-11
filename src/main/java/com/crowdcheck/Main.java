package com.crowdcheck;

import com.crowdcheck.config.DatabaseConnection;
import com.crowdcheck.ui.ConsoleMenu;

/**
 * Entry point for the CrowdCheck Live Crowd Monitoring System.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("             CrowdCheck - Live Crowd Monitoring System v1.0.0                  ");
        System.out.println("===============================================================================");
        System.out.println("[*] Initializing system and verifying database connectivity...");

        boolean dbReady = DatabaseConnection.testConnection();
        if (!dbReady) {
            System.err.println("-------------------------------------------------------------------------------");
            System.err.println("[!] WARNING: Unable to establish connection to MySQL database!");
            System.err.println("    Target URL : " + DatabaseConnection.getUrl());
            System.err.println("    User       : " + DatabaseConnection.getUser());
            System.err.println();
            System.err.println("    To resolve this:");
            System.err.println("    1. Verify MySQL server 8.0+ is running on your system.");
            System.err.println("    2. Execute 'database/schema.sql' and 'database/sample_data.sql' in MySQL.");
            System.err.println("    3. Configure your credentials in 'db.properties' or via environment variables:");
            System.err.println("       DB_URL, DB_USER, DB_PASSWORD");
            System.err.println("-------------------------------------------------------------------------------");
            System.out.println("[*] Launching Console Menu interface in offline/diagnostic mode...\n");
        } else {
            System.out.println("[+] Database connected successfully.\n");
        }

        ConsoleMenu menu = new ConsoleMenu();
        menu.start();
    }
}
