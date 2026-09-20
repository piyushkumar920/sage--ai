const assert = require('assert');

const BASE_URL = process.env.BASE_URL || 'http://127.0.0.1:3001';

async function runSmokeTests() {
  console.log(`\n========================================================`);
  console.log(`Starting Sage Backend Smoke Tests against: ${BASE_URL}`);
  console.log(`========================================================\n`);

  // 1. GET /api/health
  {
    console.log('Test 1: GET /api/health');
    const res = await fetch(`${BASE_URL}/api/health`);
    assert.strictEqual(res.status, 200, 'Expected 200 from /api/health');
    const data = await res.json();
    assert.strictEqual(data.status, 'ok', 'Expected status ok');
    console.log('  -> PASS (Health status ok, model:', data.model, ')');
  }

  // 2. POST /api/chat
  {
    console.log('Test 2: POST /api/chat');
    const res = await fetch(`${BASE_URL}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        history: [{ role: 'user', text: 'Hello Sage, reply with OK.' }],
        mode: 'normal'
      })
    });
    assert.strictEqual(res.status, 200, 'Expected 200 from /api/chat');
    const data = await res.json();
    assert.strictEqual(data.success, true, 'Expected success: true');
    assert.ok(data.reply && data.reply.length > 0, 'Expected non-empty reply');
    console.log('  -> PASS (Chat reply received)');
  }

  // 3. POST /api/study-tools with operation=notes
  {
    console.log('Test 3: POST /api/study-tools (operation=notes)');
    const res = await fetch(`${BASE_URL}/api/study-tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation: 'notes',
        topic: 'Evolution Of operating system',
        subject: 'Operating system AM301',
        academicContext: {
          department: 'Computer Science',
          programme: 'B.Tech',
          regulation: 'R20',
          semester: 3,
          courseCode: 'AM301',
          courseName: 'Operating system',
          module: 'Unit 1',
          topic: 'Evolution Of operating system',
          officialSyllabusContent: 'Batch operating systems, Multi-programmed batch systems, Time-sharing systems, Distributed systems, Real-time systems.'
        }
      })
    });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    const body = await res.json();
    assert.strictEqual(body.success, true, 'Expected success: true');
    assert.strictEqual(body.operation, 'notes');
    assert.ok(body.data, 'Expected data object');
    assert.strictEqual(body.data.type, 'notes');
    assert.ok(body.data.title, 'Expected notes title');
    assert.ok(Array.isArray(body.data.sections), 'Expected sections array');
    console.log('  -> PASS (Notes generated with', body.data.sections.length, 'sections)');
  }

  // 4. POST /api/study-tools with operation=flashcards
  {
    console.log('Test 4: POST /api/study-tools (operation=flashcards)');
    const res = await fetch(`${BASE_URL}/api/study-tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation: 'flashcards',
        topic: 'CPU Scheduling Algorithms',
        subject: 'Operating Systems'
      })
    });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    const body = await res.json();
    assert.strictEqual(body.success, true);
    assert.strictEqual(body.operation, 'flashcards');
    assert.ok(Array.isArray(body.data.cards), 'Expected cards array');
    assert.ok(body.data.cards.length > 0, 'Expected at least 1 card');
    console.log('  -> PASS (Flashcards generated with', body.data.cards.length, 'cards)');
  }

  // 5. POST /api/study-tools with operation=mindmap
  {
    console.log('Test 5: POST /api/study-tools (operation=mindmap)');
    const res = await fetch(`${BASE_URL}/api/study-tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation: 'mindmap',
        topic: 'Process Synchronization',
        subject: 'Operating Systems'
      })
    });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    const body = await res.json();
    assert.strictEqual(body.success, true);
    assert.strictEqual(body.operation, 'mindmap');
    assert.ok(body.data.root, 'Expected root node in mindmap');
    assert.ok(body.data.root.label, 'Expected label in root node');
    console.log('  -> PASS (Mindmap generated with root label:', body.data.root.label, ')');
  }

  // 6. POST /api/study-tools with operation=revision
  {
    console.log('Test 6: POST /api/study-tools (operation=revision)');
    const res = await fetch(`${BASE_URL}/api/study-tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation: 'revision',
        topic: 'Deadlocks and Prevention',
        subject: 'Operating Systems'
      })
    });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    const body = await res.json();
    assert.strictEqual(body.success, true);
    assert.strictEqual(body.operation, 'revision');
    assert.ok(Array.isArray(body.data.coreConcepts), 'Expected coreConcepts');
    console.log('  -> PASS (Revision sheet generated with', body.data.coreConcepts.length, 'core concepts)');
  }

  // 7. POST /api/study-tools with operation=formulas
  {
    console.log('Test 7: POST /api/study-tools (operation=formulas)');
    const res = await fetch(`${BASE_URL}/api/study-tools`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        operation: 'formulas',
        topic: 'Disk Scheduling and Turnaround Time',
        subject: 'Operating Systems'
      })
    });
    assert.strictEqual(res.status, 200, `Expected 200, got ${res.status}`);
    const body = await res.json();
    assert.strictEqual(body.success, true);
    assert.strictEqual(body.operation, 'formulas');
    assert.ok(Array.isArray(body.data.formulas), 'Expected formulas array');
    console.log('  -> PASS (Formula sheet generated with', body.data.formulas.length, 'equations/rules)');
  }

  console.log('\n========================================================');
  console.log('ALL STUDY TOOLS SMOKE TESTS PASSED SUCCESSFULLY!');
  console.log('========================================================\n');
}

runSmokeTests().catch(err => {
  console.error('\nSMOKE TEST FAILURE:', err);
  process.exit(1);
});
