-- =============================================================================
-- CrowdCheck – Sample Data Population Script
-- =============================================================================
USE crowdcheck;

-- Disable foreign key checks during batch seed
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE CROWD_ALERTS;
TRUNCATE TABLE CROWD_UPDATES;
TRUNCATE TABLE LOCATIONS;
TRUNCATE TABLE USERS;
SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. Insert Sample USERS
-- Passwords:
--   admin            : Admin@123
--   organizer_rachel : Organizer@123
--   staff_john       : Staff@123
--   staff_priya      : Staff@123
--   visitor_alex     : Visitor@123
-- -----------------------------------------------------------------------------
INSERT INTO USERS (user_id, user_name, email, contact_info, user_role, password_hash, created_at) VALUES
(1, 'admin', 'admin@crowdcheck.edu', '+1-555-0101', 'ADMIN', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', NOW() - INTERVAL 10 DAY),
(2, 'organizer_rachel', 'rachel.organizer@crowdcheck.edu', '+1-555-0102', 'ORGANIZER', '2fa08015c1e37237388554c96fef4855b888ccae0df846f533f4c75c9d97b9d2', NOW() - INTERVAL 8 DAY),
(3, 'staff_john', 'john.staff@crowdcheck.edu', '+1-555-0103', 'STAFF', 'dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9', NOW() - INTERVAL 7 DAY),
(4, 'staff_priya', 'priya.staff@crowdcheck.edu', '+1-555-0104', 'STAFF', 'dfd48f36338aa36228ebb9e204bba6b4e18db0b623e25c458901edc831fb18e9', NOW() - INTERVAL 5 DAY),
(5, 'visitor_alex', 'alex.visitor@gmail.com', '+1-555-0105', 'VISITOR', '496de6f21a6ba295004fb2ad6696a00463b33d36b21543bb59d9fc9db409ef8a', NOW() - INTERVAL 2 DAY);

-- -----------------------------------------------------------------------------
-- 2. Insert Sample LOCATIONS
-- Formula: Occupancy = (Current / Capacity) * 100
-- Low: <40% | Moderate: 40% - <70% | High: 70% - <90% | Critical: >=90%
-- -----------------------------------------------------------------------------
INSERT INTO LOCATIONS (location_id, location_name, location_type, capacity, current_crowd_count, crowd_level, last_updated) VALUES
(1, 'Main Auditorium', 'AUDITORIUM', 500, 465, 'CRITICAL', NOW() - INTERVAL 15 MINUTE),
(2, 'Central Cafeteria', 'CAFETERIA', 250, 195, 'HIGH', NOW() - INTERVAL 25 MINUTE),
(3, 'University Library', 'LIBRARY', 300, 145, 'MODERATE', NOW() - INTERVAL 40 MINUTE),
(4, 'Indoor Sports Complex', 'SPORTS_COMPLEX', 400, 110, 'LOW', NOW() - INTERVAL 1 HOUR),
(5, 'Computer Science Lab 1', 'LABORATORY', 60, 46, 'HIGH', NOW() - INTERVAL 30 MINUTE),
(6, 'Main Campus Gate', 'GATE', 150, 42, 'LOW', NOW() - INTERVAL 10 MINUTE),
(7, 'South Campus Grounds', 'CAMPUS_GROUND', 1000, 610, 'MODERATE', NOW() - INTERVAL 55 MINUTE),
(8, 'Student Parking Lot A', 'PARKING', 200, 192, 'CRITICAL', NOW() - INTERVAL 5 MINUTE);

-- -----------------------------------------------------------------------------
-- 3. Insert Sample CROWD_UPDATES (Audit trail)
-- -----------------------------------------------------------------------------
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

-- -----------------------------------------------------------------------------
-- 4. Insert Sample CROWD_ALERTS
-- High and Critical occurrences
-- -----------------------------------------------------------------------------
INSERT INTO CROWD_ALERTS (location_id, alert_message, alert_time, crowd_level, status) VALUES
(1, 'CRITICAL CROWD LEVEL: Main Auditorium has reached 93.0% capacity (465/500). Immediate access regulation advised.', NOW() - INTERVAL 15 MINUTE, 'CRITICAL', 'ACTIVE'),
(8, 'CRITICAL CROWD LEVEL: Student Parking Lot A has reached 96.0% capacity (192/200). Directing traffic to Overflow Lot B.', NOW() - INTERVAL 5 MINUTE, 'CRITICAL', 'ACTIVE'),
(2, 'HIGH CROWD LEVEL: Central Cafeteria is at 78.0% capacity (195/250). Floor attendants notified.', NOW() - INTERVAL 25 MINUTE, 'HIGH', 'ACKNOWLEDGED'),
(5, 'HIGH CROWD LEVEL: Computer Science Lab 1 reached 76.67% capacity (46/60). Additional seating deployed.', NOW() - INTERVAL 30 MINUTE, 'HIGH', 'RESOLVED');
