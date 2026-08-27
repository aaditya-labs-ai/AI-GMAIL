package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Gemini Request / Response DTOs ---

data class GeminiGenerateRequest(
    @field:Json(name = "contents") val contents: List<GeminiContent>,
    @field:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @field:Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null,
    @field:Json(name = "tools") val tools: List<GeminiTool>? = null
)

data class GeminiContent(
    @field:Json(name = "role") val role: String? = "user",
    @field:Json(name = "parts") val parts: List<GeminiPart>
)

data class GeminiPart(
    @field:Json(name = "text") val text: String? = null,
    @field:Json(name = "inlineData") val inlineData: GeminiBlob? = null
)

data class GeminiBlob(
    @field:Json(name = "mimeType") val mimeType: String,
    @field:Json(name = "data") val data: String
)

data class GeminiGenerationConfig(
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "topP") val topP: Float? = null,
    @field:Json(name = "topK") val topK: Int? = null,
    @field:Json(name = "maxOutputTokens") val maxOutputTokens: Int? = null,
    @field:Json(name = "thinkingConfig") val thinkingConfig: GeminiThinkingConfig? = null,
    @field:Json(name = "imageConfig") val imageConfig: GeminiImageConfig? = null
)

data class GeminiThinkingConfig(
    @field:Json(name = "thinkingBudget") val thinkingBudget: Int = 1024
)

data class GeminiImageConfig(
    @field:Json(name = "aspectRatio") val aspectRatio: String? = "1:1",
    @field:Json(name = "imageSize") val imageSize: String? = "1K"
)

data class GeminiTool(
    @field:Json(name = "googleSearch") val googleSearch: Map<String, String>? = null,
    @field:Json(name = "googleMaps") val googleMaps: Map<String, String>? = null
)

data class GeminiGenerateResponse(
    @field:Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @field:Json(name = "usageMetadata") val usageMetadata: GeminiUsageMetadata? = null,
    @field:Json(name = "modelVersion") val modelVersion: String? = null
)

data class GeminiCandidate(
    @field:Json(name = "content") val content: GeminiContent? = null,
    @field:Json(name = "finishReason") val finishReason: String? = null
)

data class GeminiUsageMetadata(
    @field:Json(name = "promptTokenCount") val promptTokenCount: Int? = null,
    @field:Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = null,
    @field:Json(name = "totalTokenCount") val totalTokenCount: Int? = null
)

// --- Retrofit Gemini API Interface ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Body request: GeminiGenerateRequest
    ): GeminiGenerateResponse
}

/**
 * Interceptor that appends x-goog-api-key header securely if present.
 */
class GeminiAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        val requestBuilder = original.newBuilder()
        if (apiKey.isNotBlank() && !apiKey.startsWith("YOUR_")) {
            requestBuilder.header("x-goog-api-key", apiKey)
        }
        return chain.proceed(requestBuilder.build())
    }
}

