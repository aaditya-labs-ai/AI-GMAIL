package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Android interface for communication with the secure, authenticated AI backend service.
 * The Gemini API key and server credentials exist ONLY on the backend server.
 */
interface AiBackendApi {

    @POST("v1/ai/generate")
    suspend fun generate(
        @Body request: AiGenerateRequest
    ): AiGenerateResponse
}

data class AiGenerateRequest(
    @field:Json(name = "prompt") val prompt: String,
    @field:Json(name = "model") val model: String = "default",
    @field:Json(name = "systemInstruction") val systemInstruction: String? = null,
    @field:Json(name = "temperature") val temperature: Float? = null,
    @field:Json(name = "imagePayloadBase64") val imagePayloadBase64: String? = null,
    @field:Json(name = "audioPayloadBase64") val audioPayloadBase64: String? = null
)

data class AiGenerateResponse(
    @field:Json(name = "text") val text: String = "",
    @field:Json(name = "imageUrl") val imageUrl: String? = null,
    @field:Json(name = "status") val status: String = "success"
)

object AiBackendClient {
    // Backend service placeholder domain. In production, this points to your authenticated cloud endpoint.
    private const val BACKEND_URL = "https://YOUR-AI-BACKEND-DOMAIN/"

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
