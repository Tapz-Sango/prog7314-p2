require('dotenv').config();
const app = require('./app');

const PORT = process.env.PORT || 3000;
const HOST = '0.0.0.0';

app.listen(PORT, HOST, () => {
  console.log(`NaviGo API listening on http://${HOST}:${PORT}`);
});