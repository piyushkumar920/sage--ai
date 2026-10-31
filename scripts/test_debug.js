const http = require('http');

const options = {
  hostname: 'localhost', // Wait, localhost might be wrong, it's Render.
  port: 8080, // or the port the server is running on
  path: '/api/debug/routes',
  method: 'GET',
};

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', (chunk) => data += chunk);
  res.on('end', () => console.log(data));
});
req.on('error', (e) => console.error(e));
req.end();
