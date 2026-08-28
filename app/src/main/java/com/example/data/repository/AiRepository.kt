package com.example.data.repository

import com.example.data.api.AiBackendApi
import com.example.data.api.AiBackendClient
import com.example.data.api.AiGenerateRequest
import com.example.util.SafeLogger

/**
 * AI Repository that routes all generative AI queries exclusively through
 * the secure authenticated AI backend. The client holds zero API keys.
 */
class AiRepository(
    private val api: AiBackendApi = AiBackendClient.api
) {

    suspend fun generate(
        prompt: String,
        model: String = "default"
    ): Result<String> {
        if (prompt.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Prompt cannot be empty")
            )
        }

        return try {
            val response = api.generate(
                AiGenerateRequest(
                    prompt = prompt,
                    model = model
                )
            )

            if (response.text.isBlank()) {
                Result.failure(
                    IllegalStateException("AI returned an empty response")
                )
            } else {
                Result.success(response.text)
            }
        } catch (e: Exception) {
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
        temperature: Float? = null
    ): Result<String> {
        if (prompt.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Prompt cannot be empty")
            )
        }

        return try {
            val response = api.generate(
                AiGenerateRequest(
                    prompt = prompt,
                    model = model,
                    systemInstruction = systemInstruction,
                    temperature = temperature
                )
            )

            if (response.text.isBlank()) {
                Result.failure(
                    IllegalStateException("AI returned an empty response")
                )
            } else {
                Result.success(response.text)
            }
        } catch (e: Exception) {
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
        if (prompt.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Prompt cannot be empty")
            )
        }

        return try {
            val response = api.generate(
                AiGenerateRequest(
                    prompt = prompt,
                    model = "gemini-2.5-pro",
                    imagePayloadBase64 = imageBytesBase64
                )
            )

            if (response.text.isBlank()) {
                Result.failure(
                    IllegalStateException("AI vision returned an empty response")
                )
            } else {
                Result.success(response.text)
            }
        } catch (e: Exception) {
            SafeLogger.e("AiRepository", "AI vision backend failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun transcribeAudio(
        audioBytesBase64: String
    ): Result<String> {
        return try {
            val response = api.generate(
                AiGenerateRequest(
                    prompt = "Transcribe audio verbatim",
                    model = "gemini-2.5-flash",
                    audioPayloadBase64 = audioBytesBase64
                )
            )

            if (response.text.isBlank()) {
                Result.failure(
                    IllegalStateException("AI audio transcription returned an empty response")
                )
            } else {
                Result.success(response.text)
            }
        } catch (e: Exception) {
            SafeLogger.e("AiRepository", "AI audio backend failed: ${e.message}")
            Result.failure(e)
        }
    }
}
