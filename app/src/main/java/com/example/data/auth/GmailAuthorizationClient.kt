package com.example.data.auth

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.util.SafeLogger
import com.google.android.gms.auth.api.identity.AuthorizationClient
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Outcome of a Gmail authorization attempt.
 */
sealed interface GmailAuthorizationOutcome {
    /** The user still needs to approve the consent screen: launch [intentSender]. */
    data class ConsentRequired(val intentSender: IntentSender) : GmailAuthorizationOutcome

    /** A valid access token is now held (in-memory only) by [GmailOAuthManager]. */
    data class Authorized(val grantedScopes: Set<String>) : GmailAuthorizationOutcome

    /** Authorization failed (cancelled, Play Services missing, OAuth client not configured, …). */
    data class Error(val message: String) : GmailAuthorizationOutcome
}

/**
 * Acquires Gmail OAuth 2.0 access tokens through Google Play Services
 * (Authorization Client) and feeds them to [GmailOAuthManager].
 *
 * Security design (SEC-014):
 * - Least privilege: only `gmail.readonly` and `gmail.send` are requested —
 *   never `gmail.modify`, never full `mail.google.com` access.
 * - The access token lives in memory only; it is never persisted, logged,
 *   or written to Firestore.
 * - Fails closed: if Play Services is unavailable or the Google Cloud OAuth
 *   client / consent screen is not configured, the outcome is [Error] and
 *   Gmail features stay disabled.
 *
 * One-time Google Cloud setup required before this works on a device:
 * enable the Gmail API, configure the OAuth consent screen with the scopes
 * [GmailScopes.READ_ONLY] and [GmailScopes.SEND], and have an OAuth client
 * for this app's package name + signing SHA-1 (see README, "Gmail OAuth setup").
 */
class GmailAuthorizationClient(private val context: Context) {

    private val authorizationClient: AuthorizationClient by lazy {
        Identity.getAuthorizationClient(context)
    }

    /** The scopes this app requests — least privilege by design. */
    fun requestedScopes(): List<Scope> = REQUIRED_SCOPES

    /**
     * Starts Gmail authorization. Completes with:
     * - [GmailAuthorizationOutcome.Authorized] if consent was already granted,
     * - [GmailAuthorizationOutcome.ConsentRequired] if the user must approve a
     *   consent screen (launch the returned [IntentSender], then pass the
     *   resulting Intent to [handleConsentResult]),
     * - [GmailAuthorizationOutcome.Error] otherwise (fails closed).
     *
     * @param accountEmail the signed-in user's Google account email, if known.
     */
    suspend fun requestAuthorization(accountEmail: String?): GmailAuthorizationOutcome =
        withContext(Dispatchers.IO) {
            val request = AuthorizationRequest.builder()
                .setRequestedScopes(REQUIRED_SCOPES)
                .apply {
                    val email = accountEmail?.trim().orEmpty()
                    if (email.contains('@')) {
                        setAccount(Account(email, GOOGLE_ACCOUNT_TYPE))
                    }
                }
                .build()
            try {
                val result = authorizationClient.authorize(request).await()
                when {
                    result.hasResolution() -> {
                        val pendingIntent = result.pendingIntent
                        if (pendingIntent != null) {
                            GmailAuthorizationOutcome.ConsentRequired(pendingIntent.intentSender)
                        } else {
                            SafeLogger.w(TAG, "Consent required but no pending intent provided")
                            GmailAuthorizationOutcome.Error("Google consent screen is unavailable.")
                        }
                    }

                    else -> {
                        val token = result.accessToken
                        if (token.isNullOrBlank()) {
                            SafeLogger.w(TAG, "Authorization finished without a token")
                            GmailAuthorizationOutcome.Error("Google did not return an access token.")
                        } else {
                            establishSession(token, extractScopeUris(result.grantedScopes))
                        }
                    }
                }
            } catch (e: Exception) {
                SafeLogger.e(TAG, "Gmail authorization failed: ${e.message}")
                GmailAuthorizationOutcome.Error(e.localizedMessage ?: "Gmail authorization failed.")
            }
        }

    /**
     * Call with the Intent returned after launching the consent screen
     * ([GmailAuthorizationOutcome.ConsentRequired.intentSender]).
     */
    fun handleConsentResult(data: Intent?): GmailAuthorizationOutcome {
        if (data == null) {
            return GmailAuthorizationOutcome.Error("Gmail authorization was cancelled.")
        }
        return try {
            val result = authorizationClient.getAuthorizationResultFromIntent(data)
            val token = result.accessToken
            if (token.isNullOrBlank()) {
                SafeLogger.w(TAG, "Consent result carried no access token")
                GmailAuthorizationOutcome.Error("Google did not return an access token.")
            } else {
                establishSession(token, extractScopeUris(result.grantedScopes))
            }
        } catch (e: Exception) {
            SafeLogger.e(TAG, "Gmail consent result failed: ${e.message}")
            GmailAuthorizationOutcome.Error(e.localizedMessage ?: "Gmail authorization failed.")
        }
    }

    /**
     * Disconnects: revokes the app's Gmail grants with Google and clears the
     * in-memory session. The session is cleared even if the network call fails.
     */
    fun revokeAccess(onComplete: () -> Unit = {}) {
        try {
            authorizationClient.revokeAccess(RevokeAccessRequest.builder().build())
                .addOnCompleteListener {
                    GmailOAuthManager.clearSession()
                    SafeLogger.d(TAG, "Gmail grants revoked and session cleared")
                    onComplete()
                }
        } catch (e: Exception) {
            GmailOAuthManager.clearSession()
            SafeLogger.e(TAG, "Gmail revoke failed (session still cleared): ${e.message}")
            onComplete()
        }
    }

    /** Maps Google's nullable scope list to a plain set of scope URIs. */
    private fun extractScopeUris(grantedScopes: List<Scope>?): Set<String> {
        val scopes = grantedScopes ?: return emptySet()
        return scopes.mapNotNull { it?.scopeUri }.toSet()
    }

    private fun establishSession(
        accessToken: String,
        grantedScopes: Set<String>
    ): GmailAuthorizationOutcome.Authorized {
        val effectiveScopes = grantedScopes.ifEmpty { REQUIRED_SCOPES.map { it.scopeUri }.toSet() }
        GmailOAuthManager.setSession(
            accessToken = accessToken,
            expiresInSeconds = DEFAULT_TOKEN_LIFETIME_SECONDS,
            scopes = effectiveScopes
        )
        return GmailAuthorizationOutcome.Authorized(effectiveScopes)
    }

    companion object {
        private const val TAG = "GmailAuthClient"
        private const val GOOGLE_ACCOUNT_TYPE = "com.google"
        private const val DEFAULT_TOKEN_LIFETIME_SECONDS = 3600L

        /** Least-privilege Gmail scopes: read-only + send. */
        val REQUIRED_SCOPES: List<Scope> =
            listOf(Scope(GmailScopes.READ_ONLY), Scope(GmailScopes.SEND))
    }
}
