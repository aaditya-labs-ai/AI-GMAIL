# AI-GMAIL Security Architecture

_Last updated: 2026-09-22 (security-hardening audit)_

## Component map (as verified from source)

```
MainActivity (single exported launcher activity)
└── MainAppScreen (Compose navigation host)
    ├── InboxScreen / EmailDetailSheet / ComposeEmailSheet
    ├── ColdMailScreen, AutomationScreen, SocialHubScreen
    ├── AICopilotScreen, SettingsDialog
    └── AccountAuthDialog
        └── AssistantViewModel (AndroidViewModel)
            ├── FirebaseAuthService  → Firebase Auth (Google Sign-In, Credential Manager, nonce)
            ├── AssistantRepository
            │   ├── Room (AppDatabase: emails, gmail_threads, gmail_messages, drafts,
            │   │         cold_mail_campaigns, automation_rules, automation_logs,
            │   │         social_accounts, social_outreach_posts)
            │   ├── GmailApiClient (Retrofit/OkHttp → gmail.googleapis.com)
            │   │   ├── GmailOAuthManager (in-memory access token, 1-min expiry margin)
            │   │   └── GmailAuthInterceptor (Bearer injection, 401 → session invalidation)
            │   ├── AiRepository → AiBackendApi (Retrofit → AI_BACKEND_URL, configurable)
            │   │   └── FirebaseIdTokenProvider (FirebaseUser.getIdToken(false))
            │   └── FirestoreService (per-UID document paths, verified against auth UID)
            └── GmailAssistantApp (Application: Firebase init + App Check provider install)
```

## The three separate security systems

| System | Purpose | Credential | Where it lives |
|---|---|---|---|
| Firebase Authentication | Identity | Firebase Web API key (public client identifier) | `.env` → BuildConfig |
| Gmail OAuth 2.0 | Mailbox authorization | Short-lived access token | Memory only (`GmailOAuthManager`) |
| AI backend auth | AI request authorization | Firebase ID token (Bearer, ~1 h lifetime) | Memory only, attached per request |

These are intentionally separate: signing in with Google (Firebase) does **not**
grant Gmail API scopes, and neither grants AI access by itself.

## AI request security chain

```
Android client
  1. FirebaseUser must exist  (fail closed otherwise)
  2. FirebaseUser.getIdToken(false) → short-lived ID token
  3. AiRepository validates: prompt (non-blank, ≤ 32,000 chars),
     model (allowlist), temperature (clamped 0–2), thinkingBudget (clamped 0–8192),
     image/audio payload size (≤ 4,000,000 base64 chars each)
  4. POST /v1/ai/generate with Authorization: Bearer <id-token>
AI backend (NOT in this repository)
  5. Verify ID token with Firebase Admin SDK → verified UID
  6. Authorization, rate limiting, per-user quotas
  7. Call Gemini with the server-held API key
  8. Return text to client
```

Client-side validation is defense-in-depth; the backend must independently
re-validate everything (see `docs/BACKEND_SECURITY_CONTRACT.md`).

## Prompt-injection defense

All email-derived content (subject, body, snippets) is wrapped in explicit
`<untrusted_email_content>` / `<untrusted_pitch_content>` boundaries inside
prompts, with instructions to treat the enclosed text as passive data
(`AssistantRepository.analyzeEmailForSmartReplies`, `generateSmartReply`,
`summarizeEmail`, `convertToSocialOutreach`). System instructions state that
embedded instructions must not be obeyed. The AI output is used for drafting and
summarizing only — no code path allows AI output to send, delete, or modify email
without an explicit user confirmation dialog.

## Local storage & privacy

- Email contents, drafts, and AI summaries are cached in a local Room database.
- Backups are excluded (`allowBackup=false`, backup rules exclude databases and
  preferences from both cloud backup and device transfer).
- Signing out purges **all** user-generated local tables (emails, gmail
  threads/messages, drafts, campaigns, automation rules/logs, social outreach)
  and resets in-memory UI state, preventing cross-account contamination.
- Gmail OAuth tokens are never written to disk.

## Logging

`SafeLogger` strips debug/info logs in release builds and redacts Bearer tokens,
`AIza…` API keys, and `key/token/password/secret` assignments from every message.
OkHttp logging is disabled (`Level.NONE`) for the AI backend client and restricted
to `BASIC` (headers redacted) in debug for the Gmail client.

## Known limitations (verified)

1. **Gmail OAuth token acquisition is not implemented in the app.**
   `GmailOAuthManager.setSession` is never called from production code — only tests.
   Until a real OAuth flow is added (requires Google Cloud OAuth client config),
   all live Gmail operations correctly fail closed with an authorization-required
   message.
2. **The AI backend service is not part of this repository.** `AI_BACKEND_URL`
   is a placeholder until a real backend is deployed and configured in `.env`.
3. **App Check is installed but enforcement is off.** Enforcement must be enabled
   in the Firebase Console only after legitimate traffic is verified.
