# Hardening Log — AI-GMAIL

_Ongoing security management record. One entry per finding. Never record secrets here._

---

## SEC-001 — AI backend requests carried no authentication
- DATE: 2026-09-22
- SEVERITY: Critical
- CATEGORY: Authentication
- FILE: `app/src/main/java/com/example/data/api/AiBackendApi.kt`, `app/src/main/java/com/example/data/repository/AiRepository.kt`
- PROBLEM: `/v1/ai/generate` was called with no `Authorization` header and no check that a Firebase user existed. Any caller (or any code path) could reach the backend unauthenticated; the documented security boundary (Firebase ID token → backend verification) did not exist client-side.
- ROOT CAUSE: The backend client was built as a plain Retrofit interface with no auth interceptor or header parameter, and the repository never consulted Firebase Auth.
- FIX: Added `@Header("Authorization")` to the API; new `FirebaseIdTokenProvider` obtains `FirebaseUser.getIdToken(false)`; `AiRepository` attaches `Bearer <token>` and throws `SecurityException` (fail closed) when no user is signed in. Added `kotlinx-coroutines-play-services` explicitly for `Task.await`.
- TEST: `AiRepositorySecurityTest` — unauthenticated request fails closed with no network call; Bearer header attached when authenticated.
- COMMIT: (security: harden authentication/backend requests)
- STATUS: Verified (unit tests). End-to-end backend verification: NOT VERIFIED (no backend deployed).

## SEC-002 — No AI request input validation
- DATE: 2026-09-22
- SEVERITY: High
- CATEGORY: Authorization / abuse control
- FILE: `app/src/main/java/com/example/data/repository/AiRepository.kt`
- PROBLEM: No bounds on prompt length, model names, temperature, thinking budget, or image/audio payload sizes; client could send arbitrary content and unbounded payloads.
- ROOT CAUSE: Repository only checked for blank prompts.
- FIX: New `AiRequestPolicy` (allowlisted models; prompt ≤ 32,000 chars; systemInstruction ≤ 4,000; temperature clamped 0–2; thinkingBudget clamped 0–8192; image/audio ≤ 4,000,000 base64 chars). Backend must re-validate (documented in `docs/BACKEND_SECURITY_CONTRACT.md`).
- TEST: `AiRepositorySecurityTest` — oversized prompt/image/audio rejected; disallowed model rejected; temperature/thinkingBudget clamped.
- COMMIT: (security: harden authentication/backend requests)
- STATUS: Verified (unit tests).

## SEC-003 — `thinkingBudget` silently dropped
- DATE: 2026-09-22
- SEVERITY: High
- CATEGORY: Correctness
- FILE: `app/src/main/java/com/example/data/api/AiBackendApi.kt`, `app/src/main/java/com/example/data/api/GeminiApiService.kt`
- PROBLEM: `callHighThinking` passed `thinkingBudget = 2048` but `AiGenerateRequest` had no such field — the value never left the device.
- ROOT CAUSE: Request DTO was missing the field that the caller API accepted.
- FIX: Added `thinkingBudget` to `AiGenerateRequest` and forwarded it through `generateWithConfig`.
- TEST: `AiRepositorySecurityTest` — thinkingBudget is forwarded and clamped.
- COMMIT: (fix: resolve identified reliability/debugging issues)
- STATUS: Verified (unit tests).

## SEC-004 — Fake success and exception leakage in AI fallbacks
- DATE: 2026-09-22
- SEVERITY: High
- CATEGORY: Privacy / correctness
- FILE: `app/src/main/java/com/example/data/api/GeminiApiService.kt`
- PROBLEM: On backend failure, `generateImage` returned "Visual asset campaign generated for review." (fabricated success) and other paths returned `e.localizedMessage` (provider/backend internals) directly into the UI. The ViewModel then reported "banner generated via Imagen!" unconditionally.
- ROOT CAUSE: Fallbacks were written to look successful rather than fail honestly.
- FIX: `generateImage` now returns `null` on failure; the ViewModel reports "Banner generation failed. Please try again."; all user-facing AI error strings are generic and never contain exception details.
- TEST: Manual + covered indirectly by repository tests.
- COMMIT: (security: harden logging and secret handling; fix: reliability)
- STATUS: Verified (code inspection). Requires manual testing on device for UI text.

