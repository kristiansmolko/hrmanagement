const express = require('express');

const app = express();

app.get('/', function (req, res) {
  res.send('Hello world');
});

app.get('/home', (req, res) => {
  res.status(200).send('Home');
});

const server = app.listen(3000, function () {
  const host = server.address().address;
  const { port } = server.address();

  console.log('https://%s:%s', host, port);
});