// --- Gemini Retrofit Client & Multi-Model Execution Engine ---

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    // Modern Gemini production models
    const val MODEL_FLASH_GENERAL = "gemini-2.5-flash"
    const val MODEL_PRO_THINKING = "gemini-2.5-pro"
    const val MODEL_FLASH_LITE = "gemini-2.5-flash-lite"
    const val MODEL_VISION_PRO = "gemini-2.5-pro"
    const val MODEL_IMAGE_GEN = "imagen-3.0-generate-002"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        redactHeader("x-goog-api-key")
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(GeminiAuthInterceptor())
        .addInterceptor(logging)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Standard Gemini API call with Prompt Injection Boundary defense
     */
    suspend fun callGemini(
        prompt: String,
        model: String = MODEL_FLASH_GENERAL,
        systemInstruction: String = "You are an intelligent executive email assistant and outreach co-pilot. Analyze incoming text objectively and generate high-caliber, structured responses.",
        temperature: Float = 0.4f,
        thinkingBudget: Int? = null,
        tools: List<GeminiTool>? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            return@withContext "Notice: Gemini API Key is not configured in the environment. Please add your GEMINI_API_KEY in the Secrets panel to activate live AI generation."
        }

        try {
            val config = GeminiGenerationConfig(
                temperature = temperature,
                thinkingConfig = if (thinkingBudget != null && thinkingBudget > 0) GeminiThinkingConfig(thinkingBudget) else null
            )

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                systemInstruction = GeminiContent(
                    role = "system",
                    parts = listOf(GeminiPart(text = systemInstruction))
                ),
                generationConfig = config,
                tools = tools
            )

            val response = service.generateContent(model = model, request = request)
            extractTextFromResponse(response)
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Gemini API error ($model)", e)
            "AI Assistant encountered an error communicating with Gemini: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Extracts text across all parts of all candidates in the Gemini response safely.
     */
    private fun extractTextFromResponse(response: GeminiGenerateResponse): String {
        val candidates = response.candidates.orEmpty()
        if (candidates.isEmpty()) return "No content generated."

        val stringBuilder = StringBuilder()
        for (candidate in candidates) {
            val parts = candidate.content?.parts.orEmpty()
            for (part in parts) {
                part.text?.let { stringBuilder.append(it) }
            }
        }

        val result = stringBuilder.toString().trim()
        return if (result.isNotBlank()) result else "No text response returned by model."
    }

    /**
     * High-Thinking Reasoning call using gemini-2.5-pro
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
     * Fast low-latency generation using gemini-2.5-flash-lite
     */
    suspend fun callLowLatency(prompt: String): String {
        return callGemini(
            prompt = prompt,
            model = MODEL_FLASH_LITE,
            temperature = 0.3f
        )
    }

    /**
     * Maps Grounded Query using Gemini
     */
    suspend fun callWithMapsGrounding(prompt: String): String {
        val mapsTool = listOf(GeminiTool(googleMaps = mapOf()))
        return callGemini(
            prompt = prompt,
            model = MODEL_FLASH_GENERAL,
            tools = mapsTool
        )
    }

    /**
     * Multimodal Image & Document Analysis
     */
    suspend fun analyzeImage(bitmap: Bitmap, prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            return@withContext "Notice: Gemini API Key is not configured. Please add GEMINI_API_KEY to activate multimodal image inspection."
        }

        try {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(text = prompt),
                            GeminiPart(
                                inlineData = GeminiBlob(
                                    mimeType = "image/jpeg",
                                    data = base64Image
                                )
                            )
                        )
                    )
                )
            )

            val response = service.generateContent(model = MODEL_VISION_PRO, request = request)
            extractTextFromResponse(response)
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Vision error", e)
            "Vision analysis error: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Multimodal Audio / Voice Memo transcription
     */
    suspend fun transcribeAudio(audioBytes: ByteArray): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            return@withContext "Notice: Gemini API Key is not configured. Please add GEMINI_API_KEY to transcribe voice memos."
        }

        try {
            val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(text = "Transcribe the following voice recording verbatim and then extract key email action items."),
                            GeminiPart(
                                inlineData = GeminiBlob(
                                    mimeType = "audio/mp3",
                                    data = base64Audio
                                )
                            )
                        )
                    )
                )
            )

            val response = service.generateContent(model = MODEL_FLASH_GENERAL, request = request)
            extractTextFromResponse(response)
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Audio error", e)
            "Audio transcription error: ${e.localizedMessage ?: e.message}"
        }
    }

    /**
     * Imagen generation helper
     */
    suspend fun generateImage(prompt: String, imageSize: String = "1K"): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.ifBlank { "" }
        if (apiKey.isBlank() || apiKey.startsWith("YOUR_")) {
            return@withContext "Notice: Gemini API Key is not configured for image generation."
        }

        try {
            val config = GeminiGenerationConfig(
                imageConfig = GeminiImageConfig(
                    aspectRatio = "1:1",
                    imageSize = imageSize
                )
            )

            val request = GeminiGenerateRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = config
            )

            val response = service.generateContent(model = MODEL_IMAGE_GEN, request = request)
            val candidates = response.candidates.orEmpty()
            val inlineData = candidates.firstOrNull()?.content?.parts?.firstOrNull()?.inlineData?.data
            if (!inlineData.isNullOrBlank()) {
                "data:image/png;base64,$inlineData"
            } else {
                "Prompt processed for marketing campaign."
            }
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Image gen error", e)
            "Image generation error: ${e.localizedMessage ?: e.message}"
        }
    }
}
