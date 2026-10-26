const db = require('../db');

const RouteModel = {
  all() {
    return db.prepare('SELECT * FROM routes ORDER BY id DESC').all();
  },

  findById(id) {
    return db.prepare('SELECT * FROM routes WHERE id = ?').get(id);
  },

  create(r) {
    const stmt = db.prepare(`
      INSERT INTO routes (name, origin, destination, travel_mode, distance_km, duration_min)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    const info = stmt.run(
      r.name,
      r.origin,
      r.destination,
      r.travel_mode,
      r.distance_km,
      r.duration_min
    );
    return this.findById(info.lastInsertRowid);
  },

  update(id, r) {
    const stmt = db.prepare(`
      UPDATE routes
         SET name = ?,
             origin = ?,
             destination = ?,
             travel_mode = ?,
             distance_km = ?,
             duration_min = ?,
             updated_at = datetime('now')
       WHERE id = ?
    `);
    const info = stmt.run(
      r.name,
      r.origin,
      r.destination,
      r.travel_mode,
      r.distance_km,
      r.duration_min,
      id
    );
    if (info.changes === 0) return null;
    return this.findById(id);
  },

  remove(id) {
    return db.prepare('DELETE FROM routes WHERE id = ?').run(id).changes;
  },
};

module.exports = RouteModel;