# CrowdCheck – Database Design & SQL Reference

This document provides the complete database design specification for **CrowdCheck: Live Crowd Monitoring System**, detailing the relational schema, entity relationships, integrity constraints, indexing strategies, and core application SQL queries.

---

## 1. Relational Schema Architecture

The database `crowdcheck` is hosted on **MySQL 8.0+** using the **InnoDB** storage engine to guarantee full ACID transactional compliance, referential integrity via foreign key cascades, and row-level locking during live headcount updates.

```
+-----------------------------------------------------------------------------------+
|                                  DATABASE SCHEMA                                  |
+-----------------------------------------------------------------------------------+

     +-----------------------+                    +------------------------+
     |        USERS          |                    |       LOCATIONS        |
     +-----------------------+                    +------------------------+
     | PK  user_id           |                    | PK  location_id        |
     |     user_name  (UQ)   |                    |     location_name (UQ) |
     |     email      (UQ)   |                    |     location_type      |
     |     contact_info      |                    |     capacity           |
     |     user_role         |                    |     current_crowd_count|
     |     password_hash     |                    |     crowd_level        |
     |     created_at        |                    |     last_updated       |
     +-----------------------+                    +------------------------+
                 | 1                                          | 1
                 |                                            |
                 | 0..*                                       | 0..*
     +-----------------------+                    +------------------------+
     |     CROWD_UPDATES     |                    |      CROWD_ALERTS      |
     +-----------------------+                    +------------------------+
     | PK  update_id         |                    | PK  alert_id           |
     | FK  location_id       |<-------------------+ FK  location_id        |
     | FK  user_id           |                    |     alert_message      |
     |     previous_count    |                    |     alert_time         |
     |     new_count         |                    |     crowd_level        |
     |     crowd_level       |                    |     status             |
     |     update_time       |                    +------------------------+
     +-----------------------+
```

---

## 2. Table Specifications & Data Dictionaries

### 2.1 USERS Table
Stores authentication credentials, user profiles, and role assignments for Role-Based Access Control (RBAC).

| Column Name | Data Type | Nullable | Constraints & Defaults | Description |
|---|---|---|---|---|
| `user_id` | `INT` | NO | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique internal surrogate identifier for user |
| `user_name` | `VARCHAR(50)` | NO | `UNIQUE` | Distinct system username used for login |
| `email` | `VARCHAR(100)` | NO | `UNIQUE`, `CHECK (email LIKE '%_@__%.__%')` | User contact email address |
| `contact_info` | `VARCHAR(50)` | YES | `NULL` | Primary phone or communication identifier |
| `user_role` | `ENUM` | NO | `'ADMIN', 'ORGANIZER', 'STAFF', 'VISITOR'`, Default: `'VISITOR'` | Access authorization tier |
| `password_hash` | `VARCHAR(255)` | NO | None | 64-character hex SHA-256 password hash |
| `created_at` | `TIMESTAMP` | NO | `DEFAULT CURRENT_TIMESTAMP` | Account registration timestamp |

---

### 2.2 LOCATIONS Table
Stores campus buildings, zones, facilities, physical capacities, and live real-time crowd status.

| Column Name | Data Type | Nullable | Constraints & Defaults | Description |
|---|---|---|---|---|
| `location_id` | `INT` | NO | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique identifier for campus location |
| `location_name` | `VARCHAR(100)` | NO | `UNIQUE` | Human-readable facility designation |
| `location_type` | `ENUM` | NO | `'AUDITORIUM', 'CAFETERIA', 'LIBRARY', 'SPORTS_COMPLEX', 'LABORATORY', 'GATE', 'CAMPUS_GROUND', 'PARKING', 'OTHER'` | Facility functional category |
| `capacity` | `INT` | NO | `CHECK (capacity > 0)` | Maximum safe occupancy limit |
| `current_crowd_count` | `INT` | NO | `DEFAULT 0`, `CHECK (current_crowd_count >= 0)` | Live recorded headcount |
| `crowd_level` | `ENUM` | NO | `'LOW', 'MODERATE', 'HIGH', 'CRITICAL'`, Default: `'LOW'` | Automated density classification |
| `last_updated` | `TIMESTAMP` | NO | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last time headcount was adjusted |

