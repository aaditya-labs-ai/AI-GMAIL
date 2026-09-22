package com.example

import com.example.data.api.AiBackendApi
import com.example.data.api.AiGenerateRequest
import com.example.data.api.AiGenerateResponse
import com.example.data.repository.AiRepository
import com.example.data.repository.AiRequestPolicy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Security and regression coverage for the AI request path:
 * authentication, request validation, and response handling.
 */
class AiRepositorySecurityTest {

    /**
     * Fake backend that records the last request and Authorization header it saw.
     */
    private class RecordingAiBackendApi(
        private val cannedResponse: String = "High deliverability email response",
        private val status: String = "success",
        private val throwException: Boolean = false
    ) : AiBackendApi {
        var lastAuthorization: String? = null
            private set
        var lastRequest: AiGenerateRequest? = null
            private set
        var callCount: Int = 0
            private set

        override suspend fun generate(
            authorization: String,
            request: AiGenerateRequest
        ): AiGenerateResponse {
            callCount++
            lastAuthorization = authorization
            lastRequest = request
            if (throwException) throw RuntimeException("Backend unavailable")
            return AiGenerateResponse(text = cannedResponse, status = status)
        }
    }

    private val validToken: suspend () -> String? = { "test-id-token-123" }

    // --- AUTHENTICATION ---

    @Test
    fun `unauthenticated request fails closed and never reaches the network`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = { null })

        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        assertEquals(0, fake.callCount)
    }

    @Test
    fun `authenticated request carries a Bearer authorization header`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)

        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isSuccess)
        assertEquals("Bearer test-id-token-123", fake.lastAuthorization)
    }

    // --- REQUEST VALIDATION ---

    @Test
    fun `test empty prompt is rejected locally before network dispatch`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "")

        assertTrue(result.isFailure)
        assertEquals("Prompt cannot be empty", result.exceptionOrNull()?.message)
        assertEquals(0, fake.callCount)
    }

    @Test
    fun `oversized prompt is rejected before network dispatch`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "a".repeat(AiRequestPolicy.MAX_PROMPT_CHARS + 1))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals(0, fake.callCount)
    }

    @Test
    fun `model outside the allowlist is rejected before network dispatch`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "Draft partner outreach", model = "attacker-controlled-model")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not on the allowed list") == true)
        assertEquals(0, fake.callCount)
    }

    @Test
    fun `out-of-range temperature is clamped into the allowed band`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)

        repo.generateWithConfig(prompt = "Draft partner outreach", temperature = 99f)

        assertEquals(AiRequestPolicy.MAX_TEMPERATURE, fake.lastRequest?.temperature)
    }

    @Test
    fun `out-of-range thinking budget is clamped into the allowed band`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)

        repo.generateWithConfig(prompt = "Draft partner outreach", thinkingBudget = 1_000_000)

        assertEquals(AiRequestPolicy.MAX_THINKING_BUDGET, fake.lastRequest?.thinkingBudget)
    }

    @Test
    fun `oversized image payload is rejected before network dispatch`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.analyzeImage(
            imageBytesBase64 = "a".repeat(AiRequestPolicy.MAX_IMAGE_BASE64_CHARS + 1),
            prompt = "Analyze this document"
        )

        assertTrue(result.isFailure)
        assertEquals(0, fake.callCount)
    }

    @Test
    fun `oversized audio payload is rejected before network dispatch`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.transcribeAudio(
            audioBytesBase64 = "a".repeat(AiRequestPolicy.MAX_AUDIO_BASE64_CHARS + 1)
        )

        assertTrue(result.isFailure)
        assertEquals(0, fake.callCount)
    }

    // --- RESPONSE HANDLING ---

    @Test
    fun `test successful backend generation response`() = runTest {
        val fake = RecordingAiBackendApi(cannedResponse = "Generated cold email pitch")
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isSuccess)
        assertEquals("Generated cold email pitch", result.getOrNull())
    }

    @Test
    fun `test empty backend response returns failure`() = runTest {
        val fake = RecordingAiBackendApi(cannedResponse = "")
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
        assertEquals("AI returned an empty response", result.exceptionOrNull()?.message)
    }

    @Test
    fun `backend error status returns failure`() = runTest {
        val fake = RecordingAiBackendApi(cannedResponse = "irrelevant", status = "error")
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
        assertEquals("AI backend returned an error status", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test backend exception handling returns failure result`() = runTest {
        val fake = RecordingAiBackendApi(throwException = true)
        val repo = AiRepository(api = fake, idTokenProvider = validToken)
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
    }

    @Test
    fun `thinking budget is forwarded to the backend request`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = validToken)

        repo.generateWithConfig(prompt = "Deep analysis", thinkingBudget = 2048)

        assertEquals(2048, fake.lastRequest?.thinkingBudget)
    }

    @Test
    fun `unauthenticated request never leaks an authorization header value`() = runTest {
        val fake = RecordingAiBackendApi()
        val repo = AiRepository(api = fake, idTokenProvider = { null })

        repo.generate(prompt = "Draft partner outreach")

        assertNull(fake.lastAuthorization)
        assertEquals(0, fake.callCount)
    }
}
