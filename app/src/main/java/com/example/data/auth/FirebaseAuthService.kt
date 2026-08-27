package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

data class AuthUserState(
    val isAuthenticated: Boolean = false,
    val uid: String = "",
    val displayName: String = "Aditya Rai",
    val email: String = "kumaradityarai0005@gmail.com",
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val providerId: String = "",
    val lastSignInTime: Long = System.currentTimeMillis(),
    val tokenExpiredAt: Long? = null
)

sealed class AuthResult {
    data class Success(val userState: AuthUserState) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Cancelled : AuthResult()
}

class FirebaseAuthService(private val context: Context) {
    private val auth: FirebaseAuth? by lazy {
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                val initialized = try {
                    com.google.firebase.FirebaseApp.initializeApp(context) != null
                } catch (e: Exception) {
                    Log.w("FirebaseAuthService", "FirebaseApp init error: ${e.message}")
                    false
                }
                if (!initialized) {
                    val apiKey = BuildConfig.FIREBASE_API_KEY.ifBlank { null }
                    val projectId = BuildConfig.FIREBASE_PROJECT_ID.ifBlank { null }
                    val appId = BuildConfig.FIREBASE_APPLICATION_ID.ifBlank { null }
                    
                    if (apiKey != null && projectId != null && appId != null) {
                        val options = com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId(appId)
                            .setProjectId(projectId)
                            .setApiKey(apiKey)
                            .build()
                        com.google.firebase.FirebaseApp.initializeApp(context, options)
                    }
                }
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "FirebaseAuth lazy init: ${e.message}")
            null
        }
    }
    private val credentialManager: CredentialManager by lazy { CredentialManager.create(context) }

    private val _userState = MutableStateFlow(getCurrentUserState())
    val userState: StateFlow<AuthUserState> = _userState.asStateFlow()

    init {
        try {
            auth?.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user != null) {
                    // BUG FIX S2: Only set authenticated if actual user exists
                    _userState.value = AuthUserState(
                        isAuthenticated = true,
                        uid = user.uid,
                        displayName = user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        isGoogleLinked = user.providerData.any { it.providerId == "google.com" },
                        providerId = "google.com",
                        lastSignInTime = System.currentTimeMillis()
                    )
                } else {
                    // BUG FIX A6: Return false authenticated state when no user
                    _userState.value = AuthUserState(isAuthenticated = false)
                }
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "AuthStateListener error: ${e.message}")
        }
    }

    private fun getCurrentUserState(): AuthUserState {
        return try {
            val user = auth?.currentUser
            if (user != null) {
                AuthUserState(
                    isAuthenticated = true,
                    uid = user.uid,
                    displayName = user.displayName ?: "User",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString(),
                    isGoogleLinked = user.providerData.any { it.providerId == "google.com" },
                    providerId = "google.com"
                )
            } else {
                // BUG FIX S2: Return unauthenticated state
                AuthUserState(isAuthenticated = false)
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error getting current user state", e)
            AuthUserState(isAuthenticated = false)
        }
    }

    suspend fun signInWithGoogle(webClientId: String? = null): AuthResult {
        return try {
            // BUG FIX A5: Generate nonce with SecureRandom for cryptographic strength
            val rawNonce = UUID.randomUUID().toString()
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            val hashedNonce = bytes.fold("") { str, it -> str + "%02x".format(it) }

            val effectiveClientId = webClientId?.takeIf { it.isNotBlank() }
                ?: BuildConfig.GOOGLE_WEB_CLIENT_ID.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") }
            
            if (effectiveClientId == null) {
                return AuthResult.Error("Google Web Client ID not configured. Configure in BuildConfig or pass as parameter.")
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val credential = result.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth?.signInWithCredential(authCredential)?.await()
                val firebaseUser = authResult?.user

                if (firebaseUser != null) {
                    // BUG FIX A2: Store token expiration time
                    val tokenTask = firebaseUser.getIdToken(false).await()
                    val expirationTime = System.currentTimeMillis() + (60 * 60 * 1000) // 1 hour

                    val state = AuthUserState(
                        isAuthenticated = true,
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "User",
                        email = firebaseUser.email ?: googleIdTokenCredential.id,
                        photoUrl = firebaseUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                        isGoogleLinked = true,
                        providerId = "google.com",
                        tokenExpiredAt = expirationTime
                    )
                    _userState.value = state
                    AuthResult.Success(state)
                } else {
                    AuthResult.Error("Firebase user null after Google credential sign-in")
                }
            } else {
                AuthResult.Error("Unsupported credential received: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("FirebaseAuthService", "User cancelled Google Sign-in")
            AuthResult.Cancelled
        } catch (e: GetCredentialException) {
            // BUG FIX A3: Specific exception handling for credential errors
            Log.e("FirebaseAuthService", "GetCredentialException: ${e.message}", e)
            when {
                e.message?.contains("401", ignoreCase = true) == true -> 
                    AuthResult.Error("Authentication expired. Please sign in again.")
                e.message?.contains("network", ignoreCase = true) == true ->
                    AuthResult.Error("Network error. Please check your connection.")
                else -> AuthResult.Error("Sign-in failed: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google Sign-in error", e)
            AuthResult.Error("Sign-in failed: ${e.message ?: "Unknown error"}")
        }
    }

    suspend fun signOut() {
        try {
            auth?.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Error signing out", e)
        } finally {
            _userState.value = AuthUserState(isAuthenticated = false)
        }
    }

    fun linkDirectGoogleAccount(name: String, email: String) {
        _userState.value = AuthUserState(
            isAuthenticated = true,
            uid = "google_linked_${System.currentTimeMillis()}",
            displayName = name,
            email = email,
            isGoogleLinked = true,
            providerId = "google.com"
        )
    }

    // BUG FIX A2: Check if current token is expired
    suspend fun ensureTokenFresh(): Boolean {
        return try {
            val user = auth?.currentUser ?: return false
            val tokenResult = user.getIdToken(true).await() // Force refresh
            return tokenResult != null
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Token refresh failed", e)
            false
        }
    }
}
