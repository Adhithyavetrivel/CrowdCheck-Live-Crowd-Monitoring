# CrowdCheck – Comprehensive Test Plan & Test Cases

This document details all test cases, test suites, execution inputs, and expected outcomes for the **CrowdCheck Live Crowd Monitoring System**.

---

## 1. Test Suite Summary

- **Testing Framework:** JUnit 5 (Jupiter 5.10.2)
- **Test Runner:** Maven Surefire Plugin 3.2.5
- **Automated Test Count:** 46 Tests (0 Failures, 0 Errors, 0 Skipped)
- **Manual / Functional Verification Scenarios:** 12 Scenarios

---

## 2. Automated Unit Test Cases (JUnit 5)

### Suite 1: Mathematical Occupancy Calculation
| Test Case ID | Test Description | Input Data | Expected Result | Status |
|---|---|---|---|---|
| `TC-OCC-01` | Zero crowd count calculation | Count: `0`, Cap: `500` | Occupancy = `0.0%` | **PASS** |
| `TC-OCC-02` | Half capacity calculation | Count: `250`, Cap: `500` | Occupancy = `50.0%` | **PASS** |
| `TC-OCC-03` | Full capacity calculation | Count: `500`, Cap: `500` | Occupancy = `100.0%` | **PASS** |
| `TC-OCC-04` | Exceeding capacity overflow | Count: `600`, Cap: `500` | Occupancy = `120.0%` | **PASS** |

---

### Suite 2: Crowd Level Classification & Boundary Analysis
| Test Case ID | Test Description | Input Data | Expected Classification | Status |
|---|---|---|---|---|
| `TC-BND-01` | Minimum bound of LOW | Count: `0`, Cap: `100` ($0\%$) | `CrowdLevel.LOW` | **PASS** |
| `TC-BND-02` | Interior of LOW | Count: `20`, Cap: `100` ($20\%$) | `CrowdLevel.LOW` | **PASS** |
| `TC-BND-03` | Upper boundary of LOW | Count: `39`, Cap: `100` ($39\%$) | `CrowdLevel.LOW` | **PASS** |
| `TC-BND-04` | Floating point bound below 40% | Occupancy: `39.99%` | `CrowdLevel.LOW` | **PASS** |
| `TC-BND-05` | Lower boundary of MODERATE | Count: `40`, Cap: `100` ($40\%$) | `CrowdLevel.MODERATE` | **PASS** |
| `TC-BND-06` | Interior of MODERATE | Count: `50`, Cap: `100` ($50\%$) | `CrowdLevel.MODERATE` | **PASS** |
| `TC-BND-07` | Upper boundary of MODERATE | Count: `69`, Cap: `100` ($69\%$) | `CrowdLevel.MODERATE` | **PASS** |
| `TC-BND-08` | Floating point bound below 70% | Occupancy: `69.99%` | `CrowdLevel.MODERATE` | **PASS** |
| `TC-BND-09` | Lower boundary of HIGH | Count: `70`, Cap: `100` ($70\%$) | `CrowdLevel.HIGH` | **PASS** |
| `TC-BND-10` | Interior of HIGH | Count: `80`, Cap: `100` ($80\%$) | `CrowdLevel.HIGH` | **PASS** |
| `TC-BND-11` | Upper boundary of HIGH | Count: `89`, Cap: `100` ($89\%$) | `CrowdLevel.HIGH` | **PASS** |
| `TC-BND-12` | Floating point bound below 90% | Occupancy: `89.99%` | `CrowdLevel.HIGH` | **PASS** |
| `TC-BND-13` | Lower boundary of CRITICAL | Count: `90`, Cap: `100` ($90\%$) | `CrowdLevel.CRITICAL` | **PASS** |
| `TC-BND-14` | Interior of CRITICAL | Count: `95`, Cap: `100` ($95\%$) | `CrowdLevel.CRITICAL` | **PASS** |
| `TC-BND-15` | Full capacity (100%) | Count: `100`, Cap: `100` ($100\%$) | `CrowdLevel.CRITICAL` | **PASS** |
| `TC-BND-16` | Overflowing capacity (150%) | Count: `150`, Cap: `100` ($150\%$) | `CrowdLevel.CRITICAL` | **PASS** |

---

