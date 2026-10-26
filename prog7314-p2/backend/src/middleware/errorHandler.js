function notFound(req, res) {
  res.status(404).json({
    error: 'Not Found',
    message: `Route ${req.method} ${req.originalUrl} does not exist`,
  });
}

function errorHandler(err, req, res, next) {
  const status = err.status || 500;

  // Only log to console when NOT running tests, so Jest output stays clean.
  if (process.env.NODE_ENV !== 'test') {
    console.error('[API error]', err.message);
  }

  res.status(status).json({
    error: err.name || 'InternalServerError',
    message: err.message || 'Something went wrong',
  });
}

class HttpError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
    this.name = 'HttpError';
  }
}

module.exports = { notFound, errorHandler, HttpError };