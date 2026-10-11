-- =============================================================================
-- CrowdCheck – Live Crowd Monitoring System
-- Database Schema Definition (MySQL 8.0+)
-- =============================================================================

-- 1. Create and select the database
CREATE DATABASE IF NOT EXISTS crowdcheck
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE crowdcheck;

-- 2. Drop existing tables if re-running (respecting foreign key hierarchy)
DROP TABLE IF EXISTS CROWD_ALERTS;
DROP TABLE IF EXISTS CROWD_UPDATES;
DROP TABLE IF EXISTS LOCATIONS;
DROP TABLE IF EXISTS USERS;

-- =============================================================================
-- Table: USERS
-- Stores system users and their role-based access levels
-- =============================================================================
CREATE TABLE USERS (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_info VARCHAR(50),
    user_role ENUM('ADMIN', 'ORGANIZER', 'STAFF', 'VISITOR') NOT NULL DEFAULT 'VISITOR',
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_email_format CHECK (email LIKE '%_@__%.__%')
) ENGINE=InnoDB;

-- =============================================================================
-- Table: LOCATIONS
-- Stores campus locations, their capacity limits, and current live crowd count
-- =============================================================================
CREATE TABLE LOCATIONS (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    location_name VARCHAR(100) NOT NULL UNIQUE,
    location_type ENUM(
        'AUDITORIUM',
        'CAFETERIA',
        'LIBRARY',
        'SPORTS_COMPLEX',
        'LABORATORY',
        'GATE',
        'CAMPUS_GROUND',
        'PARKING',
        'OTHER'
    ) NOT NULL,
    capacity INT NOT NULL,
    current_crowd_count INT NOT NULL DEFAULT 0,
    crowd_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'LOW',
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity_positive CHECK (capacity > 0),
    CONSTRAINT chk_crowd_non_negative CHECK (current_crowd_count >= 0)
) ENGINE=InnoDB;

-- =============================================================================
-- Table: CROWD_UPDATES
-- Historical audit trail of all crowd count updates made by authorized personnel
-- =============================================================================
CREATE TABLE CROWD_UPDATES (
    update_id INT AUTO_INCREMENT PRIMARY KEY,
    location_id INT NOT NULL,
    user_id INT NULL,
    previous_count INT NOT NULL,
    new_count INT NOT NULL,
    crowd_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_updates_location FOREIGN KEY (location_id)
        REFERENCES LOCATIONS (location_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_updates_user FOREIGN KEY (user_id)
        REFERENCES USERS (user_id)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT chk_update_prev_non_negative CHECK (previous_count >= 0),
    CONSTRAINT chk_update_new_non_negative CHECK (new_count >= 0)
) ENGINE=InnoDB;

-- =============================================================================
-- Table: CROWD_ALERTS
-- Stores safety and capacity alerts triggered when crowds reach HIGH or CRITICAL
-- =============================================================================
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

-- =============================================================================
-- Indexes for performance optimization
-- =============================================================================
CREATE INDEX idx_locations_type ON LOCATIONS(location_type);
CREATE INDEX idx_locations_crowd_level ON LOCATIONS(crowd_level);
CREATE INDEX idx_updates_location_time ON CROWD_UPDATES(location_id, update_time DESC);
CREATE INDEX idx_alerts_location_status ON CROWD_ALERTS(location_id, status);
CREATE INDEX idx_alerts_status_time ON CROWD_ALERTS(status, alert_time DESC);

-- =============================================================================
-- Views for Reporting and Dashboard Summaries
-- =============================================================================
CREATE OR REPLACE VIEW vw_live_crowd_status AS
SELECT 
    l.location_id,
    l.location_name,
    l.location_type,
    l.capacity,
    l.current_crowd_count,
    ROUND((l.current_crowd_count / l.capacity) * 100, 2) AS occupancy_percentage,
    l.crowd_level,
    l.last_updated,
    CASE 
        WHEN l.crowd_level = 'CRITICAL' THEN 'Immediate crowd regulation required'
        WHEN l.crowd_level = 'HIGH' THEN 'Approaching capacity limit'
        WHEN l.crowd_level = 'MODERATE' THEN 'Normal operating traffic'
        ELSE 'Ample capacity available'
    END AS status_summary
FROM LOCATIONS l;

CREATE OR REPLACE VIEW vw_overcrowding_summary AS
SELECT 
    l.location_id,
    l.location_name,
    l.capacity,
    COUNT(cu.update_id) AS total_updates,
    SUM(CASE WHEN cu.crowd_level IN ('HIGH', 'CRITICAL') THEN 1 ELSE 0 END) AS high_critical_events,
    MAX(cu.new_count) AS peak_crowd_recorded,
    ROUND(AVG(cu.new_count), 1) AS average_crowd_count
FROM LOCATIONS l
LEFT JOIN CROWD_UPDATES cu ON l.location_id = cu.location_id
GROUP BY l.location_id, l.location_name, l.capacity;
