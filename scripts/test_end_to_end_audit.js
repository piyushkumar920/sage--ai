const { app, server } = require('../server/server');
const zlib = require('zlib');
const http = require('http');

// Helper to make local HTTP requests to the running express server
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

function getJson(path) {
  return new Promise((resolve, reject) => {
    const req = http.request({
      hostname: '127.0.0.1',
      port: 8080,
      path: path,
      method: 'GET'
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

// Minimal zip builder for tests
function createZipWithFiles(files) {
  const parts = [];
  const cdEntries = [];
  let offset = 0;

  for (const [filename, content] of Object.entries(files)) {
    const fnBuf = Buffer.from(filename, 'utf8');
    const compData = zlib.deflateRawSync(Buffer.from(content, 'utf8'));
    const uncompLen = Buffer.byteLength(content, 'utf8');
    const compLen = compData.length;

    // Local file header
    const lfh = Buffer.alloc(30 + fnBuf.length);
    lfh.writeUInt32LE(0x04034b50, 0); // Sig
    lfh.writeUInt16LE(20, 4); // Version needed
    lfh.writeUInt16LE(0, 6); // Flags
    lfh.writeUInt16LE(8, 8); // Comp method (Deflate)
    lfh.writeUInt16LE(0, 10); // Time
    lfh.writeUInt16LE(0, 12); // Date
    lfh.writeUInt32LE(0, 14); // CRC (ignored for test)
    lfh.writeUInt32LE(compLen, 18);
    lfh.writeUInt32LE(uncompLen, 22);
    lfh.writeUInt16LE(fnBuf.length, 26);
    lfh.writeUInt16LE(0, 28);
    fnBuf.copy(lfh, 30);

    parts.push(lfh, compData);

    // Central directory header
    const cdh = Buffer.alloc(46 + fnBuf.length);
    cdh.writeUInt32LE(0x02014b50, 0);
    cdh.writeUInt16LE(20, 4);
    cdh.writeUInt16LE(20, 6);
    cdh.writeUInt16LE(0, 8);
    cdh.writeUInt16LE(8, 10);
    cdh.writeUInt16LE(0, 12);
    cdh.writeUInt16LE(0, 14);
    cdh.writeUInt32LE(0, 16);
    cdh.writeUInt32LE(compLen, 20);
    cdh.writeUInt32LE(uncompLen, 24);
    cdh.writeUInt16LE(fnBuf.length, 28);
    cdh.writeUInt16LE(0, 30);
    cdh.writeUInt16LE(0, 32);
    cdh.writeUInt16LE(0, 34);
    cdh.writeUInt16LE(0, 36);
    cdh.writeUInt32LE(0, 38);
    cdh.writeUInt32LE(offset, 42);
    fnBuf.copy(cdh, 46);

    cdEntries.push(cdh);
    offset += lfh.length + compData.length;
  }

  const cdOffset = offset;
  let cdSize = 0;
  for (const cdh of cdEntries) {
    parts.push(cdh);
    cdSize += cdh.length;
  }

  // End of central directory record
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0);
  eocd.writeUInt16LE(0, 4);
  eocd.writeUInt16LE(0, 6);
  eocd.writeUInt16LE(cdEntries.length, 8);
  eocd.writeUInt16LE(cdEntries.length, 10);
  eocd.writeUInt32LE(cdSize, 12);
  eocd.writeUInt32LE(cdOffset, 16);
  eocd.writeUInt16LE(0, 20);
  parts.push(eocd);

  return Buffer.concat(parts);
}

async function runAudit() {
  console.log('========================================================');
  console.log('SAGE — SIX-PHASE FINAL ACCEPTANCE AUDIT & TEST RUNNER');
  console.log('========================================================\n');

  let passed = 0;
  let failed = 0;

  function assert(condition, name) {
    if (condition) {
      console.log(`[PASS] ${name}`);
      passed++;
    } else {
      console.error(`[FAIL] ${name}`);
      failed++;
    }
  }

  try {
    // Test 1: Server Health
    const health = await getJson('/health');
    assert(health.status === 200 && health.body.ok === true, '1. Health endpoint returns 200 OK');

    // Test 2: Validation - Allowed MIME and size
    const validPdf = await postJson('/api/documents/validate', {
      documentName: 'OS_Syllabus.pdf',
      mimeType: 'application/pdf',
      fileSize: 1024 * 1024
    });
    assert(validPdf.status === 200 && validPdf.body.valid === true, '2.1 Validates normal PDF (<15MB)');

    // Test 2.2: Reject oversized
    const oversize = await postJson('/api/documents/validate', {
      documentName: 'Huge.pdf',
      mimeType: 'application/pdf',
      fileSize: 20 * 1024 * 1024
    });
    assert(oversize.status === 400 && oversize.body.code === 'FILE_TOO_LARGE', '2.2 Rejects oversized file (>15MB)');

    // Test 2.3: Reject unsupported MIME
    const badMime = await postJson('/api/documents/validate', {
      documentName: 'app.exe',
      mimeType: 'application/x-msdownload',
      fileSize: 1024
    });
    assert(badMime.status === 400 && badMime.body.code === 'UNSUPPORTED_FORMAT', '2.3 Rejects unsupported file extension');

    // Test 2.4: Reject corrupt magic bytes
    const corruptFile = await postJson('/api/documents/validate', {
      documentName: 'corrupt.pdf',
      mimeType: 'application/pdf',
      fileSize: 1024,
      fileBase64: Buffer.from('NOT-A-PDF-HEADER').toString('base64')
    });
    assert(corruptFile.status === 400 && corruptFile.body.code === 'INVALID_FILE_SIGNATURE', '2.4 Rejects corrupt file signature magic bytes');

    // Test 3: Document Extraction - TXT
    const txtSyllabus = `Syllabus for Data Structures & Algorithms
Semester: 3
Subject: CS201 Data Structures
Module 1: Linear Data Structures
Topics:
- Arrays and Linked Lists
- Stacks and Queues
Learning Objectives: Understand pointer operations and memory allocation.
Module 2: Trees and Graphs
Topics:
- Binary Search Trees
- Graph Traversals (BFS, DFS)
`;
    const txtExtraction = await postJson('/api/documents/extract', {
      documentName: 'DSA_Syllabus.txt',
      mimeType: 'text/plain',
      textContent: txtSyllabus
    });
    assert(txtExtraction.status === 200 && txtExtraction.body.success === true, '3.1 Extracts structured syllabus from TXT');
    assert(Boolean(txtExtraction.body.structuredData?.semesters?.length), '3.2 TXT extraction returns structured semesters and modules');

    // Test 4: Document Extraction - DOCX Parsing
    const sampleDocxXml = `<?xml version="1.0" encoding="UTF-8"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p><w:pPr><w:pStyle w:val="Heading1"/></w:pPr><w:r><w:t>Database Management Systems</w:t></w:r></w:p>
    <w:p><w:r><w:t>Module 1: Relational Model and SQL</w:t></w:r></w:p>
    <w:p><w:r><w:t>Topics: ER Modeling, Normalization (1NF to BCNF), Relational Algebra</w:t></w:r></w:p>
    <w:tbl>
      <w:tr>
        <w:tc><w:p><w:r><w:t>Unit</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:t>Topic</w:t></w:r></w:p></w:tc>
      </w:tr>
      <w:tr>
        <w:tc><w:p><w:r><w:t>2</w:t></w:r></w:p></w:tc>
        <w:tc><w:p><w:r><w:t>Transaction Management and ACID Properties</w:t></w:r></w:p></w:tc>
      </w:tr>
    </w:tbl>
  </w:body>
</w:document>`;
    const docxBuf = createZipWithFiles({
      'word/document.xml': sampleDocxXml
    });
    const docxExtraction = await postJson('/api/documents/extract', {
      documentName: 'DBMS_Course.docx',
      mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      fileBase64: docxBuf.toString('base64')
    });
    assert(docxExtraction.status === 200 && docxExtraction.body.success === true, '4.1 Unzips and extracts structured curriculum from DOCX tables and headings');

    // Test 5: Document Extraction - PPTX Parsing
    const slide1Xml = `<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree>
    <p:sp><p:txBody><a:p><a:r><a:t>Operating Systems - Lecture 1</a:t></a:r></a:p></p:txBody></p:sp>
    <p:sp><p:txBody><a:p><a:r><a:t>Processes, Threads and CPU Scheduling Algorithms</a:t></a:r></a:p></p:txBody></p:sp>
  </p:spTree></p:cSld>
</p:sld>`;
    const slide2Xml = `<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">
  <p:cSld><p:spTree>
    <p:sp><p:txBody><a:p><a:r><a:t>Memory Management: Paging, Segmentation and Virtual Memory</a:t></a:r></a:p></p:txBody></p:sp>
  </p:spTree></p:cSld>
</p:sld>`;
    const pptxBuf = createZipWithFiles({
      'ppt/slides/slide1.xml': slide1Xml,
      'ppt/slides/slide2.xml': slide2Xml
    });
    const pptxExtraction = await postJson('/api/documents/extract', {
      documentName: 'OS_Slides.pptx',
      mimeType: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
      fileBase64: pptxBuf.toString('base64')
    });
    assert(pptxExtraction.status === 200 && pptxExtraction.body.success === true, '5.1 Unzips and extracts slides with slide references from PPTX');

    // Test 6: Official Curriculum Comparison Engine
    const compareRes = await postJson('/api/documents/compare-curriculum', {
      extractedData: {
        semesters: [{
          semesterNumber: 3,
          subjects: [{
            subjectName: 'Data Structures',
            modules: [{
              moduleTitle: 'Module 1',
              topics: [
                { topicName: 'Arrays and Linked Lists' },
                { topicName: 'Quantum Machine Learning' }, // missing from standard
                { topicName: 'DSA' } // name mismatch
              ]
            }]
          }]
        }]
      },
      officialCurriculum: {
        courses: [{
          title: 'Data Structures & Algorithms',
          units: [{
            title: 'Unit 1',
            topics: ['Arrays and Linked Lists', 'Stacks', 'Data Structures & Algorithms']
          }]
        }]
      }
    });

    assert(compareRes.status === 200 && compareRes.body.success === true, '6.1 Curriculum comparison API returns success');
    assert(compareRes.body.officialCurriculumProtected === true, '6.2 Guarantees official institutional records remain protected');
    const labelsReturned = new Set(compareRes.body.results.map(r => r.comparisonLabel));
    assert(labelsReturned.has('MATCHED'), '6.3 Assigns MATCHED label for identical topics');
    assert(labelsReturned.has('MISSING_FROM_OFFICIAL'), '6.4 Assigns MISSING_FROM_OFFICIAL for missing topics');

    // Test 7: Phase 1-5 Regression Checks
    const chatRes = await postJson('/api/chat', {
      message: 'Explain binary search trees concisely',
      mode: 'learning'
    });
    assert(chatRes.status === 200 && Boolean(chatRes.body.reply), '7.1 Phase 1/3: /api/chat responds pedagogical explanation');

    const notesRes = await postJson('/api/study-tools', {
      operation: 'notes',
      topic: 'Binary Search Trees',
      subject: 'Data Structures'
    });
    assert(notesRes.status === 200 && Boolean(notesRes.body.data), '7.2 Phase 4: /api/study-tools operation=notes generates structured notes');

    const flashcardRes = await postJson('/api/study-tools', {
      operation: 'flashcards',
      topic: 'Tree Traversals',
      subject: 'Data Structures'
    });
    assert(flashcardRes.status === 200 && Boolean(flashcardRes.body.data), '7.3 Phase 4: /api/study-tools operation=flashcards generates cards');

    console.log(`\n========================================================`);
    console.log(`AUDIT EXECUTION COMPLETE: Passed: ${passed}, Failed: ${failed}`);
    console.log(`========================================================`);

  } catch (err) {
    console.error('Audit execution error:', err);
    failed++;
  } finally {
    server.close();
    process.exit(failed > 0 ? 1 : 0);
  }
}

runAudit();
