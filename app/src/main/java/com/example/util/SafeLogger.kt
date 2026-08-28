package com.example.util

import android.util.Log
import com.example.BuildConfig

/**
 * Safe logging utility that redacts sensitive tokens, credentials, and API keys.
 * In production/release builds, debug logs are stripped.
 */
object SafeLogger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, sanitize(message))
        }
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, sanitize(message))
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, sanitize(message), throwable)
        } else {
            Log.w(tag, sanitize(message))
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, sanitize(message), throwable)
        } else {
            Log.e(tag, sanitize(message))
        }
    }

    fun sanitize(message: String): String {
        return message
            .replace(
                Regex("(?i)Bearer\\s+[A-Za-z0-9._-]+"),
                "Bearer [REDACTED]"
            )
            .replace(
                Regex("(?i)(api[_-]?key|token|password|secret|client_secret)\\s*[:=]\\s*[^\\s,;}\\]]+"),
                "$1=[REDACTED]"
            )
            .replace(
                Regex("AIza[0-9A-Za-z-_]{35}"),
                "[REDACTED_API_KEY]"
            )
    }
}
