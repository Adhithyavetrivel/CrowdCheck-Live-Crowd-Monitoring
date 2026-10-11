-- =============================================================================
-- CrowdCheck – Database Reset Script
-- Drops and recreates the database with all tables, constraints, and sample data
-- =============================================================================

DROP DATABASE IF EXISTS crowdcheck;
CREATE DATABASE crowdcheck CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE crowdcheck;

-- Source the schema
-- Table: USERS
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

-- Table: LOCATIONS
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

-- Table: CROWD_UPDATES
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

-- Table: CROWD_ALERTS
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

-- Indexes
CREATE INDEX idx_locations_type ON LOCATIONS(location_type);
CREATE INDEX idx_locations_crowd_level ON LOCATIONS(crowd_level);
CREATE INDEX idx_updates_location_time ON CROWD_UPDATES(location_id, update_time DESC);
CREATE INDEX idx_alerts_location_status ON CROWD_ALERTS(location_id, status);
CREATE INDEX idx_alerts_status_time ON CROWD_ALERTS(status, alert_time DESC);

-- Views
CREATE OR REPLACE VIEW vw_live_crowd_status AS
SELECT 
    l.location_id,
    l.location_name,
    l.location_type,
    l.capacity,
    l.current_crowd_count,
    ROUND((l.current_crowd_count / l.capacity) * 100, 2) AS occupancy_percentage,
    l.crowd_level,
    l.last_updated
FROM LOCATIONS l;

-- Sample Seed Data
INSERT INTO USERS (user_id, user_name, email, contact_info, user_role, password_hash, created_at) VALUES
(1, 'admin', 'admin@crowdcheck.edu', '+1-555-0101', 'ADMIN', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', NOW()),
(2, 'organizer_rachel', 'rachel.organizer@crowdcheck.edu', '+1-555-0102', 'ORGANIZER', '2fa08015c1e37237388554c96fef4855b888ccae0df846f533f4c75c9d97b9d2', NOW()),
(3, 'staff_john', 'john.staff@crowdcheck.edu', '+1-555-0103', 'STAFF', 'dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9', NOW()),
(4, 'staff_priya', 'priya.staff@crowdcheck.edu', '+1-555-0104', 'STAFF', 'dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9', NOW()),
(5, 'visitor_alex', 'alex.visitor@gmail.com', '+1-555-0105', 'VISITOR', '496de6f21a6ba295004fb2ad6696a00463b33d36b21543bb59d9fc9db409ef8a', NOW());

INSERT INTO LOCATIONS (location_id, location_name, location_type, capacity, current_crowd_count, crowd_level, last_updated) VALUES
(1, 'Main Auditorium', 'AUDITORIUM', 500, 465, 'CRITICAL', NOW()),
(2, 'Central Cafeteria', 'CAFETERIA', 250, 195, 'HIGH', NOW()),
(3, 'University Library', 'LIBRARY', 300, 145, 'MODERATE', NOW()),
(4, 'Indoor Sports Complex', 'SPORTS_COMPLEX', 400, 110, 'LOW', NOW()),
(5, 'Computer Science Lab 1', 'LABORATORY', 60, 46, 'HIGH', NOW()),
(6, 'Main Campus Gate', 'GATE', 150, 42, 'LOW', NOW()),
(7, 'South Campus Grounds', 'CAMPUS_GROUND', 1000, 610, 'MODERATE', NOW()),
(8, 'Student Parking Lot A', 'PARKING', 200, 192, 'CRITICAL', NOW());

INSERT INTO CROWD_UPDATES (location_id, user_id, previous_count, new_count, crowd_level, update_time) VALUES
(1, 1, 0, 150, 'LOW', NOW() - INTERVAL 3 HOUR),
(1, 3, 150, 320, 'MODERATE', NOW() - INTERVAL 2 HOUR),
(1, 3, 320, 380, 'HIGH', NOW() - INTERVAL 1 HOUR),
(1, 3, 380, 465, 'CRITICAL', NOW() - INTERVAL 15 MINUTE),
(2, 4, 50, 120, 'MODERATE', NOW() - INTERVAL 2 HOUR),
(2, 4, 120, 160, 'MODERATE', NOW() - INTERVAL 1 HOUR),
(2, 4, 160, 195, 'HIGH', NOW() - INTERVAL 25 MINUTE),
(3, 3, 80, 145, 'MODERATE', NOW() - INTERVAL 40 MINUTE),
(4, 3, 60, 110, 'LOW', NOW() - INTERVAL 1 HOUR),
(5, 4, 25, 46, 'HIGH', NOW() - INTERVAL 30 MINUTE),
(6, 3, 20, 42, 'LOW', NOW() - INTERVAL 10 MINUTE),
(7, 2, 300, 610, 'MODERATE', NOW() - INTERVAL 55 MINUTE),
(8, 3, 120, 175, 'HIGH', NOW() - INTERVAL 45 MINUTE),
(8, 3, 175, 192, 'CRITICAL', NOW() - INTERVAL 5 MINUTE);

INSERT INTO CROWD_ALERTS (location_id, alert_message, alert_time, crowd_level, status) VALUES
(1, 'CRITICAL CROWD LEVEL: Main Auditorium has reached 93.0% capacity (465/500). Immediate access regulation advised.', NOW() - INTERVAL 15 MINUTE, 'CRITICAL', 'ACTIVE'),
(8, 'CRITICAL CROWD LEVEL: Student Parking Lot A has reached 96.0% capacity (192/200). Directing traffic to Overflow Lot B.', NOW() - INTERVAL 5 MINUTE, 'CRITICAL', 'ACTIVE'),
(2, 'HIGH CROWD LEVEL: Central Cafeteria is at 78.0% capacity (195/250). Floor attendants notified.', NOW() - INTERVAL 25 MINUTE, 'HIGH', 'ACKNOWLEDGED'),
(5, 'HIGH CROWD LEVEL: Computer Science Lab 1 reached 76.67% capacity (46/60). Additional seating deployed.', NOW() - INTERVAL 30 MINUTE, 'HIGH', 'RESOLVED');
