package com.example

import android.app.Application
import android.util.Log
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
                    val apiKey = BuildConfig.FIREBASE_API_KEY.ifBlank { "<REDACTED>" }
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("com.aistudio.gmailassistant.kdqpxz")
                        .setProjectId("ai-studio-gmail-assistant")
                        .setApiKey(apiKey)
                        .build()
                    FirebaseApp.initializeApp(this, options)
                    Log.d("GmailAssistantApp", "Initialized FirebaseApp with configured options")
                }
            }
        } catch (e: Exception) {
            Log.w("GmailAssistantApp", "Firebase init notice: ${e.message}")
        }
    }
}
