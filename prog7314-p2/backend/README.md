# NaviGo REST API (Tapiwa Sango ST10276025)

Backend for the NaviGo Android app — REST API, database, tests, and CI.

## Stack
- Node.js 18+ / Express 4
- SQLite via sqlite3
- express-validator (validation)
- Jest + Supertest (tests)

## Run locally
```bash
cd backend
npm install
npm run dev        # http://localhost:3000
npm test           # run tests
```
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


## Database
SQLite file is auto-created at `backend/data/navigo.db` on first run.
Tables: `routes`, `incidents`, `planned_trips`.
Firestore also used.


## Error responses
- `400 ValidationError` → `{ error, details: [{field, message}] }`
- `404 Not Found`
- `500 InternalServerError`

## CI
`.github/workflows/api-ci.yml` runs `npm test` on every push that touches `backend/`.

