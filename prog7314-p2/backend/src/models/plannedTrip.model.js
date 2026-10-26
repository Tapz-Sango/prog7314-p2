const db = require('../db');

const PlannedTripModel = {
  all({ userId } = {}) {
    if (userId) {
      return db
        .prepare('SELECT * FROM planned_trips WHERE user_id = ? ORDER BY id DESC')
        .all(userId);
    }
    return db.prepare('SELECT * FROM planned_trips ORDER BY id DESC').all();
  },

  findById(id) {
    return db.prepare('SELECT * FROM planned_trips WHERE id = ?').get(id);
  },

  create(t) {
    const stmt = db.prepare(`
      INSERT INTO planned_trips (user_id, name, destination, date, time, mode)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    const info = stmt.run(
      t.user_id,
      t.name,
      t.destination,
      t.date,
      t.time,
      t.mode
    );
    return this.findById(info.lastInsertRowid);
  },

  update(id, t) {
    const stmt = db.prepare(`
      UPDATE planned_trips
         SET user_id = ?,
             name = ?,
             destination = ?,
             date = ?,
             time = ?,
             mode = ?,
             updated_at = datetime('now')
       WHERE id = ?
    `);
    const info = stmt.run(
      t.user_id,
      t.name,
      t.destination,
      t.date,
      t.time,
      t.mode,
      id
    );
    if (info.changes === 0) return null;
    return this.findById(id);
  },

  remove(id) {
    return db.prepare('DELETE FROM planned_trips WHERE id = ?').run(id).changes;
  },
};

module.exports = PlannedTripModel;