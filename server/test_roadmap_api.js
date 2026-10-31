const test = require('node:test');
const assert = require('node:assert/strict');
const http = require('http');

// Load app
const { app, server } = require('./server');

// Close the listen server immediately for test isolation
if (server && server.close) {
  server.close();
}

// Helper to make requests to the express app using an ephemeral HTTP server
function makeRequest(appInstance, options, body = null) {
  return new Promise((resolve, reject) => {
    const testServer = http.createServer(appInstance);
    testServer.listen(0, '127.0.0.1', () => {
      const port = testServer.address().port;
      const reqOptions = {
        hostname: '127.0.0.1',
        port: port,
        path: options.path,
        method: options.method || 'GET',
        headers: options.headers || {}
      };

      const req = http.request(reqOptions, (res) => {
        let rawData = '';
        res.on('data', chunk => rawData += chunk);
        res.on('end', () => {
          testServer.close();
          let json = null;
          try {
            json = JSON.parse(rawData);
          } catch (e) {
            json = rawData;
          }
          resolve({ status: res.statusCode, headers: res.headers, body: json });
        });
      });

      req.on('error', (err) => {
        testServer.close();
        reject(err);
      });

      if (body) {
        req.write(typeof body === 'string' ? body : JSON.stringify(body));
      }
      req.end();
    });
  });
}

test('SAGE BACKEND: Route registration verification', async () => {
  const res = await makeRequest(app, {
    path: '/api/debug/routes',
    method: 'GET'
  });

  assert.equal(res.status, 200, 'Expected /api/debug/routes to return 200 OK');
  assert.ok(res.body.routes, 'Expected routes list in response');
  assert.ok(res.body.routes.includes('POST /api/roadmaps/generate'), 'POST /api/roadmaps/generate must be registered');
  assert.ok(res.body.routes.includes('POST /api/chat'), 'POST /api/chat must remain registered');
  assert.ok(res.body.routes.some(r => r.includes('health')), 'Health route must remain registered');
});

test('SAGE BACKEND: Unauthenticated roadmap request is rejected with 401', async () => {
  const res = await makeRequest(app, {
    path: '/api/roadmaps/generate',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, { goal: 'Learn Rust programming' });

  assert.equal(res.status, 401, 'Should return HTTP 401 Unauthorized');
  assert.equal(res.body.success, false);
  assert.equal(res.body.code, 'UNAUTHORIZED');
  assert.ok(res.body.requestId, 'Response must include requestId');
});

test('SAGE BACKEND: Invalid token rejected with 401', async () => {
  const res = await makeRequest(app, {
    path: '/api/roadmaps/generate',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer invalid.jwt.token'
    }
  }, { goal: 'Learn Go programming' });

  assert.equal(res.status, 401, 'Should return HTTP 401 Unauthorized');
  assert.equal(res.body.success, false);
  assert.equal(res.body.code, 'UNAUTHORIZED');
});

test('SAGE BACKEND: Missing goal rejected with 400', async () => {
  const res = await makeRequest(app, {
    path: '/api/roadmaps/generate',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer test_token_user_123'
    }
  }, {});

  assert.equal(res.status, 400, 'Should return HTTP 400 Bad Request');
  assert.equal(res.body.success, false);
  assert.equal(res.body.code, 'INVALID_REQUEST');
  assert.ok(res.body.error.includes('Goal is required'));
});

test('SAGE BACKEND: Empty goal rejected with 400', async () => {
  const res = await makeRequest(app, {
    path: '/api/roadmaps/generate',
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer test_token_user_123'
    }
  }, { goal: '   ' });

  assert.equal(res.status, 400, 'Should return HTTP 400 Bad Request');
  assert.equal(res.body.success, false);
  assert.equal(res.body.code, 'INVALID_REQUEST');
});

test('SAGE BACKEND: Regression check - /api/chat still functions', async () => {
  const res = await makeRequest(app, {
    path: '/api/chat',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, {});

  assert.equal(res.status, 400, 'Chat without history returns 400 Bad Request');
  assert.equal(res.body.code, 'BAD_REQUEST');
});

test('SAGE BACKEND: Regression check - /api/study-tools still functions', async () => {
  const res = await makeRequest(app, {
    path: '/api/study-tools',
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  }, {});

  assert.equal(res.status, 400, 'Study tools without operation returns 400');
  assert.equal(res.body.code, 'INVALID_REQUEST');
});

test('SAGE BACKEND: Unmatched route still returns 404 JSON', async () => {
  const res = await makeRequest(app, {
    path: '/api/nonexistent-route',
    method: 'GET'
  });

  assert.equal(res.status, 404);
  assert.equal(res.body.code, 'NOT_FOUND');
  assert.equal(res.body.error, 'API route not found');
});

test('SAGE BACKEND: Successful roadmap generation with mocked Gemini', async () => {
  const originalFetch = global.fetch;
  const mockRoadmap = {
    id: "personal_1730335200000",
    title: "Mastering Rust Systems Programming",
    description: "A comprehensive roadmap from syntax basics to concurrent network servers in Rust.",
    icon: "🦀",
    difficulty: "Intermediate",
    time: "3 months",
    scheduleTarget: "2 hours / day",
    targetCompletion: "3 months from today",
    categories: ["fundamentals", "ownership_borrowing", "concurrency", "projects"],
    nodes: [
      {
        id: "node_1",
        title: "Rust Fundamentals & Toolchain",
        icon: "⚙️",
        category: "fundamentals",
        description: "Variables, basic types, functions, cargo toolchain, and basic control flow.",
        difficulty: "Beginner",
        resources: [
          { title: "The Rust Book", url: "https://doc.rust-lang.org/book/", type: "docs", free: true }
        ],
        children: ["node_2"]
      },
      {
        id: "node_2",
        title: "Ownership, Borrowing & Lifetimes",
        icon: "🔒",
        category: "ownership_borrowing",
        description: "Understanding stack vs heap, move semantics, mutable references, and lifetime elision rules.",
        difficulty: "Intermediate",
        resources: [
          { title: "Rust by Example", url: "https://doc.rust-lang.org/rust-by-example/", type: "docs", free: true }
        ],
        children: []
      }
    ]
  };

  global.fetch = async (url) => {
    return {
      ok: true,
      status: 200,
      json: async () => ({
        candidates: [
          {
            content: {
              parts: [{ text: JSON.stringify(mockRoadmap) }]
            }
          }
        ]
      })
    };
  };

  try {
    const res = await makeRequest(app, {
      path: '/api/roadmaps/generate',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer test_token_usr_rust_learner'
      }
    }, {
      goal: 'Mastering Rust Systems Programming',
      duration: '3 months',
      preferences: {
        difficulty: 'Intermediate',
        dailyHours: 2
      }
    });

    assert.equal(res.status, 200, 'Expected 200 OK for successful roadmap generation');
    assert.equal(res.body.success, true);
    assert.equal(res.body.ok, true);
    assert.equal(res.body.operation, 'roadmap');
    assert.ok(res.body.data, 'Expected data object');
    assert.equal(res.body.data.title, 'Mastering Rust Systems Programming');
    assert.equal(res.body.data.icon, '🦀');
    assert.equal(res.body.data.nodes.length, 2);
    assert.equal(res.body.data.nodes[0].id, 'node_1');
    assert.equal(res.body.data.nodes[0].resources[0].title, 'The Rust Book');
    assert.ok(res.body.requestId);
  } finally {
    global.fetch = originalFetch;
  }
});
