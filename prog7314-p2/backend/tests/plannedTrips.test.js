const request = require('supertest');
const app = require('../src/app');
const db = require('../src/db');

beforeEach(() => {
  db.prepare('DELETE FROM planned_trips').run();
});

describe('Planned Trips API', () => {
  const validTrip = {
    user_id: 'uid-123',
    name: 'Trip to varsity',
    destination: 'Wits West Campus',
    date: '05 Jan 2027',
    time: '07:30',
    mode: 'Bus',
  };

  test('POST creates a trip', async () => {
    const res = await request(app).post('/api/planned-trips').send(validTrip);
    expect(res.status).toBe(201);
    expect(res.body.name).toBe('Trip to varsity');
  });

  test('POST rejects malformed date', async () => {
    const res = await request(app)
      .post('/api/planned-trips')
      .send({ ...validTrip, date: '2027-01-05' });
    expect(res.status).toBe(400);
  });

  test('GET filters by userId', async () => {
    await request(app).post('/api/planned-trips').send(validTrip);
    await request(app).post('/api/planned-trips').send({ ...validTrip, user_id: 'someone-else' });
    const res = await request(app).get('/api/planned-trips?userId=uid-123');
    expect(res.status).toBe(200);
    expect(res.body).toHaveLength(1);
  });

  test('PUT updates trip', async () => {
    const created = await request(app).post('/api/planned-trips').send(validTrip);
    const res = await request(app)
      .put(`/api/planned-trips/${created.body.id}`)
      .send({ ...validTrip, mode: 'Train' });
    expect(res.status).toBe(200);
    expect(res.body.mode).toBe('Train');
  });

  test('DELETE removes trip', async () => {
    const created = await request(app).post('/api/planned-trips').send(validTrip);
    const res = await request(app).delete(`/api/planned-trips/${created.body.id}`);
    expect(res.status).toBe(204);
  });
});