const request = require('supertest');
const app = require('../src/app');
const db = require('../src/db');

beforeEach(() => {
  db.prepare('DELETE FROM routes').run();
});

describe('Routes API', () => {
  const validRoute = {
    name: 'Home to Campus',
    origin: 'Braamfontein',
    destination: 'Wits West Campus',
    travel_mode: 'driving',
    distance_km: 4.2,
    duration_min: 12,
  };

  test('POST /api/routes creates a route', async () => {
    const res = await request(app).post('/api/routes').send(validRoute);
    expect(res.status).toBe(201);
    expect(res.body.name).toBe(validRoute.name);
  });

  test('POST /api/routes rejects invalid payload', async () => {
    const res = await request(app)
      .post('/api/routes')
      .send({ ...validRoute, travel_mode: 'teleport' });
    expect(res.status).toBe(400);
    expect(res.body.error).toBe('ValidationError');
  });

  test('GET /api/routes returns all', async () => {
    await request(app).post('/api/routes').send(validRoute);
    const res = await request(app).get('/api/routes');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(1);
  });

  test('GET /api/routes/:id returns 404 for missing', async () => {
    const res = await request(app).get('/api/routes/9999');
    expect(res.status).toBe(404);
  });

  test('PUT /api/routes/:id updates', async () => {
    const created = await request(app).post('/api/routes').send(validRoute);
    const res = await request(app)
      .put(`/api/routes/${created.body.id}`)
      .send({ ...validRoute, duration_min: 20 });
    expect(res.status).toBe(200);
    expect(res.body.duration_min).toBe(20);
  });

  test('DELETE /api/routes/:id removes', async () => {
    const created = await request(app).post('/api/routes').send(validRoute);
    const res = await request(app).delete(`/api/routes/${created.body.id}`);
    expect(res.status).toBe(204);
  });
});