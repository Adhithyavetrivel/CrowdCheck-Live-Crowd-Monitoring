# CrowdCheck – Live Crowd Monitoring System

A robust, enterprise-grade Console DBMS application built in **Java 17**, **JDBC**, and **MySQL 8.0** to monitor, track, and regulate real-time crowd occupancy across campus facilities (Auditoriums, Cafeterias, Libraries, Sports Arenas, Labs, and Gates).

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Key Functionalities](#key-functionalities)
3. [Crowd Classification Rules](#crowd-classification-rules)
4. [Technology Stack](#technology-stack)
5. [Project Structure](#project-structure)
6. [Database Setup & Configuration](#database-setup--configuration)
7. [Building and Running](#building-and-running)
8. [Sample Credentials](#sample-credentials)
9. [Sample Workflows & Usage](#sample-workflows--usage)
10. [Troubleshooting](#troubleshooting)

---

## 🌟 Project Overview

CrowdCheck was engineered to solve overcrowding, stampede hazards, and facility congestion across college campuses. Using transactional JDBC database routines, the system maintains accurate, real-time headcounts, audits every change, generates safety alerts on high and critical occupancies, and provides historical analytical reports for campus administrators.

---

## 🎯 Key Functionalities

1. **Location Management:** Complete CRUD capabilities to register, update capacities, search, and safely delete facilities.
2. **Transactional Crowd Updates:** Atomic database updates that modify live headcounts and write historical audit records simultaneously.
3. **Live Status Display:** Formatted console tables presenting location metrics, capacities, occupancy percentage, and status indicators.
4. **Flexible Search:** Multi-parameter search across facility IDs, keywords, categories, and crowd levels.
5. **Automated Classification:** Mathematical evaluation of crowd density based on capacity thresholds.
6. **Alert Generation & Deduplication:** Immediate trigger of alerts on `HIGH` and `CRITICAL` levels, preventing duplicate active notifications.
7. **Alert Lifecycle Management:** View, acknowledge, and resolve alerts while retaining complete audit history.
8. **Historical Audit & Analytics:** Campus-wide and facility-specific analytics, peak occupancy stats, and hotspot detection.
9. **Role-Based Access Control (RBAC):** Distinct privileges for `ADMIN`, `ORGANIZER`, `STAFF`, and `VISITOR`.
10. **Secure Credentials:** SHA-256 password hashing and decoupled configuration via environment variables or properties files.

---

## 📐 Crowd Classification Rules

The occupancy percentage is calculated using:
$$\text{Occupancy (\%)} = \left(\frac{\text{Current Crowd Count}}{\text{Designated Capacity}}\right) \times 100$$

| Level | Occupancy Range | Description & Action |
|---|---|---|
| **LOW** | Occupancy strictly below 40% ($< 40\%$) | Ample capacity available. Green indicator. |
| **MODERATE** | 40% to below 70% ($40\% \le \text{Occ} < 70\%$) | Normal operational traffic. Yellow indicator. |
| **HIGH** | 70% to below 90% ($70\% \le \text{Occ} < 90\%$) | Approaching capacity limit. Safety alert triggered. |
| **CRITICAL** | 90% and above ($\ge 90\%$) or overflow | Overcrowding / emergency threshold. High-priority alert triggered. |

---

## 💻 Technology Stack

- **Language:** Java 17 (Eclipse Temurin / OpenJDK 17)
- **Database:** MySQL Server 8.0+
- **Driver:** MySQL Connector/J 8.3.0 (`com.mysql.cj.jdbc.Driver`)
- **Build System:** Apache Maven 3.9+
- **Testing Framework:** JUnit 5 (Jupiter 5.10.2)
- **Architecture:** Layered Model-DAO-Service-UI architecture with JDBC transactions

---

## 📂 Project Structure

```
CrowdCheck/
├── pom.xml                               # Maven project definition and plugins
├── README.md                             # Comprehensive project documentation
├── .gitignore                            # Excludes build artifacts and secrets
├── database/
│   ├── schema.sql                        # Table definitions, DDL, constraints & indexes
│   ├── sample_data.sql                   # Initial seed data with hashed passwords
│   └── reset_database.sql                # Complete database teardown & re-seed script
├── docs/
│   ├── project_report.md                 # Full DBMS college project report
│   ├── database_design.md                # Schema documentation, ER model, & SQL queries
│   └── test_cases.md                     # Comprehensive test scenarios & verification
├── src/
│   ├── main/
│   │   ├── java/com/crowdcheck/
│   │   │   ├── Main.java                 # Main application bootstrap
│   │   │   ├── config/
│   │   │   │   └── DatabaseConnection.java # Singleton JDBC manager & env loader
│   │   │   ├── dao/
│   │   │   │   ├── CrowdAlertDAO.java    # Alert queries & lifecycle updates
│   │   │   │   ├── CrowdUpdateDAO.java   # Update history logging & aggregations
│   │   │   │   ├── LocationDAO.java      # Location CRUD & transactional updates
│   │   │   │   └── UserDAO.java          # User persistence & authentication queries
│   │   │   ├── enums/
│   │   │   │   ├── AlertStatus.java      # ACTIVE, ACKNOWLEDGED, RESOLVED
│   │   │   │   ├── CrowdLevel.java       # LOW, MODERATE, HIGH, CRITICAL
│   │   │   │   ├── LocationType.java     # AUDITORIUM, CAFETERIA, LIBRARY, etc.
│   │   │   │   └── UserRole.java         # ADMIN, ORGANIZER, STAFF, VISITOR
│   │   │   ├── model/
│   │   │   │   ├── CrowdAlert.java       # Alert data entity
│   │   │   │   ├── CrowdUpdate.java      # Audit update entity
│   │   │   │   ├── Location.java         # Facility entity
│   │   │   │   └── User.java             # User entity
│   │   │   ├── service/
│   │   │   │   ├── AlertService.java     # Alert evaluation & state logic
│   │   │   │   ├── AuthenticationService.java # SHA-256 hashing & session state
│   │   │   │   ├── CrowdLevelCalculator.java # Density calculator & progress bars
│   │   │   │   └── CrowdMonitoringService.java # Transactional update coordinator
│   │   │   └── ui/
│   │   │       ├── ConsoleMenu.java      # Interactive 10-option console UI
│   │   │       └── InputValidator.java   # Input filtering & regex verifiers
│   │   └── resources/
│   │       ├── db.properties             # Local database credentials
│   │       └── db.properties.example     # Template credentials file
│   └── test/
│       └── java/com/crowdcheck/
│           ├── AlertServiceTest.java     # Alert rule verification tests
│           ├── CrowdLevelCalculatorTest.java # Boundary & classification unit tests
│           └── InputValidatorTest.java   # Email, phone, & hashing unit tests
└── scripts/
    ├── run.bat                           # One-click Windows runner
    └── run.sh                            # One-click Linux/macOS runner
```

---

## 🗄️ Database Setup & Configuration

### 1. Initialize MySQL Database
Open your MySQL terminal or MySQL Workbench and run:
```sql
SOURCE D:/Projects/DBMS/CrowdCheck/database/schema.sql;
SOURCE D:/Projects/DBMS/CrowdCheck/database/sample_data.sql;
```
Or execute directly from PowerShell/Command Prompt:
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```

### 2. Configure Credentials
The system checks credentials in the following order:
1. **Environment Variables:** `DB_URL`, `DB_USER`, `DB_PASSWORD`
2. **External file:** `./db.properties` in current working directory
3. **Classpath resource:** `src/main/resources/db.properties`

Example `db.properties`:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/crowdcheck?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=your_mysql_password
```

---

## 🚀 Building and Running

### Prerequisites
- JDK 17 or higher (`java -version`)
- Apache Maven 3.8+ (`mvn -version`)
- MySQL 8.0 running on localhost:3306

### Compile & Test
```bash
cd CrowdCheck
mvn clean test
```

### Build Executable Fat JAR
```bash
mvn package
```
This produces `target/crowdcheck.jar` bundled with all required JDBC dependencies.

### Run in Terminal
Using java directly:
```bash
java -jar target/crowdcheck.jar
```
Or with Maven:
```bash
mvn exec:java
```
Or run the bundled launcher:
- **Windows:** `.\scripts\run.bat`
- **Linux/macOS:** `./scripts/run.sh`

### Run in Visual Studio Code
1. Open Visual Studio Code and choose **File > Open Folder...** -> select `d:\Projects\DBMS\CrowdCheck`.
2. Open `src/main/java/com/crowdcheck/Main.java`.
3. Press **F5** or click **Run Java** at the top right of the editor.
4. Alternatively, open the integrated terminal (`Ctrl + \``) and run:
   ```bash
   mvn clean package
   java -jar target/crowdcheck.jar
   ```

---

## 🔑 Sample Credentials

The sample data script seeds accounts for all four user roles with SHA-256 hashed passwords:

| Username | Role | Password | Description & Permissions |
|---|---|---|---|
| `admin` | **ADMIN** | `Admin@123` | Full administrative control: Manage users, facilities, and alerts. |
| `organizer_rachel` | **ORGANIZER** | `Organizer@123` | Event coordination: Manage facilities, update headcounts, view reports. |
| `staff_john` | **STAFF** | `Staff@123` | Operational staff: Update crowd counts, view and manage safety alerts. |
| `staff_priya` | **STAFF** | `Staff@123` | Operational staff: Stationed at cafeteria / labs. |
| `visitor_alex` | **VISITOR** | `Visitor@123` | Read-only campus visitor: Check live status and search facilities. |

---

## 🔄 Sample Workflows & Usage

### Workflow 1: Live Status Check (Visitor/Student)
1. Launch application (Guest mode active).
2. Choose **Option 2: View live crowd status**.
3. View real-time occupancy percentages, crowd levels, and visual capacity indicators across campus.

### Workflow 2: Update Crowd & Trigger High-Capacity Alert (Staff)
1. Choose **Option 1: Login** -> enter `staff_john` / `Staff@123`.
2. Choose **Option 4: Update crowd count**.
3. Select Location `3` (University Library, capacity 300).
4. Enter new crowd count `270` (90.0% occupancy).
5. System computes new level `CRITICAL`, updates `LOCATIONS`, inserts `CROWD_UPDATES` audit row, and automatically triggers an active safety alert.

### Workflow 3: Acknowledge & Resolve Safety Alert (Staff/Organizer)
1. Choose **Option 6: View alerts** to review active alert details.
2. Choose **Option 7: Manage alerts** -> Select `1` (Acknowledge) to mark that personnel are dispatching.
3. Later, when crowd levels dissipate or are directed, select `2` (Resolve) to close the alert.

### Workflow 4: Register Facility & View Analytics (Admin)
1. Login as `admin` / `Admin@123`.
2. Choose **Option 5: Manage locations** -> Add new facility: "Robotics Auditorium", Capacity 350.
3. Choose **Option 9: Crowd analysis and reports** to review top overcrowding hotspots across the semester.

---

## 🛠️ Troubleshooting

- **Error: Access denied for user 'root'@'localhost'**:
  Ensure your MySQL root password is configured in `db.properties` or set via `$env:DB_PASSWORD="your_password"`.
- **Error: Communications link failure**:
  Verify the MySQL 8.0 service is running in Windows Services (`services.msc`) or via `Start-Service MySQL80`.
- **Database 'crowdcheck' not found**:
  Execute `database/schema.sql` to create the schema and initial tables before running the app.
