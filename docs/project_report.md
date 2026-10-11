# CrowdCheck – Live Crowd Monitoring System
## College DBMS Project Report

---

### Academic Project Details
- **Project Title:** CrowdCheck – Real-Time Campus Crowd Monitoring System
- **Technology Stack:** Java 17, JDBC (PreparedStatement & Transactions), MySQL 8.0, Maven, JUnit 5
- **Architecture:** Layered Architecture (Model – DAO – Service – UI)
- **Domain:** Database Management Systems (DBMS) / Campus Safety & Automation

---

## 1. Abstract

Large academic campuses, university event centers, cafeterias, and recreational sports complexes frequently experience sudden influxes of students and visitors, leading to severe congestion, safety hazards, long queues, and reduced operational efficiency. **CrowdCheck** is a database-backed, real-time crowd management software system designed to monitor, track, and regulate occupancy levels across multiple campus facilities. 

Developed strictly using standard Java 17, native JDBC, and MySQL 8.0 without heavy web or ORM frameworks, CrowdCheck ensures absolute control over relational transaction lifecycles (`ACID`), query efficiency, and connection handling. The system computes crowd density automatically against designated facility thresholds (`LOW`, `MODERATE`, `HIGH`, `CRITICAL`), prevents race conditions via atomic transactions, generates proactive safety alerts, and retains a permanent audit log for administrative decision-making.

---

## 2. Problem Statement

Modern university campuses house diverse facilities with varying capacity limitations. Current challenges include:
1. **Lack of Real-Time Visibility:** Students travel to cafeterias or libraries only to find them completely packed, wasting time and causing bottlenecks.
2. **Safety & Stampede Hazards:** High-density venues such as auditoriums during symposiums or sports complexes during matches can rapidly reach dangerous levels without automated alert notification.
3. **Absence of Audit Accountability:** Changes in occupancy are rarely logged, leaving administrators without data regarding peak usage times, congestion trends, or security logs.
4. **Data Inconsistency & Race Conditions:** When multiple staff members update crowd counts simultaneously, unmanaged connections or non-transactional database updates cause dirty reads, lost updates, or duplicate notifications.

---

## 3. Project Objectives

- **Automated Density Calculation:** Dynamically compute facility occupancy percentages from live counts and designated capacities.
- **Transactional Consistency:** Ensure that updating a facility count, appending an audit record, and generating safety alerts occur atomically within a single JDBC transaction.
- **Proactive Alert Automation:** Automatically trigger safety alerts when facilities reach `HIGH` ($\ge 70\%$) or `CRITICAL` ($\ge 90\%$) density, preventing redundant active alarms.
- **Role-Based Access Control (RBAC):** Protect system integrity by distinguishing between `ADMIN`, `ORGANIZER`, `STAFF`, and `VISITOR` privileges.
- **Analytical Intelligence:** Provide historical reports showing frequent congestion hotspots, maximum historical headcounts, and alert resolution stats.

---

## 4. System Architecture

CrowdCheck follows a **Layered Architecture** adhering to Separation of Concerns (SoC):

```
+--------------------------------------------------------------------------+
|                        CONSOLE PRESENTATION LAYER                        |
|               com.crowdcheck.ui.ConsoleMenu (10 Menu Options)            |
|               com.crowdcheck.ui.InputValidator (Regex & Type Checks)     |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                           SERVICE / BUSINESS LAYER                       |
|   com.crowdcheck.service.CrowdMonitoringService (Atomic Tx Coordinator)  |
|   com.crowdcheck.service.CrowdLevelCalculator (Threshold Algorithm)       |
|   com.crowdcheck.service.AlertService (Alert Evaluation & Deduplication) |
|   com.crowdcheck.service.AuthenticationService (SHA-256 Hashing & RBAC)  |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                        DATA ACCESS OBJECT (DAO) LAYER                    |
|   com.crowdcheck.dao.LocationDAO     com.crowdcheck.dao.CrowdUpdateDAO   |
|   com.crowdcheck.dao.CrowdAlertDAO   com.crowdcheck.dao.UserDAO          |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                       DATABASE CONFIGURATION & DRIVER                    |
|   com.crowdcheck.config.DatabaseConnection (JDBC Factory & Env Loader)   |
|   MySQL Connector/J 8.3.0 Driver (com.mysql.cj.jdbc.Driver)              |
+--------------------------------------------------------------------------+
                                     |
                                     v
+--------------------------------------------------------------------------+
|                             DATABASE ENGINE                              |
|           MySQL 8.0 Server (crowdcheck database - InnoDB Engine)         |
+--------------------------------------------------------------------------+
```

