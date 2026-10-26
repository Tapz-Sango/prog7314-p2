const express = require('express');
const { body, param, query } = require('express-validator');
const validate = require('../middleware/validate');
const { HttpError } = require('../middleware/errorHandler');
const PlannedTripModel = require('../models/plannedTrip.model');

const router = express.Router();

const tripRules = [
  body('user_id').isString().trim().isLength({ min: 1, max: 128 }),
  body('name').isString().trim().isLength({ min: 1, max: 60 }),
  body('destination').isString().trim().isLength({ min: 1, max: 100 }),
  body('date').matches(/^\d{2} \w{3} \d{4}$/),
  body('time').matches(/^\d{2}:\d{2}$/),
  body('mode').isIn(['Driving', 'Walking', 'Cycling', 'Bus', 'Train']),
];

router.get('/',
  query('userId').optional().isString().trim(),
  validate,
  (req, res) => res.json(PlannedTripModel.all({ userId: req.query.userId }))
);

router.get('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const trip = PlannedTripModel.findById(Number(req.params.id));
    if (!trip) return next(new HttpError(404, 'Planned trip not found'));
    res.json(trip);
  }
);

router.post('/', tripRules, validate, (req, res) => {
  const created = PlannedTripModel.create({
    user_id: req.body.user_id,
    name: req.body.name,
    destination: req.body.destination,
    date: req.body.date,
    time: req.body.time,
    mode: req.body.mode,
  });
  res.status(201).json(created);
});

router.put('/:id',
  param('id').isInt(),
  ...tripRules,
  validate,
  (req, res, next) => {
    const updated = PlannedTripModel.update(Number(req.params.id), {
      user_id: req.body.user_id,
      name: req.body.name,
      destination: req.body.destination,
      date: req.body.date,
      time: req.body.time,
      mode: req.body.mode,
    });
    if (!updated) return next(new HttpError(404, 'Planned trip not found'));
    res.json(updated);
  }
);

router.delete('/:id',
  param('id').isInt(),
  validate,
  (req, res, next) => {
    const removed = PlannedTripModel.remove(Number(req.params.id));
    if (!removed) return next(new HttpError(404, 'Planned trip not found'));
    res.status(204).send();
  }
);

module.exports = router;