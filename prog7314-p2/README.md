# Tinodiwanashe Dzoro [ST10402234] - Member 2: Authentication, User Data & App Preferences

## Implementation Overview

* **Firebase Authentication Integration:** Configured Firebase Auth within an architectural repository pattern (`AuthRepository`) to manage secure user sessions.
* **SSO / Google Sign-In:** Integrated Google Sign-In services to streamline authentication alongside traditional credentials.
* **Registration and Login Flows:** Implemented asynchronous registration and login logic with complete error handling and input validation for user credentials.
* **Session Management and Logout:** Implemented startup checks to guard activities (e.g., verifying active sessions in `HomeActivity`) and built a complete session-clearing logout mechanism from the settings interface.
* **User Profile & Settings Functionality:** Centralized application configuration, preference toggles, and account management inside `SettingsActivity`.
* **Hybrid Cloud-Sync Architecture:** Implemented an offline-first data persistence pattern, pairing local SharedPreferences JSON caching with remote Firebase Firestore synchronization using the authenticated user's unique identifier (UID).
* **Preferred Transport Settings:** Stored user-preferred transport modes persistently using local `SharedPreferences`.
* **Saved Locations & Search History:** Implemented local persistence using `JSONArray` storage in `SearchActivity` to record, display, and quickly select recent search destinations.
* **Planned-Trip User Data:** Developed full CRUD capabilities for planned trips in `PlannedTripsActivity`, complete with future-date validation, interactive dialogs, and JSON serialization.
* **Device Testing:** Validated all authentication workflows, input validation checks, and persistent storage mechanisms on physical Android hardware.

---

## AI Declaration

### Utilization Statement

Artificial Intelligence tools were utilized throughout the development lifecycle to assist with architectural design, debug asynchronous threading exceptions, refine user-input validation rules, and optimize local JSON persistence logic. The AI functioned as a collaborative technical assistant to accelerate implementation and ensure adherence to Android development best practices.

### Specific Examples of AI Assistance

1. **Asynchronous Threading & Geocoding:** Assisted in structuring background execution pools (`Executors.newSingleThreadExecutor`) within `SearchActivity` to prevent main-thread blocking during location geocoding lookups.
2. **Persistent JSON Serialization:** Designed and refined the serialization and deserialization routines using `JSONObject` and `JSONArray` for local `SharedPreferences` management in `PlannedTripsActivity`.
3. **Edge-to-Edge Window Insets:** Resolved system-bar padding conflicts by implementing modern `WindowInsetsCompat` application listeners across core activities to handle display cutouts and navigation bars cleanly.

# Tapiwa Sango ST10276025 - Member 3: REST API, Database, Testing & DevOps

## Implementation Overview

* **Custom REST API Development:** Designed and implemented a standalone RESTful API using Node.js and Express, exposing three resource groups (`/api/routes`, `/api/incidents`, `/api/planned-trips`) with clean separation of routing, model, and middleware layers.
* **API Models & Data Layer:** Created modular data-access models (`route.model.js`, `incident.model.js`, `plannedTrip.model.js`) that encapsulate all SQL operations and expose a consistent CRUD surface to the route handlers.
* **Database Setup & Connection:** Provisioned a lightweight SQLite database using Node's built-in `node:sqlite` module, with schema auto-initialisation on boot and enforced CHECK constraints, foreign keys, and indexes for data integrity.
* **Full CRUD Operations:** Implemented complete Create, Read, Update, and Delete endpoints for all three resources, including a dedicated `PATCH /api/incidents/:id/resolve` action for lifecycle transitions.
* **Route-Related Endpoints:** Built endpoints to persist and retrieve route previews including origin, destination, travel mode, distance, and duration, supporting the app's Route Comparison and Route Details screens.
* **Incident-Related Endpoints:** Delivered endpoints for community incident reporting, status filtering (`active` / `resolved`), geolocation capture, and incident resolution workflows.
* **Planned-Trip Endpoints:** Implemented per-user planned-trip persistence with `userId` filtering, supporting the Android app's offline-first hybrid cloud-sync architecture.
* **Validation & Error Handling:** Applied declarative request validation using `express-validator` across every endpoint, with a centralised error-handling middleware returning structured JSON errors (`400 ValidationError`, `404 Not Found`, `500 InternalServerError`).
* **Deployment & Hosting:** Deployed the API to Replit's cloud platform with a public HTTPS endpoint, environment-variable-driven configuration, and a dedicated production run command.
* **Continuous Integration:** Configured a GitHub Actions workflow that triggers on every push touching `backend/`, installs dependencies, and executes the full Jest test suite automatically.
* **Automated Testing:** Authored sixteen Jest + Supertest integration tests covering all CRUD operations, validation rejection paths, filtering logic, and lifecycle transitions, achieving full endpoint coverage.
* **Documentation:** Produced complete API reference, database schema documentation, testing guide, and CI/CD instructions inside `backend/README.md` and the project root README.
* Live API
- **Public URL:** https://navigo-apizip--t6102412.replit.app
- **Health check:** https://navigo-apizip--t6102412.replit.app/health
- **API docs:** see `backend/README.md`

---

## AI Declaration

### Utilization Statement