---

### 2.3 CROWD_UPDATES Table
Immutable audit trail capturing every incremental headcount adjustment, previous count, new count, and the staff member who logged it.

| Column Name | Data Type | Nullable | Constraints & Defaults | Description |
|---|---|---|---|---|
| `update_id` | `INT` | NO | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique log entry sequence number |
| `location_id` | `INT` | NO | `FK -> LOCATIONS(location_id) ON DELETE CASCADE` | Associated campus facility |
| `user_id` | `INT` | YES | `FK -> USERS(user_id) ON DELETE SET NULL` | Authorized staff member logging update |
| `previous_count` | `INT` | NO | `CHECK (previous_count >= 0)` | Headcount prior to adjustment |
| `new_count` | `INT` | NO | `CHECK (new_count >= 0)` | New reported headcount |
| `crowd_level` | `ENUM` | NO | `'LOW', 'MODERATE', 'HIGH', 'CRITICAL'` | Computed level at time of event |
| `update_time` | `TIMESTAMP` | NO | `DEFAULT CURRENT_TIMESTAMP` | Exact timestamp when update occurred |

---

### 2.4 CROWD_ALERTS Table
Stores capacity overflow and safety alerts triggered whenever facility occupancy breaches HIGH ($70\%$) or CRITICAL ($90\%$) thresholds.

| Column Name | Data Type | Nullable | Constraints & Defaults | Description |
|---|---|---|---|---|
| `alert_id` | `INT` | NO | `AUTO_INCREMENT`, `PRIMARY KEY` | Unique alert event number |
| `location_id` | `INT` | NO | `FK -> LOCATIONS(location_id) ON DELETE CASCADE` | Affected campus facility |
| `alert_message` | `VARCHAR(255)` | NO | None | Human-readable alert notification text |
| `alert_time` | `TIMESTAMP` | NO | `DEFAULT CURRENT_TIMESTAMP` | Timestamp when alert condition triggered |
| `crowd_level` | `ENUM` | NO | `'LOW', 'MODERATE', 'HIGH', 'CRITICAL'` | Severity level of triggered alert |
| `status` | `ENUM` | NO | `'ACTIVE', 'ACKNOWLEDGED', 'RESOLVED'`, Default: `'ACTIVE'` | Operational lifecycle status |

---

## 3. Database Constraints & Referential Integrity

1. **Primary Keys:** Every table features a surrogate synthetic integer primary key (`AUTO_INCREMENT`) providing optimal B-Tree index branching factor and zero foreign key update anomalies.
2. **Foreign Key: `CROWD_UPDATES.location_id -> LOCATIONS.location_id` (`ON DELETE CASCADE`):** If a facility is permanently decommissioned, its history cascades cleanly.
3. **Foreign Key: `CROWD_UPDATES.user_id -> USERS.user_id` (`ON DELETE SET NULL`):** If a staff account is deleted, past headcount audit entries are retained for analytical compliance with `user_id` set to `NULL`.
4. **Foreign Key: `CROWD_ALERTS.location_id -> LOCATIONS.location_id` (`ON DELETE CASCADE`):** Alert history drops if the parent facility is purged.
5. **Domain Integrity (Check Constraints):**
   - `chk_capacity_positive`: Prohibits zero or negative capacity limits.
   - `chk_crowd_non_negative`: Prohibits negative live headcount entries.
   - `chk_email_format`: Ensures basic email syntactic validity at database engine level.

---

## 4. Indexing Strategy & Query Optimization