---

## 5. Functional Modules

### Module 1: Authentication & RBAC Module
- Handles credential validation using deterministic SHA-256 password hashing.
- Enforces strict role authorization across service boundaries:
  - `ADMIN`: Full authority over locations, staff accounts, alerts, and analytical reports.
  - `ORGANIZER`: Management of event facilities, crowd updates, alerts, and analytics.
  - `STAFF`: Live crowd adjustments, alert acknowledgment, and resolution.
  - `VISITOR`: Read-only queries for live occupancy and facility searches.

### Module 2: Location Management Module
- Manages the lifecycle of campus facilities (`AUDITORIUM`, `CAFETERIA`, `LIBRARY`, `SPORTS_COMPLEX`, `LABORATORY`, `GATE`, `CAMPUS_GROUND`, `PARKING`, `OTHER`).
- Guarantees data integrity via unique naming constraints and strict capacity validation ($> 0$).
- Enforces safe deletion checks: warns users and requires explicit override if historical updates exist.

### Module 3: Transactional Crowd Monitoring & Calculation Module
- Computes mathematical occupancy: $\text{Occupancy} = \frac{\text{Count}}{\text{Capacity}} \times 100$.
- Implements classification logic:
  - `LOW`: $< 40\%$
  - `MODERATE`: $40\% \le \text{Occ} < 70\%$
  - `HIGH`: $70\% \le \text{Occ} < 90\%$
  - `CRITICAL`: $\ge 90\%$ (including overflow)
- Coordinates atomic JDBC transactions:
  1. Updates `LOCATIONS` table.
  2. Inserts audit record into `CROWD_UPDATES`.
  3. Evaluates and registers alerts into `CROWD_ALERTS`.
  4. Auto-resolves active alarms if level drops below alert threshold.
  5. Commits atomically or triggers full rollback on failure.

### Module 4: Alert Generation & Lifecycle Management Module
- Monitors capacity breaches and prevents duplicate active alarms for the same venue.
- Supports lifecycle status transitions: `ACTIVE` $\rightarrow$ `ACKNOWLEDGED` $\rightarrow$ `RESOLVED`.
- Preserves complete alert history for retrospective review.

### Module 5: Analytics and Reporting Module
- Identifies frequent congestion hotspots using aggregation queries (`COUNT`, `SUM`, `GROUP BY`).
- Delivers facility profiles showing historical averages, peak counts, and update volumes.
- Computes alert lifecycle statistics.

---

