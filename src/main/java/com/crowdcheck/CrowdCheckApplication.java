package com.crowdcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application bootstrap for CrowdCheck Live Crowd Monitoring System.
 */
@SpringBootApplication
public class CrowdCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrowdCheckApplication.class, args);
        System.out.println("===============================================================================");
        System.out.println("  CrowdCheck Live Crowd Monitoring System (Spring Boot) is running!            ");
        System.out.println("  Access Web Dashboard at: http://localhost:8080                               ");
        System.out.println("===============================================================================");
    }
}
