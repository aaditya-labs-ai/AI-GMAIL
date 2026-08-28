package com.example

import com.example.util.SafeLogger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SafeLoggerTest {

    @Test
    fun `test sanitize redacts Bearer access tokens`() {
        val rawMessage = "Sending request with header Authorization: Bearer ya29.a0AfH6SMA..."
        val sanitized = SafeLogger.sanitize(rawMessage)

        assertFalse(sanitized.contains("ya29.a0AfH6SMA"))
        assertTrue(sanitized.contains("Bearer [REDACTED]"))
    }

    @Test
    fun `test sanitize redacts query and body api keys`() {
        val rawMessage = "Request failed: api_key=AIzaSyDxyz123456789 secret=mySuperSecretPass"
        val sanitized = SafeLogger.sanitize(rawMessage)

        assertFalse(sanitized.contains("mySuperSecretPass"))
        assertTrue(sanitized.contains("api_key=[REDACTED]"))
        assertTrue(sanitized.contains("secret=[REDACTED]"))
    }

    @Test
    fun `test sanitize redacts standalone Google AIza API keys`() {
        val rawMessage = "Key value: AIzaSyD9876543210abcdefghijklmnopqrstu"
        val sanitized = SafeLogger.sanitize(rawMessage)

        assertFalse(sanitized.contains("AIzaSyD9876543210abcdefghijklmnopqrstu"))
        assertTrue(sanitized.contains("[REDACTED_API_KEY]"))
    }
}
