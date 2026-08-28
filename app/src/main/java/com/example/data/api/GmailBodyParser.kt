package com.example.data.api

import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * Robust parser for Gmail message payloads and MIME structures.
 * Supports multipart/mixed, multipart/alternative, nested MIME parts,
 * URL-safe / standard Base64 encoding, and HTML tag sanitization.
 */
object GmailBodyParser {

    fun parseMimeBody(payload: GmailMessagePayload?): String {
        if (payload == null) return ""

        // 1. Direct body data on the root payload
        payload.body?.data?.let { encodedData ->
            val decoded = decodeBase64Safe(encodedData)
            if (decoded.isNotBlank()) return decoded
        }

        // 2. Search parts recursively
        val parts = payload.parts.orEmpty()
        return extractBodyFromParts(parts)
    }

    private fun extractBodyFromParts(parts: List<GmailMessagePart>): String {
        // Priority 1: Check for text/plain
        for (part in parts) {
            if (part.mimeType.equals("text/plain", ignoreCase = true)) {
                part.body?.data?.let { data ->
                    val text = decodeBase64Safe(data)
                    if (text.isNotBlank()) return text
                }
            }
            // Recurse deeper if child parts exist
            if (!part.parts.isNullOrEmpty()) {
                val nested = extractBodyFromParts(part.parts)
                if (nested.isNotBlank()) return nested
            }
        }

        // Priority 2: Fallback to text/html with tag cleanup
        for (part in parts) {
            if (part.mimeType.equals("text/html", ignoreCase = true)) {
                part.body?.data?.let { data ->
                    val html = decodeBase64Safe(data)
                    if (html.isNotBlank()) {
                        return stripHtmlTags(html)
                    }
                }
            }
        }

        return ""
    }

    fun decodeBase64Safe(data: String): String {
        if (data.isBlank()) return ""
        return try {
            val sanitized = data.trim()
                .replace('-', '+')
                .replace('_', '/')
            // Add padding if missing
            val padded = when (sanitized.length % 4) {
                2 -> "$sanitized=="
                3 -> "$sanitized="
                else -> sanitized
            }
            val decodedBytes = Base64.decode(padded, Base64.DEFAULT)
            String(decodedBytes, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }

    fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("<style[^>]*>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<script[^>]*>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n\\s*\\n"), "\n\n")
            .trim()
    }
}
