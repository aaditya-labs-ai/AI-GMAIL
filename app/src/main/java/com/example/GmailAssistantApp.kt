package com.example

import android.app.Application
import com.example.util.SafeLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

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
                    } else {
                        SafeLogger.w("GmailAssistantApp", "Firebase configuration is not present or incomplete in environment")
                    }
                }
            }
        } catch (e: Exception) {
            SafeLogger.w("GmailAssistantApp", "Firebase initialization check: ${e.message}")
        }
    }
}
