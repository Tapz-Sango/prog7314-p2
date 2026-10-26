const db = require('../db');

const IncidentModel = {
  all({ status } = {}) {
    if (status) {
      return db
        .prepare('SELECT * FROM incidents WHERE status = ? ORDER BY id DESC')
        .all(status);
    }
    return db.prepare('SELECT * FROM incidents ORDER BY id DESC').all();
  },

  findById(id) {
    return db.prepare('SELECT * FROM incidents WHERE id = ?').get(id);
  },

  create(i) {
    const stmt = db.prepare(`
      INSERT INTO incidents (type, description, latitude, longitude, reported_by, status)
      VALUES (?, ?, ?, ?, ?, ?)
    `);
    const info = stmt.run(
      i.type,
      i.description,
      i.latitude ?? null,
      i.longitude ?? null,
      i.reported_by ?? null,
      'active'
    );
    return this.findById(info.lastInsertRowid);
  },

  update(id, i) {
    const stmt = db.prepare(`
      UPDATE incidents
         SET type = ?,
             description = ?,
             latitude = ?,
             longitude = ?,
             reported_by = ?,
             status = ?,
             updated_at = datetime('now')
       WHERE id = ?
    `);
    const info = stmt.run(
      i.type,
      i.description,
      i.latitude ?? null,
      i.longitude ?? null,
      i.reported_by ?? null,
      i.status,
      id
    );
    if (info.changes === 0) return null;
    return this.findById(id);
  },

  remove(id) {
    return db.prepare('DELETE FROM incidents WHERE id = ?').run(id).changes;
  },
};

module.exports = IncidentModel;