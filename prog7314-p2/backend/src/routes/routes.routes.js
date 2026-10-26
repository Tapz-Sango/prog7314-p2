const express = require('express');
const { body, param } = require('express-validator');
const validate = require('../middleware/validate');
const { HttpError } = require('../middleware/errorHandler');
const RouteModel = require('../models/route.model');

const router = express.Router();

const routeRules = [
  body('name').isString().trim().isLength({ min: 1, max: 100 }),
  body('origin').isString().trim().isLength({ min: 1, max: 200 }),
  body('destination').isString().trim().isLength({ min: 1, max: 200 }),
  body('travel_mode').isIn(['driving', 'walking', 'bicycling', 'bus', 'train']),
  body('distance_km').isFloat({ min: 0 }),
  body('duration_min').isInt({ min: 0 }),
];

router.get('/', (req, res) => res.json(RouteModel.all()));

router.get('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const route = RouteModel.findById(Number(req.params.id));
    if (!route) return next(new HttpError(404, 'Route not found'));
    res.json(route);
  }
);

router.post('/', routeRules, validate, (req, res) => {
  const created = RouteModel.create({
    name: req.body.name,
    origin: req.body.origin,
    destination: req.body.destination,
    travel_mode: req.body.travel_mode,
    distance_km: Number(req.body.distance_km),
    duration_min: Number(req.body.duration_min),
  });
  res.status(201).json(created);
});

router.put('/:id',
  param('id').isInt(),
  ...routeRules,
  validate,
  (req, res, next) => {
    const updated = RouteModel.update(Number(req.params.id), {
      name: req.body.name,
      origin: req.body.origin,
      destination: req.body.destination,
      travel_mode: req.body.travel_mode,
      distance_km: Number(req.body.distance_km),
      duration_min: Number(req.body.duration_min),
    });
    if (!updated) return next(new HttpError(404, 'Route not found'));
    res.json(updated);
  }
);

router.delete('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const removed = RouteModel.remove(Number(req.params.id));
    if (!removed) return next(new HttpError(404, 'Route not found'));
    res.status(204).send();
  }
);

module.exports = router;