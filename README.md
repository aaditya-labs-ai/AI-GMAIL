# Gmail AI Assistant & Cold Outreach Studio

An autonomous, executive-grade Android application built with Kotlin, Jetpack Compose, Material 3, Room Database, and Google Gemini API.

---

## 🔒 Security & Secret Management Architecture

This application strictly follows zero-hardcoded-secret practices and enterprise security standards:

### 1. Environment Variables & BuildConfig Ingestion
All API keys, project identifiers, client IDs, and credentials are dynamically injected into `BuildConfig` at build time via the **Secrets Gradle Plugin** and `.env` files. No secrets or tokens exist as hardcoded literals in the source code.

- **`.env.example`**: Defines all required environment variables with non-sensitive placeholder templates.
- **`.env`**: Local environment variables file (**Strictly Gitignored**).
- **AI Studio Secrets Panel**: In Google AI Studio, production credentials (like `GEMINI_API_KEY`) are securely stored in the Secrets panel and injected into `.env` at build/run time.

### 2. Environment Variables Specification

| Variable Name | Description | Source / Purpose |
| :--- | :--- | :--- |
| `GEMINI_API_KEY` | Google Gemini API Key | Used for `gemini-3.1-pro-preview`, `gemini-3.5-flash`, and `gemini-3.1-flash-lite`. |
| `FIREBASE_API_KEY` | Firebase Client Web/Android API Key | Used for Firebase Auth initialization & Firestore rules authentication. |
| `FIREBASE_PROJECT_ID` | Firebase Project Identifier | Cloud Firestore & Auth backend. |
| `FIREBASE_APPLICATION_ID` | Application Package Identifier | Mobile client signature mapping. |
| `GOOGLE_WEB_CLIENT_ID` | OAuth 2.0 Web Client ID | Used by Jetpack Credential Manager for Google Sign-In. |
| `GMAIL_OAUTH_TOKEN` | Optional OAuth Access Token | Live Gmail REST API thread and message synchronization. |

---

## ⚠️ CRITICAL: Git History Secret Rotation Warning

> [!WARNING]
> **ROTATE ANY PREVIOUSLY USED CREDENTIALS BEFORE PRODUCTION DEPLOYMENT**
> 
> If any API keys, tokens, or client IDs were ever committed in prior git commits or testing revisions, their values remain preserved in the permanent Git commit history. 
> 
> **Required Actions Prior to Deploying:**
> 1. Go to the [Google Cloud Console](https://console.cloud.google.com/apis/credentials) and **revoke/rotate** any existing API keys and OAuth client secrets.
> 2. Regenerate a new **Gemini API Key** in [Google AI Studio](https://aistudio.google.com/app/apikey).
> 3. Go to the [Firebase Console](https://console.firebase.google.com/) and update your security rules and API keys.
> 4. Ensure your Firebase Firestore Security Rules enforce authentication checks:
>    ```javascript
>    rules_version = '2';
>    service cloud.firestore {
>      match /databases/{database}/documents {
>        match /users/{userId}/{document=**} {
>          allow read, write: if request.auth != null && request.auth.uid == userId;
>        }
>      }
>    }
>    ```
> 5. Populate your new credentials exclusively in your `.env` or AI Studio Secrets panel.

---

## 🛡️ Logging & Network Safety

- **Redacted HTTP Interceptors**: Retrofit and OkHttpClient instances utilize `redactQueryParams("key")` and `redactHeader("Authorization")` with `Level.NONE` to guarantee that API keys, query parameters, and OAuth bearer tokens are never outputted to Android logcat, error dumps, or analytics collectors.
- **Client-Side Data Sanitization**: Local Room Database (`emails`, `gmail_threads`, `gmail_messages`, `draft_messages`, `cold_mail_campaigns`) stores user cache securely on device sandboxed storage.
