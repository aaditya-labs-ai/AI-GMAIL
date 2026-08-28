package com.example.data.auth

import android.content.Context
import android.util.Base64
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.BuildConfig
import com.example.util.SafeLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom

data class AuthUserState(
    val isAuthenticated: Boolean = false,
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val providerId: String = "",
    val lastSignInTime: Long = 0L
)

sealed class AuthResult {
    data class Success(val userState: AuthUserState) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Cancelled : AuthResult()
}

class FirebaseAuthService(private val context: Context) {
    private val auth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val initialized = try {
                    FirebaseApp.initializeApp(context) != null
                } catch (e: Exception) {
                    false
                }
                if (!initialized) {
                    val apiKey = BuildConfig.FIREBASE_API_KEY.ifBlank { "" }
                    val projectId = BuildConfig.FIREBASE_PROJECT_ID.ifBlank { "" }
                    val appId = BuildConfig.FIREBASE_APPLICATION_ID.ifBlank { "" }
                    if (apiKey.isNotBlank() && projectId.isNotBlank() && appId.isNotBlank() && !apiKey.startsWith("YOUR_")) {
                        val options = FirebaseOptions.Builder()
                            .setApplicationId(appId)
                            .setProjectId(projectId)
                            .setApiKey(apiKey)
                            .build()
                        FirebaseApp.initializeApp(context, options)
                    }
                }
            }
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            SafeLogger.w("FirebaseAuthService", "FirebaseAuth lazy init notice: ${e.message}")
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
                    _userState.value = AuthUserState(
                        isAuthenticated = true,
                        uid = user.uid,
                        displayName = user.displayName ?: "",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        isGoogleLinked = user.providerData.any {
                            it.providerId == GoogleAuthProvider.PROVIDER_ID
                        },
                        providerId = user.providerData
                            .firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                            ?.providerId ?: (user.providerData.firstOrNull()?.providerId ?: ""),
                        lastSignInTime = user.metadata?.lastSignInTimestamp
                            ?: System.currentTimeMillis()
                    )
                } else {
                    _userState.value = AuthUserState(
                        isAuthenticated = false,
                        uid = "",
                        displayName = "",
                        email = "",
                        photoUrl = null,
                        isGoogleLinked = false,
                        providerId = "",
                        lastSignInTime = 0L
                    )
                }
            }
        } catch (e: Exception) {
            SafeLogger.w("FirebaseAuthService", "AuthStateListener notice: ${e.message}")
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth?.currentUser
    }

    fun isAuthenticated(): Boolean {
        return auth?.currentUser != null
    }

    fun requireAuthenticatedUser(): FirebaseUser {
        return auth?.currentUser ?: throw SecurityException("Authentication required. Please sign in to proceed.")
    }

    fun getCurrentUid(): String {
        return auth?.currentUser?.uid ?: ""
    }

    private fun getCurrentUserState(): AuthUserState {
        val user = auth?.currentUser

        return if (user != null) {
            AuthUserState(
                isAuthenticated = true,
                uid = user.uid,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                photoUrl = user.photoUrl?.toString(),
                isGoogleLinked = user.providerData.any {
                    it.providerId == GoogleAuthProvider.PROVIDER_ID
                },
                providerId = user.providerData
                    .firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                    ?.providerId ?: (user.providerData.firstOrNull()?.providerId ?: ""),
                lastSignInTime = user.metadata?.lastSignInTimestamp
                    ?: System.currentTimeMillis()
            )
        } else {
            AuthUserState(
                isAuthenticated = false,
                uid = "",
                displayName = "",
                email = "",
                photoUrl = null,
                isGoogleLinked = false,
                providerId = "",
                lastSignInTime = 0L
            )
        }
    }

    suspend fun signInWithGoogle(webClientId: String? = null): AuthResult {
        return try {
            // Cryptographically secure nonce generation
            val secureRandom = SecureRandom()
            val nonceBytes = ByteArray(32)
            secureRandom.nextBytes(nonceBytes)
            val rawNonce = Base64.encodeToString(nonceBytes, Base64.NO_WRAP or Base64.URL_SAFE)
            val digest = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray(Charsets.UTF_8))
            val hashedNonce = digest.joinToString("") { "%02x".format(it) }

            val effectiveClientId = webClientId?.takeIf { it.isNotBlank() }
                ?: BuildConfig.GOOGLE_WEB_CLIENT_ID.takeIf { it.isNotBlank() && !it.startsWith("YOUR_") }
                ?: ""

            if (effectiveClientId.isBlank()) {
                return AuthResult.Error("Google Web Client ID is not configured. Please provide a valid Client ID.")
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
                val firebaseAuthInstance = auth ?: return AuthResult.Error("Firebase Auth service unavailable")
                val authResult = firebaseAuthInstance.signInWithCredential(authCredential).await()
                val firebaseUser = authResult?.user

                if (firebaseUser != null) {
                    val state = AuthUserState(
                        isAuthenticated = true,
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "",
                        email = firebaseUser.email ?: googleIdTokenCredential.id,
                        photoUrl = firebaseUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString(),
                        isGoogleLinked = true,
                        providerId = GoogleAuthProvider.PROVIDER_ID,
                        lastSignInTime = firebaseUser.metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
                    )
                    _userState.value = state
                    SafeLogger.d("FirebaseAuthService", "User authenticated successfully")
                    AuthResult.Success(state)
                } else {
                    AuthResult.Error("Firebase user null after Google credential sign-in")
                }
            } else {
                AuthResult.Error("Unsupported credential received: ${credential.type}")
            }
        } catch (e: GetCredentialCancellationException) {
            SafeLogger.w("FirebaseAuthService", "User cancelled Google Sign-in")
            AuthResult.Cancelled
        } catch (e: Exception) {
            SafeLogger.e("FirebaseAuthService", "Google sign-in error: ${e.message}")
            AuthResult.Error(
                e.message ?: "Google sign-in failed"
            )
        }
    }

    suspend fun signOut() {
        try {
            auth?.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            GmailOAuthManager.clearSession()
            SafeLogger.d("FirebaseAuthService", "User signed out successfully")
        } catch (e: Exception) {
            SafeLogger.e("FirebaseAuthService", "Error signing out: ${e.message}")
        } finally {
            _userState.value = AuthUserState(
                isAuthenticated = false,
                uid = "",
                displayName = "",
                email = "",
                photoUrl = null,
                isGoogleLinked = false,
                providerId = "",
                lastSignInTime = 0L
            )
        }
    }
}
