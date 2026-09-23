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
- FIX: Added `.env.example` with placeholders only (no real credentials), including `AI_BACKEND_URL` and `RECAPTCHA_SITE_KEY`.
- TEST: Build with only `.env.example` present (CI does this).
- COMMIT: (security: harden logging and secret handling)
- STATUS: Verified (placeholders only, history-scanned; CI builds green with only this file present).

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
- FIX: `GmailAssistantApp` now installs `RecaptchaAppCheckProviderFactory.getInstance(RECAPTCHA_SITE_KEY)` as soon as a Firebase app exists, before protected services are used. Enforcement intentionally NOT enabled (must be enabled in Firebase Console after traffic validation). No debug token committed.
- TEST: Requires manual testing (Firebase Console → App Check → Network insights).
- COMMIT: (security: harden Firebase/Firestore authorization; fix: pass reCAPTCHA site key to App Check factory)
- STATUS: NOT VERIFIED at runtime (needs Firebase project + RECAPTCHA_SITE_KEY in .env); compile + unit tests verified in CI run 35759119652.

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
- STATUS: VERIFIED — full green run 35759119652 (2026-09-22): secret scan ✅, CodeQL ✅, 28/28 unit tests ✅.

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
- FIX (2026-09-23): Implemented the in-app acquisition flow — new `app/src/main/java/com/example/data/auth/GmailAuthorizationClient.kt` (Google Play Services Authorization Client, `play-services-auth` 22.0.0; API surface verified against the published AAR). Requests only the least-privilege scopes (`gmail.readonly` + `gmail.send`), keeps the token in memory only (GmailOAuthManager), and fails closed on any error. UI: Settings → "Gmail Connection" with Connect / Disconnect (revoke + purge). No credentials are stored or invented; the flow activates only once the Google Cloud OAuth client + consent screen exist.
- TEST: Unit tests in `GmailAuthorizationClientTest` (least-privilege scopes; fails closed on missing consent result). End-to-end authorization REQUIRES MANUAL TESTING after Google Cloud configuration (Gmail API enabled, OAuth consent screen with the two scopes, OAuth client for the app package + signing SHA-1).
- STATUS: PARTIALLY FIXED — code complete and verified in CI; end-to-end activation requires Google Cloud configuration + on-device testing (tracked in README "Gmail OAuth setup").

## SEC-015 — Firestore user document writes unconstrained
- DATE: 2026-09-22
- SEVERITY: Low
- CATEGORY: Firestore authorization
- FILE: `firestore.rules`
- PROBLEM: Rules allow each user to read/write their own `users/{uid}` document with arbitrary fields. No privileged fields (roles/admin/subscription) exist in the current data model, so there is no exploit today; the risk appears if privileged fields are added to this document later.
- ROOT CAUSE: Rules predate any privileged data model.
- FIX (2026-09-23): Guardrail added to `firestore.rules` — the `users/{uid}` profile document is now READ-ONLY for clients, and every subcollection write is restricted to exactly the field list `FirestoreService.kt` writes (per-collection allowlists). A compromised client can no longer write undocumented fields (e.g. a future `role`/`isAdmin`) to its own documents.
- STATUS: FIXED in the repository; REQUIRES DEPLOYMENT — upload the rules via Firebase Console (Firestore → Rules) or `firebase deploy --only firestore:rules`. Until deployed, the console's current rules remain in force. Re-test after deploy: sign-in, save a campaign, verify sync still works (rules change must not break legitimate writes).

## SEC-016 — Release build not minified
- DATE: 2026-09-22
- SEVERITY: Low
- CATEGORY: Release hardening
- FILE: `app/build.gradle.kts`
- PROBLEM: `isMinifyEnabled = false` in release; R8 shrinking/obfuscation disabled.
- FIX: Enabled as of 2026-09-23: `isMinifyEnabled = true` and `isShrinkResources = true` in the release build type; Kotlin-reflection keep rules added for Moshi. The `release-build` CI job runs the full R8/ProGuard pass on every push, so any shrinking failure is caught before merge.
- STATUS: Build verified in CI. Runtime verification on a real device still pending (test login, AI generation, Gmail flows on a release build before publishing).

## SEC-017 — CI toolchain incompatible with AGP 9.1.1
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: CI/CD / reliability
- FILE: `.github/workflows/security-ci.yml`
- PROBLEM: The unit-test job pinned Gradle 9.2.0 and CodeQL `autobuild` resolved Gradle 8.6 — both below the minimum (9.3.1) required by Android Gradle Plugin 9.1.1, so every build failed at the version check before compiling anything.
- ROOT CAUSE: Gradle version chosen without confirming AGP's minimum requirement; `autobuild` had no way to know the project's requirement (no wrapper is committed).
- FIX: Both jobs now install Gradle 9.3.1; CodeQL `autobuild` replaced with an explicit `compileDebugSources` step using the same toolchain; CodeQL action migrated v3 → v4 (v3 deprecation December 2026).
- TEST: CI run 35759119652 green.
- COMMIT: (fix: use Gradle 9.3.1 for AGP 9.1.1 and migrate CodeQL to v4)
- STATUS: Verified.