## SEC-005 — Sign-out left cross-account data on device
- DATE: 2026-09-22
- SEVERITY: High
- CATEGORY: Privacy / account isolation
- FILES: `app/src/main/java/com/example/data/local/Daos.kt`, `app/src/main/java/com/example/data/repository/AssistantRepository.kt`, `app/src/main/java/com/example/ui/viewmodel/AssistantViewModel.kt`
- PROBLEM: `clearUserDataOnSignOut` cleared only emails/gmail tables/drafts. Cold-mail campaigns, automation rules and logs, and social outreach posts survived sign-out — visible to the next account on a shared device.
- ROOT CAUSE: Partial table list.
- FIX: Added `clearAllCampaigns`, `clearAllRules`, `clearAllLogs`, `clearAllOutreach` DAO methods; sign-out now purges every user-generated local table and resets in-memory UI state (scheduled emails, generated content, smart replies).
- TEST: Requires manual testing (sign out → verify empty screens). Trade-off documented: local-only campaigns without cloud sync are lost on sign-out (they are synced to Firestore per UID while signed in).
- COMMIT: (security: harden Gmail OAuth/action authorization & privacy)
- STATUS: Verified (code). Manual device testing pending.

## SEC-006 — Missing `.env.example`
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Secret hygiene / build
- FILE: `.env.example` (new)
- PROBLEM: The secrets Gradle plugin and README depended on `.env.example`, but the file was absent — fresh clones lack BuildConfig fields (`FIREBASE_API_KEY`, etc.), and contributors had no placeholder template.
- ROOT CAUSE: File was never committed.
- FIX: Added `.env.example` with placeholders only (no real credentials), including new `AI_BACKEND_URL`.
- TEST: Build with only `.env.example` present (CI does this).
- COMMIT: (security: harden logging and secret handling)
- STATUS: Verified (placeholders only, history-scanned).

## SEC-007 — Backend URL hardcoded placeholder
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Configuration
- FILE: `app/src/main/java/com/example/data/api/AiBackendApi.kt`
- PROBLEM: `BACKEND_URL` was a hardcoded constant. There was no supported way to configure a real backend without editing source.
- ROOT CAUSE: Constant instead of environment configuration.
- FIX: `BACKEND_URL` now comes from `BuildConfig.AI_BACKEND_URL` (via `.env`/`.env.example`), validated to be HTTPS and non-placeholder; otherwise fails closed to the placeholder (requests fail rather than misroute).
- TEST: Config presence is build-time; placeholder default verified by inspection.
- COMMIT: (security: harden authentication/backend requests)
- STATUS: Verified (code).

## SEC-008 — App Check never initialized
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Firebase hardening
- FILE: `app/src/main/java/com/example/GmailAssistantApp.kt`
- PROBLEM: `firebase-appcheck-recaptcha` dependency was present but App Check was never installed, so no attestation tokens were ever attached to Firebase traffic.
- ROOT CAUSE: Initialization code missing.
- FIX: `GmailAssistantApp` now installs `ReCaptchaAppCheckProviderFactory` as soon as a Firebase app exists, before protected services are used. Enforcement intentionally NOT enabled (must be enabled in Firebase Console after traffic validation). No debug token committed.
- TEST: Requires manual testing (Firebase Console → App Check → Network insights).
- COMMIT: (security: harden Firebase/Firestore authorization)
- STATUS: NOT VERIFIED at runtime (needs Firebase project).

## SEC-009 — Unhandled coroutine exceptions in ViewModel
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Reliability
- FILE: `app/src/main/java/com/example/ui/viewmodel/AssistantViewModel.kt`
- PROBLEM: A `CoroutineExceptionHandler` was defined but never attached, so any uncaught exception in a `viewModelScope.launch` block would crash the app.
- ROOT CAUSE: Handler was dead code.
- FIX: All 33 launches now use `viewModelScope.launch(exceptionHandler)`; failures are logged and surfaced as a status message instead of crashing.
- TEST: Requires manual testing (fault injection on device). No behavior change on success paths.
- COMMIT: (fix: resolve identified reliability/debugging issues)
- STATUS: Verified (code).

## SEC-010 — Delete email had no confirmation
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Action authorization
- FILE: `app/src/main/java/com/example/ui/screens/EmailDetailSheet.kt`
- PROBLEM: A single tap on the delete icon permanently deleted the locally cached email with no confirmation (the only irreversible one-tap action in the UI).
- ROOT CAUSE: Direct wiring of the icon to `viewModel.deleteEmail`.
- FIX: Delete now opens a confirmation dialog consistent with the existing Confirm & Send pattern; deletion only happens on explicit confirmation.
- TEST: Requires manual testing.
- COMMIT: (security: harden Gmail OAuth/action authorization)
- STATUS: Verified (code). Manual device testing pending.