Artificial Intelligence tools were utilized throughout the development lifecycle to assist with API architecture design, database schema normalisation, request validation rule construction, and CI/CD pipeline configuration. The AI functioned as a collaborative technical assistant to accelerate backend implementation, resolve runtime compatibility issues across Node.js versions, and ensure adherence to RESTful API best practices.

### Specific Examples of AI Assistance

1. **Node.js Runtime Compatibility:** Diagnosed and resolved the `ERR_UNKNOWN_BUILTIN_MODULE: node:sqlite` error by migrating the deployment runtime from Node.js 20 to Node.js 24, and replacing the third-party `better-sqlite3` dependency with Node's built-in SQLite module to eliminate native compilation requirements.
2. **RESTful Endpoint Design & Validation Schemas:** Assisted in structuring the three Express routers with declarative `express-validator` rule chains, ensuring consistent request validation, parameter coercion, and structured error responses across all endpoints.
3. **CI/CD Pipeline Construction:** Assisted in authoring the GitHub Actions workflow (`.github/workflows/api-ci.yml`) with path-based trigger filters, dependency caching, Node.js version pinning, and isolated test database configuration via environment variables.

# Navigo API - Testing Guide (Postman)

This guide covers testing the Navigo API both locally and against the hosted deployment using Postman.

## Setup

### Base URLs

| Environment | Base URL |
|-------------|----------|
| Local | `http://localhost:3000` |
| Hosted | `https://navigo-api.onrender.com` *(replace with your actual hosted URL)* |

### Postman Environment Variables (Recommended)

Create a Postman environment with these variables so you can switch between local and hosted with one click:

| Variable | Local Value | Hosted Value |
|----------|-------------|--------------|
| `baseUrl` | `http://localhost:3000` | `https://navigo-api.onrender.com` |

Then use `{{baseUrl}}` in every request URL below.

### Headers

For all POST and PATCH requests, set:

```
Content-Type: application/json
```

---

## 1. Health Check

**Method:** `GET`

**URL:** `{{baseUrl}}/health`

**Body:** none

**Expected Response (200 OK):**

```json
{"status":"ok","service":"navigo-api"}
```

---

## 2. Create a Route

**Method:** `POST`

**URL:** `{{baseUrl}}/api/routes`

**Headers:** `Content-Type: application/json`

**Body (raw → JSON):**

```json
{
  "name": "Home to Campus",
  "origin": "Braamfontein",
  "destination": "Wits West",
  "travel_mode": "driving",
  "distance_km": 4.2,
  "duration_min": 12
}
```

**Expected:** `201 Created` with the route object.

---

## 3. List Routes

**Method:** `GET`

**URL:** `{{baseUrl}}/api/routes`

**Body:** none

**Expected:** An array containing your route.

---

## 4. Create an Incident

**Method:** `POST`

**URL:** `{{baseUrl}}/api/incidents`

**Headers:** `Content-Type: application/json`

**Body (raw → JSON):**

```json
{
  "type": "traffic",
  "description": "Heavy traffic on the M1 north",
  "latitude": -26.2041,
  "longitude": 28.0473
}
```

**Expected:** `201 Created` with the incident object. Note the `id` in the response — you'll need it for step 5.

---

## 5. Resolve an Incident

Replace `1` with the `id` returned from step 4.

**Method:** `PATCH`

**URL:** `{{baseUrl}}/api/incidents/1/resolve`

**Body:** none

**Expected:** `200 OK` with the updated incident (status marked as resolved).

---

## 6. Create a Planned Trip

**Method:** `POST`

**URL:** `{{baseUrl}}/api/planned-trips`

**Headers:** `Content-Type: application/json`

**Body (raw → JSON):**

```json
{
  "user_id": "uid-123",
  "name": "Trip to varsity",
  "destination": "Wits West",
  "date": "05 Jan 2027",
  "time": "07:30",
  "mode": "Bus"
}
```

**Expected:** `201 Created` with the planned trip object.

---

## 7. Trigger a Validation Error (on purpose)

**Method:** `POST`

**URL:** `{{baseUrl}}/api/routes`

**Headers:** `Content-Type: application/json`

**Body (raw → JSON):**

```json
{
  "name": "Bad",
  "origin": "A",
  "destination": "B",
  "travel_mode": "teleport",
  "distance_km": 1,
  "duration_min": 1
}
```

**Expected:**

```json
{"error":"ValidationError","details":[...]}
```

This confirms that the API correctly rejects invalid `travel_mode` values.

---

## Quick Reference

| # | Endpoint | Method | Purpose |
|---|----------|--------|---------|
| 1 | `/health` | GET | Health check |
| 2 | `/api/routes` | POST | Create a route |
| 3 | `/api/routes` | GET | List all routes |
| 4 | `/api/incidents` | POST | Create an incident |
| 5 | `/api/incidents/:id/resolve` | PATCH | Resolve an incident |
| 6 | `/api/planned-trips` | POST | Create a planned trip |
| 7 | `/api/routes` | POST | Trigger validation error (invalid `travel_mode`) |

---

## Notes

- Always set `Content-Type: application/json` for `POST` and `PATCH` requests.
- Use the `id` returned in step 4 when testing step 5.
- To switch environments, simply change the `baseUrl` variable in your Postman environment.
