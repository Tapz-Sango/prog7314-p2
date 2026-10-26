const request = require('supertest');
const app = require('../src/app');
const db = require('../src/db');

beforeEach(() => {
  db.prepare('DELETE FROM incidents').run();
});

describe('Incidents API', () => {
  const validIncident = {
    type: 'accident',
    description: 'Two-car collision blocking the left lane',
    latitude: -26.2041,
    longitude: 28.0473,
    reported_by: 'test-user',
  };

  test('POST creates an incident with active status', async () => {
    const res = await request(app).post('/api/incidents').send(validIncident);
    expect(res.status).toBe(201);
    expect(res.body.status).toBe('active');
  });

  test('POST rejects short description', async () => {
    const res = await request(app)
      .post('/api/incidents')
      .send({ ...validIncident, description: 'short' });
    expect(res.status).toBe(400);
  });

  test('GET filters by status', async () => {
    await request(app).post('/api/incidents').send(validIncident);
    const res = await request(app).get('/api/incidents?status=active');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(1);
  });

  test('PATCH /:id/resolve marks incident resolved', async () => {
    const created = await request(app).post('/api/incidents').send(validIncident);
    const res = await request(app).patch(`/api/incidents/${created.body.id}/resolve`);
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('resolved');
  });

  test('DELETE removes incident', async () => {
    const created = await request(app).post('/api/incidents').send(validIncident);
    const res = await request(app).delete(`/api/incidents/${created.body.id}`);
    expect(res.status).toBe(204);
  });
});