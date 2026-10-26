const express = require('express');
const cors = require('cors');
const morgan = require('morgan');

const routesRouter = require('./routes/routes.routes');
const incidentsRouter = require('./routes/incidents.routes');
const plannedTripsRouter = require('./routes/plannedTrips.routes');
const { notFound, errorHandler } = require('./middleware/errorHandler');

const app = express();

app.use(cors());
app.use(express.json());
if (process.env.NODE_ENV !== 'test') {
  app.use(morgan('dev'));
}

app.get('/health', (req, res) => res.json({ status: 'ok', service: 'navigo-api' }));

app.use('/api/routes', routesRouter);
app.use('/api/incidents', incidentsRouter);
app.use('/api/planned-trips', plannedTripsRouter);

app.use(notFound);
app.use(errorHandler);

module.exports = app;