package com.example.data.auth

import android.util.Log

/**
 * Manages Gmail OAuth 2.0 access tokens separately from Firebase Authentication.
 * Ensures access tokens are scoped, checked for expiration, and never logged or exposed.
 */
object GmailOAuthManager {
    data class GmailSession(
        val accessToken: String,
        val expirationTimestamp: Long,
        val grantedScopes: Set<String> = setOf("https://www.googleapis.com/auth/gmail.modify", "https://www.googleapis.com/auth/gmail.send")
    ) {
        val isExpired: Boolean
            get() = System.currentTimeMillis() >= (expirationTimestamp - 60_000L) // 1-minute safety window
    }

    @Volatile
    private var currentSession: GmailSession? = null

    fun setSession(accessToken: String, expiresInSeconds: Long = 3600L, scopes: Set<String> = emptySet()) {
        if (accessToken.isBlank()) {
            currentSession = null
            return
        }
        val expiry = System.currentTimeMillis() + (expiresInSeconds * 1000L)
        currentSession = GmailSession(
            accessToken = accessToken.trim(),
            expirationTimestamp = expiry,
            grantedScopes = if (scopes.isNotEmpty()) scopes else setOf("https://www.googleapis.com/auth/gmail.modify", "https://www.googleapis.com/auth/gmail.send")
        )
        Log.d("GmailOAuthManager", "Gmail OAuth session established with expiration at $expiry")
    }

    fun getValidAccessToken(): String? {
        val session = currentSession ?: return null
        return if (!session.isExpired && session.accessToken.isNotBlank()) {
            session.accessToken
        } else {
            null
        }
    }

    fun hasValidAuthorization(): Boolean = getValidAccessToken() != null

    fun clearSession() {
        currentSession = null
        Log.d("GmailOAuthManager", "Gmail OAuth session cleared")
    }
}
