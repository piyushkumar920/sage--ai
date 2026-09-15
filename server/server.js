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
const PRIMARY_MODEL = process.env.GEMINI_MODEL || 'gemini-3.5-flash';
const CANDIDATE_MODELS = [
  PRIMARY_MODEL,
  'gemini-3.5-flash-lite',
  'gemini-3.1-flash-lite-preview',
  'gemini-3.1-flash-lite',
  'gemini-2.5-flash',
  'gemini-flash-latest'
];

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
