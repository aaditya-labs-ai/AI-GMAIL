package com.example.data.auth

/**
 * Minimal, granular Gmail OAuth 2.0 scopes.
 */
object GmailScopes {
    const val READ_ONLY = "https://www.googleapis.com/auth/gmail.readonly"
    const val SEND = "https://www.googleapis.com/auth/gmail.send"
    const val MODIFY = "https://www.googleapis.com/auth/gmail.modify"
}

/**
 * Contract for supplying valid, unexpired Gmail OAuth 2.0 access tokens.
 */
interface GmailTokenProvider {
    suspend fun getAccessToken(): String?
    fun getValidAccessToken(): String?
    fun hasValidAuthorization(): Boolean
    fun clearSession()
}

/**
 * Represents the current authorization status for Gmail API interactions.
 */
sealed interface GmailAuthState {
    data object Authorized : GmailAuthState
    data object AuthenticationRequired : GmailAuthState
    data class Error(val message: String) : GmailAuthState
}
