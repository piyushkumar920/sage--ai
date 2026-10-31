const { app, server } = require('../server/server');
const http = require('http');

function postJson(path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      hostname: '127.0.0.1',
      port: 8080,
      path: path,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data),
        ...headers
      }
    }, (res) => {
      let resBody = '';
      res.on('data', chunk => resBody += chunk);
      res.on('end', () => {
        try {
          const parsed = JSON.parse(resBody);
          resolve({ status: res.statusCode, body: parsed });
        } catch (e) {
          resolve({ status: res.statusCode, rawBody: resBody });
        }
      });
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function getJson(path, headers = {}) {
  return new Promise((resolve, reject) => {
    const req = http.request({
      hostname: '127.0.0.1',
      port: 8080,
      path: path,
      method: 'GET',
      headers: headers
    }, (res) => {
      let resBody = '';
      res.on('data', chunk => resBody += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(resBody) });
        } catch (e) {
          resolve({ status: res.statusCode, rawBody: resBody });
        }
      });
    });
    req.on('error', reject);
    req.end();
  });
}

function deleteJson(path, body = {}, headers = {}) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      hostname: '127.0.0.1',
      port: 8080,
      path: path,
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data),
        ...headers
      }
    }, (res) => {
      let resBody = '';
      res.on('data', chunk => resBody += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(resBody) });
        } catch (e) {
          resolve({ status: res.statusCode, rawBody: resBody });
        }
      });
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

let passed = 0;
let failed = 0;

function assert(condition, message) {
  if (condition) {
    console.log(`  ✓ ${message}`);
    passed++;
  } else {
    console.error(`  ✗ FAIL: ${message}`);
    failed++;
  }
}

