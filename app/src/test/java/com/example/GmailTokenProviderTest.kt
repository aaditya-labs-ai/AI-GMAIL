package com.example

import com.example.data.auth.GmailOAuthManager
import com.example.data.auth.GmailScopes
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric is required: GmailOAuthManager logs through SafeLogger (android.util.Log).
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GmailTokenProviderTest {

    @Before
    fun setup() {
        GmailOAuthManager.clearSession()
    }

    @Test
    fun `test valid token session management`() {
        assertFalse(GmailOAuthManager.hasValidAuthorization())
        assertNull(GmailOAuthManager.getValidAccessToken())

        GmailOAuthManager.setSession(
            accessToken = "test_valid_token_123",
            expiresInSeconds = 3600,
            scopes = setOf(GmailScopes.READ_ONLY, GmailScopes.SEND)
        )

        assertTrue(GmailOAuthManager.hasValidAuthorization())
        assertEquals("test_valid_token_123", GmailOAuthManager.getValidAccessToken())
    }

    @Test
    fun `test expired token is invalidated`() {
        // Expiration in the past (0 seconds)
        GmailOAuthManager.setSession(
            accessToken = "expired_token",
            expiresInSeconds = -100
        )

        assertFalse(GmailOAuthManager.hasValidAuthorization())
        assertNull(GmailOAuthManager.getValidAccessToken())
    }

    @Test
    fun `test clearSession purges active tokens`() {
        GmailOAuthManager.setSession("temp_token_abc", expiresInSeconds = 1800)
        assertTrue(GmailOAuthManager.hasValidAuthorization())

        GmailOAuthManager.clearSession()
        assertFalse(GmailOAuthManager.hasValidAuthorization())
        assertNull(GmailOAuthManager.getValidAccessToken())
    }
}
