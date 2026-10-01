# Sage — Intelligent AI Learning Companion

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-green.svg?style=flat&logo=android)](https://www.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Gemini](https://img.shields.io/badge/AI%20Engine-Gemini%203.5%20Flash-orange.svg?style=flat&logo=google)](https://ai.google.dev/)
[![Room Database](https://img.shields.io/badge/Database-Room%20SQLite-teal.svg?style=flat)](https://developer.android.com/training/data-storage/room)
[![LaTeX Support](https://img.shields.io/badge/Math-SageRichText%20%2B%20LaTeX-blue.svg?style=flat)]()
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-blue.svg?style=flat)]()

> **"Tell me and I forget. Teach me and I remember. Involve me and I learn."** — Benjamin Franklin

**Sage** is an intelligent, calm, and pedagogically engineered AI learning companion built natively for Android using Jetpack Compose, Material Design 3, and Google's latest **Gemini 3.5 Flash** models.

Unlike generic chatbots that dump walls of text, Sage is designed from the ground up as a **personal 1-on-1 academic tutor**. It assesses your baseline understanding, grounds explanations in official college curricula (JIS College of Engineering / MAKAUT), renders textbook-grade mathematical LaTeX formatting, creates tailored roadmaps, and adapts its explanations whenever you feel stuck.

---

## 🌟 What's New in Sage

### 1. 🎓 Official Academic Curriculum & Syllabus Grounding
- **10+ Supported Engineering & Professional Departments**: CSE (AI & ML), IT, CST, ECE, EE, ME, CE, BME, BBA, MCA.
- **Regulation R25 / R26 Official Syllabus Integration**: Course codes, prerequisites, course outcomes, credit structures, and prescribed textbook references.
- **Pedagogical Directive**: *"The Syllabus is the Roadmap."* Explanations, exam questions, and study tools strictly align with prescribed institutional curriculum standards.

### 2. 📸 Multi-Modal AI Study Tools
- **Scan & Solve (Visual Problem Solver)**: Capture or upload textbook problems, circuits, and handwritten math equations for step-by-step AI breakdowns with full mathematical formatting.
- **AI Academic Notes Generator**: Structured lecture summaries, high-yield exam takeaways, and core concept breakdowns.
- **Interactive Flashcards**: Active recall study cards with flip animations, key definitions, and formula memorization.
- **Hierarchical Mind Maps**: Visual conceptual trees organizing modules into subtopics, dependencies, and algorithm trees.
- **Rapid Revision Sheet**: Last-minute exam cramming sheets highlighting common student misconceptions and high-frequency exam facts.
- **AI Formula Sheet with SI Units**: Mathematical formulas with variable descriptions, standard SI units, and derivation guidance.

### 3. 📐 Textbook-Grade Mathematical LaTeX Typesetting Engine
- **`SageRichText` & `MathRenderer`**: Native Jetpack Compose formatting engine converting raw LaTeX expressions into textbook typography.
- **Full Notation Support**: Stacked division fractions ($\frac{2I_m}{\pi}$), radicals ($\sqrt{R^2 + (X_L-X_C)^2}$), Greek letters ($\alpha, \beta, \pi, \theta, \omega, \Delta, \sum, \int$), subscripts, superscripts, and matrix layouts.
- **`MathFormulaCard`**: Dedicated glassmorphism cards with one-tap clipboard copying.

### 4. 💬 Interactive Chat Workspace with Message Edit & AI Retry
- **In-Place Message Editing**: Tap **Edit** on any sent user message to convert it into a multiline glassmorphism text field. Tapping **Update** updates the message, prunes subsequent downstream turns, and cleanly regenerates the conversation branch.
- **AI Response Retry**: Regenerate responses with one tap while preserving conversation context. Automatic rollback protection restores previous answers if network/API errors occur.
- **Zero-Gap Keyboard Dock**: Fluid Compose window insets docking the input field directly above the Android soft keyboard.

### 5. 🎯 Adaptive Learning & Weekly Retention Review (Phase C3)
- **Weak Concept Remediation**: Tracks incorrect quiz answers and auto-generates targeted practice sessions.
- **Weekly Analytics**: Visual charts summarizing focus time, quiz mastery percentages, and syllabus completion velocity.

### 6. ⏱️ Focus Mode & Pomodoro Workspaces (Phase C1 & C3)
- **Dedicated Focus Timer**: 5-minute daily mission focus, 25-minute Pomodoro, and 50-minute deep-work cycles.
- **Non-Transparent Topic Modal**: Dedicated full-screen curriculum selector with live subject search and module filters.

### 7. 📜 Previous Year Questions (PYQ) Integration (Phase B)
- **Exam Archive**: Filter past university questions by year, subject, and module with instant AI-powered solution breakdowns.

---

## 📚 Table of Contents

1. [Why Use Sage? (The Science of Accelerated Learning)](#why-use-sage-the-science-of-accelerated-learning)
2. [The 3 Cognitive Tutoring Modes](#the-3-cognitive-tutoring-modes)
3. [The 7-Step Guided Learning Framework](#the-7-step-guided-learning-framework)
4. [Comprehensive Feature Matrix](#comprehensive-feature-matrix)
5. [Architecture & Technical Stack](#architecture--technical-stack)
6. [How to Install Sage on Your Phone](#how-to-install-sage-on-your-phone)
7. [API Key Setup & Configuration](#api-key-setup--configuration)
8. [Developer Diagnostics & Telemetry](#developer-diagnostics--telemetry)
9. [Repository Visibility & Sharing](#repository-visibility--sharing)
10. [Privacy & Security](#privacy--security)

---

## Why Use Sage? (The Science of Accelerated Learning)

| Traditional Learning (Web/Videos) | Learning with Sage |
| :--- | :--- |
| **Passive Consumption**: Watching or reading without active formulation. Retention fades within 24 hours (Ebbinghaus Forgetting Curve). | **Active Recall & Retrieval Practice**: Sage poses targeted micro-questions that force your brain to retrieve and apply knowledge, multiplying retention by up to 400%. |
| **One-Size-Fits-All**: Videos and generic articles assume a generic audience, glossing over tricky parts or repeating basics. | **Curriculum Baseline Calibration**: Sage diagnoses your exact academic profile (Department, Year, Semester) and aligns 100% of teaching with your course syllabus. |
| **Textbook Monologues**: If an author's explanation makes no sense, re-reading the exact same sentence rarely helps. | **Multi-Angle Cognitive Reframing**: If you indicate confusion, Sage switches strategies—using intuitive mechanical analogies, visual metaphors, or step-by-step code proofs. |
| **Raw LaTeX Artifacts**: Other tools display raw unparsed LaTeX text like `\frac{1}{2\pi fC}`. | **Textbook Typesetting**: Sage converts mathematical notation into stacked fractions, Greek symbols, and radical signs. |

---

## The 3 Cognitive Tutoring Modes

You can switch between tutoring modes at any time from the chat workspace:

### 1. 🎓 Learning Mode (Structured Mastery)
The primary mode for comprehensive curriculum study:
- Assesses your current goal and baseline knowledge.
- Follows the official course syllabus step-by-step.
- Explains concepts incrementally with real-world engineering examples.
- Checks comprehension after every concept before advancing.
- Quizzes you at milestones (~70% score required to pass).

### 2. 🏛️ Socratic Mode (Deep Critical Thinking)
Sage **never just gives you the raw answer**:
- Asks insightful counter-questions.
- Prompts you to break down your reasoning and identify edge cases.
- Ideal for debugging code, algorithm proofs, and complex architectural trade-offs.

### 3. ⚡ Normal Mode (Direct & Crisp Briefings)
Need a rapid, direct clarification? Normal mode delivers accurate, concise explanations supported by key equations and code snippets.

---

## The 7-Step Guided Learning Framework

When operating in **Learning Mode**, Sage follows an evidence-based 7-step pedagogical pipeline:

```
[ Step 1: Goal Alignment ] ────────► Identifies syllabus module or engineering problem
           │
[ Step 2: Knowledge Probe ] ────────► 1 diagnostic question to test prerequisite background
           │
[ Step 3: Level Assessment ] ───────► Calibrates: Beginner | Foundational | Practitioner | Advanced
           │
[ Step 4: Roadmap Generation ] ─────► Creates a tailored 3–5 module visual syllabus
           │
[ Step 5: Concept Delivery ] ───────► Teaches 1 concept at a time with textbook LaTeX math
           │
[ Step 6: Milestone Quizzes ] ──────► Interactive multi-choice questions with instant rationale
           │
[ Step 7: Cognitive Synthesis ] ────► End-of-session review: concepts mastered & next roadmap topic
```

---

## Comprehensive Feature Matrix

- ⚡ **Gemini 3.5 Flash Inference**: Fast response generation (~600–900ms) with strong reasoning and code analysis.
- 🎨 **Glassmorphism Design System**: Dynamic dark obsidian palette (`#08080F`), multi-level glass surfaces (L1–L4), subtle neon glows, and animated transitions.
- 📐 **Integrated LaTeX Typesetting Engine**: Automatic rendering of equations, fractions, square roots, and variable definitions.
- 📸 **Camera & Image Vision Solver**: Multi-modal problem analysis using Android Photo Picker and camera input.
- 📝 **6 Built-in Academic Study Tools**: Notes, Flashcards, Mind Maps, Revision Sheets, Formula Sheets, and Scan & Solve.
- ✏️ **In-Place Chat Message Editing**: Edit user messages in-line with automatic conversation branching.
- 🔄 **AI Response Retry**: 1-tap regeneration with failure rollback protection.
- 🗺️ **Interactive Academic Roadmaps**: Visual progress bars, completed topic counters, and syllabus trackers.
- ⏱️ **Focus Timer & Pomodoro**: Dedicated study timer with mission integration.
- 📊 **Adaptive Learning & Progress Analytics**: Weekly retention tracking and streak accountability.
- 💾 **Offline-Resilient Room Database**: Complete local persistence for topics, chat history, and study tools.

---

## Architecture & Technical Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│  Jetpack Compose • Material 3 • SageRichText • SageViewModel│
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Events
┌──────────────────────────────▼──────────────────────────────┐
│                      Domain & Logic                         │
│  Pedagogical Engine • MathRenderer • Adaptive Learning Repo │
└──────────────────────────────┬──────────────────────────────┘
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
┌──────────────────────────────┐    ┌─────────────────────────┐
│       Local Data Layer       │    │    Remote Network Layer │
│  Room Database (SQLite)      │    │  Retrofit 2 • Moshi     │
│  Messages, Topics, Profiles  │    │  Gemini 3.5 Flash REST  │
│  Saved Study Tools, Missions │    │  Cloud Run HTTPS Proxy  │
└──────────────────────────────┘    └─────────────────────────┘
```

- **Language**: Kotlin 2.0+ (100% Kotlin Coroutines & Flow).
- **UI Framework**: Jetpack Compose with Material Design 3 (M3).
- **Persistence**: Room Database with SQLite and KSP.
- **Serialization**: KotlinX Serialization & Moshi.
- **AI Backend**: Google Gemini Generative AI via secure HTTPS Proxy / direct API.
- **Math Engine**: Native `SageRichText` AST parser with Unicode LaTeX transliteration and fraction composition.

---

## How to Install Sage on Your Phone

### Method 1: Direct APK Installation (Fastest)

1. Download the latest `Sage.apk` release artifact from AI Studio or the GitHub Releases tab.
2. Open the downloaded file on your Android phone.
3. Tap **Allow from this source** if prompted by Android.
4. Tap **Install** and launch Sage!

### Method 2: Build from Source (Android Studio)

```bash
# Clone the repository
git clone https://github.com/your-username/sage-learning-companion.git
cd sage-learning-companion

# Set up environment variables
cp .env.example .env

# Build debug APK
gradle assembleDebug
```
The compiled APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

## API Key Setup & Configuration

Sage supports **two easy setup methods**:

### Option A: In-App Setup (No Code Required)
1. Get a free Gemini API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Open Sage on your phone and tap **Developer Diagnostics & API Key Setup**.
3. Enter your Gemini API key in the input field and tap **Save Key**.
4. Tap **Test Connection** to verify live communication.

### Option B: Build-Time Injection (`.env`)
Create a `.env` file in the project root:
```properties
GEMINI_API_KEY=AIzaSyYourGoogleGeminiApiKeyHere
```
The Secrets Gradle Plugin automatically injects this into `BuildConfig.GEMINI_API_KEY` during compilation.

---

## Repository Visibility & Sharing

> ℹ️ **How to change repository visibility (Private to Public)**:
>
> Repository visibility (Private vs. Public) is managed directly through your GitHub repository settings or Google AI Studio export panel:
> 1. Go to your repository on **GitHub.com** (`https://github.com/<username>/<repo-name>`).
> 2. Click the **Settings** tab at the top.
> 3. Scroll down to the **Danger Zone** at the bottom of the *General* settings page.
> 4. Under **Change repository visibility**, click **Change visibility** and select **Make public**.
> 5. Confirm by typing your repository name.

---

## Privacy & Security

- **Zero Third-Party Trackers**: No third-party ad networks, trackers, or data miners.
- **Local-First History**: All chat conversations, notes, flashcards, and progress stay encrypted inside your local Room SQLite database.
- **Secure Key Storage**: API keys are saved exclusively in private Android app storage (`EncryptedSharedPreferences` / `BuildConfig`).

---

<p align="center">
  <b>Built with ❤️ using Google AI Studio, Jetpack Compose, and Gemini 3.5 Flash.</b><br>
  <i>Empowering students and engineers to master any subject, one concept at a time.</i>
</p>
