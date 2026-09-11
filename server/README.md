# Sage Backend Proxy (Secure Server-Side Gemini Architecture)

This backend service acts as a secure HTTPS reverse proxy between the Sage Android app and Google's Gemini API (`gemini-3.6-flash`).

## Security Architecture

1. **Zero Key in APK**: The Android application does **not** store or package the Gemini API key anywhere.
2. **Server-Side Secret**: The `GEMINI_API_KEY` is loaded exclusively as an environment variable in this server process.
3. **Tutoring Mode & History Forwarding**: The server handles system prompts for `normal`, `learning`, and `socratic` modes, and sends multi-turn conversation history to Gemini.
4. **Resilience**: Comprehensive error handling for Gemini 429 rate limits, 5xx server issues, network timeouts, and health checks.

---

## 24/7 Online Hosting Options (No Laptop Required)

### Option 1: Google Cloud Run (Recommended)
1. Install Google Cloud SDK or use Google Cloud Shell.
2. From the `server` directory:
   ```bash
   gcloud run deploy sage-backend \
     --source . \
     --platform managed \
     --region us-central1 \
     --allow-unauthenticated \
     --set-env-vars GEMINI_API_KEY="YOUR_API_KEY"
   ```
3. Cloud Run provides an HTTPS URL like `https://sage-backend-xyz-uc.a.run.app`.

### Option 2: Render.com (Free Tier)
1. Create a new Web Service on Render.
2. Connect your Git repository or use the Dockerfile in this directory.
3. In Render Environment Variables, set:
   - `GEMINI_API_KEY`: Your Google Gemini API Key
4. Render gives you an HTTPS URL: `https://sage-backend.onrender.com`.

### Option 3: Railway / Fly.io
1. Deploy the directory using `railway up` or `fly deploy`.
2. Set the `GEMINI_API_KEY` secret in their web dashboard.
