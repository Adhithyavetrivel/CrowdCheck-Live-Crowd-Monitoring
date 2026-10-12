-- =============================================================================
-- CrowdCheck – Sample Data Population Script
-- =============================================================================

USE crowdcheck;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE CROWD_ALERTS;
TRUNCATE TABLE CROWD_UPDATES;
TRUNCATE TABLE LOCATIONS;
TRUNCATE TABLE USERS;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Sample USERS
INSERT INTO USERS (user_id, user_name, email, contact_info, user_role) VALUES
(1, 'Admin User', 'admin@crowdcheck.com', '+1-555-0101', 'ADMIN'),
(2, 'Event Organizer', 'organizer@crowdcheck.com', '+1-555-0102', 'ORGANIZER'),
(3, 'Field Staff', 'staff@crowdcheck.com', '+1-555-0103', 'STAFF'),
(4, 'Campus Visitor', 'visitor@crowdcheck.com', '+1-555-0104', 'VISITOR');

-- 2. Insert Sample LOCATIONS
-- Types: ENTRANCE, FOOD_COURT, AUDITORIUM, PARKING
INSERT INTO LOCATIONS (location_id, location_name, location_type, capacity, current_crowd_count, crowd_level, last_updated) VALUES
(1, 'Main Campus Entrance', 'ENTRANCE', 200, 50, 'LOW', NOW() - INTERVAL 1 HOUR),
(2, 'Central Food Court', 'FOOD_COURT', 300, 230, 'HIGH', NOW() - INTERVAL 20 MINUTE),
(3, 'Grand Auditorium', 'AUDITORIUM', 500, 460, 'CRITICAL', NOW() - INTERVAL 10 MINUTE),
(4, 'North Parking Plaza', 'PARKING', 150, 75, 'MODERATE', NOW() - INTERVAL 35 MINUTE),
(5, 'South Gate Entrance', 'ENTRANCE', 100, 30, 'LOW', NOW() - INTERVAL 45 MINUTE),
(6, 'Student Center Food Court', 'FOOD_COURT', 250, 185, 'HIGH', NOW() - INTERVAL 15 MINUTE);

-- 3. Insert Historical CROWD_UPDATES
INSERT INTO CROWD_UPDATES (location_id, previous_count, new_count, crowd_level, update_time) VALUES
(1, 0, 30, 'LOW', NOW() - INTERVAL 3 HOUR),
(1, 30, 50, 'LOW', NOW() - INTERVAL 1 HOUR),
(2, 50, 150, 'MODERATE', NOW() - INTERVAL 2 HOUR),
(2, 150, 230, 'HIGH', NOW() - INTERVAL 20 MINUTE),
(3, 100, 300, 'MODERATE', NOW() - INTERVAL 3 HOUR),
(3, 300, 400, 'HIGH', NOW() - INTERVAL 1 HOUR),
(3, 400, 460, 'CRITICAL', NOW() - INTERVAL 10 MINUTE),
(4, 30, 75, 'MODERATE', NOW() - INTERVAL 35 MINUTE),
(6, 80, 185, 'HIGH', NOW() - INTERVAL 15 MINUTE);

-- 4. Insert CROWD_ALERTS
INSERT INTO CROWD_ALERTS (location_id, alert_message, alert_time, crowd_level, status) VALUES
(3, 'CRITICAL alert: Grand Auditorium has reached 92.0% capacity (460/500). Immediate crowd control advised.', NOW() - INTERVAL 10 MINUTE, 'CRITICAL', 'ACTIVE'),
(2, 'HIGH alert: Central Food Court is at 76.7% capacity (230/300). Monitor entry queues.', NOW() - INTERVAL 20 MINUTE, 'HIGH', 'ACTIVE'),
(6, 'HIGH alert: Student Center Food Court reached 74.0% capacity (185/250).', NOW() - INTERVAL 15 MINUTE, 'HIGH', 'ACKNOWLEDGED');
