# Aura Mail — AI-Powered Gmail Assistant & Outreach Studio

A luxury, executive-grade AI email client and automated assistant built with Kotlin, Jetpack Compose, Material 3, Room Database, and a secure backend AI proxy.

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

## 🔒 Security Overview (honest, evidence-based)

Full details live in [`SECURITY.md`](SECURITY.md), [`docs/SECURITY_ARCHITECTURE.md`](docs/SECURITY_ARCHITECTURE.md),
[`docs/THREAT_MODEL.md`](docs/THREAT_MODEL.md), [`docs/HARDENING_LOG.md`](docs/HARDENING_LOG.md), and
[`docs/BACKEND_SECURITY_CONTRACT.md`](docs/BACKEND_SECURITY_CONTRACT.md).

What is implemented and verified in source:

- **Separate security systems**: Firebase Authentication (identity), Gmail OAuth 2.0
  with least-privilege scopes (mailbox authorization), and per-request Firebase ID
  tokens for AI backend calls (AI authorization). They are never conflated.
- **Authenticated AI requests**: every AI request carries
  `Authorization: Bearer <firebase-id-token>` and fails closed when no user is signed in.
- **Client-side request policy**: prompt length cap, model allowlist, temperature and
  thinking-budget bounds, image/audio payload size limits (see `AiRequestPolicy`).
- **No secrets in source**: API keys and IDs come from `.env` via the Secrets Gradle
  Plugin; `.gitignore` excludes `.env`, keystores, and `google-services.json`.
- **Log hygiene**: `SafeLogger` redacts tokens/keys; debug logs stripped in release;
  OkHttp logging off for the AI backend client; user-facing errors never contain
  backend exception details.
- **Privacy on sign-out**: all local user data (emails, drafts, campaigns, rules,
  outreach) is purged; Gmail tokens live in memory only and are never persisted.
- **Prompt-injection boundaries**: email content is passed to AI wrapped in
  untrusted-content tags and is never treated as application instructions; the AI can
  only draft/summarize — sending email requires explicit user confirmation.
- **Manifest hardening**: `usesCleartextTraffic="false"`, `allowBackup="false"`,
  backups exclude databases and preferences; only the launcher activity is exported.

Known limitations (documented, not hidden):

- **Gmail OAuth acquisition is not yet implemented** — the app fails closed with an
  authorization-required message until a Google Cloud OAuth client is configured
  (see "Gmail OAuth setup" below).
- **The AI backend service is not part of this repository** — `AI_BACKEND_URL` must be
  configured in `.env` after deploying a backend that implements
  [`docs/BACKEND_SECURITY_CONTRACT.md`](docs/BACKEND_SECURITY_CONTRACT.md).
- **App Check is installed but enforcement is off** — enable it in the Firebase Console
  only after verifying legitimate traffic.

---

## ⚙️ Local setup

1. Copy `.env.example` to `.env` and fill in your real values (never commit `.env`):
   - `FIREBASE_API_KEY`, `FIREBASE_PROJECT_ID`, `FIREBASE_APPLICATION_ID`
     (Firebase Console → Project settings → General → Your apps)
   - `GOOGLE_WEB_CLIENT_ID` (Google Cloud Console → Credentials → OAuth client, type
     "Web application")
   - `AI_BACKEND_URL` (your authenticated AI backend, HTTPS only)
2. Place `google-services.json` in the project root if you use the google-services
   plugin flow (it is gitignored).
3. Build: `gradle assembleDebug` (tests: `gradle test`).

## 📦 Release builds & signing in CI

The `release-build` CI job builds a minified (R8) release APK on every push. To make it
produce a **signed** APK, add three encrypted secrets in GitHub
(**Settings → Secrets and variables → Actions → New repository secret**):

| Secret name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | your upload keystore file, Base64-encoded: `base64 -w 0 my-upload-key.jks` |
| `ANDROID_KEYSTORE_PASSWORD` | the keystore (store) password |
| `ANDROID_KEY_PASSWORD` | the key password (key alias is `upload`, from `app/build.gradle.kts`) |

Until these exist, the job builds an unsigned release APK — still useful to verify the
R8/ProGuard pass. Secrets are never printed in logs and the keystore is never committed.
The signed APK appears under each workflow run's **Artifacts** (`release-apk`).
If you rotate your keystore, update the secrets; old artifacts remain signed with the
old key.

## 📧 Gmail OAuth setup (required for live Gmail features)

Firebase Google Sign-In does **not** grant Gmail API scopes. To enable live inbox sync
and sending you must additionally:

1. In Google Cloud Console → APIs & Services → enable the **Gmail API**.
2. Create an OAuth consent screen and configure the scopes
   `https://www.googleapis.com/auth/gmail.readonly` and
   `https://www.googleapis.com/auth/gmail.send`.
3. Configure an OAuth client for your application signature/package
   (`com.aistudio.gmailassistant.kdqpxz`).
4. In the app: open **Settings → Gmail Connection → Connect Gmail Account**.
   The token acquisition flow is implemented (`GmailAuthorizationClient`, least
   privilege: `gmail.readonly` + `gmail.send`, in-memory token only, fails
   closed) — see SEC-014 in `docs/HARDENING_LOG.md`. It will show an error
   until steps 1–3 are configured, which is intended.

Also deploy the hardened `firestore.rules` (SEC-015): Firebase Console →
Firestore Database → Rules → paste the contents of `firestore.rules` → Publish.

---

## ⚠️ Security notice: credential rotation

> [!WARNING]
> **ROTATE ANY PREVIOUSLY USED CREDENTIALS BEFORE PRODUCTION DEPLOYMENT**
>
> 1. Revoke/rotate previous OAuth client secrets and API keys in the
>    [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
> 2. Regenerate your **Gemini API Key** in [Google AI Studio](https://aistudio.google.com/app/apikey).
> 3. Store new keys only in `.env` (local) or your backend's secret manager — never in source.
>
> Removing a secret from the latest commit does not revoke it; assume anything ever
> committed stays compromised until rotated. (A full history scan on 2026-09-22 found
> only placeholder values, but rotate out of caution.)
