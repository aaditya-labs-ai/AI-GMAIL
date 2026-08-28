package com.example.data.auth

import com.example.util.SafeLogger

/**
 * In-memory manager for Gmail OAuth 2.0 access tokens.
 * Implements [GmailTokenProvider] to supply valid, unexpired Bearer tokens.
 * Access tokens are stored strictly in-memory and are never logged, persisted in plaintext, or exposed.
 */
object GmailOAuthManager : GmailTokenProvider {

    data class GmailSession(
        val accessToken: String,
        val expirationTimestamp: Long,
        val grantedScopes: Set<String> = setOf(GmailScopes.READ_ONLY, GmailScopes.SEND)
    ) {
        val isExpired: Boolean
            get() = System.currentTimeMillis() >= (expirationTimestamp - 60_000L) // 1-minute safety margin
    }

    @Volatile
    private var currentSession: GmailSession? = null

    fun setSession(
        accessToken: String,
        expiresInSeconds: Long = 3600L,
        scopes: Set<String> = emptySet()
    ) {
        if (accessToken.isBlank()) {
            currentSession = null
            return
        }
        val expiry = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        currentSession = GmailSession(
            accessToken = accessToken.trim(),
            expirationTimestamp = expiry,
            grantedScopes = if (scopes.isNotEmpty()) scopes else setOf(GmailScopes.READ_ONLY, GmailScopes.SEND)
        )
        SafeLogger.d("GmailOAuthManager", "Gmail OAuth session established safely (duration: ${expiresInSeconds}s)")
    }

    override suspend fun getAccessToken(): String? {
        return getValidAccessToken()
    }

    override fun getValidAccessToken(): String? {
        val session = currentSession ?: return null
        return if (!session.isExpired && session.accessToken.isNotBlank()) {
            session.accessToken
        } else {
            if (session.isExpired) {
                SafeLogger.d("GmailOAuthManager", "Gmail session has expired; invalidating")
                currentSession = null
            }
            null
        }
    }

    override fun hasValidAuthorization(): Boolean = getValidAccessToken() != null

    override fun clearSession() {
        currentSession = null
        SafeLogger.d("GmailOAuthManager", "Gmail OAuth session invalidated")
    }
}
