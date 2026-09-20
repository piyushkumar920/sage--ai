const express = require('express');
const cors = require('cors');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');
require('dotenv').config();

// Attempt to load GEMINI_API_KEY from .dev.env.json if not in environment
if (!process.env.GEMINI_API_KEY || process.env.GEMINI_API_KEY === 'MY_GEMINI_API_KEY') {
  try {
    const devEnvPaths = [
      '/app/.dev.env.json',
      path.join(__dirname, '../.dev.env.json'),
      path.join(process.cwd(), '.dev.env.json')
    ];
    for (const devPath of devEnvPaths) {
      if (fs.existsSync(devPath)) {
        const devEnv = JSON.parse(fs.readFileSync(devPath, 'utf8'));
        if (devEnv.GEMINI_API_KEY && devEnv.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY') {
          process.env.GEMINI_API_KEY = devEnv.GEMINI_API_KEY;
          break;
        }
      }
    }
  } catch (e) {
    // Ignore error reading dev env
  }
}

const app = express();

/**
 * Cloud Run & Container Port Handling:
 * In standalone Cloud Run: listens on process.env.PORT || 8080 on 0.0.0.0
 * Behind Nginx container (NGINX_PORT set): listens on DEFAULT_APP_PORT || 3000 on 0.0.0.0
 */
const PORT = Number(
  process.env.BACKEND_PORT ||
  (process.env.NGINX_PORT ? (process.env.DEFAULT_APP_PORT || 3000) : (process.env.PORT || 8080))
);

const SERVICE_NAME = 'sage-backend-api';
const PRIMARY_MODEL = process.env.GEMINI_MODEL || 'gemini-3.5-flash-lite';
const CANDIDATE_MODELS = Array.from(new Set([
  PRIMARY_MODEL,
  'gemini-3.5-flash-lite',
  'gemini-3.5-flash'
]));