async function runStage1Tests() {
  console.log('\n========================================');
  console.log('STAGE 1: UNIVERSAL STUDY ATTACHMENTS TEST SUITE');
  console.log('========================================\n');

  const aliceToken = 'test_token_alice_123';
  const bobToken = 'test_token_bob_456';
  const aliceHeaders = { 'Authorization': `Bearer ${aliceToken}` };
  const bobHeaders = { 'Authorization': `Bearer ${bobToken}` };

  // 1. Authentication rejection without token
  console.log('Test 1: Rejection of unauthenticated requests');
  const unauthRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_1',
    fileName: 'test.pdf',
    mimeType: 'application/pdf',
    fileBase64: Buffer.from('%PDF-1.4 test').toString('base64')
  });
  assert(unauthRes.status === 401, 'POST /api/attachments/upload without token returns 401 Unauthorized');

  // 2. Format validation: Unsupported file type
  console.log('\nTest 2: Validation of unsupported file format');
  const exeRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_1',
    fileName: 'virus.exe',
    mimeType: 'application/x-msdownload',
    fileBase64: Buffer.from('MZ...').toString('base64')
  }, aliceHeaders);
  assert(exeRes.status === 400 && exeRes.body.error.includes('Unsupported file'), 'Rejects .exe format with 400 Bad Request');

  // 3. Size limit: Reject > 15MB
  console.log('\nTest 3: File size limit enforcement (> 15MB)');
  const fakeOver15Mb = Buffer.alloc(10);
  const sizeRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_1',
    fileName: 'huge.pdf',
    mimeType: 'application/pdf',
    fileBase64: fakeOver15Mb.toString('base64'),
    fileSizeBytes: 16 * 1024 * 1024 // Claimed 16MB
  }, aliceHeaders);
  assert(sizeRes.status === 400 && sizeRes.body.error.includes('15MB'), 'Rejects files exceeding 15MB limit with 400');

  // 4. File signature verification (Corrupted PDF header)
  console.log('\nTest 4: Magic bytes validation against corrupted/spoofed files');
  const spoofedRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_1',
    fileName: 'spoofed.pdf',
    mimeType: 'application/pdf',
    fileBase64: Buffer.from('NOT_A_REAL_PDF_HEADER').toString('base64')
  }, aliceHeaders);
  assert(spoofedRes.status === 400 && (spoofedRes.body.code === 'INVALID_FILE_SIGNATURE' || spoofedRes.body.error.includes('magic bytes')), 'Rejects corrupted/spoofed PDF header');

  // 5. Successful Upload and Extraction (TXT file)
  console.log('\nTest 5: Successful upload and extraction for Alice in CHAT_CONVERSATION');
  const txtContent = "CS101 Algorithms\nUnit 1: Divide and Conquer.\nMerge Sort and Quick Sort time complexity analysis.";
  const uploadRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_alice_1',
    fileName: 'algorithms_unit1.txt',
    mimeType: 'text/plain',
    fileBase64: Buffer.from(txtContent, 'utf8').toString('base64')
  }, aliceHeaders);

  assert(uploadRes.status === 200 && uploadRes.body.success === true, 'Alice uploads txt attachment successfully');
  assert(uploadRes.body.attachmentId && uploadRes.body.documentId, 'Response contains attachmentId and documentId');
  assert(uploadRes.body.extractionSummary && uploadRes.body.extractedTextAvailability === true, 'Document extracted text is immediately available');

  const aliceAttId1 = uploadRes.body.attachmentId;
  const docId1 = uploadRes.body.documentId;

  // 6. Cross-User Isolation: Bob cannot access Alice's attachment
  console.log('\nTest 6: Cross-user privacy isolation');
  const bobGetRes = await getJson(`/api/attachments/${aliceAttId1}`, bobHeaders);
  assert(bobGetRes.status === 403, 'Bob cannot access Alice’s attachment (403 Forbidden)');

  // 7. Context Isolation: Context queries strictly scope to contextId
  console.log('\nTest 7: Context-scoped attachment querying');
  const conv1List = await getJson(`/api/contexts/CHAT_CONVERSATION/conv_alice_1/attachments`, aliceHeaders);
  assert(conv1List.status === 200 && conv1List.body.attachments.length === 1, 'conv_alice_1 returns exactly 1 attachment');
  assert(conv1List.body.attachments[0].attachmentId === aliceAttId1, 'Returned attachment matches aliceAttId1');

  const conv2List = await getJson(`/api/contexts/CHAT_CONVERSATION/conv_alice_2/attachments`, aliceHeaders);
  assert(conv2List.status === 200 && conv2List.body.attachments.length === 0, 'conv_alice_2 returns 0 attachments (strict context isolation)');

  // 8. Attaching existing document to another context (PERSONAL_ROADMAP)
  console.log('\nTest 8: Cross-context safe attachment reuse');
  const attachRes = await postJson('/api/attachments/attach', {
    documentId: docId1,
    contextType: 'PERSONAL_ROADMAP',
    contextId: 'roadmap_alice_ds'
  }, aliceHeaders);
  assert(attachRes.status === 200 && attachRes.body.success === true, 'Alice attaches existing document to roadmap context');
  const aliceAttId2 = attachRes.body.attachmentId;

  // Duplicate attach prevention
  const dupAttachRes = await postJson('/api/attachments/attach', {
    documentId: docId1,
    contextType: 'PERSONAL_ROADMAP',
    contextId: 'roadmap_alice_ds'
  }, aliceHeaders);
  assert(dupAttachRes.status === 200 && dupAttachRes.body.isExisting === true, 'Prevents duplicate attach records, returning existing attachment');

  // Bob cannot attach Alice's document
  const bobAttachRes = await postJson('/api/attachments/attach', {
    documentId: docId1,
    contextType: 'PERSONAL_ROADMAP',
    contextId: 'roadmap_bob_ds'
  }, bobHeaders);
  assert(bobAttachRes.status === 404 || bobAttachRes.status === 403, 'Bob cannot attach Alice’s document (unauthorized/not found)');

  // 9. Reference-aware deletion
  console.log('\nTest 9: Reference-safe context removal');
  // Remove from conv_alice_1
  const delConv1Res = await deleteJson(`/api/attachments/${aliceAttId1}`, { permanent: false }, aliceHeaders);
  assert(delConv1Res.status === 200 && delConv1Res.body.success === true, 'Successfully removes attachment from conv_alice_1');
  assert(delConv1Res.body.documentStillReferenced === true, 'Source document remains referenced in roadmap context');

  // Confirm conv_alice_1 now returns 0 active attachments
  const conv1After = await getJson(`/api/contexts/CHAT_CONVERSATION/conv_alice_1/attachments`, aliceHeaders);
  assert(conv1After.status === 200 && conv1After.body.attachments.length === 0, 'conv_alice_1 has 0 active attachments after removal');

  // Confirm roadmap context STILL has active attachment
  const roadmapList = await getJson(`/api/contexts/PERSONAL_ROADMAP/roadmap_alice_ds/attachments`, aliceHeaders);
  assert(roadmapList.status === 200 && roadmapList.body.attachments.length === 1, 'Roadmap context preserves attachment access to underlying document');

  // 10. Valid PDF magic bytes upload
  console.log('\nTest 10: PDF upload with valid %PDF- magic bytes');
  const validPdfRes = await postJson('/api/attachments/upload', {
    contextType: 'ROADMAP_TOPIC',
    contextId: 'topic_trees',
    fileName: 'binary_trees.pdf',
    mimeType: 'application/pdf',
    fileBase64: Buffer.from('%PDF-1.4\n1 0 obj\n<<>>\nendobj\ntrailer\n<<>>\n%%EOF').toString('base64')
  }, aliceHeaders);
  assert(validPdfRes.status === 200 && validPdfRes.body.success === true, 'PDF with valid magic bytes uploaded successfully');

  console.log('\n========================================');
  console.log(`STAGE 1 AUDIT RESULTS: Passed: ${passed}, Failed: ${failed}`);
  console.log('========================================\n');

  if (server && server.close) {
    server.close();
  }

  process.exit(failed === 0 ? 0 : 1);
}

runStage1Tests().catch(err => {
  console.error("Fatal test error:", err);
  process.exit(1);
});
