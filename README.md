# ☁️ Private Cloud Storage

A **Spring Boot** REST API for personal cloud storage where each registered user gets **50 MB** of isolated storage space. Built for Lab 08 — Automated Software Testing.

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Allure CLI** (for viewing reports): `npm install -g allure-commandline` or [install guide](https://docs.qameta.io/allure/#_installing_a_commandline)

### Run the Application

```bash
mvn spring-boot:run
```

The app starts at `http://localhost:8080`.

- **Web UI**: [http://localhost:8080/login](http://localhost:8080/login)
- **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:cloudstore`)

### Run the Tests

```bash
# Install Playwright browsers (first time only)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"

# Run the full test suite
mvn clean test
```

### View the Allure Report

```bash
# Serve the report (opens in browser automatically)
allure serve target/allure-results

# Or generate a static report
allure generate target/allure-results --clean -o target/allure-report
```

---

## 📋 Features

| # | Requirement | Description |
|---|---|---|
| R1 | Register | New user created with a **50 MB** quota |
| R2 | Authenticate | Login via HTTP Basic or form; each request acts as the logged-in user |
| R3 | Manage profile | View & update display name / password |
| R4 | Delete account | User deletes **own** account + all their data |
| R5 | Folders | Create, rename, move, list, delete folders |
| R6 | Files | Upload, download, rename, move, delete files |
| R7 | Quota | Reject uploads that exceed remaining space |
| R8 | Isolation | No access to another user's files/folders |

---

## 🔗 API Endpoints

```
# Accounts
POST   /api/auth/register     # {email, password} → user (50 MB)
POST   /api/auth/login        # → profile (HTTP Basic auth)
GET    /api/me                # profile + quota usage
PUT    /api/me                # update display name / password
DELETE /api/me                # delete own account + data

# Folders
POST   /api/folders           # create folder
GET    /api/folders?parent=ID # list folders
PATCH  /api/folders/{id}      # rename / move
DELETE /api/folders/{id}      # delete (recursive)

# Files
POST   /api/files (multipart) # upload
GET    /api/files?folder=ID   # list files
GET    /api/files/{id}/download
PATCH  /api/files/{id}        # rename / move
DELETE /api/files/{id}        # delete
```

---

## 🧪 Testing Method Mapping

All **10 testing methods** from the lecture are demonstrated in the test suite:

| # | Testing Method | Test Class | Test Method | What It Verifies |
|---|---|---|---|---|
| 1 | **Content Equals** | `QuotaServiceTest` | `newUserQuotaEquals50MB()` | New user quota == 52 428 800 bytes (50 MB) |
| 2 | **Contains** | `FileServiceTest` | `fileListingContainsUploadedFileName()` | File listing contains uploaded file name |
| 3 | **Regex Matched** | `ProfileApiTest` | `profileEmailMatchesRegex()` | Email matches `^[\w.+-]+@[\w.-]+$` |
| 4 | **Formula Matched** | `QuotaServiceTest` | `quotaFormulaHolds()` | `freeBytes = quotaBytes − Σ(file sizes)` |
| 5 | **Predicate** | `IsolationTest` | `userBUsesNothingAfterUserAUploads()` | `usedBytes(userB) == 0` after userA uploads |
| 6 | **Collection** | `IsolationTest` | `userBCannotSeeUserAFiles()` | userB's file list is empty, no userA files |
| 7 | **Exception** | `QuotaServiceTest` | `overQuotaUploadThrowsException()` | Over-quota upload throws `QuotaExceededException` |
| 8 | **Tolerance** | `QuotaServiceTest` | `usedBytesToleranceCheck()` | Used MB ≈ expected within delta (0.01) |
| 9 | **Schema/JSON** | `ProfileApiTest` | `profileResponseHasRequiredFields()` | `/api/me` response has all required fields |
| 10 | **Visual/Snapshot** | `DashboardPlaywrightTest` | `dashboardShowsQuotaAndSnapshot()` | Dashboard shows "50 MB" + screenshot |

---

## 🏗️ Project Structure

```
private-cloud-storage/
├── src/main/java/com/cloudstore/
│   ├── CloudStoreApplication.java        # Spring Boot entry point
│   ├── config/SecurityConfig.java        # Spring Security (HTTP Basic + form login)
│   ├── controller/
│   │   ├── AuthController.java           # POST /api/auth/register, /api/auth/login
│   │   ├── ProfileController.java        # GET/PUT/DELETE /api/me
│   │   ├── FolderController.java         # CRUD /api/folders
│   │   ├── FileController.java           # CRUD /api/files
│   │   └── WebController.java            # Thymeleaf pages
│   ├── dto/                              # Request/Response DTOs
│   ├── exception/                        # QuotaExceededException, handlers
│   ├── model/                            # User, Folder, FileEntity
│   ├── repository/                       # Spring Data JPA repositories
│   └── service/
│       ├── UserService.java              # Registration, profile, account deletion
│       └── StorageService.java           # File/folder CRUD, quota, isolation
├── src/main/resources/
│   ├── application.properties
│   └── templates/                        # Thymeleaf: login, register, dashboard
├── src/test/java/com/cloudstore/
│   ├── QuotaServiceTest.java             # Methods: equals, formula, exception, tolerance
│   ├── FileServiceTest.java              # Method: contains
│   ├── IsolationTest.java                # Methods: predicate, collection
│   ├── ProfileApiTest.java               # Methods: regex, schema/JSON (Playwright API)
│   └── DashboardPlaywrightTest.java      # Method: visual/snapshot (Playwright browser)
├── pom.xml
└── README.md
```

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Backend | Spring Boot 3.2.5 |
| Database | H2 (in-memory) |
| Security | Spring Security (BCrypt, HTTP Basic, form login) |
| UI | Thymeleaf |
| Unit Tests | JUnit 5 + AssertJ |
| E2E / API Tests | Playwright (Java) |
| Reporting | Allure |
| Build | Maven |

---

## 📊 Allure Report

Tests are annotated with `@Epic`, `@Feature`, `@Story`, and `@Severity`. Test phases use `Allure.step()` for traceability. Screenshots and JSON responses are attached as Allure attachments.

To generate and view the report:

```bash
mvn clean test
mvn allure:serve
```
