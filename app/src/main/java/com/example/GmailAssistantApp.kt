package com.example

import android.app.Application
import com.example.util.SafeLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.recaptcha.ReCaptchaAppCheckProviderFactory

class GmailAssistantApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val initialized = try {
                    FirebaseApp.initializeApp(this) != null
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
                        FirebaseApp.initializeApp(this, options)
                        SafeLogger.d("GmailAssistantApp", "Initialized FirebaseApp with configured environment options")
                        initializeAppCheck()
                    } else {
                        SafeLogger.w("GmailAssistantApp", "Firebase configuration is not present or incomplete in environment")
                    }
                } else {
                    initializeAppCheck()
                }
            } else {
                initializeAppCheck()
            }
        } catch (e: Exception) {
            SafeLogger.w("GmailAssistantApp", "Firebase initialization check: ${e.message}")
        }
    }

    /**
     * Firebase App Check — installed as soon as a Firebase app exists, BEFORE any
     * protected Firebase service (Firestore/Auth network calls) is used.
     *
     * NOTES:
     * - Uses the reCAPTCHA provider (works on devices without Play Services).
     * - Enforcement is NOT enabled here. Enforcement must be enabled gradually in the
     *   Firebase Console (App Check -> Apps) only after legitimate traffic is verified.
     * - No debug token is hardcoded. For debug builds you may register a debug token
     *   in the Firebase Console and never commit it to source control.
     */
    private fun initializeAppCheck() {
        try {
            if (FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseAppCheck.getInstance()
                    .installAppCheckProviderFactory(
                        ReCaptchaAppCheckProviderFactory.getInstance()
                    )
                SafeLogger.d("GmailAssistantApp", "Firebase App Check provider installed")
            }
        } catch (e: Exception) {
            SafeLogger.w("GmailAssistantApp", "App Check initialization unavailable: ${e.message}")
        }
    }
}
