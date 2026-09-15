const assert = require('assert');

const BASE_URL = process.env.BASE_URL || 'http://localhost:8080';

async function runTests() {
  console.log(`Running Sage AI Backend regression tests against: ${BASE_URL}`);

  // Test 1: GET /api/health
  {
    console.log('Test 1: GET /api/health');
    const res = await fetch(`${BASE_URL}/api/health`);
    assert.strictEqual(res.status, 200, 'Expected 200 from /api/health');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const data = await res.json();
    assert.strictEqual(data.status, 'ok');
    assert.strictEqual(data.service, 'sage-backend-api');
    assert.strictEqual(data.model, 'gemini-3.5-flash');
    console.log('  -> PASS');
  }

  // Test 2: GET /api/health?checkGemini=true
  {
    console.log('Test 2: GET /api/health?checkGemini=true');
    const res = await fetch(`${BASE_URL}/api/health?checkGemini=true`);
    assert.strictEqual(res.status, 200, 'Expected 200 from Gemini health check');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const data = await res.json();
    assert.strictEqual(data.status, 'ok');
    assert.strictEqual(data.geminiConnected, true);
    assert.strictEqual(data.service, 'sage-backend-api');
    console.log('  -> PASS (Gemini connected: true)');
  }

  // Test 3: POST /api/chat
  {
    console.log('Test 3: POST /api/chat');
    const res = await fetch(`${BASE_URL}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        history: [{ role: 'user', text: 'What is 5+5? Answer in one word.' }],
        mode: 'normal'
      })
    });
    assert.strictEqual(res.status, 200, 'Expected 200 from chat');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const data = await res.json();
    assert.strictEqual(data.success, true);
    assert.ok(typeof data.reply === 'string' && data.reply.trim().length > 0, 'Expected non-empty reply');
    console.log(`  -> PASS (Reply: "${data.reply.trim()}")`);
  }

  // Test 4: Unknown API route (GET) - Guarantee JSON, NEVER HTML
  {
    console.log('Test 4: GET /api/this-endpoint-does-not-exist');
    const res = await fetch(`${BASE_URL}/api/this-endpoint-does-not-exist`);
    assert.strictEqual(res.status, 404, 'Expected 404 for unknown endpoint');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const rawText = await res.text();
    assert.ok(!rawText.includes('<html') && !rawText.includes('<!doctype'), 'Must NOT contain HTML markup');
    const data = JSON.parse(rawText);
    assert.strictEqual(data.code, 'NOT_FOUND');
    console.log('  -> PASS (Returned 404 JSON, zero HTML)');
  }

  // Test 5: Unknown API route (POST) - Guarantee JSON, NEVER HTML
  {
    console.log('Test 5: POST /api/invalid-action');
    const res = await fetch(`${BASE_URL}/api/invalid-action`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ foo: 'bar' })
    });
    assert.strictEqual(res.status, 404, 'Expected 404 for invalid route');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const rawText = await res.text();
    assert.ok(!rawText.includes('<html') && !rawText.includes('<!doctype'), 'Must NOT contain HTML markup');
    const data = JSON.parse(rawText);
    assert.strictEqual(data.code, 'NOT_FOUND');
    console.log('  -> PASS (Returned 404 JSON, zero HTML)');
  }

  // Test 6: Invalid Chat Request (400 Bad Request)
  {
    console.log('Test 6: POST /api/chat with invalid payload');
    const res = await fetch(`${BASE_URL}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ history: [] })
    });
    assert.strictEqual(res.status, 400, 'Expected 400 Bad Request');
    const ct = res.headers.get('content-type') || '';
    assert.ok(ct.includes('application/json'), `Expected JSON Content-Type, got ${ct}`);
    const data = await res.json();
    assert.strictEqual(data.success, false);
    assert.strictEqual(data.code, 'BAD_REQUEST');
    console.log('  -> PASS (Returned 400 JSON)');
  }

  console.log('\n========================================================');
  console.log('ALL REGRESSION TESTS PASSED! No HTML is ever returned.');
  console.log('========================================================');
}

runTests().catch(err => {
  console.error('\nREGRESSION TEST FAILED:', err);
  process.exit(1);
});
