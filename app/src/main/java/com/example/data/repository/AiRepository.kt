package com.example.data.repository

import com.example.data.api.AiBackendApi
import com.example.data.api.AiBackendClient
import com.example.data.api.AiGenerateRequest
import com.example.data.auth.FirebaseIdTokenProvider
import com.example.util.SafeLogger

/**
 * Central, hard request policy for every AI backend call.
 * All values are enforced CLIENT-SIDE before any network traffic, and the backend
 * is expected to enforce them again (see docs/BACKEND_SECURITY_CONTRACT.md).
 */
object AiRequestPolicy {
    /** Maximum accepted prompt length (characters). */
    const val MAX_PROMPT_CHARS = 32_000

    /** Maximum accepted system instruction length (characters). */
    const val MAX_SYSTEM_INSTRUCTION_CHARS = 4_000

    /** Only these model identifiers may be requested. */
    val ALLOWED_MODELS: Set<String> = setOf(
        "default",
        "gemini-2.5-flash",
        "gemini-2.5-pro",
        "gemini-2.5-flash-lite",
        "imagen-3.0-generate-002"
    )

    /** Allowed temperature range, inclusive. Out-of-range values are clamped. */
    const val MIN_TEMPERATURE = 0.0f
    const val MAX_TEMPERATURE = 2.0f

    /** Allowed thinking budget range, inclusive. Out-of-range values are clamped. */
    const val MIN_THINKING_BUDGET = 0
    const val MAX_THINKING_BUDGET = 8_192

    /** Maximum accepted base64 payload size (characters) for images and audio. */
    const val MAX_IMAGE_BASE64_CHARS = 4_000_000
    const val MAX_AUDIO_BASE64_CHARS = 4_000_000
}

/**
 * AI Repository that routes all generative AI queries exclusively through
 * the secure authenticated AI backend. The client holds zero API keys.
 *
 * SECURITY INVARIANTS:
 * 1. FAIL CLOSED — if no Firebase user is signed in, no request leaves the device.
 * 2. Every request carries `Authorization: Bearer <firebase-id-token>`.
 * 3. Prompt, model, temperature, thinking budget, and payload sizes are validated
 *    against [AiRequestPolicy] before dispatch.
 */
class AiRepository(
    private val api: AiBackendApi = AiBackendClient.api,
    private val idTokenProvider: suspend () -> String? = { FirebaseIdTokenProvider.currentIdToken() }
) {

    /** Returns the Authorization header value, or throws if unauthenticated (fail closed). */
    private suspend fun authorizationHeader(): String {
        val token = idTokenProvider()
            ?: throw SecurityException(
                "Authentication required: no signed-in Firebase user. AI request blocked."
            )
        return "Bearer $token"
    }

    private fun validatePrompt(prompt: String) {
        if (prompt.isBlank()) {
            throw IllegalArgumentException("Prompt cannot be empty")
        }
        if (prompt.length > AiRequestPolicy.MAX_PROMPT_CHARS) {
            throw IllegalArgumentException(
                "Prompt exceeds the maximum length of ${AiRequestPolicy.MAX_PROMPT_CHARS} characters"
            )
        }
    }

    private fun validatedModel(model: String): String {
        if (model !in AiRequestPolicy.ALLOWED_MODELS) {
            throw IllegalArgumentException("Model '$model' is not on the allowed list")
        }
        return model
    }

    private fun validatedTemperature(temperature: Float?): Float? =
        temperature?.coerceIn(AiRequestPolicy.MIN_TEMPERATURE, AiRequestPolicy.MAX_TEMPERATURE)

    private fun validatedSystemInstruction(systemInstruction: String?): String? {
        if (systemInstruction != null && systemInstruction.length > AiRequestPolicy.MAX_SYSTEM_INSTRUCTION_CHARS) {
            throw IllegalArgumentException(
                "System instruction exceeds the maximum length of ${AiRequestPolicy.MAX_SYSTEM_INSTRUCTION_CHARS} characters"
            )
        }
        return systemInstruction
    }

    private fun validatedThinkingBudget(thinkingBudget: Int?): Int? =
        thinkingBudget?.coerceIn(AiRequestPolicy.MIN_THINKING_BUDGET, AiRequestPolicy.MAX_THINKING_BUDGET)

    private fun validateImagePayload(base64: String) {
        if (base64.length > AiRequestPolicy.MAX_IMAGE_BASE64_CHARS) {
            throw IllegalArgumentException("Image payload exceeds the maximum allowed size")
        }
    }

    private fun validateAudioPayload(base64: String) {
        if (base64.length > AiRequestPolicy.MAX_AUDIO_BASE64_CHARS) {
            throw IllegalArgumentException("Audio payload exceeds the maximum allowed size")
        }
    }

    private fun toResult(response: com.example.data.api.AiGenerateResponse): Result<String> {
        return if (response.status.equals("error", ignoreCase = true)) {
            Result.failure(IllegalStateException("AI backend returned an error status"))
        } else if (response.text.isBlank()) {
            Result.failure(IllegalStateException("AI returned an empty response"))
        } else {
            Result.success(response.text)
        }
    }

    suspend fun generate(
        prompt: String,
        model: String = "default"
    ): Result<String> {
        return try {
            validatePrompt(prompt)
            val response = api.generate(
                authorization = authorizationHeader(),
                request = AiGenerateRequest(
                    prompt = prompt,
                    model = validatedModel(model)
                )
            )
            toResult(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e(
                "AiRepository",
                "AI backend request failed: ${e.localizedMessage ?: e.message}"
            )
            Result.failure(e)
        }
    }

    suspend fun generateWithConfig(
        prompt: String,
        model: String = "default",
        systemInstruction: String? = null,
        temperature: Float? = null,
        thinkingBudget: Int? = null
    ): Result<String> {
        return try {
            validatePrompt(prompt)
            val response = api.generate(
                authorization = authorizationHeader(),
                request = AiGenerateRequest(
                    prompt = prompt,
                    model = validatedModel(model),
                    systemInstruction = validatedSystemInstruction(systemInstruction),
                    temperature = validatedTemperature(temperature),
                    thinkingBudget = validatedThinkingBudget(thinkingBudget)
                )
            )
            toResult(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e(
                "AiRepository",
                "AI backend request failed: ${e.localizedMessage ?: e.message}"
            )
            Result.failure(e)
        }
    }

    suspend fun analyzeImage(
        imageBytesBase64: String,
        prompt: String
    ): Result<String> {
        return try {
            validatePrompt(prompt)
            validateImagePayload(imageBytesBase64)
            val response = api.generate(
                authorization = authorizationHeader(),
                request = AiGenerateRequest(
                    prompt = prompt,
                    model = "gemini-2.5-pro",
                    imagePayloadBase64 = imageBytesBase64
                )
            )
            toResult(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e("AiRepository", "AI vision backend failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun transcribeAudio(
        audioBytesBase64: String
    ): Result<String> {
        return try {
            validateAudioPayload(audioBytesBase64)
            val response = api.generate(
                authorization = authorizationHeader(),
                request = AiGenerateRequest(
                    prompt = "Transcribe audio verbatim",
                    model = "gemini-2.5-flash",
                    audioPayloadBase64 = audioBytesBase64
                )
            )
            toResult(response)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e("AiRepository", "AI audio backend failed: ${e.message}")
            Result.failure(e)
        }
    }
}
