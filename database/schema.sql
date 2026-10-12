-- =============================================================================
-- CrowdCheck – Live Crowd Monitoring System
-- Database Schema for MySQL 8.x
-- =============================================================================

CREATE DATABASE IF NOT EXISTS crowdcheck
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crowdcheck;

-- Drop child tables first
DROP TABLE IF EXISTS CROWD_ALERTS;
DROP TABLE IF EXISTS CROWD_UPDATES;
DROP TABLE IF EXISTS LOCATIONS;
DROP TABLE IF EXISTS USERS;

-- 1. Table: USERS
CREATE TABLE USERS (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_info VARCHAR(50),
    user_role ENUM('ADMIN', 'ORGANIZER', 'STAFF', 'VISITOR') NOT NULL
) ENGINE=InnoDB;

-- 2. Table: LOCATIONS
CREATE TABLE LOCATIONS (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL UNIQUE,
    location_type ENUM('ENTRANCE', 'FOOD_COURT', 'AUDITORIUM', 'PARKING') NOT NULL,
    capacity INT NOT NULL,
    current_crowd_count INT NOT NULL DEFAULT 0,
    crowd_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'LOW',
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity_positive CHECK (capacity > 0),
    CONSTRAINT chk_crowd_non_negative CHECK (current_crowd_count >= 0)
) ENGINE=InnoDB;

-- 3. Table: CROWD_ALERTS
CREATE TABLE CROWD_ALERTS (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    location_id INT NOT NULL,
    alert_message VARCHAR(255) NOT NULL,
    alert_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    crowd_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL,
    status ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED') NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_alerts_location FOREIGN KEY (location_id)
        REFERENCES LOCATIONS (location_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB;

-- 4. Table: CROWD_UPDATES
CREATE TABLE CROWD_UPDATES (
    update_id INT AUTO_INCREMENT PRIMARY KEY,
    location_id INT NOT NULL,
    previous_count INT NOT NULL,
    new_count INT NOT NULL,
    crowd_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_updates_location FOREIGN KEY (location_id)
        REFERENCES LOCATIONS (location_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT chk_update_prev_non_neg CHECK (previous_count >= 0),
    CONSTRAINT chk_update_new_non_neg CHECK (new_count >= 0)
) ENGINE=InnoDB;

-- Indexes for search & analytics performance
CREATE INDEX idx_locations_type ON LOCATIONS(location_type);
CREATE INDEX idx_locations_crowd_level ON LOCATIONS(crowd_level);
CREATE INDEX idx_alerts_location_status ON CROWD_ALERTS(location_id, status);
CREATE INDEX idx_updates_location_time ON CROWD_UPDATES(location_id, update_time DESC);
