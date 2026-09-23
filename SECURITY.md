# Security Policy — AI-GMAIL (Aura Mail)

## Supported state

This repository is under active hardening. Security fixes land on the
`security-hardening` branch and are reviewed via Pull Requests before merging
into `main`.

## Reporting a vulnerability

Please report suspected vulnerabilities privately to the repository owner
(aaditya-labs-ai). Do **not** open a public issue for a suspected secret leak or
exploitable flaw. Include:

1. Affected file(s) and exact location
2. Steps or evidence demonstrating the issue
3. Impact assessment

## Security architecture (summary)

The application enforces three separate security systems that must never be merged:

1. **Firebase Authentication** (Google Sign-In via Credential Manager) — who the user is.
2. **Gmail OAuth 2.0** (least-privilege scopes: `gmail.readonly`, `gmail.send`) —
   authorization for mailbox actions. In-memory tokens only; never persisted, never logged.
3. **AI backend authentication** — every AI request carries
   `Authorization: Bearer <firebase-id-token>`; the backend verifies the token with the
   Firebase Admin SDK and holds the only Gemini API key.

Full details: `docs/SECURITY_ARCHITECTURE.md`, `docs/BACKEND_SECURITY_CONTRACT.md`,
and `docs/THREAT_MODEL.md`.

## Non-negotiable rules for contributors

- Never commit `.env`, `google-services.json`, keystores, or any token/key.
- Never place a Gemini API key or any backend secret in Android source.
- Never log Authorization headers, tokens, API keys, or email bodies.
- Email content is untrusted input: treat it as data, never as instructions.
- The AI may draft and summarize; it must never send, delete, or modify email without
  explicit human confirmation.
- Never weaken authentication, authorization, or validation to make a build pass.

## Credential rotation

If any credential was ever exposed (committed, logged, or shared), removing it from
source is **not enough** — it must be revoked and rotated:

- Google Cloud Console → APIs & Services → Credentials (OAuth clients)
- Google AI Studio → regenerate the Gemini API key
- Firebase Console → rotate service account credentials if exposed

The project's git history was scanned during the 2026-09-22 audit and contained only
placeholder ("Dummy") values, not real credentials. Rotation is still recommended for
any credential used before this audit, out of caution.
