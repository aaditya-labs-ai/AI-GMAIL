# Aura Mail — AI-Powered Gmail Assistant & Outreach Studio

A luxury, executive-grade AI email client and automated assistant built with Kotlin, Jetpack Compose, Material 3, Room Database, and Google Gemini API, inspired by modern editorial aesthetics.

---

## 🎨 Visual Identity & Aesthetic System ("Aura Theme")

- **Background Canvas**: Warm Alabaster (`#FAF7F0`)
- **Card Background**: Soft Parchment (`#FDFBF7`)
- **Selected Card / Active State**: Highlighted Warm Cream (`#F4ECE1`)
- **Borders & Dividers**: Soft Paper Linen (`#E8E2D5`)
- **Primary Typography**: Deep Espresso Charcoal (`#2B2824`)
- **Secondary Subtitles & Labels**: Warm Muted Cashmere (`#8C8375`)
- **AI Accents & Sparkles**: Warm Amber Gold (`#935F28`) & Highlight (`#E6C387`)

---

## 🔒 Security Audit & 5-Stage Verification Report

This application has undergone a full-spectrum security audit addressing all commands specified in the security specification:

### Check 1: Secret Leak Prevention (Gitleaks Standard)
- **Zero Hardcoded Secrets**: All API keys, Client IDs, and OAuth credentials have been extracted from source code and are dynamically injected via `BuildConfig` using Gradle Secrets and `.env` files.
- **Gitignore Protection**: `.env`, `debug.keystore`, build caches, and sensitive credential files are strictly excluded from source control.
- **Placeholder Configuration**: `.env.example` provides non-sensitive template definitions.
- **Log Sanitation**: No authorization headers, bearer tokens, or API keys are printed in Android `Log` or OkHttp interceptors.

### Check 2: Personal Data Flow Audit (Bearer Standard)
- **PII Protection**: User emails, contact names, and email contents are stored exclusively on-device in the local SQLite Room database with encrypted storage considerations.
- **Logger Redaction**: Network logging utilizes custom redactions so headers like `Authorization` and `X-Goog-Api-Key` are hidden.
- **Credential Storage**: Google Sign-In tokens are managed securely via Jetpack `CredentialManager` without storing raw passwords or plain secrets on disk.
- **User Disconnect & Data Purge**: Users can sign out and clear offline caches at any time through the Account Dialog.

### Check 3: Pre-Deploy Production Audit
- **TLS/SSL Enforcement**: All network endpoints (Gemini API, Google OAuth, Gmail REST API, Firebase) enforce HTTPS / TLS 1.3 encryption.
- **Manifest Hardening**: Cleartext traffic (`android:usesCleartextTraffic="false"`) and unencrypted local backups (`android:allowBackup="false"`) are strictly disabled in `AndroidManifest.xml`.
- **Safe Error Handling**: Network and AI generation failures gracefully fall back to local offline Room database caches without exposing raw stack traces or internal backend schemas to the UI.

### Check 4: Deep Security Audit for Complex Logic
- **Parameterized SQL**: All database operations in `EmailDao`, `ColdMailDao`, `AutomationDao`, and `SocialHubDao` use Room's parameterized SQL queries to prevent SQL injection vulnerabilities.
- **XSS & Injection Protection**: HTML email bodies and dynamic prompts are sanitized before rendering or forwarding to the Gemini models.
- **Authorization Gating**: Cloud operations require authenticated Google accounts (`request.auth != null && request.auth.uid == userId`).

### Check 5: Attacker's Perspective Review
- **ID Manipulation & IDOR**: Email selections and draft mutations validate ownership and thread consistency before execution.
- **Rate Limiting & Cost Protection**: Gemini API requests include client-side debouncing, token caps, and temperature controls to prevent resource exhaustion.
- **Business Logic Integrity**: Draft generation and message sending actions require explicit user confirmation or automated rule criteria.

---

## ⚠️ Security Notice: Git History Secret Rotation

> [!WARNING]
> **ROTATE ANY PREVIOUSLY USED CREDENTIALS BEFORE PRODUCTION DEPLOYMENT**
> 
> 1. Revoke/rotate previous OAuth client secrets and API keys in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
> 2. Regenerate your **Gemini API Key** in [Google AI Studio](https://aistudio.google.com/app/apikey).
> 3. Store new keys directly in AI Studio's Secrets panel or `.env`.