## SEC-018 — App Check integration used wrong Firebase API
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Firebase hardening / correctness
- FILES: `app/src/main/java/com/example/GmailAssistantApp.kt`, `app/src/main/java/com/example/data/auth/FirebaseIdTokenProvider.kt`, `.env.example`
- PROBLEM: (a) `ReCaptchaAppCheckProviderFactory` does not exist — the class in `firebase-appcheck-recaptcha` 19.2.0 is `RecaptchaAppCheckProviderFactory`; (b) its `getInstance()` requires the reCAPTCHA Enterprise site key parameter; (c) `FirebaseIdTokenProvider` awaited the `GetTokenResult` wrapper instead of its `.token` string, so the Authorization header would have been a `toString()` of the result object, not the token.
- ROOT CAUSE: Written from memory without compiling against the resolved dependency versions; first compile happened in CI.
- FIX: Correct class + `getInstance(siteKey)` with the key configurable via `RECAPTCHA_SITE_KEY` in `.env` (App Check skipped fail-safe when unconfigured); token unwrapped with `.await()?.token`.
- TEST: Compiled and all 14 AiRepositorySecurityTest auth/validation tests pass in CI (including Bearer header format).
- COMMIT: (fix: correct App Check factory class name and ID token result unwrapping; fix: pass reCAPTCHA site key to App Check factory)
- STATUS: Verified (compile + unit tests). Runtime attestation still requires Firebase/Google Cloud configuration.

## SEC-019 — Pre-existing test suite had never been executed
- DATE: 2026-09-22
- SEVERITY: Medium
- CATEGORY: Testability
- FILES: `app/src/test/java/com/example/` (5 test classes)
- PROBLEM: With no CI before this audit, the committed test suite had never run: 3 classes crashed on a plain JVM (`android.util.Log`/`Base64` "not mocked"); `ExampleRobolectricTest` expected app name "Gmail Assistant" but the app is "Aura Mail"; the SafeLogger AIza fixture was 34 chars after the prefix (real keys are 35, so the redaction regex correctly did not match); the HTML-sanitization assertion did not match the tag→space stripping behavior.
- ROOT CAUSE: Tests written but never executed; expectations drifted from implementation.
- FIX: Robolectric runner added to the 3 affected classes (Robolectric was already a project test dependency); expectations corrected to the actual behavior. Security-relevant assertions (tag stripping, token redaction, session invalidation) are all still asserted — no coverage was weakened.
- TEST: 28/28 unit tests pass in CI run 35759119652.
- COMMIT: (test: repair pre-existing broken test suite; test: match html sanitization assertion to actual stripping behavior)
- STATUS: Verified.

## SEC-020 — Debug build unusable on fresh checkouts; APK assembly and lint not verified in CI
- DATE: 2026-09-23
- SEVERITY: Medium
- CATEGORY: Release hardening / CI completeness
- FILES: `app/build.gradle.kts`, `.github/workflows/security-ci.yml`
- PROBLEM: The debug build type unconditionally used the `debugConfig` signing config pointing at `${rootDir}/debug.keystore`, which is intentionally gitignored — so `assembleDebug` failed on any fresh checkout (including CI). Additionally, no CI job assembled a full APK or ran Android Lint, leaving those unverified.
- ROOT CAUSE: Signing config assumed a developer-local keystore always exists.
- FIX: The debug build type now uses the project keystore only when the file exists and otherwise falls back to AGP's default debug signing. CI gained two jobs: `Debug APK assembly` (`gradle assembleDebug`, APK uploaded as a build artifact) and `Android Lint` (`gradle lintDebug`). Existing jobs untouched.
- TEST: CI run 35813957846 (2026-09-23): secret scan ✅, unit tests ✅, debug APK assembly ✅, Android Lint ✅, CodeQL ✅.
- COMMIT: (fix: fall back to default debug signing when keystore is absent; ci: add debug APK assembly and Android Lint jobs)
- STATUS: Verified.

## SEC-021 — Release signing not automated; secrets handling
- DATE: 2026-09-23
- SEVERITY: Low
- CATEGORY: CI/CD / release engineering
- FILES: `.github/workflows/security-ci.yml`, `app/build.gradle.kts`
- PROBLEM: Release builds could not be produced or verified in CI because signing requires an upload keystore and passwords that must never be committed. Combined with SEC-016, the release path was entirely untested.
- ROOT CAUSE: Signing was only possible with developer-local files/env vars.
- FIX: New `release-build` CI job: three encrypted repository secrets (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`; key alias `upload` stays in `build.gradle.kts`). The keystore is base64-decoded into the runner's temp dir (never printed, never committed) and passed via `KEYSTORE_PATH`/`STORE_PASSWORD`/`KEY_PASSWORD` env vars. When the secrets are absent the job still runs `assembleRelease` and produces an UNSIGNED APK, so the R8 pass is verified on every push and CI stays green before secrets exist. The release build type only attaches the signing config when `KEYSTORE_PATH` is set and the file exists.
- SECURITY NOTES: Secrets are referenced only in `env:` blocks; the workflow never echoes them. The signed release APK is uploaded as a run artifact (readable by anyone with repository read access — the repository is public, so treat it as a published build).
- TEST: CI build of the release APK (unsigned until secrets are configured).
- COMMIT: (feat(ci): release build job wired to encrypted secrets; R8 enabled for release)
- STATUS: Verified — CI run 35816328148 (2026-09-23): all seven jobs green, including a minified, resource-shrunk (R8) release APK (unsigned until secrets are configured). Signed path requires the repository owner to add the three secrets — documented in README. Runtime verification of the minified build on a device is still pending (login, AI generation, Gmail flows).
