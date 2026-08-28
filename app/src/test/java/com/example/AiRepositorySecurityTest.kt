package com.example

import com.example.data.api.AiBackendApi
import com.example.data.api.AiGenerateRequest
import com.example.data.api.AiGenerateResponse
import com.example.data.repository.AiRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRepositorySecurityTest {

    private class FakeAiBackendApi(
        private val cannedResponse: String = "High deliverability email response",
        private val throwException: Boolean = false
    ) : AiBackendApi {
        override suspend fun generate(request: AiGenerateRequest): AiGenerateResponse {
            if (throwException) throw RuntimeException("Backend unavailable")
            return AiGenerateResponse(text = cannedResponse)
        }
    }

    @Test
    fun `test empty prompt is rejected locally before network dispatch`() = runTest {
        val repo = AiRepository(FakeAiBackendApi())
        val result = repo.generate(prompt = "")

        assertTrue(result.isFailure)
        assertEquals("Prompt cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test successful backend generation response`() = runTest {
        val repo = AiRepository(FakeAiBackendApi(cannedResponse = "Generated cold email pitch"))
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isSuccess)
        assertEquals("Generated cold email pitch", result.getOrNull())
    }

    @Test
    fun `test empty backend response returns failure`() = runTest {
        val repo = AiRepository(FakeAiBackendApi(cannedResponse = ""))
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
        assertEquals("AI returned an empty response", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test backend exception handling returns failure result`() = runTest {
        val repo = AiRepository(FakeAiBackendApi(throwException = true))
        val result = repo.generate(prompt = "Draft partner outreach")

        assertTrue(result.isFailure)
    }
}
