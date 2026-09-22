package com.example

import com.example.data.api.GmailBodyParser
import com.example.data.api.GmailMessageBody
import com.example.data.api.GmailMessagePart
import com.example.data.api.GmailMessagePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric is required: GmailBodyParser decodes with android.util.Base64.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GmailBodyParserTest {

    @Test
    fun `test plaintext base64 decoding`() {
        // Base64 for "Hello, this is a secure email body."
        val rawBase64 = "SGVsbG8sIHRoaXMgaXMgYSBzZWN1cmUgZW1haWwgYm9keS4="
        val payload = GmailMessagePayload(
            body = GmailMessageBody(data = rawBase64)
        )
        val extracted = GmailBodyParser.parseMimeBody(payload)
        assertEquals("Hello, this is a secure email body.", extracted)
    }

    @Test
    fun `test html tag sanitization`() {
        val rawHtml = "<p>Meeting confirmed for <strong>10:00 AM</strong>.<br>Please bring your notes.</p>"
        val sanitized = GmailBodyParser.stripHtmlTags(rawHtml)
        // Tags are replaced with a space during stripping, so we assert on the
        // security-relevant outcome: the text content survives and no tags remain.
        assertTrue(sanitized.contains("Meeting confirmed for 10:00 AM"))
        assertTrue(sanitized.contains("Please bring your notes"))
        assertTrue(!sanitized.contains("<p>"))
        assertTrue(!sanitized.contains("<strong>"))
    }

    @Test
    fun `test nested multipart fallback to plain text`() {
        val plainPart = GmailMessagePart(
            mimeType = "text/plain",
            body = GmailMessageBody(data = "SGVsbG8gRnJvbSBQbGFpbiBUZXh0") // "Hello From Plain Text"
        )
        val htmlPart = GmailMessagePart(
            mimeType = "text/html",
            body = GmailMessageBody(data = "PGRpdj5IZWxsbyBGcm9tIEhUTUw8L2Rpdj4=") // "<div>Hello From HTML</div>"
        )
        val payload = GmailMessagePayload(
            parts = listOf(plainPart, htmlPart)
        )
        val extracted = GmailBodyParser.parseMimeBody(payload)
        assertEquals("Hello From Plain Text", extracted)
    }

    @Test
    fun `test empty and null payload returns empty string without crashing`() {
        assertEquals("", GmailBodyParser.parseMimeBody(null))
        assertEquals("", GmailBodyParser.parseMimeBody(GmailMessagePayload()))
        assertEquals("", GmailBodyParser.decodeBase64Safe(""))
    }
}