## 6. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ CROWD_UPDATES : "logs/updates"
    LOCATIONS ||--o{ CROWD_UPDATES : "has history of"
    LOCATIONS ||--o{ CROWD_ALERTS : "triggers"

    USERS {
        int user_id PK
        string user_name UQ
        string email UQ
        string contact_info
        enum user_role
        string password_hash
        timestamp created_at
    }

    LOCATIONS {
        int location_id PK
        string location_name UQ
        enum location_type
        int capacity
        int current_crowd_count
        enum crowd_level
        timestamp last_updated
    }

    CROWD_UPDATES {
        int update_id PK
        int location_id FK
        int user_id FK
        int previous_count
        int new_count
        enum crowd_level
        timestamp update_time
    }

    CROWD_ALERTS {
        int alert_id PK
        int location_id FK
        string alert_message
        timestamp alert_time
        enum crowd_level
        enum status
    }
```

---

## 7. Database Normalization Analysis

### First Normal Form (1NF)
- **Atomic Values:** Every column in `USERS`, `LOCATIONS`, `CROWD_UPDATES`, and `CROWD_ALERTS` holds indivisible values. Multi-valued attributes (such as multiple historical counts) are stored in dedicated rows in `CROWD_UPDATES`.
- **Primary Keys:** Every relation possesses a unique synthetic integer primary key.
- **Uniformity:** Each attribute contains values of a single specified data type.

### Second Normal Form (2NF)
- A table is in 2NF if it is in 1NF and all non-key attributes are fully functionally dependent on the entire primary key.
- Since all primary keys in CrowdCheck are single-attribute integer identifiers (`user_id`, `location_id`, `update_id`, `alert_id`), **partial functional dependency is impossible**. Therefore, all tables inherently satisfy 2NF.

### Third Normal Form (3NF)
- A relation is in 3NF if it is in 2NF and has no transitive dependencies ($X \rightarrow Y$ and $Y \rightarrow Z$).
- In `LOCATIONS`: `location_id -> location_name, capacity, current_crowd_count, crowd_level`. No non-key attribute determines another non-key attribute.
- In `CROWD_UPDATES`: Attributes depend solely on `update_id`. Foreign keys `location_id` and `user_id` are pointers and do not embed redundant location or user attributes in the base table.
- Hence, all tables satisfy 3NF.

### Boyce-Codd Normal Form (BCNF)
- For every non-trivial functional dependency $X \rightarrow Y$, $X$ must be a superkey.
- In `USERS`, candidate keys are `user_id`, `user_name`, and `email`. Any determinant is a candidate key.
- In `LOCATIONS`, candidate keys are `location_id` and `location_name`. Both are candidate superkeys.
- The schema is fully compliant with **BCNF**.

---

## 8. Implementation Details

1. **PreparedStatement Enforcement:** Eliminates SQL injection vulnerabilities across all 15+ database queries.
2. **Try-With-Resources:** Guarantees deterministic closure of `Connection`, `PreparedStatement`, and `ResultSet` instances, eliminating memory and connection pool exhaustion.
3. **Transaction Management:** Employs explicit `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` blocks to prevent database inconsistency during multi-step updates.
4. **Decoupled Credentials:** Reads credentials from environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`) or external `db.properties`, ensuring zero hardcoded secrets in source code.

---

## 9. Testing & Quality Assurance

A rigorous test suite utilizing **JUnit 5 (Jupiter)** was constructed:
- **Boundary Testing:** Verified mathematical calculations at 39.99%, 40.0%, 69.99%, 70.0%, 89.99%, and 90.0%.
- **Capacity Overflow:** Verified that headcounts exceeding 100% capacity are classified as `CRITICAL`.
- **Exception Verification:** Ensured negative counts and non-positive capacities throw `IllegalArgumentException`.
- **Security & RBAC:** Verified permission matrix across `ADMIN`, `ORGANIZER`, `STAFF`, and `VISITOR`.
- **Test Results:** 46 unit test cases passed with **0 failures and 0 errors**.

---

## 10. Advantages of CrowdCheck

1. **Safety First:** Immediate notification when high-density risks develop prevents stampedes and accidents.
2. **ACID Compliant:** Strict transactional controls prevent data divergence.
3. **Lightweight & Portable:** Built on pure Java 17 and JDBC without large enterprise framework dependencies.
4. **Permanent Audit Trail:** Every update is tracked with previous/new counts and staff timestamps.
5. **Role-Aware Security:** Prevents unauthorized personnel from modifying critical occupancy thresholds.

---

## 11. Limitations & Future Scope

### Limitations
- Headcount updates are entered by staff or sensor gateway scripts via JDBC rather than direct IoT edge devices.
- Console interface requires terminal access.

### Future Scope
- **IoT Integration:** Direct integration with turnstile gate counters and thermal camera vision feeds via MQTT.
- **RESTful API Layer:** Exposing endpoints for native mobile student apps (Android/iOS).
- **Predictive AI:** Utilizing time-series models to predict cafeteria rushes based on class schedules.

---

## 12. Conclusion

CrowdCheck successfully achieves all design goals of an enterprise-grade College DBMS project. By combining rigorous relational database principles, clean OOP architecture, PreparedStatement security, ACID transaction safety, and comprehensive documentation, the application provides an exemplary solution for live campus crowd monitoring.