| Index Name | Table | Columns | Index Type | Optimization Target |
|---|---|---|---|---|
| `PRIMARY` | `USERS` | `user_id` | Clustered B-Tree | Single-row PK lookups |
| `user_name` | `USERS` | `user_name` | Unique B-Tree | Fast credential lookup during login ($O(1)$) |
| `email` | `USERS` | `email` | Unique B-Tree | Registration deduplication lookups |
| `PRIMARY` | `LOCATIONS` | `location_id` | Clustered B-Tree | Facility lookups and updates |
| `location_name` | `LOCATIONS` | `location_name` | Unique B-Tree | Facility search and uniqueness enforcement |
| `idx_locations_type` | `LOCATIONS` | `location_type` | Secondary B-Tree | Filtering facilities by category (Cafeteria, Library) |
| `idx_locations_crowd_level` | `LOCATIONS` | `crowd_level` | Secondary B-Tree | Dashboard filtering for High/Critical facilities |
| `idx_updates_location_time`| `CROWD_UPDATES` | `(location_id, update_time DESC)` | Composite B-Tree | Fast retrieval of recent history for a given location |
| `idx_alerts_location_status`| `CROWD_ALERTS` | `(location_id, status)` | Composite B-Tree | Rapid active alert deduplication checks ($O(\log N)$) |
| `idx_alerts_status_time` | `CROWD_ALERTS` | `(status, alert_time DESC)` | Composite B-Tree | Dashboard sorting of currently active alarms |

---

## 5. Core SQL Queries Implemented in Application

### 5.1 Transactional Crowd Update & Alert Creation
Executed atomically inside a single JDBC transaction (`conn.setAutoCommit(false)`):

```sql
-- Step 1: Update current headcount in LOCATIONS
UPDATE LOCATIONS 
SET current_crowd_count = ?, crowd_level = ?, last_updated = CURRENT_TIMESTAMP 
WHERE location_id = ?;

-- Step 2: Append audit record into CROWD_UPDATES
INSERT INTO CROWD_UPDATES (location_id, user_id, previous_count, new_count, crowd_level) 
VALUES (?, ?, ?, ?, ?);

-- Step 3: Check for existing ACTIVE alert (deduplication)
SELECT alert_id, location_id, alert_message, alert_time, crowd_level, status 
FROM CROWD_ALERTS 
WHERE location_id = ? AND status = 'ACTIVE' 
LIMIT 1;

-- Step 4: Insert new alert if no active alert exists
INSERT INTO CROWD_ALERTS (location_id, alert_message, crowd_level, status) 
VALUES (?, ?, ?, 'ACTIVE');
```

### 5.2 Facility Search with Dynamic Filtering
```sql
SELECT location_id, location_name, location_type, capacity, current_crowd_count, crowd_level, last_updated 
FROM LOCATIONS 
WHERE 1=1 
  AND LOWER(location_name) LIKE ? 
  AND location_type = ? 
  AND crowd_level = ? 
ORDER BY location_name ASC;
```

### 5.3 Overcrowded Campus Hotspots Analytics
```sql
SELECT 
    l.location_id, 
    l.location_name, 
    l.capacity, 
    COUNT(a.alert_id) AS total_alerts,
    SUM(CASE WHEN a.crowd_level = 'CRITICAL' THEN 1 ELSE 0 END) AS critical_alerts,
    SUM(CASE WHEN a.crowd_level = 'HIGH' THEN 1 ELSE 0 END) AS high_alerts
FROM LOCATIONS l
JOIN CROWD_ALERTS a ON l.location_id = a.location_id
GROUP BY l.location_id, l.location_name, l.capacity
ORDER BY total_alerts DESC, critical_alerts DESC 
LIMIT 10;
```

### 5.4 Aggregated Facility Statistical Profile
```sql
SELECT 
    AVG(new_count) AS avg_count,
    MAX(new_count) AS max_count,
    COUNT(update_id) AS total_events
FROM CROWD_UPDATES 
WHERE location_id = ?;
```
