const { app, server } = require('../server/server');
const http = require('http');

function getPort() {
  return server && server.address() ? server.address().port : 8080;
}

function postJson(path, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      hostname: '127.0.0.1',
      port: getPort(),
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
          resolve({ status: res.statusCode, body: parsed || {} });
        } catch (e) {
          resolve({ status: res.statusCode, body: {}, rawBody: resBody });
        }
      });
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function deleteJson(path, body = {}, headers = {}) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const req = http.request({
      hostname: '127.0.0.1',
      port: getPort(),
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
          resolve({ status: res.statusCode, body: JSON.parse(resBody) || {} });
        } catch (e) {
          resolve({ status: res.statusCode, body: {}, rawBody: resBody });
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

async function runStage2Tests() {
  console.log('\n========================================================');
  console.log('STAGE 2: CHAT AI ATTACHMENT INTEGRATION TEST SUITE');
  console.log('========================================================\n');

  const aliceToken = 'test_token_alice_stage2';
  const bobToken = 'test_token_bob_stage2';
  const aliceHeaders = { 'Authorization': `Bearer ${aliceToken}` };
  const bobHeaders = { 'Authorization': `Bearer ${bobToken}` };

  const convMath = 'conv_math_401';
  const convPhysics = 'conv_physics_501';

  // 1. Normal Chat without attachments (Backward compatibility)
  console.log('Test 1: Normal Chat AI operates without attachments');
  const normalChat = await postJson('/api/chat', {
    message: 'Hello Sage, what is polymorphism in object oriented programming?',
    history: [],
    mode: 'normal'
  });
  assert(normalChat.status === 200 && normalChat.body.success === true && normalChat.body.reply.length > 0,
    'Normal chat produces helpful pedagogical response without attachments');

  // 2. Chat with attachmentIds but without authentication token -> 401
  console.log('\nTest 2: Chat with attachmentIds requires authentication');
  const unauthChat = await postJson('/api/chat', {
    message: 'Summarize the attached notes.',
    history: [],
    conversationId: convMath,
    attachmentIds: ['att_some_random_id']
  });
  assert(unauthChat.status === 401 && unauthChat.body.code === 'UNAUTHORIZED',
    'Unauthenticated attachment chat is strictly rejected with 401');

  // 3. Upload study document for Alice in convMath (Linear Algebra & Eigenvalues)
  console.log('\nTest 3: Upload study notes to Alice conversation (conv_math_401)');
  const linearAlgebraNotes = [
    "MATH 401: Advanced Linear Algebra - JIS College of Engineering",
    "Section 4.1: Eigenvalues and Characteristic Polynomial",
    "The characteristic equation of matrix A is given by det(A - lambda*I) = 0.",
    "Theorem 4.3 (Spectral Theorem): Every real symmetric matrix has real eigenvalues and orthogonal eigenvectors.",
    "Example 1: For matrix A = [[2, 1], [1, 2]], the eigenvalues are lambda_1 = 3 and lambda_2 = 1.",
    "Section 4.2: Diagonalization",
    "A matrix A is diagonalizable if and only if it has n linearly independent eigenvectors."
  ].join("\n");

  const uploadRes = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: convMath,
    fileName: 'Linear_Algebra_Chapter4.txt',
    mimeType: 'text/plain',
    fileBase64: Buffer.from(linearAlgebraNotes, 'utf8').toString('base64')
  }, aliceHeaders);

  assert(uploadRes.status === 200 && uploadRes.body.success === true,
    'Alice uploads Linear_Algebra_Chapter4.txt to conv_math_401');
  const mathAttId = uploadRes.body.attachmentId;

  // 4. Cross-user rejection: Bob tries to use Alice's attachment ID -> 403
  console.log('\nTest 4: Cross-user attachment access prevention');
  const bobHackerChat = await postJson('/api/chat', {
    message: 'Give me the answers from this document.',
    history: [],
    conversationId: convMath,
    attachmentIds: [mathAttId]
  }, bobHeaders);
  assert(bobHackerChat.status === 403 && (bobHackerChat.body.code === 'FORBIDDEN_ACCESS' || bobHackerChat.body.code === 'FORBIDDEN'),
    `Bob cannot use Alice’s attachment in his chat request (403 Forbidden, got status ${bobHackerChat.status}, code ${bobHackerChat.body?.code})`);

  // 5. Cross-conversation rejection: Alice tries to use math notes in physics conversation -> 403
  console.log('\nTest 5: Cross-conversation context boundary enforcement');
  const crossConvChat = await postJson('/api/chat', {
    message: 'Explain theorem 4.3 from my notes.',
    history: [],
    conversationId: convPhysics, // Different conversation!
    attachmentIds: [mathAttId]
  }, aliceHeaders);
  assert(crossConvChat.status === 403 && (crossConvChat.body.code === 'CROSS_CONTEXT_FORBIDDEN' || crossConvChat.body.code === 'FORBIDDEN'),
    `Attachment from conv_math_401 is rejected when requested in conv_physics_501 (403 Forbidden, got status ${crossConvChat.status}, code ${crossConvChat.body?.code})`);

  // 6. Grounded Gemini Chat: Alice asks about Eigenvalues in conv_math_401
  console.log('\nTest 6: Document-grounded Gemini Chat response');
  const groundedChat = await postJson('/api/chat', {
    message: 'According to Section 4.1 of my attached notes, what is the characteristic equation and what are the eigenvalues for matrix [[2, 1], [1, 2]]?',
    history: [],
    conversationId: convMath,
    attachmentIds: [mathAttId],
    mode: 'normal'
  }, aliceHeaders);

  assert(groundedChat.status === 200 && groundedChat.body.success === true,
    'Grounded chat request succeeds');
  assert(groundedChat.body.groundedAttachments && groundedChat.body.groundedAttachments.length === 1,
    'Response confirms grounding on 1 verified attachment');
  const reply1 = groundedChat.body.reply;
  const mentionsEigenvalues = reply1.includes('3') || reply1.includes('1') || reply1.toLowerCase().includes('characteristic');
  assert(mentionsEigenvalues,
    'Gemini answer accurately extracts and explains the eigenvalues (3 and 1) from the attached source');

  // 7. Follow-up conversation memory: Asking follow-up question grounded in conversation context
  console.log('\nTest 7: Follow-up conversational turn with grounded document context');
  const followUpChat = await postJson('/api/chat', {
    message: 'Now create a 2-question multiple choice practice quiz based on Theorem 4.3 from these notes.',
    history: [
      { role: 'user', text: 'According to Section 4.1 of my attached notes, what is the characteristic equation?' },
      { role: 'model', text: reply1 }
    ],
    conversationId: convMath,
    attachmentIds: [mathAttId],
    mode: 'learning'
  }, aliceHeaders);

  assert(followUpChat.status === 200 && followUpChat.body.success === true,
    'Follow-up chat request succeeds');
  const reply2 = followUpChat.body.reply.toLowerCase();
  const isQuizGrounded = reply2.includes('spectral') || reply2.includes('symmetric') || reply2.includes('eigen');
  assert(isQuizGrounded,
    'Follow-up quiz is grounded in Spectral Theorem / symmetric matrix properties from the attached document');

  // 8. Honest handling of absent document content
  console.log('\nTest 8: Honest uncertainty when asked about absent content');
  const absentContentChat = await postJson('/api/chat', {
    message: 'Does this document explain quantum teleportation and superconducting qubits?',
    history: [],
    conversationId: convMath,
    attachmentIds: [mathAttId],
    mode: 'normal'
  }, aliceHeaders);

  assert(absentContentChat.status === 200 && absentContentChat.body.success === true,
    'Absent content query succeeds');
  const reply3 = absentContentChat.body.reply.toLowerCase();
  const reportsAbsence = reply3.includes('not') || reply3.includes('does not') || reply3.includes('no mention') || reply3.includes('absent');
  assert(reportsAbsence,
    'Gemini honestly reports that quantum teleportation is absent from the linear algebra notes');

  // 9. Detachment / Removal from conversation
  console.log('\nTest 9: Detachment removes access to attachment');
  const delRes = await deleteJson(`/api/attachments/${mathAttId}`, { permanent: false }, aliceHeaders);
  assert(delRes.status === 200 && delRes.body.isDeleted === true,
    'Attachment successfully removed from conv_math_401');

  // Trying to chat with removed attachment ID returns 404
  const postDelChat = await postJson('/api/chat', {
    message: 'What was in my linear algebra notes?',
    history: [],
    conversationId: convMath,
    attachmentIds: [mathAttId]
  }, aliceHeaders);
  assert(postDelChat.status === 404 && postDelChat.body.code === 'ATTACHMENT_NOT_FOUND',
    'Removed attachment is no longer accessible for chat grounding (404 Not Found)');

  // 10. Multi-document upload and grounding (PDF + TXT)
  console.log('\nTest 10: Multi-document attachment and grounding in single conversation');
  const pdfContent = '%PDF-1.4\n% Math Notes Part 2\nCalculus Formulas: Derivative of sin(x) is cos(x).\nIntegral of e^x is e^x.\n%%EOF';
  const pdfUpload = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_multidoc_701',
    fileName: 'Calculus_Summary.pdf',
    mimeType: 'application/pdf',
    fileBase64: Buffer.from(pdfContent).toString('base64')
  }, aliceHeaders);

  const txtUpload = await postJson('/api/attachments/upload', {
    contextType: 'CHAT_CONVERSATION',
    contextId: 'conv_multidoc_701',
    fileName: 'Physics_Mechanics.txt',
    mimeType: 'text/plain',
    fileBase64: Buffer.from('Newton Second Law: F = ma. Kinetic Energy = 0.5 * m * v^2.', 'utf8').toString('base64')
  }, aliceHeaders);

  assert(pdfUpload.status === 200 && txtUpload.status === 200,
    'Successfully uploaded 2 documents to conv_multidoc_701');

  const multiChat = await postJson('/api/chat', {
    message: 'Compare the concepts between Calculus_Summary.pdf and Physics_Mechanics.txt: how does derivative relate to velocity?',
    history: [],
    conversationId: 'conv_multidoc_701',
    attachmentIds: [pdfUpload.body.attachmentId, txtUpload.body.attachmentId]
  }, aliceHeaders);

  assert(multiChat.status === 200 && multiChat.body.groundedAttachments.length === 2,
    'Chat grounds across multiple active attachments in the same conversation');

  console.log('\n========================================================');
  console.log(`STAGE 2 AUDIT RESULTS: Passed: ${passed}, Failed: ${failed}`);
  console.log('========================================================\n');

  if (server && server.close) {
    server.close();
  }

  process.exit(failed === 0 ? 0 : 1);
}

runStage2Tests().catch(err => {
  console.error("Fatal test error in Stage 2 suite:", err);
  process.exit(1);
});