## SEC-011 — Fake "Account linked" action
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Authentication integrity
- FILES: `AssistantViewModel.kt`, `AccountAuthDialog.kt`, `MainAppScreen.kt`
- PROBLEM: `linkCustomGoogleEmail` displayed "Account linked: <email>" without any verification — a decorative, fake authentication affordance.
- ROOT CAUSE: Placeholder feature that never performed linking.
- FIX: Removed the dead ViewModel method, the unused dialog parameter, and its wiring. Google sign-in remains the only account path.
- TEST: Compile-time (no callers remain).
- COMMIT: (fix: resolve identified reliability/debugging issues)
- STATUS: Verified (no remaining references).

## SEC-012 — No CI security checks
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: CI/CD
- FILE: `.github/workflows/security-ci.yml` (new)
- PROBLEM: The repository had no workflows — no build/test validation, no secret scanning, no static analysis.
- ROOT CAUSE: Never configured.
- FIX: Added `security-ci.yml`: gitleaks secret scan, `gradle test` unit suite, CodeQL java-kotlin analysis. Official/well-established actions only; least-privilege permissions; no secrets in YAML.
- TEST: The workflow runs on the PR itself.
- COMMIT: (chore: add repository security/CI checks)
- STATUS: NOT VERIFIED until first CI run (Gradle/JDK versions may need adjustment — no wrapper is committed).

## SEC-013 — README overstated security
- DATE: 2026-09-22
- SEVERITY: Low
- CATEGORY: Documentation
- FILE: `README.md`
- PROBLEM: README claimed a completed "5-Stage Verification Report" with claims that were not all true at the time (e.g. `.env.example` did not exist; Gmail OAuth was not functional).
- ROOT CAUSE: Documentation written ahead of implementation.
- FIX: README now describes actual, verified state, with honest limitations and setup instructions.
- COMMIT: (docs: record hardening results)
- STATUS: Verified.

## SEC-014 — Gmail OAuth token acquisition not implemented
- DATE: 2026-09-22
- SEVERITY: High (functional; security posture is fail-closed, so not exploitable)
- CATEGORY: Correctness / Gmail OAuth
- FILES: `app/src/main/java/com/example/data/auth/GmailOAuthManager.kt` and callers
- PROBLEM: `GmailOAuthManager.setSession` is never called from production code (only tests). There is no OAuth flow to obtain a Gmail access token, so all live Gmail operations always fail with an authorization-required message.
- ROOT CAUSE: The OAuth acquisition step was never built; requires a Google Cloud OAuth client with Gmail scopes (external configuration).
- FIX: None implemented (do not invent infrastructure). Documented in README ("Gmail OAuth setup") — the security design (fail closed, memory-only tokens) is correct.
- TEST: Requires manual testing after Google Cloud configuration.
- STATUS: OPEN — requires manual Google Cloud/backend configuration. Not fixable in-source without inventing infrastructure.

## SEC-015 — Firestore user document writes unconstrained
- DATE: 2026-09-22
- SEVERITY: Low
- CATEGORY: Firestore authorization
- FILE: `firestore.rules`
- PROBLEM: Rules allow each user to read/write their own `users/{uid}` document with arbitrary fields. No privileged fields (roles/admin/subscription) exist in the current data model, so there is no exploit today; the risk appears if privileged fields are added to this document later.
- ROOT CAUSE: Rules predate any privileged data model.
- FIX: None (no change needed today). Recorded guidance: privileged attributes must live in custom claims or backend-validated documents, never in client-writable fields.
- STATUS: OPEN (documented guardrail). Rules verified: unauthenticated denied, cross-user denied, unknown paths denied.

## SEC-016 — Release build not minified
- DATE: 2026-09-22
- SEVERITY: Low
- CATEGORY: Release hardening
- FILE: `app/build.gradle.kts`
- PROBLEM: `isMinifyEnabled = false` in release; R8 shrinking/obfuscation disabled.
- FIX: Left unchanged deliberately — enabling R8 without device testing risks breaking release behavior. Recommend enabling after regression testing on a release build.
- STATUS: OPEN (recommendation).
