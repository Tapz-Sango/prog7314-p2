const express = require('express');
const { body, param, query } = require('express-validator');
const validate = require('../middleware/validate');
const { HttpError } = require('../middleware/errorHandler');
const IncidentModel = require('../models/incident.model');

const router = express.Router();

const incidentRules = [
  body('type').isIn(['accident', 'road_blockage', 'traffic', 'road_hazard']),
  body('description').isString().trim().isLength({ min: 10, max: 500 }),
  body('latitude').optional({ nullable: true }).isFloat({ min: -90, max: 90 }),
  body('longitude').optional({ nullable: true }).isFloat({ min: -180, max: 180 }),
  body('reported_by').optional({ nullable: true }).isString().trim().isLength({ max: 100 }),
];

router.get('/',
  query('status').optional().isIn(['active', 'resolved']),
  validate,
  (req, res) => res.json(IncidentModel.all({ status: req.query.status }))
);

router.get('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const incident = IncidentModel.findById(Number(req.params.id));
    if (!incident) return next(new HttpError(404, 'Incident not found'));
    res.json(incident);
  }
);

router.post('/', incidentRules, validate, (req, res) => {
  const created = IncidentModel.create({
    type: req.body.type,
    description: req.body.description,
    latitude: req.body.latitude,
    longitude: req.body.longitude,
    reported_by: req.body.reported_by,
  });
  res.status(201).json(created);
});

router.put('/:id',
  param('id').isInt(),
  ...incidentRules,
  body('status').isIn(['active', 'resolved']),
  validate,
  (req, res, next) => {
    const updated = IncidentModel.update(Number(req.params.id), {
      type: req.body.type,
      description: req.body.description,
      latitude: req.body.latitude,
      longitude: req.body.longitude,
      reported_by: req.body.reported_by,
      status: req.body.status,
    });
    if (!updated) return next(new HttpError(404, 'Incident not found'));
    res.json(updated);
  }
);

router.patch('/:id/resolve',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const existing = IncidentModel.findById(Number(req.params.id));
    if (!existing) return next(new HttpError(404, 'Incident not found'));
    const updated = IncidentModel.update(Number(req.params.id), {
      ...existing,
      status: 'resolved',
    });
    res.json(updated);
  }
);

router.delete('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const removed = IncidentModel.remove(Number(req.params.id));
    if (!removed) return next(new HttpError(404, 'Incident not found'));
    res.status(204).send();
  }
);

module.exports = router;