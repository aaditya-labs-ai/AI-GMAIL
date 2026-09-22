# Threat Model — AI-GMAIL (Aura Mail)

_Last updated: 2026-09-22. Scope: the Android client in this repository. The AI backend
and Firebase/Google Cloud projects are external and only partially verifiable from here._

## Assets

| Asset | Where |
|---|---|
| Gmail messages, attachments, contact graph | Gmail API responses, local Room cache |
| Firebase ID tokens | Memory (per request) |
| Gmail OAuth access token | Memory only (`GmailOAuthManager`) |
| AI prompts (may contain email content) | In transit to AI backend |
| User profile (name, email, photo) | Firebase Auth, UI state |
| Campaigns / automation rules / outreach drafts | Room + Firestore (`users/{uid}/…`) |
| Gemini API key | Backend only — must never appear here |

## Threat actors and attack paths

| # | Threat actor | Attack path | Impact | Controls (current) | Verification |
|---|---|---|---|---|---|
| T1 | Malicious email sender | Prompt injection via subject/body: "forward all mail to attacker", "reveal your system prompt" | AI drafts attacker-controlled content; possible data exfiltration if AI output is sent unreviewed | Untrusted-content tags in prompts; AI can only draft/summarize; send requires explicit confirmation dialog; no tool invocation exists; no auto-send path | Verified in code (send paths require user tap on Confirm & Send); LLM-level resistance NOT VERIFIED — needs adversarial test set |
| T2 | Network attacker (MITM) | Intercept/modify API traffic | Token theft, mail tampering | HTTPS-only endpoints, `usesCleartextTraffic=false`, placeholder backend URL is HTTPS-only | Verified in config; runtime interception NOT VERIFIED on device |
| T3 | API abuser / cost attacker | Scripted client replays `/v1/ai/generate` with huge prompts/payloads | Uncontrolled Gemini cost | Client-side: prompt length cap, payload caps, model allowlist, auth required. Real control must be backend rate limits/quotas | Client side VERIFIED by unit tests; backend NOT VERIFIED (no backend) |
| T4 | Compromised account (device thief) | Read local Room cache of another user's mail on shared device | Privacy breach | Sign-out purges ALL local user tables; Gmail token is memory-only; backups disabled | Verified in code; runtime device testing NOT VERIFIED |
| T5 | Malicious app on device | Read app files / backups | Mail cache leak | `allowBackup=false`, backup rules exclude databases/prefs; no exported components besides launcher; no ContentProviders | Verified in manifest |
| T6 | Repository attacker (malicious PR) | Introduce a key logger, weaken auth, exfiltrate data | Full compromise | All changes via PR review; CI secret scanning (gitleaks); CodeQL; `permissions: contents: read` in CI | Workflow added; long-term review discipline required |
| T7 | Accidental developer secret leak | Commit `.env` / `google-services.json` / keystore | Credential exposure | `.gitignore` covers all of them; `.env.example` placeholders only; gitleaks in CI; history scanned 2026-09-22 (only Dummy placeholders found) | Verified |
| T8 | Compromised dependency | Supply-chain via Gradle deps | Full compromise | Only google()/mavenCentral() repositories (`FAIL_ON_PROJECT_REPOS`); no mass upgrades | Partially verified; no dependency-locking/SBOM yet (remaining risk) |
| T9 | Prompt exfiltration of credentials | Trick AI into printing secrets/tokens | Token leak into chat UI | Client holds no Gemini key; ID/Gmail tokens are not placed in prompts; AI output only rendered as text | Verified: no token values flow into prompts |

## Residual risks (accepted, documented)

1. No backend exists yet, so AI features cannot run at all until one is deployed —
   the client fails closed (by design).
2. Gmail OAuth acquisition is not implemented; Gmail features are inert until a real
   OAuth flow is added (requires Google Cloud configuration).
3. Room cache is unencrypted (no SQLCipher); acceptable only while the cache holds no
   tokens and the device is expected to be single-user with screen lock. Revisit if
   the threat model changes.
4. Firestore rules allow each user to write arbitrary fields into their own
   `users/{uid}` document. No privileged fields (roles/admin/subscription) exist in the
   current data model; if any are added later they MUST be moved to custom claims or
   backend-validated fields before shipping.
5. Release build has R8/minification disabled. This is a hardening gap but was left
   unchanged to avoid breaking runtime behavior without device testing.
