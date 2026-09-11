# Sage — Intelligent AI Learning Companion

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-green.svg?style=flat&logo=android)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/AI%20Engine-Gemini%203.5%20Flash-orange.svg?style=flat&logo=google)](https://ai.google.dev/)
[![Room Database](https://img.shields.io/badge/Database-Room%20SQLite-teal.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-blue.svg?style=flat)]()

> **"Tell me and I forget. Teach me and I remember. Involve me and I learn."** — Benjamin Franklin

**Sage** is an intelligent, calm, and pedagogically engineered AI learning companion built natively for Android using Jetpack Compose, Material Design 3, and Google's latest **Gemini 3.5 Flash** models. 

Unlike generic chatbots that dump walls of text or overwhelm you with unstructured information, Sage is designed from the ground up as a **personal 1-on-1 tutor**. It evaluates your current understanding, creates tailored learning roadmaps, checks comprehension at every step, and adapts its explanations whenever you feel confused.

---

## Table of Contents

1. [What is Sage?](#what-is-sage)
2. [Why Use Sage? (The Science of Accelerated Learning)](#why-use-sage-the-science-of-accelerated-learning)
3. [How Sage Helps You Learn Any Concept Faster](#how-sage-helps-you-learn-any-concept-faster)
4. [The 3 Cognitive Tutoring Modes](#the-3-cognitive-tutoring-modes)
5. [The 7-Step Guided Learning Framework](#the-7-step-guided-learning-framework)
6. [Key Features & Capabilities](#key-features--capabilities)
7. [How to Install Sage on Your Phone](#how-to-install-sage-on-your-phone)
   - [Method 1: Direct APK Installation (Fastest & Recommended)](#method-1-direct-apk-installation-fastest--recommended)
   - [Method 2: Build & Install from Source (Android Studio)](#method-2-build--install-from-source-android-studio)
8. [API Key Setup & Configuration](#api-key-setup--configuration)
9. [Developer Diagnostics & Telemetry](#developer-diagnostics--telemetry)
10. [Architecture & Technical Stack](#architecture--technical-stack)
11. [Privacy & Local Data Storage](#privacy--local-data-storage)

---

## What is Sage?

Have you ever spent hours reading documentation, watching 40-minute tutorials, or skimming endless search results, only to realize you still can't apply the concept yourself? That is the **passive learning trap**.

**Sage** transforms learning from a passive spectator activity into an active cognitive partnership. Whether you are:
- A **student** preparing for computer science, mathematics, biology, or economics exams.
- A **software developer** mastering distributed systems, Kotlin coroutines, or machine learning math.
- A **professional** upskilling in project management, cloud infrastructure, or financial modeling.
- A **lifelong learner** curious about quantum mechanics, neuroscience, or world history.

Sage acts as your private professor who never loses patience, never rushes you, and instantly alters its teaching style the moment an explanation doesn't click.

---

## Why Use Sage? (The Science of Accelerated Learning)

| Traditional Learning (Web/Videos) | Learning with Sage |
| :--- | :--- |
| **Passive Consumption**: You watch or read without your brain actively formulating answers. Retention fades within 24 hours (Ebbinghaus Forgetting Curve). | **Active Recall & Retrieval Practice**: Sage poses targeted micro-questions that force your brain to retrieve and apply knowledge, multiplying retention by up to 400%. |
| **One-Size-Fits-All**: Videos and articles assume a generic audience, often moving too fast through tricky parts or droning through concepts you already know. | **Dynamic Baseline Calibration**: Sage diagnoses your exact skill tier (Beginner, Foundational, Practitioner, Advanced) and focuses 100% of effort on your specific knowledge gap. |
| **Textbook Monologues**: If an author's explanation makes no sense, re-reading the exact same sentence rarely helps. | **Multi-Angle Cognitive Reframing**: If you indicate confusion, Sage immediately switches strategies—using mechanical analogies, visual metaphors, or step-by-step code proofs. |
| **No Accountability**: It is easy to skim past difficult equations or syntax and fool yourself into thinking you understood it. | **Mastery Milestones**: Sage quizzes you with diagnostic check questions before advancing to subsequent modules. |

---

## How Sage Helps You Learn Any Concept Faster

Sage leverages proven cognitive science principles:

1. **The Feynman Technique**: Sage encourages you to explain complex ideas in plain, everyday language. If your explanation has gaps, Sage pinpoints the exact ambiguity and guides you to refine it.
2. **Cognitive Chunking**: Human working memory can only hold 3–5 items at once. Sage breaks massive, daunting subjects (e.g., "Build a Neural Network" or "Learn Quantum Computing") into bite-sized, digestible modules taught one step at a time.
3. **The Socratic Dialogue**: Rather than spoon-feeding you facts, Sage asks precision questions that illuminate contradictions in your mental model, letting you experience the "Eureka!" moment yourself.
4. **Immediate Feedback Loops**: Every prompt and quiz response receives instant constructive analysis, cementing correct mental connections before misconceptions can take root.

---

## The 3 Cognitive Tutoring Modes

You can switch between tutoring modes at any time with a single tap from the chat screen:

### 1. 🎓 Learning Mode (Recommended for Mastery)
The primary mode for comprehensive study. Sage guides you through structured curriculum roadmaps:
- Assesses your current goal and baseline knowledge.
- Proposes an interactive roadmap.
- Explains concepts incrementally with real-world examples.
- Checks your comprehension after every concept before proceeding.
- Quizzes you at milestones (~70% score required to pass).

### 2. 🏛️ Socratic Mode (Deep Critical Thinking)
In Socratic mode, Sage **never just gives you the raw answer**. 
- It asks thought-provoking counter-questions.
- It prompts you to break down your reasoning.
- Perfect for debugging code, understanding mathematical theorems, philosophical logic, and ethical dilemmas.

### 3. ⚡ Normal Mode (Direct & Crisp Answers)
Need a fast, zero-fluff clarification? Normal mode answers your query directly, concisely, and accurately, backed by concrete examples.

---

## The 7-Step Guided Learning Framework

When operating in **Learning Mode**, Sage follows an evidence-based 7-step pedagogical pipeline:

```
[ Step 1: Goal Alignment ] ────────► Clarifies what you want to build or achieve
           │
[ Step 2: Knowledge Probe ] ────────► 1 diagnostic question to test prior background
           │
[ Step 3: Level Assessment ] ───────► Calibrates: Beginner | Foundational | Practitioner | Advanced
           │
[ Step 4: Roadmap Generation ] ─────► Creates a tailored 3–5 module visual syllabus
           │
[ Step 5: Concept Delivery ] ───────► Teaches 1 concept at a time + micro-check question
           │
[ Step 6: Milestone Quizzes ] ──────► 3–5 targeted questions (70%+ passing threshold)
           │
[ Step 7: Cognitive Synthesis ] ────► End-of-session review: concepts mastered & what's next
```

---

## Key Features & Capabilities

- ⚡ **Powered by Gemini 3.5 Flash**: Lightning-fast inference (~600–900ms latency) with state-of-the-art reasoning and code comprehension.
- 🎨 **Modern Material 3 UI**: Clean emerald-sage palette, dynamic dark/light surface elevation, smooth Jetpack Compose transitions, and fluid keyboard insets.
- 🗺️ **Interactive Learning Roadmaps**: Generate, track, and complete structured curriculum milestones with visual progress bars.
- 📝 **Diagnostic Quizzing Engine**: Practice questions with instant answer evaluation, detailed rationale, and score tracking.
- 📊 **Progress & Telemetry Dashboard**: Track concepts mastered, questions answered, daily learning streaks, and historic topic archives.
- 💾 **Local-First & Offline Resilient**: Powered by an encrypted Android Room SQLite database. Your conversation history, bookmarks, and progress remain safely on your device even without an internet connection.
- 🛡️ **Dual-Engine Architecture**: Seamlessly switch between the secure server-side Cloud Run proxy or direct Google Generative AI API calls.
- 🔧 **In-App Developer Diagnostics**: Comprehensive live telemetry showing connection status, model verification, latency benchmarks, and an interactive API key manager.

---

## How to Install Sage on Your Phone

### Method 1: Direct APK Installation (Fastest & Recommended)

You can install Sage on any modern Android device (Android 8.0 Oreo or higher) in less than 2 minutes without needing a computer:

#### Step 1: Download the APK
1. Download the latest `Sage.apk` release from your AI Studio export menu, or download the APK artifact from this repository's **Releases** section.
2. If downloading directly on your phone via Chrome or Firefox, tap **Download anyway** if prompted with a standard warning.

#### Step 2: Allow Installation of Unknown Apps
Android requires you to grant permission before installing applications outside Google Play:
1. When you tap the downloaded file, a pop-up may appear: *"For your security, your phone is not allowed to install unknown apps from this source."*
2. Tap **Settings** on that prompt.
3. Toggle on **Allow from this source** (for Chrome, Files by Google, or your browser).
4. Tap the **Back** button to return to the installer.

#### Step 3: Install & Launch
1. Tap **Install**.
2. Once installation completes, tap **Open**.
3. Sage will open the **Connection Verification** screen and initialize your AI companion!

---

### Method 2: Build & Install from Source (Android Studio)

If you are a developer and want to inspect the code, modify UI components, or compile the APK yourself:

#### Prerequisites
- **Android Studio** (Ladybug | 2024.2.1 or newer recommended)
- **JDK 17** or **JDK 21**
- **Android SDK Platform 35** (Android 15)
- **Git** installed on your workstation

#### Step 1: Clone the Repository
```bash
git clone https://github.com/your-username/sage-learning-companion.git
cd sage-learning-companion
```

#### Step 2: Configure Your Gemini API Key
Create a `.env` file in the project root (or inside the `/app` folder):
```bash
cp .env.example .env
```
Open `.env` in any text editor and paste your free Google Gemini API key:
```properties
GEMINI_API_KEY=AIzaSyYourGoogleGeminiApiKeyHere
```
*(The build system uses the Secrets Gradle Plugin to safely inject this key into `BuildConfig.GEMINI_API_KEY` during compilation).*

#### Step 3: Open Project in Android Studio
1. Open Android Studio and select **Open**.
2. Navigate to the cloned folder and click **OK**.
3. Allow Gradle to sync dependencies (Room, Retrofit, Jetpack Compose, Moshi).

#### Step 4: Run on Your Physical Phone or Emulator
1. Enable **Developer Options** on your Android phone:
   - Go to **Settings** > **About Phone**.
   - Tap **Build Number** 7 times until you see *"You are now a developer!"*.
   - Go to **Settings** > **System** > **Developer Options** and enable **USB Debugging**.
2. Connect your phone via USB cable and allow USB debugging when prompted on your screen.
3. In Android Studio, select your phone in the top device dropdown and click the green **Play (Run)** button (or press `Shift + F10`).
4. To build a standalone APK file from terminal:
   ```bash
   gradle assembleDebug
   ```
   The APK will be generated at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## API Key Setup & Configuration

Sage supports **two flexible operating configurations**:

### Option A: In-App Setup (No Coding Required)
1. Get a free Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Open Sage on your phone.
3. On the startup verification screen (or via the gear icon in the top app bar), tap **Developer Diagnostics & API Key Setup**.
4. Scroll to the **Google Gemini API Key** card.
5. Paste your key and tap **Save Key**.
6. Tap **Test Connection** — Sage will verify live communication and display a green success badge!

### Option B: Build-Time Injection (`.env`)
Add your key to `.env` as shown in [Method 2](#method-2-build--install-from-source-android-studio). The app automatically detects and utilizes the injected key on launch.

---

## Developer Diagnostics & Telemetry

Sage includes a built-in diagnostic suite accessible at any time:

- **Live Heartbeat Probe**: Tests end-to-end latency and model responsiveness.
- **Model Fallback System**: Automatically fails over from `gemini-3.5-flash` to `gemini-3.5-flash-lite` or `gemini-3.6-flash` if rate limits occur.
- **Network Telemetry**: Displays latency in milliseconds, architecture type, and error diagnostics.
- **Offline Learning Bypass**: If you are in airplane mode or have no connectivity, tap **Continue to App (Offline Mode)** to access your saved notes, roadmaps, and local topics.

---

## Architecture & Technical Stack

Sage is engineered adhering to modern Android Architecture Components and Clean MVVM principles:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  Jetpack Compose • Material 3 • Navigation • SageViewModel  │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│                      Domain & Logic                         │
│   7-Step Pedagogical Engine • Tutoring Modes • Formatter    │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌──────────────────────────────┐    ┌─────────────────────────┐
│       Local Data Layer       │    │    Remote Network Layer │
│  Room Database (SQLite)      │    │  Retrofit 2 • Moshi     │
│  Topic & Message Entities    │    │  Gemini 3.5 Flash REST  │
│  Encrypted SharedPreferences │    │  Cloud Run HTTPS Proxy  │
└──────────────────────────────┘    └─────────────────────────┘
```

- **UI Framework**: Jetpack Compose with Material Design 3 (M3) components.
- **State Management**: `ViewModel`, Kotlin Coroutines, and `MutableStateFlow` with lifecycle-aware collection.
- **Local Database**: Android Room SQLite with KSP (Kotlin Symbol Processing).
- **Networking**: Retrofit 2, OkHttp 4 with HTTP logging, and Moshi Kotlin JSON serialization.
- **AI Integration**: Google Gemini Generative Language REST API (`v1beta`).
- **Testing**: Robolectric local JVM testing and Roborazzi screenshot regression verification.

---

## Privacy & Local Data Storage

Your privacy is paramount:
- **No Third-Party Tracking**: Sage contains zero third-party advertising SDKs or analytics trackers.
- **Local History**: Your questions, roadmaps, and quiz histories are stored exclusively in your on-device Room SQLite database.
- **Direct Encryption**: API keys configured on your phone are stored securely in Android private app preferences and are never shared or logged.

---

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <b>Built with ❤️ using Google AI Studio, Jetpack Compose, and Gemini 3.5 Flash.</b><br>
  <i>Empowering curious minds to master any subject, one concept at a time.</i>
</p>