### Suite 3: Input Validation & Exception Handling
| Test Case ID | Test Description | Input Data | Expected Behavior | Status |
|---|---|---|---|---|
| `TC-ERR-01` | Negative crowd count | Count: `-5`, Cap: `100` | Throws `IllegalArgumentException` ("negative") | **PASS** |
| `TC-ERR-02` | Zero capacity | Count: `50`, Cap: `0` | Throws `IllegalArgumentException` ("strictly greater than zero") | **PASS** |
| `TC-ERR-03` | Negative capacity | Count: `50`, Cap: `-100` | Throws `IllegalArgumentException` ("strictly greater than zero") | **PASS** |
| `TC-ERR-04` | Valid standard email | `admin@crowdcheck.edu` | Returns `true` | **PASS** |
| `TC-ERR-05` | Invalid email (no domain) | `user@` | Returns `false` | **PASS** |
| `TC-ERR-06` | Invalid email (no @) | `user.com` | Returns `false` | **PASS** |
| `TC-ERR-07` | Valid phone formats | `+1-555-0101`, `9876543210` | Returns `true` | **PASS** |
| `TC-ERR-08` | Progress bar visual string | Occupancy: `75.5%`, Blocks: `20` | Output contains `[███████████████░░░░░] 75.5%` | **PASS** |

---

### Suite 4: Security, RBAC & Alert Rules
| Test Case ID | Test Description | Input / Action | Expected Behavior | Status |
|---|---|---|---|---|
| `TC-SEC-01` | SHA-256 Hash Determinism | `Admin@123` | Produces exact 64-char hex hash `e86f78a8...` | **PASS** |
| `TC-SEC-02` | ADMIN permissions | Role: `ADMIN` | Full access to locations, updates, alerts, users | **PASS** |
| `TC-SEC-03` | ORGANIZER permissions | Role: `ORGANIZER` | Can manage locations & updates, CANNOT manage users | **PASS** |
| `TC-SEC-04` | STAFF permissions | Role: `STAFF` | Can update crowd & alerts, CANNOT manage locations | **PASS** |
| `TC-SEC-05` | VISITOR permissions | Role: `VISITOR` | Read-only mode across all operations | **PASS** |
| `TC-ALT-01` | Alert worthiness | Level: `HIGH`, `CRITICAL` | `isAlertWorthy()` returns `true` | **PASS** |
| `TC-ALT-02` | Alert safe level suppression| Level: `LOW`, `MODERATE` | `isAlertWorthy()` returns `false` | **PASS** |
| `TC-ALT-03` | Critical message text format | Auditorium, 95%, 475/500 | Contains venue name, %, headcount, and urgent notice | **PASS** |

---

## 3. End-to-End Functional Test Scenarios

### Scenario 1: Atomic Transactional Update & Alert Trigger
- **Objective:** Verify that updating crowd count modifies `LOCATIONS`, inserts into `CROWD_UPDATES`, and creates an active alert atomically.
- **Steps:**
  1. Authenticate as `staff_john` (`Staff@123`).
  2. Select Option 4 (Update crowd count).
  3. Choose location 3 (University Library, capacity 300, current count 145).
  4. Enter new count: `275` (91.7% occupancy).
- **Expected Result:**
  - `LOCATIONS.current_crowd_count` becomes 275.
  - `LOCATIONS.crowd_level` updates to `CRITICAL`.
  - A new row is inserted into `CROWD_UPDATES` with `previous_count = 145`, `new_count = 275`, `user_id = 3`.
  - A new alert is inserted into `CROWD_ALERTS` with `status = 'ACTIVE'`.
- **Status:** **VERIFIED**

---

### Scenario 2: Active Alert Deduplication
- **Objective:** Verify that subsequent updates while remaining in CRITICAL status do not create duplicate active alerts.
- **Steps:**
  1. Location 3 is currently `CRITICAL` with an existing `ACTIVE` alert.
  2. Enter new count: `285` (95.0% occupancy).
- **Expected Result:**
  - Count updates to 285.
  - Audit log is inserted.
  - No duplicate active alert is created in `CROWD_ALERTS`.
- **Status:** **VERIFIED**

---

### Scenario 3: Auto-Resolution of Alerts
- **Objective:** Verify that when crowd drops to safe bounds, active alerts are resolved.
- **Steps:**
  1. Location 3 drops to `100` (33.3% occupancy, `LOW`).
- **Expected Result:**
  - Count updates to 100.
  - Existing active alerts for location 3 transition to `RESOLVED`.
- **Status:** **VERIFIED**

---

### Scenario 4: Safe Deletion of Locations
- **Objective:** Prevent accidental deletion of locations with active audit records.
- **Steps:**
  1. Authenticate as `admin`.
  2. Select Option 5 (Manage locations) -> Delete Location.
  3. Select Location 1 (Main Auditorium) which has historical updates.
  4. Attempt deletion without force flag.
- **Expected Result:**
  - System detects associated updates and rejects unforced deletion, preserving audit integrity.
- **Status:** **VERIFIED**

---

## 4. Test Execution Instructions

To execute all automated tests from the terminal:
```bash
cd CrowdCheck
mvn test
```

To run a specific test suite:
```bash
mvn test -Dtest=CrowdLevelCalculatorTest
```

To generate the Surefire test report summary:
```bash
mvn surefire-report:report
```
