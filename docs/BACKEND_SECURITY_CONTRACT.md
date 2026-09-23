# AI Backend Security Contract

_Last updated: 2026-09-22. **Backend verification status: NOT VERIFIED** — no backend
implementation exists in this repository yet. This document defines the contract the
Android client already implements, so a future backend can be built to match._

## Endpoint

```
POST {AI_BACKEND_URL}/v1/ai/generate
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
```

`AI_BACKEND_URL` is configured per environment via `.env` (see `.env.example`).
It MUST be HTTPS. No default production domain is hardcoded in the client.

## Request (as sent by the Android client)

| Field | Type | Client-side rule | Required backend rule |
|---|---|---|---|
| `prompt` | string | non-blank, ≤ 32,000 chars | Re-validate non-blank and length; treat as untrusted data |
| `model` | string | allowlist: `default`, `gemini-2.5-flash`, `gemini-2.5-pro`, `gemini-2.5-flash-lite`, `imagen-3.0-generate-002` | Server-side allowlist (never trust the client list) |
| `systemInstruction` | string, optional | ≤ 4,000 chars | Server-defined instruction always takes precedence |
| `temperature` | float, optional | clamped 0.0–2.0 | Re-clamp |
| `thinkingBudget` | int, optional | clamped 0–8192 | Re-clamp |
| `imagePayloadBase64` | string, optional | ≤ 4,000,000 base64 chars (~3 MB binary) | Re-validate; enforce per-user quota |
| `audioPayloadBase64` | string, optional | ≤ 4,000,000 base64 chars (~3 MB binary) | Re-validate; enforce per-user quota |

The client never sends a `tools` field. Any future tool invocation feature must use
typed schemas, an explicit allowlist, argument validation, authorization, server-side
execution policy, audit logging, and user confirmation for privileged actions.

## Response

```json
{ "text": "...", "imageUrl": null, "status": "success" }
```

- `text` blank or `status` = `error` is treated as a failure by the client.
- The client never surfaces backend exception details to the end user.

## Required backend behavior

1. **Verify the ID token** with the Firebase Admin SDK (`verifyIdToken`). Extract the
   UID. Reject the request (HTTP 401) if the token is missing, malformed, expired, or
   from an unknown app. NEVER trust a client-supplied UID, email, or claim.
2. **Authorize per UID.** Every request is attributable to one verified user.
3. **Re-validate all inputs** (the table above). Client-side checks are convenience
   only; a hostile client bypasses them.
4. **Rate-limit and quota per UID** — prompt length, request count, image/audio
   payload size, and per-day request volume — so no single user or scripted client
   can create uncontrolled Gemini cost.
5. **Hold the only Gemini API key.** It must never appear in this repository, in
   BuildConfig, in CI, or in any client log.
6. **Treat prompt content as data.** The system instruction that the backend defines
   must be kept separate from, and take precedence over, any user/email-supplied
   content (prompt-injection defense in depth).
7. **Return sanitized errors.** Do not echo stack traces, internal hosts, or the
   provider's raw error to the client.
8. **Use TLS everywhere** (backend ingress and the Gemini provider call).

## Deployment checklist (manual, required before production)

- [ ] Deploy the backend implementing this contract
- [ ] Set `AI_BACKEND_URL` in `.env` (never commit it)
- [ ] Verify a real device request reaches the backend with a valid Bearer token
- [ ] Verify a tampered/expired token is rejected with HTTP 401
- [ ] Verify backend-side rate limits fire (HTTP 429)
