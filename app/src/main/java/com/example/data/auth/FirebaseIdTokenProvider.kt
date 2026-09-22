package com.example.data.auth

import com.example.util.SafeLogger
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

/**
 * Supplies the current Firebase user's ID token for authenticated AI backend requests.
 *
 * SECURITY:
 * - Uses FirebaseUser.getIdToken(false) — cached token, refreshed automatically when expired.
 * - Returns null (fail closed) when there is no signed-in user or Firebase is unavailable.
 * - The token is short-lived and is never logged or persisted by this provider.
 */
object FirebaseIdTokenProvider {

    /**
     * @return the current Firebase ID token, or null when the client is not authenticated.
     */
    suspend fun currentIdToken(forceRefresh: Boolean = false): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser ?: return null
            user.getIdToken(forceRefresh).await()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.w("FirebaseIdTokenProvider", "Unable to obtain Firebase ID token: ${e.message}")
            null
        }
    }
}