function logStartup() {
  console.log('========================================================');
  console.log(`[${SERVICE_NAME}] Starting Dedicated Production AI Backend`);
  console.log(`[${SERVICE_NAME}] Listening on port: ${PORT} (0.0.0.0)`);
  console.log(`[${SERVICE_NAME}] Primary AI Model: ${PRIMARY_MODEL}`);
  const hasKey = Boolean(process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY');
  console.log(`[${SERVICE_NAME}] GEMINI_API_KEY configured: ${hasKey ? 'YES' : 'NO'}`);
  console.log('========================================================');
}
logStartup();

// Request ID and Tracing middleware
app.use((req, res, next) => {
  const reqId = req.headers['x-request-id'] || crypto.randomUUID();
  req.requestId = reqId;
  res.setHeader('X-Request-ID', reqId);
  const startTime = Date.now();
  res.on('finish', () => {
    const elapsed = Date.now() - startTime;
    console.log(`[${reqId}] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${elapsed}ms)`);
  });
  next();
});

// Middleware - Strict JSON only
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Content-Type enforcement: Ensure EVERY response is JSON
app.use((req, res, next) => {
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  next();
});

// Tutoring system prompts for pedagogical modes
const SYSTEM_PROMPTS = {
  normal: `You are Sage, a brilliant, warm, and highly capable AI learning companion.
Your mission is to help the user learn, understand complex topics, solve problems, and master skills.
Guidelines:
- Explain concepts clearly, concisely, and insightfully.
- When explaining technical or code concepts, use Markdown code blocks with appropriate syntax highlighting.
- Be encouraging, patient, and intellectually curious.
- Tailor explanations to the user's level of comprehension.`,

  learning: `You are Sage in Learning Mode: a structured, pedagogical tutor.
Guidelines:
- Break complex ideas down into sequential, digestible steps.
- Use intuitive real-world analogies, concrete examples, and practical mental models.
- At the end of key explanations, offer a quick comprehension check question to solidify learning.
- Highlight key takeaways with bullet points.`,

  socratic: `You are Sage in Socratic Mode: a thoughtful philosophical guide and mentor.
Guidelines:
- Guide the user toward self-discovery rather than giving direct answers immediately.
- Ask probing, reflective questions that encourage critical thinking and deeper examination.
- Acknowledge insights warmly and challenge assumptions constructively.
- Keep responses focused and prompt the learner to reflect on the next step.`
};

/**
 * Root endpoint - Always JSON, never HTML
 * GET /
 */
app.get('/', (req, res) => {
  res.json({
    status: 'ok',
    service: SERVICE_NAME,
    model: PRIMARY_MODEL,
    message: 'Sage Dedicated Production AI Backend API',
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

/**
 * Health check endpoint
 * GET /api/health
 * Optional query parameter: ?checkGemini=true
 */
app.get('/api/health', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;
  const hasKey = Boolean(apiKey && apiKey !== 'MY_GEMINI_API_KEY' && apiKey.trim() !== '');

  if (req.query.checkGemini === 'true') {
    if (!hasKey) {
      return res.status(500).json({
        ok: false,
        status: 'error',
        service: SERVICE_NAME,
        backend: 'healthy',
        gemini: 'unavailable',
        geminiConnected: false,
        error: 'GEMINI_API_KEY is not configured on the backend server.',
        code: 'MISSING_API_KEY',
        model: PRIMARY_MODEL,
        requestId: req.requestId,
        timestamp: new Date().toISOString()
      });
    }

    let lastError = null;
    for (const modelName of CANDIDATE_MODELS) {
      try {
        const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
        const upstreamRes = await fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{ role: 'user', parts: [{ text: 'Reply with exactly: SAGE_CONNECTION_OK' }] }],
            generationConfig: { temperature: 0.0, maxOutputTokens: 64 }
          }),
          signal: AbortSignal.timeout(15000)
        });

        if (upstreamRes.status === 429) {
          const errBody = await upstreamRes.text();
          console.warn(`[${req.requestId}] Model ${modelName} returned 429 quota exhausted, trying next model...`);
          lastError = { status: 429, error: `Gemini rate limit exceeded for ${modelName}`, code: 'RATE_LIMIT', details: errBody };
          continue;
        }

        if (!upstreamRes.ok) {
          const errorText = await upstreamRes.text();
          lastError = { status: upstreamRes.status, error: `Gemini API returned HTTP ${upstreamRes.status}`, code: 'UPSTREAM_ERROR', details: errorText };
          continue;
        }

        const data = await upstreamRes.json();
        const text = data?.candidates?.[0]?.content?.parts?.map(p => p.text).join('') || '';

        return res.json({
          ok: true,
          status: 'ok',
          service: SERVICE_NAME,
          api: 'healthy',
          backend: 'healthy',
          gemini: 'connected',
          geminiConnected: true,
          model: modelName,
          configuredModel: PRIMARY_MODEL,
          testVerified: text.includes('SAGE_CONNECTION_OK') || text.length > 0,
          requestId: req.requestId,
          timestamp: new Date().toISOString()
        });
      } catch (err) {
        lastError = { status: 504, error: `Connection to ${modelName} timed out or failed: ${err.message}`, code: 'TIMEOUT' };
      }
    }

    return res.status(lastError?.status === 429 ? 429 : 502).json({
      ok: false,
      status: 'error',
      service: SERVICE_NAME,
      backend: 'healthy',
      gemini: 'unavailable',
      geminiConnected: false,
      error: lastError?.error || 'All candidate Gemini models failed.',
      code: lastError?.code || 'GEMINI_UNAVAILABLE',
      model: PRIMARY_MODEL,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  return res.json({
    ok: true,
    status: 'ok',
    service: SERVICE_NAME,
    api: 'healthy',
    backend: 'healthy',
    model: PRIMARY_MODEL,
    hasApiKey: hasKey,
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

/**
 * Chat completion endpoint
 * POST /api/chat
 * Body: { history: [{ role, text }], mode?: 'normal'|'learning'|'socratic', systemPrompt?: string }
 */
app.post('/api/chat', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY' || apiKey.trim() === '') {
    return res.status(500).json({
      ok: false,
      success: false,
      error: 'GEMINI_API_KEY is not configured on the Sage backend server.',
      code: 'SERVER_MISCONFIGURED',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  const { message, history, mode = 'normal', systemPrompt } = req.body || {};

  let effectiveHistory = Array.isArray(history) ? [...history] : [];
  if (message && typeof message === 'string' && message.trim()) {
    const lastItem = effectiveHistory[effectiveHistory.length - 1];
    if (!lastItem || lastItem.text !== message.trim()) {
      effectiveHistory.push({ role: 'user', text: message.trim() });
    }
  }

  if (effectiveHistory.length === 0) {
    return res.status(400).json({
      ok: false,
      success: false,
      error: 'Missing message or conversation history.',
      code: 'BAD_REQUEST',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  // Determine system instruction
  const baseInstruction = SYSTEM_PROMPTS[mode] || SYSTEM_PROMPTS.normal;
  const effectiveSystemPrompt = systemPrompt ? `${baseInstruction}\n\n${systemPrompt}` : baseInstruction;

  // Format history for Gemini API
  const formattedContents = effectiveHistory.map(item => ({
    role: item.role === 'model' || item.role === 'assistant' ? 'model' : 'user',
    parts: [{ text: item.text || '' }]
  }));

  const payload = {
    contents: formattedContents,
    systemInstruction: {
      parts: [{ text: effectiveSystemPrompt }]
    },
    generationConfig: {
      temperature: 0.7,
      topP: 0.95,
      maxOutputTokens: 2048
    }
  };

  let lastError = null;
  for (const modelName of CANDIDATE_MODELS) {
    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
      const upstreamRes = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(60000)
      });

      if (upstreamRes.status === 429) {
        console.warn(`[${req.requestId}] Chat on model ${modelName} returned 429 quota exhausted, attempting fallback...`);
        lastError = { status: 429, error: `Gemini rate limit exceeded for ${modelName}`, code: 'RATE_LIMIT' };
        continue;
      }

      if (upstreamRes.status >= 500) {
        lastError = { status: 502, error: 'Google Gemini service is temporarily unavailable.', code: 'GEMINI_UNAVAILABLE' };
        continue;
      }

      if (!upstreamRes.ok) {
        const errorJson = await upstreamRes.json().catch(() => ({}));
        const msg = errorJson?.error?.message || `Gemini API returned status ${upstreamRes.status}`;
        lastError = { status: upstreamRes.status, error: msg, code: 'UPSTREAM_ERROR' };
        continue;
      }

      const data = await upstreamRes.json();
      const candidateParts = data?.candidates?.[0]?.content?.parts || [];
      const replyText = candidateParts.map(p => p.text || '').join('').trim();

      if (!replyText) {
        lastError = { status: 500, error: 'Gemini returned an empty response.', code: 'EMPTY_RESPONSE' };
        continue;
      }

      return res.json({
        ok: true,
        success: true,
        reply: replyText,
        model: modelName,
        service: SERVICE_NAME,
        requestId: req.requestId,
        timestamp: new Date().toISOString()
      });

    } catch (err) {
      if (err.name === 'TimeoutError' || err.message.includes('timeout')) {
        lastError = { status: 504, error: 'Connection to Gemini API timed out.', code: 'TIMEOUT' };
      } else {
        lastError = { status: 502, error: `Backend error communicating with Gemini: ${err.message}`, code: 'NETWORK_ERROR' };
      }
    }
  }

  return res.status(lastError?.status || 500).json({
    ok: false,
    success: false,
    error: lastError?.error || 'All candidate Gemini models failed to generate content.',
    code: lastError?.code || 'UNKNOWN_ERROR',
    service: SERVICE_NAME,
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

/**
 * Phase A — SAGE STUDY TOOLS Endpoint
 * POST /api/study-tools
 * Body: {
 *   operation: 'notes' | 'flashcards' | 'mindmap' | 'revision' | 'formulas' | 'solve_image',
 *   topic?: string,
 *   subject?: string,
 *   syllabusContext?: string,
 *   imageBase64?: string,
 *   imageMimeType?: string,
 *   prompt?: string
 * }
 */
app.post('/api/study-tools', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY' || apiKey.trim() === '') {
    return res.status(500).json({
      ok: false,
      success: false,
      error: 'GEMINI_API_KEY is not configured on the Sage backend server.',
      code: 'AUTH_OR_API_KEY_ERROR',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  const {
    operation,
    topic = '',
    subject = '',
    syllabusContext = '',
    imageBase64 = '',
    imageMimeType = 'image/jpeg',
    prompt = '',
    academicContext = null
  } = req.body || {};

  const validOperations = ['notes', 'flashcards', 'mindmap', 'revision', 'formulas', 'solve_image'];
  if (!operation || !validOperations.includes(operation)) {
    return res.status(400).json({
      ok: false,
      success: false,
      error: `Invalid or missing operation. Must be one of: ${validOperations.join(', ')}`,
      code: 'INVALID_REQUEST',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  // Structured Logging for Study Tools Request as required
  console.log(`[STUDY_TOOLS]\noperation=${operation}\nroute=/api/study-tools\nmodel=${PRIMARY_MODEL}\nrequestReceived=true`);

  if (operation === 'solve_image' && !imageBase64) {
    return res.status(400).json({
      ok: false,
      success: false,
      error: 'Missing image data (imageBase64) for solve_image operation.',
      code: 'INVALID_REQUEST',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  const effectiveTopic = (topic || (academicContext && (academicContext.topic || academicContext.courseName)) || '').trim();
  const effectiveSubject = (subject || (academicContext && (academicContext.courseName || academicContext.courseCode)) || '').trim();

  if (operation !== 'solve_image' && !effectiveTopic && !prompt.trim()) {
    return res.status(400).json({
      ok: false,
      success: false,
      error: 'Missing topic or prompt for study tool generation.',
      code: 'INVALID_REQUEST',
      service: SERVICE_NAME,
      requestId: req.requestId,
      timestamp: new Date().toISOString()
    });
  }

  // Construct operation-specific system prompts & instructions
  let systemInstructionText = `You are Sage Study Assistant, an expert academic tutor.
You MUST output ONLY a valid, single JSON object without any markdown wrapping, code blocks (no \`\`\`json), or preamble.`;

  let userPromptText = '';

  let academicDetails = '';
  if (academicContext && typeof academicContext === 'object') {
    const lines = [];
    if (academicContext.department) lines.push(`Department: ${academicContext.department}`);
    if (academicContext.programme) lines.push(`Programme: ${academicContext.programme}`);
    if (academicContext.regulation) lines.push(`Regulation: ${academicContext.regulation}`);
    if (academicContext.semester) lines.push(`Semester: ${academicContext.semester}`);
    if (academicContext.courseCode || academicContext.courseName) {
      lines.push(`Course: ${academicContext.courseCode || ''} - ${academicContext.courseName || ''}`.trim());
    }
    if (academicContext.module) lines.push(`Module: ${academicContext.module}`);
    if (academicContext.topic) lines.push(`Topic: ${academicContext.topic}`);
    if (academicContext.officialSyllabusContent) {
      lines.push(`Official Syllabus: ${academicContext.officialSyllabusContent}`);
    }
    if (lines.length > 0) {
      academicDetails = `\nAcademic Curriculum Context:\n${lines.join('\n')}`;
    }
  }

  const effectiveSyllabus = syllabusContext || (academicContext && academicContext.officialSyllabusContent) || '';
  const contextBlock = `${academicDetails}${effectiveSyllabus ? `\nOfficial Syllabus Context:\n${effectiveSyllabus}\nNote: Stay strictly grounded in this official syllabus context. If any concept goes beyond this official syllabus, set "beyondSyllabus": true or clearly flag it.` : ''}`;

  switch (operation) {
    case 'notes':
      userPromptText = `Generate comprehensive academic study notes for:
Topic: "${topic}"
Subject: "${subject}"
${contextBlock}
User Instructions: ${prompt || 'Provide well-structured, clear conceptual notes.'}

Return JSON with exactly this structure:
{
  "type": "notes",
  "title": "${topic || 'Study Notes'}",
  "summary": "High level 2-3 sentence conceptual overview",
  "sections": [
    {
      "heading": "Section Heading",
      "content": "In-depth pedagogical explanation with clear formatting",
      "keyPoints": ["Point 1", "Point 2", "Point 3"]
    }
  ],
  "examples": ["Example 1 with context", "Example 2 with real world application"],
  "importantTerms": ["Term 1: definition", "Term 2: definition"],
  "beyondSyllabus": false
}`;
      break;

    case 'flashcards':
      userPromptText = `Generate a set of 5 to 10 high-yield, interactive revision flashcards for:
Topic: "${topic}"
Subject: "${subject}"
${contextBlock}
User Instructions: ${prompt || 'Focus on high-yield exam concepts and definitions.'}

Return JSON with exactly this structure:
{
  "type": "flashcards",
  "title": "${topic || 'Flashcard Set'}",
  "cards": [
    {
      "front": "Clear question or concept prompt",
      "back": "Concise, complete, accurate answer",
      "hint": "Helpful cognitive nudge or mnemonic"
    }
  ]
}`;
      break;

    case 'mindmap':
      userPromptText = `Generate a hierarchical conceptual mind map for:
Topic: "${topic}"
Subject: "${subject}"
${contextBlock}
User Instructions: ${prompt || 'Break down the concept hierarchically into logical sub-branches.'}

Return JSON with exactly this structure:
{
  "type": "mindmap",
  "title": "${topic || 'Concept Mind Map'}",
  "root": {
    "id": "root",
    "label": "${topic || 'Core Concept'}",
    "description": "Short explanation of the central concept",
    "children": [
      {
        "id": "node_1",
        "label": "Subtopic Branch",
        "description": "Clear explanation of this branch",
        "children": [
          {
            "id": "node_1_1",
            "label": "Detail Concept",
            "description": "Explanation of detail",
            "children": []
          }
        ]
      }
    ]
  }
}`;
      break;

    case 'revision':
      userPromptText = `Generate an intense, high-impact Last-Minute Revision Sheet for:
Topic: "${topic}"
Subject: "${subject}"
${contextBlock}
User Instructions: ${prompt || 'High-yield revision points, traps, definitions, and cheat notes.'}

Return JSON with exactly this structure:
{
  "type": "revision",
  "title": "${topic || 'Revision Sheet'}",
  "coreConcepts": ["Core concept 1", "Core concept 2"],
  "definitions": ["Definition 1", "Definition 2"],
  "keyFacts": ["Key fact 1", "Key fact 2"],
  "importantFormulas": ["Formula or relationship if applicable"],
  "commonMistakes": ["Common misconception 1 and how to avoid it", "Exam trap 2"],
  "quickExamples": ["Quick practical example with concise solution"],
  "lastMinuteRevisionPoints": ["Point to remember 5 minutes before exam 1", "Point 2"],
  "beyondSyllabus": false
}`;
      break;

    case 'formulas':
      userPromptText = `Generate an authoritative Formula & Key Equations Sheet for:
Topic: "${topic}"
Subject: "${subject}"
${contextBlock}
User Instructions: ${prompt || 'Key equations, variable definitions, SI units, and applications. If the topic has no mathematical/physical formulas, state key analytical rules or properties without making up fake math.'}

Return JSON with exactly this structure:
{
  "type": "formulas",
  "title": "${topic || 'Formula Sheet'}",
  "hasFormulas": true,
  "formulas": [
    {
      "name": "Equation / Rule Name",
      "formula": "e.g. F = m * a or E = mc^2 or Time Complexity T(n)",
      "variables": ["F: Force (Newtons, N)", "m: Mass (kg)", "a: Acceleration (m/s^2)"],
      "units": "SI units description",
      "usageExplanation": "When and how to apply this equation",
      "example": "Worked mini-example calculation"
    }
  ],
  "notes": "General guidance on applying these equations"
}`;
      break;

    case 'solve_image':
      userPromptText = `Analyze the provided educational image carefully.
${prompt ? `User question/notes: ${prompt}\n` : ''}
Determine if the image contains a clear educational question (printed, handwritten, mathematics, physics, chemistry, programming code, MCQ, engineering problem, or diagram).
If the image is blurry, unreadable, cut off, or not an educational problem:
Set "isClear": false, "problem": "Image is unclear or unreadable.", "answer": "Please upload a clearer image of the question.", "steps": [], "finalAnswer": ""

If it IS clear:
Set "isClear": true, detect the full question verbatim into "problem", break down the pedagogical solution into numbered logical "steps" with clear explanations, provide the exact "finalAnswer", and set "needsVerification": false (or true if ambiguous).

Return JSON with exactly this structure:
{
  "type": "solution",
  "isClear": true,
  "problem": "Exact detected question text from image",
  "subject": "Detected academic subject (e.g., Mathematics, Physics, Data Structures)",
  "answer": "Direct summary answer",
  "steps": [
    {
      "step": 1,
      "title": "Step Title (e.g., Identify Given Variables)",
      "explanation": "Clear, step-by-step mathematical or logical derivation"
    }
  ],
  "finalAnswer": "Definitive final answer or result",
  "needsVerification": false
}`;
      break;
  }

  // Construct Gemini request parts
  const userParts = [];
  if (operation === 'solve_image' && imageBase64) {
    // Clean base64 header if present (e.g. data:image/jpeg;base64,...)
    const cleanBase64 = imageBase64.includes(',') ? imageBase64.split(',')[1] : imageBase64;
    userParts.push({
      inlineData: {
        mimeType: imageMimeType || 'image/jpeg',
        data: cleanBase64
      }
    });
  }
  userParts.push({ text: userPromptText });

  const payload = {
    contents: [
      {
        role: 'user',
        parts: userParts
      }
    ],
    systemInstruction: {
      parts: [{ text: systemInstructionText }]
    },
    generationConfig: {
      temperature: 0.2, // Low temperature for deterministic structured JSON
      topP: 0.95,
      maxOutputTokens: 4096,
      responseMimeType: 'application/json'
    }
  };

  let lastError = null;
  for (const modelName of CANDIDATE_MODELS) {
    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=${apiKey}`;
      const upstreamRes = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(60000)
      });

      if (!upstreamRes.ok) {
        console.error(`[STUDY_TOOLS_GEMINI_ERROR]\nstatus=${upstreamRes.status}\noperation=${operation}\nmodel=${modelName}`);
        if (upstreamRes.status === 429) {
          console.warn(`[${req.requestId}] StudyTools model ${modelName} returned 429, trying next candidate model...`);
          lastError = { status: 429, error: `Sage has reached its temporary AI request limit on ${modelName}.`, code: 'RATE_LIMITED' };
          continue;
        }
        if (upstreamRes.status === 401 || upstreamRes.status === 403) {
          lastError = { status: 500, error: 'AI credentials authorization error.', code: 'AUTH_OR_API_KEY_ERROR' };
          break;
        }
        if (upstreamRes.status === 404) {
          // Upstream Gemini model not found - map to 503 so client NEVER confuses with route 404
          lastError = { status: 503, error: `Upstream model ${modelName} is unavailable.`, code: 'GEMINI_SERVICE_UNAVAILABLE' };
          continue;
        }
        const errJson = await upstreamRes.json().catch(() => ({}));
        const msg = errJson?.error?.message || `Gemini upstream service error (${upstreamRes.status})`;
        lastError = { status: 503, error: msg, code: 'GEMINI_SERVICE_UNAVAILABLE' };
        continue;
      }

      const data = await upstreamRes.json();
      const rawText = data?.candidates?.[0]?.content?.parts?.map(p => p.text).join('') || '';

      if (!rawText.trim()) {
        console.error(`[STUDY_TOOLS_GEMINI_ERROR]\nstatus=500\noperation=${operation}\nmodel=${modelName}`);
        lastError = { status: 500, error: 'Empty generation received from Gemini.', code: 'BACKEND_ERROR' };
        continue;
      }

      // Clean response (strip any accidental markdown block)
      let cleanJsonStr = rawText.trim();
      if (cleanJsonStr.startsWith('```json')) {
        cleanJsonStr = cleanJsonStr.replace(/^```json\s*/i, '').replace(/\s*```$/, '');
      } else if (cleanJsonStr.startsWith('```')) {
        cleanJsonStr = cleanJsonStr.replace(/^```\s*/, '').replace(/\s*```$/, '');
      }

      let parsedData;
      try {
        parsedData = JSON.parse(cleanJsonStr);
      } catch (parseErr) {
        console.error(`[STUDY_TOOLS_GEMINI_ERROR]\nstatus=502\noperation=${operation}\nmodel=${modelName}`);
        console.error(`[${req.requestId}] Failed to parse Gemini JSON:`, cleanJsonStr.slice(0, 200));
        lastError = { status: 502, error: 'Malformed JSON returned by AI model.', code: 'BACKEND_ERROR' };
        continue;
      }

      return res.json({
        ok: true,
        success: true,
        operation,
        data: parsedData,
        model: modelName,
        service: SERVICE_NAME,
        requestId: req.requestId,
        timestamp: new Date().toISOString()
      });
    } catch (err) {
      console.error(`[STUDY_TOOLS_GEMINI_ERROR]\nstatus=504\noperation=${operation}\nmodel=${modelName}`);
      if (err.name === 'TimeoutError' || err.message.includes('timeout')) {
        lastError = { status: 504, error: 'Connection to AI model timed out.', code: 'NETWORK_ERROR' };
      } else {
        lastError = { status: 502, error: `Error during study tool generation: ${err.message}`, code: 'NETWORK_ERROR' };
      }
    }
  }

  return res.status(lastError?.status || 500).json({
    ok: false,
    success: false,
    error: lastError?.error || "Sage couldn't generate this material right now. Please try again.",
    code: lastError?.code || 'BACKEND_ERROR',
    service: SERVICE_NAME,
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

/**
 * Standard API routes for roadmaps, progress, and quiz to guarantee JSON responses
 */
app.get('/api/roadmaps', (req, res) => {
  res.json({
    success: true,
    service: SERVICE_NAME,
    roadmaps: [
      { id: 'fullstack', title: 'Full Stack Web Developer', description: 'From modern HTML/CSS/JS to React, Node.js, and Cloud Deployment.' },
      { id: 'android', title: 'Android App Developer', description: 'Modern Kotlin, Jetpack Compose, Room, MVVM, and Play Store release.' },
      { id: 'python_ai', title: 'Python & AI Engineering', description: 'Python fundamentals, data structures, algorithms, and Gemini AI integration.' }
    ],
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

app.get('/api/progress', (req, res) => {
  res.json({
    success: true,
    service: SERVICE_NAME,
    progress: {
      streak: 3,
      studyMinutes: 45,
      completedMilestones: 4
    },
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

app.get('/api/quiz', (req, res) => {
  res.json({
    success: true,
    service: SERVICE_NAME,
    questions: [
      { id: 'q1', prompt: 'Which HTTP method is idempotent for retrieving resources?', options: ['GET', 'POST', 'PATCH', 'CONNECT'], answer: 'GET' }
    ],
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

// Explicit 404 for ANY unmatched /api/* route - strictly JSON only, never HTML
app.all('/api/*', (req, res) => {
  res.status(404).json({
    ok: false,
    error: 'API route not found',
    path: req.originalUrl,
    code: 'NOT_FOUND',
    service: SERVICE_NAME,
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

// Explicit 404 for ANY other unmatched route - STRICTLY JSON ONLY, NEVER HTML
app.all('*', (req, res) => {
  res.status(404).json({
    ok: false,
    error: 'API route not found',
    path: req.originalUrl,
    code: 'NOT_FOUND',
    service: SERVICE_NAME,
    requestId: req.requestId,
    timestamp: new Date().toISOString()
  });
});

// Centralized Global Error Handler - GUARANTEE JSON FOR ALL UNHANDLED EXCEPTIONS
app.use((err, req, res, next) => {
  const requestId = req.requestId || 'unknown';
  console.error(`[${requestId}] Global error handler:`, err);
  res.status(err.status || 500).json({
    ok: false,
    success: false,
    error: err.message || 'Internal Server Error',
    code: err.code || 'INTERNAL_ERROR',
    service: SERVICE_NAME,
    requestId: requestId,
    timestamp: new Date().toISOString()
  });
});

// Start server listening on 0.0.0.0
const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`[${SERVICE_NAME}] Server running on http://0.0.0.0:${PORT}`);
  console.log(`[${SERVICE_NAME}] Ready for requests`);
});

module.exports = { app, server };
