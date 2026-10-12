# CrowdCheck — Live Crowd Monitoring System
## Spring Boot 3.x & MySQL 8.x Web Application

A full-stack campus crowd monitoring system built using **Java 17**, **Spring Boot 3.2.4**, **Spring Data JPA**, **Hibernate**, **MySQL 8.x**, and a responsive **HTML/CSS/JS** frontend dashboard.

---

## 📋 Table of Contents
1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [Database Setup](#database-setup)
4. [Running the Application](#running-the-application)
5. [REST API Endpoints](#rest-api-endpoints)
6. [Role-Based Access Control (RBAC)](#role-based-access-control-rbac)
7. [Testing & Verification](#testing--verification)
8. [Demonstration Guide](#demonstration-guide)

---

## 💻 1. Technology Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.2.4 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- **Persistence:** Spring Data JPA / Hibernate ORM
- **Database:** MySQL Server 8.0+ (`com.mysql:mysql-connector-j`)
- **Frontend:** Vanilla HTML5, CSS3, and JavaScript (`fetch()` API)
- **Testing:** JUnit 5 (Jupiter), Mockito, Spring Boot Test

---

## 📂 2. Project Structure

```
CrowdCheck/
├── pom.xml                                   # Maven Spring Boot configuration
├── README.md                                 # Complete documentation & demonstration guide
├── database/
│   ├── schema.sql                            # 4 MySQL table schemas & constraints
│   └── sample_data.sql                       # Seed data for locations, updates, alerts
├── src/main/java/com/crowdcheck/
│   ├── CrowdCheckApplication.java            # Main Spring Boot bootstrap
│   ├── entity/
│   │   ├── User.java                         # USERS JPA entity
│   │   ├── Location.java                     # LOCATIONS JPA entity
│   │   ├── CrowdUpdate.java                  # CROWD_UPDATES JPA entity
│   │   └── CrowdAlert.java                   # CROWD_ALERTS JPA entity
│   ├── repository/
│   │   ├── UserRepository.java               # Spring Data JPA User repository
│   │   ├── LocationRepository.java           # Location queries & dynamic search
│   │   ├── CrowdUpdateRepository.java        # History & hotspot analysis query
│   │   └── CrowdAlertRepository.java         # Alert deduplication & lifecycle query
│   ├── service/
│   │   ├── UserService.java                  # User logic & RBAC checks
│   │   ├── LocationService.java              # Location CRUD & safe deletion logic
│   │   ├── CrowdMonitoringService.java       # Transactional crowd updates & calculation
│   │   └── AlertService.java                 # Alert rules, deduplication & transitions
│   ├── controller/
│   │   ├── UserController.java               # /api/users endpoints
│   │   ├── LocationController.java           # /api/locations endpoints
│   │   ├── CrowdUpdateController.java        # /api/crowd endpoints
│   │   └── CrowdAlertController.java         # /api/alerts endpoints
│   ├── dto/
│   │   ├── CrowdUpdateRequest.java           # Location ID & new count request
│   │   └── LocationResponse.java             # Live crowd status DTO
│   └── exception/
│       └── GlobalExceptionHandler.java       # Centralized REST exception handling
├── src/main/resources/
│   ├── application.properties                # Spring Boot datasource & JPA settings
│   └── static/
│       ├── index.html                        # Live Dashboard UI
│       ├── style.css                         # Clean, modern, responsive styles
│       └── script.js                         # Plain JavaScript fetch client
└── src/test/java/com/crowdcheck/
    ├── CrowdLevelCalculationTest.java        # Density calculation boundary tests
    ├── AlertServiceLogicTest.java            # Alert rules & deduplication tests
    └── LocationServiceTest.java              # Capacity validation & safe deletion tests
```

---

## 🗄️ 3. Database Setup

### Tables (Strict 4-Table Schema)
1. **`USERS`**: `user_id` (PK), `user_name`, `email`, `contact_info`, `user_role` (`ADMIN`, `ORGANIZER`, `STAFF`, `VISITOR`).
2. **`LOCATIONS`**: `location_id` (PK), `location_name`, `location_type` (`ENTRANCE`, `FOOD_COURT`, `AUDITORIUM`, `PARKING`), `capacity`, `current_crowd_count`, `crowd_level`, `last_updated`.
3. **`CROWD_ALERTS`**: `alert_id` (PK), `location_id` (FK), `alert_message`, `alert_time`, `crowd_level`, `status` (`ACTIVE`, `ACKNOWLEDGED`, `RESOLVED`).
4. **`CROWD_UPDATES`**: `update_id` (PK), `location_id` (FK), `previous_count`, `new_count`, `crowd_level`, `update_time`.

### Initialize MySQL
Run the scripts in MySQL Workbench or PowerShell:
```powershell
Get-Content "database/schema.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
Get-Content "database/sample_data.sql" | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p
```

### Configure Credentials
In `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crowdcheck?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

---

## 🚀 4. Running the Application

### Option A: Using Maven (Recommended)
```bash
mvn spring-boot:run
```

### Option B: Package and Run JAR
```bash
mvn clean package -DskipTests
java -jar target/CrowdCheck-1.0.0.jar
```

### Accessing the Web Dashboard
Open your browser and navigate to:
```
http://localhost:8080
```

---

## 🌐 5. REST API Endpoints

### Location Management (`/api/locations`)
| Method | Endpoint | Description | Permitted Roles |
|---|---|---|---|
| `GET` | `/api/locations` | Retrieve all locations with live occupancy | Any |
| `GET` | `/api/locations/{id}` | Retrieve specific location details | Any |
| `GET` | `/api/locations/search` | Search by `name`, `type`, `level` | Any |
| `POST` | `/api/locations` | Register new campus location | `ADMIN`, `ORGANIZER` |
| `PUT` | `/api/locations/{id}` | Update location details/capacity | `ADMIN`, `ORGANIZER` |
| `DELETE` | `/api/locations/{id}` | Safely delete location (rejects if updates exist) | `ADMIN`, `ORGANIZER` |

### Crowd Management & History (`/api/crowd`)
| Method | Endpoint | Description | Permitted Roles |
|---|---|---|---|
| `POST` | `/api/crowd/update` | Transactional crowd count update | `ADMIN`, `ORGANIZER`, `STAFF` |
| `GET` | `/api/crowd/history` | Complete historical audit trail | Any |
| `GET` | `/api/crowd/history/{id}` | Facility-specific update history | Any |
| `GET` | `/api/crowd/analysis/frequent-overcrowded` | Locations with highest HIGH/CRITICAL counts | Any |

### Crowd Safety Alerts (`/api/alerts`)
| Method | Endpoint | Description | Permitted Roles |
|---|---|---|---|
| `GET` | `/api/alerts` | List all crowd alerts | Any |
| `GET` | `/api/alerts/active` | List only active safety alerts | Any |
| `PUT` | `/api/alerts/{id}/acknowledge` | Acknowledge alert | `ADMIN`, `ORGANIZER`, `STAFF` |
| `PUT` | `/api/alerts/{id}/resolve` | Resolve alert | `ADMIN`, `ORGANIZER`, `STAFF` |

---

## 🔐 6. Role-Based Access Control (RBAC)
- **`ADMIN`:** Complete control across facility CRUD, crowd updates, alerts, and audit history.
- **`ORGANIZER`:** Event facility management, crowd updates, alerts, and analysis.
- **`STAFF`:** Live headcount adjustments and safety alert lifecycle management.
- **`VISITOR`:** Read-only access to view live statuses and search locations.

The Web UI features a real-time **Acting Role Selector** at the top right to dynamically demonstrate these restrictions.

---

## 🧪 7. Testing & Verification

Run all unit tests with Maven:
```bash
mvn clean test
```

### Test Coverage Highlights
- **`CrowdLevelCalculationTest`:** Validates discrete boundaries:
  - `< 40%` $\rightarrow$ `LOW`
  - `40% - 69.9%` $\rightarrow$ `MODERATE`
  - `70% - 89.9%` $\rightarrow$ `HIGH`
  - `≥ 90%` $\rightarrow$ `CRITICAL`
  - Overcapacity ($> 100\%$) $\rightarrow$ `CRITICAL`
  - Negative counts and non-positive capacities $\rightarrow$ `IllegalArgumentException`
- **`AlertServiceLogicTest`:** Validates automated alert triggering on `HIGH`/`CRITICAL`, duplicate active alert suppression, and safe level non-alerting.
- **`LocationServiceTest`:** Validates capacity restrictions, location type checks, and safe deletion prevention when history exists.

---

## 🎥 8. Demonstration Guide

1. **Start Application:** Execute `mvn spring-boot:run`. Open `http://localhost:8080`.
2. **View Live Status:** The dashboard shows facility cards with real-time occupancy percentages, crowd levels, and visual progress bars.
3. **Search & Filter:** Type "Auditorium" in the search box or filter by `FOOD_COURT` to observe instant filtering.
4. **Update Crowd Count:** Select *Grand Auditorium* and submit a new count of `470` (94.0%). The facility turns `CRITICAL` (red) and an alert is automatically generated in the Alerts table.
5. **Manage Alerts:** Click **Ack** on the newly created alert to mark it acknowledged, then click **Resolve** to close it.
6. **Safe Deletion Check:** Attempting to delete a facility that already has update history will be blocked to preserve audit integrity.
7. **Test Role Restrictions:** Switch the top right dropdown from `ADMIN` to `VISITOR` — observe that facility editing and headcount submission controls are immediately disabled!
