package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.repository.AiRepository
import com.example.util.SafeLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Client interface for AI generation.
 * All operations are delegated securely to the backend AI service via [AiRepository].
 * No Gemini API key or credentials reside on the Android client.
 */
object GeminiApiClient {
    // Standard model identifiers supported by the backend (all allowlisted in AiRequestPolicy)
    const val MODEL_FLASH_GENERAL = "gemini-2.5-flash"
    const val MODEL_PRO_THINKING = "gemini-2.5-pro"
    const val MODEL_FLASH_LITE = "gemini-2.5-flash-lite"
    const val MODEL_VISION_PRO = "gemini-2.5-pro"
    const val MODEL_IMAGE_GEN = "imagen-3.0-generate-002"

    private val aiRepository: AiRepository by lazy { AiRepository(AiBackendClient.api) }

    /**
     * Standard Gemini API call via secure backend proxy with Prompt Injection Boundary defense.
     */
    suspend fun callGemini(
        prompt: String,
        model: String = MODEL_FLASH_GENERAL,
        systemInstruction: String = "You are an intelligent executive email assistant and outreach co-pilot. Analyze incoming text objectively and generate high-caliber, structured responses.",
        temperature: Float = 0.4f,
        thinkingBudget: Int? = null
    ): String = withContext(Dispatchers.IO) {
        if (prompt.isBlank()) {
            return@withContext "Notice: Empty prompt provided."
        }

        val result = aiRepository.generateWithConfig(
            prompt = prompt,
            model = model,
            systemInstruction = systemInstruction,
            temperature = temperature,
            thinkingBudget = thinkingBudget
        )

        result.getOrElse {
            // Do not surface backend/provider exception details to the end user.
            SafeLogger.e("GeminiApiClient", "AI backend request failed")
            "Notice: the AI service could not complete this request. Please try again."
        }
    }

    /**
     * High-Thinking Reasoning call using gemini-2.5-pro via backend
     */
    suspend fun callHighThinking(prompt: String): String {
        return callGemini(
            prompt = prompt,
            model = MODEL_PRO_THINKING,
            thinkingBudget = 2048,
            temperature = 0.2f
        )
    }

    /**
     * Fast low-latency generation using gemini-2.5-flash-lite via backend
     */
    suspend fun callLowLatency(prompt: String): String {
        return callGemini(
            prompt = prompt,
            model = MODEL_FLASH_LITE,
            temperature = 0.3f
        )
    }

    /**
     * Maps Grounded Query using Gemini via backend
     */
    suspend fun callWithMapsGrounding(prompt: String): String {
        return callGemini(
            prompt = "$prompt (Provide grounded location insights)",
            model = MODEL_FLASH_GENERAL
        )
    }

    /**
     * Multimodal Image & Document Analysis via backend
     */
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val result = aiRepository.analyzeImage(base64Image, prompt)
            result.getOrElse {
                // Do not surface backend/provider exception details to the end user.
                SafeLogger.e("GeminiApiClient", "Vision analysis failed")
                "Notice: image analysis could not be completed. Please try again."
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e("GeminiApiClient", "Vision image encoding error")
            "Notice: the image could not be processed."
        }
    }

    /**
     * Multimodal Audio / Voice Memo transcription via backend
     */
    suspend fun transcribeAudio(audioBytes: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val result = aiRepository.transcribeAudio(base64Audio)
            result.getOrElse {
                // Do not surface backend/provider exception details to the end user.
                SafeLogger.e("GeminiApiClient", "Audio transcription failed")
                "Notice: transcription could not be completed. Please try again."
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            SafeLogger.e("GeminiApiClient", "Audio processing error")
            "Notice: the audio could not be processed."
        }
    }

    /**
     * Imagen generation helper via backend.
     * Returns null when generation fails — callers must not treat null as success.
     */
    suspend fun generateImage(prompt: String, imageSize: String = "1K"): String? = withContext(Dispatchers.IO) {
        val result = aiRepository.generateWithConfig(
            prompt = "Generate marketing banner: $prompt [Size: $imageSize]",
            model = MODEL_IMAGE_GEN
        )
        result.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
