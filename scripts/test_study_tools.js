const http = require('http');

const data = JSON.stringify({
  operation: 'notes',
  topic: 'Test'
});

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/study-tools',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Content-Length': data.length
  }
};

const req = http.request(options, (res) => {
  let resData = '';
  res.on('data', (chunk) => resData += chunk);
  res.on('end', () => console.log(resData));
});
req.on('error', (e) => console.error(e));
req.write(data);
req.end();
