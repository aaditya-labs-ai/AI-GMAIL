package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Android interface for communication with the secure, authenticated AI backend service.
 * The Gemini API key and server credentials exist ONLY on the backend server.
 *
 * SECURITY CONTRACT (see docs/BACKEND_SECURITY_CONTRACT.md):
 * - Every request MUST carry `Authorization: Bearer <firebase-id-token>`.
 * - The backend verifies the token with the Firebase Admin SDK before contacting Gemini.
 * - The client never holds a Gemini API key.
 */
interface AiBackendApi {

    @POST("v1/ai/generate")
    suspend fun generate(
        @Header("Authorization") authorization: String,
        @Body request: AiGenerateRequest
    ): AiGenerateResponse
}

data class AiGenerateRequest(
    @field:Json(name = "prompt") val prompt: String,
    @field:Json(name = "model") val model: String = "default",
    @field:Json(name = "systemInstruction") val systemInstruction: String? = null,
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "thinkingBudget") val thinkingBudget: Int? = null,
    @field:Json(name = "imagePayloadBase64") val imagePayloadBase64: String? = null,
    @field:Json(name = "audioPayloadBase64") val audioPayloadBase64: String? = null
)

data class AiGenerateResponse(
    @field:Json(name = "text") val text: String = "",
    @field:Json(name = "imageUrl") val imageUrl: String? = null,
    @field:Json(name = "status") val status: String = "success"
)

object AiBackendClient {
    // Backend endpoint is configurable via .env (AI_BACKEND_URL) so that no fake
    // production domain is ever shipped in source. When unconfigured, the placeholder
    // below makes requests fail closed instead of silently sending traffic anywhere.
    private val BACKEND_URL: String = BuildConfig.AI_BACKEND_URL
        .trim()
        .takeIf { it.startsWith("https://") && !it.contains("YOUR-") }
        ?: "https://YOUR-AI-BACKEND-DOMAIN/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val api: AiBackendApi by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AiBackendApi::class.java)
    }
}
