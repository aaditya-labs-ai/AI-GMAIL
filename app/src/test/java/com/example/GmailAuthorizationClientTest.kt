package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.auth.GmailAuthorizationClient
import com.example.data.auth.GmailAuthorizationOutcome
import com.example.data.auth.GmailScopes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GmailAuthorizationClientTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `requests only the two least-privilege gmail scopes`() {
        val scopes = GmailAuthorizationClient(context).requestedScopes()
        assertEquals(2, scopes.size)
        assertEquals(
            setOf(GmailScopes.READ_ONLY, GmailScopes.SEND),
            scopes.map { it.scopeUri }.toSet()
        )
    }

    @Test
    fun `required scopes never include modify or full mail access`() {
        val uris = GmailAuthorizationClient.REQUIRED_SCOPES.map { it.scopeUri }.toSet()
        assertFalse(GmailScopes.MODIFY in uris)
        assertFalse("https://mail.google.com/" in uris)
    }

    @Test
    fun `missing consent result fails closed with an error`() {
        val outcome = GmailAuthorizationClient(context).handleConsentResult(null)
        assertTrue(outcome is GmailAuthorizationOutcome.Error)
    }
}
