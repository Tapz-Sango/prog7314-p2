const path = require('path');
const fs = require('fs');
const { DatabaseSync } = require('node:sqlite');

const DB_PATH =
  process.env.DB_PATH ||
  path.join(__dirname, '..', 'data', 'navigo.db');

fs.mkdirSync(path.dirname(DB_PATH), { recursive: true });

const db = new DatabaseSync(DB_PATH);

db.exec('PRAGMA foreign_keys = ON;');

// ---------- Schema ----------
db.exec(`
  CREATE TABLE IF NOT EXISTS routes (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    origin        TEXT    NOT NULL,
    destination   TEXT    NOT NULL,
    travel_mode   TEXT    NOT NULL CHECK (travel_mode IN ('driving','walking','bicycling','bus','train')),
    distance_km   REAL    NOT NULL CHECK (distance_km >= 0),
    duration_min  INTEGER NOT NULL CHECK (duration_min >= 0),
    created_at    TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at    TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS incidents (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    type         TEXT    NOT NULL CHECK (type IN ('accident','road_blockage','traffic','road_hazard')),
    description  TEXT    NOT NULL,
    latitude     REAL,
    longitude    REAL,
    reported_by  TEXT,
    status       TEXT    NOT NULL DEFAULT 'active' CHECK (status IN ('active','resolved')),
    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE TABLE IF NOT EXISTS planned_trips (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      TEXT    NOT NULL,
    name         TEXT    NOT NULL,
    destination  TEXT    NOT NULL,
    date         TEXT    NOT NULL,
    time         TEXT    NOT NULL,
    mode         TEXT    NOT NULL,
    created_at   TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at   TEXT    NOT NULL DEFAULT (datetime('now'))
  );

  CREATE INDEX IF NOT EXISTS idx_planned_trips_user ON planned_trips(user_id);
  CREATE INDEX IF NOT EXISTS idx_incidents_status  ON incidents(status);
`);

module.exports = db;