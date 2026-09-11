const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const GEMINI_MODEL = 'gemini-3.6-flash';

// Middleware
app.use(cors());
app.use(express.json({ limit: '10mb' }));

// Tutoring system prompts
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
 * Health check endpoint.
 * GET /api/health
 */
app.get('/api/health', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;
  const hasKey = Boolean(apiKey && apiKey !== 'MY_GEMINI_API_KEY' && apiKey.trim() !== '');

  if (req.query.checkGemini === 'true') {
    if (!hasKey) {
      return res.status(500).json({
        status: 'error',
        message: 'Server GEMINI_API_KEY environment variable is not configured.',
        model: GEMINI_MODEL
      });
    }

    try {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${apiKey}`;
      const upstreamRes = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          contents: [{ role: 'user', parts: [{ text: 'Reply with exactly: SAGE_CONNECTION_OK' }] }],
          generationConfig: { temperature: 0.0, maxOutputTokens: 256 }
        }),
        signal: AbortSignal.timeout(15000)
      });

      if (!upstreamRes.ok) {
        const errorText = await upstreamRes.text();
        return res.status(502).json({
          status: 'error',
          upstreamStatus: upstreamRes.status,
          message: `Gemini API returned HTTP ${upstreamRes.status}`,
          details: errorText
        });
      }

      const data = await upstreamRes.json();
      const text = data?.candidates?.[0]?.content?.parts?.map(p => p.text).join('') || '';

      return res.json({
        status: 'ok',
        service: 'sage-backend-proxy',
        model: GEMINI_MODEL,
        geminiConnected: true,
        testVerified: text.includes('SAGE_CONNECTION_OK')
      });
    } catch (err) {
      return res.status(504).json({
        status: 'error',
        message: 'Connection to Gemini API timed out or failed: ' + err.message
      });
    }
  }

  res.json({
    status: 'ok',
    service: 'sage-backend-proxy',
    model: GEMINI_MODEL,
    hasApiKey: hasKey
  });
});

/**
 * Chat completion proxy endpoint.
 * POST /api/chat
 * Body: { history: [{ role, text }], mode: 'normal'|'learning'|'socratic', systemPrompt?: string }
 */
app.post('/api/chat', async (req, res) => {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY' || apiKey.trim() === '') {
    return res.status(500).json({
      success: false,
      error: 'GEMINI_API_KEY is not configured on the Sage backend server.',
      code: 'SERVER_MISCONFIGURED'
    });
  }

  const { history, mode = 'normal', systemPrompt } = req.body || {};

  if (!Array.isArray(history) || history.length === 0) {
    return res.status(400).json({
      success: false,
      error: 'Missing or empty conversation history array.',
      code: 'BAD_REQUEST'
    });
  }

  // Determine system instruction
  const baseInstruction = SYSTEM_PROMPTS[mode] || SYSTEM_PROMPTS.normal;
  const effectiveSystemPrompt = systemPrompt ? `${baseInstruction}\n\n${systemPrompt}` : baseInstruction;

  // Format history for Gemini API
  const formattedContents = history.map(item => ({
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

  try {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:generateContent?key=${apiKey}`;
    const upstreamRes = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
      signal: AbortSignal.timeout(60000)
    });

    if (upstreamRes.status === 429) {
      return res.status(429).json({
        success: false,
        error: 'Gemini rate limit exceeded. Please wait a moment before sending another message.',
        code: 'RATE_LIMIT'
      });
    }

    if (upstreamRes.status >= 500) {
      return res.status(502).json({
        success: false,
        error: 'Google Gemini service is temporarily unavailable. Please retry in a moment.',
        code: 'GEMINI_UNAVAILABLE'
      });
    }

    if (!upstreamRes.ok) {
      const errorJson = await upstreamRes.json().catch(() => ({}));
      const msg = errorJson?.error?.message || `Gemini API returned status ${upstreamRes.status}`;
      return res.status(upstreamRes.status).json({
        success: false,
        error: msg,
        code: 'UPSTREAM_ERROR'
      });
    }

    const data = await upstreamRes.json();
    const candidateParts = data?.candidates?.[0]?.content?.parts || [];
    const replyText = candidateParts.map(p => p.text || '').join('').trim();

    if (!replyText) {
      return res.status(500).json({
        success: false,
        error: 'Gemini returned an empty response.',
        code: 'EMPTY_RESPONSE'
      });
    }

    return res.json({
      success: true,
      reply: replyText,
      model: GEMINI_MODEL
    });

  } catch (err) {
    if (err.name === 'TimeoutError' || err.message.includes('timeout')) {
      return res.status(504).json({
        success: false,
        error: 'Connection to Gemini API timed out.',
        code: 'TIMEOUT'
      });
    }

    return res.status(502).json({
      success: false,
      error: `Backend error communicating with Gemini: ${err.message}`,
      code: 'NETWORK_ERROR'
    });
  }
});

// Start server
const server = app.listen(PORT, '0.0.0.0', () => {
  console.log(`[Sage Backend Proxy] Running on http://0.0.0.0:${PORT}`);
  console.log(`[Sage Backend Proxy] Configured model: ${GEMINI_MODEL}`);
  console.log(`[Sage Backend Proxy] API key present: ${Boolean(process.env.GEMINI_API_KEY)}`);
});

module.exports = { app, server };
