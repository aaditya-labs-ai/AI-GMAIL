package com.example.data.api

import android.util.Base64
import com.example.BuildConfig
import com.example.data.auth.GmailOAuthManager
import com.example.data.auth.GmailTokenProvider
import com.example.data.model.EmailCategory
import com.example.data.model.EmailFolder
import com.example.data.model.EmailPriority
import com.example.data.model.GmailMessageEntity
import com.example.data.model.GmailThreadEntity
import com.example.data.repository.AssistantRepository
import com.example.util.SafeLogger
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
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

// --- Gmail API Response Models ---

data class GmailThreadListResponse(
    @field:Json(name = "threads") val threads: List<GmailThreadListItem>? = null,
    @field:Json(name = "nextPageToken") val nextPageToken: String? = null,
    @field:Json(name = "resultSizeEstimate") val resultSizeEstimate: Int? = null
)

data class GmailThreadListItem(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "snippet") val snippet: String? = null,
    @field:Json(name = "historyId") val historyId: String? = null
)

data class GmailThreadDetailResponse(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "historyId") val historyId: String? = null,
    @field:Json(name = "messages") val messages: List<GmailMessageResponse>? = null
)

data class GmailMessageResponse(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "threadId") val threadId: String,
    @field:Json(name = "labelIds") val labelIds: List<String>? = null,
    @field:Json(name = "snippet") val snippet: String? = null,
    @field:Json(name = "historyId") val historyId: String? = null,
    @field:Json(name = "internalDate") val internalDate: String? = null,
    @field:Json(name = "payload") val payload: GmailMessagePayload? = null,
    @field:Json(name = "sizeEstimate") val sizeEstimate: Int? = null
)

data class GmailMessagePayload(
    @field:Json(name = "mimeType") val mimeType: String? = null,
    @field:Json(name = "filename") val filename: String? = null,
    @field:Json(name = "headers") val headers: List<GmailHeader>? = null,
    @field:Json(name = "body") val body: GmailMessageBody? = null,
    @field:Json(name = "parts") val parts: List<GmailMessagePart>? = null
)

data class GmailHeader(
    @field:Json(name = "name") val name: String,
    @field:Json(name = "value") val value: String
)

data class GmailMessageBody(
    @field:Json(name = "size") val size: Int? = null,
    @field:Json(name = "data") val data: String? = null
)

data class GmailMessagePart(
    @field:Json(name = "partId") val partId: String? = null,
    @field:Json(name = "mimeType") val mimeType: String? = null,
    @field:Json(name = "filename") val filename: String? = null,
    @field:Json(name = "headers") val headers: List<GmailHeader>? = null,
    @field:Json(name = "body") val body: GmailMessageBody? = null,
    @field:Json(name = "parts") val parts: List<GmailMessagePart>? = null
)

data class GmailDraftRequest(
    @field:Json(name = "message") val message: GmailDraftMessage
)

data class GmailDraftMessage(
    @field:Json(name = "raw") val raw: String? = null,
    @field:Json(name = "threadId") val threadId: String? = null
)

data class GmailSendMessageRequest(
    @field:Json(name = "raw") val raw: String,
    @field:Json(name = "threadId") val threadId: String? = null
)

data class GmailSendResponse(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "threadId") val threadId: String,
    @field:Json(name = "labelIds") val labelIds: List<String>? = null
)

// --- Retrofit Gmail API Service Interface ---

interface GmailApiService {
    @GET("gmail/v1/users/{userId}/threads")
    suspend fun listThreads(
        @Path("userId") userId: String = "me",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("q") query: String? = null
    ): GmailThreadListResponse

    @GET("gmail/v1/users/{userId}/threads/{id}")
    suspend fun getThread(
        @Path("userId") userId: String = "me",
        @Path("id") threadId: String,
        @Query("format") format: String = "full"
    ): GmailThreadDetailResponse

    @GET("gmail/v1/users/{userId}/messages")
    suspend fun listMessages(
        @Path("userId") userId: String = "me",
        @Query("maxResults") maxResults: Int = 25,
        @Query("q") query: String? = null
    ): GmailThreadListResponse

    @GET("gmail/v1/users/{userId}/messages/{id}")
    suspend fun getMessage(
        @Path("userId") userId: String = "me",
        @Path("id") messageId: String,
        @Query("format") format: String = "full"
    ): GmailMessageResponse

    @POST("gmail/v1/users/{userId}/messages/send")
    suspend fun sendMessage(
        @Path("userId") userId: String = "me",
        @Body request: GmailSendMessageRequest
    ): GmailSendResponse
}

/**
 * Secure OkHttp Interceptor for injecting Gmail OAuth 2.0 Bearer tokens.
 * Redacts tokens, detects HTTP 401 Unauthorized responses, and clears invalid sessions safely.
 */
class GmailAuthInterceptor(
    private val tokenProvider: GmailTokenProvider = GmailOAuthManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenProvider.getValidAccessToken()

        val requestBuilder = original.newBuilder()
        if (!token.isNullOrBlank()) {
            val formatted = if (token.startsWith("Bearer ", ignoreCase = true)) token else "Bearer $token"
            requestBuilder.header("Authorization", formatted)
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            SafeLogger.w("GmailAuthInterceptor", "Received 401 Unauthorized from Gmail API; invalidating session")
            tokenProvider.clearSession()
        }

        return response
    }
}

// --- Gmail Retrofit Client & Manager ---

object GmailApiClient {
    private const val BASE_URL = "https://gmail.googleapis.com/"

    private val authInterceptor = GmailAuthInterceptor()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        redactHeader("Authorization")
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val service: GmailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GmailApiService::class.java)
    }

    fun parseMimeBody(payload: GmailMessagePayload?): String {
        return GmailBodyParser.parseMimeBody(payload)
    }

    fun decodeBase64Safe(data: String): String {
        return GmailBodyParser.decodeBase64Safe(data)
    }

    /**
     * Sends an email directly via the Gmail API after user authorization and review.
     */
    suspend fun sendEmailDirect(
        recipientTo: String,
        subject: String,
        bodyText: String,
        threadId: String? = null
    ): Result<GmailSendResponse> = withContext(Dispatchers.IO) {
        val token = GmailOAuthManager.getValidAccessToken()
        if (token.isNullOrBlank()) {
            return@withContext Result.failure(
                SecurityException("Gmail OAuth 2.0 authorization is required to send emails. Please connect your Google/Gmail account.")
            )
        }
        if (recipientTo.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Recipient email address cannot be blank."))
        }

        try {
            // Build RFC 2822 / 5322 compliant message
            val rawMessage = buildString {
                append("To: ").append(recipientTo.trim()).append("\r\n")
                append("Subject: =?utf-8?B?")
                    .append(Base64.encodeToString(subject.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
                    .append("?=\r\n")
                append("Content-Type: text/plain; charset=\"UTF-8\"\r\n")
                append("Content-Transfer-Encoding: base64\r\n\r\n")
                append(Base64.encodeToString(bodyText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP))
            }

            val encodedRaw = Base64.encodeToString(
                rawMessage.toByteArray(StandardCharsets.UTF_8),
                Base64.URL_SAFE or Base64.NO_WRAP
            )

            val request = GmailSendMessageRequest(
                raw = encodedRaw,
                threadId = threadId?.takeIf { it.isNotBlank() }
            )

            val response = service.sendMessage(userId = "me", request = request)
            Result.success(response)
        } catch (e: Exception) {
            SafeLogger.e("GmailApiClient", "Failed to send email via Gmail API: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Performs a background sync of latest Gmail threads & messages into the local Room database.
     */
    suspend fun syncThreadsWithRoom(
        repository: AssistantRepository,
        onProgress: (progress: Float, message: String) -> Unit = { _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        val token = GmailOAuthManager.getValidAccessToken()
        if (token.isNullOrBlank()) {
            val error = SecurityException("Gmail OAuth 2.0 authorization is required to sync inbox. Please sign in with Google.")
            onProgress(1.0f, "Authentication Required: Please sign in with Google to sync Gmail.")
            return@withContext Result.failure(error)
        }

        try {
            onProgress(0.15f, "Connecting to Gmail API...")
            val threadListResponse = service.listThreads(
                userId = "me",
                maxResults = 15
            )

            val threadItems = threadListResponse.threads.orEmpty()
            if (threadItems.isEmpty()) {
                onProgress(1.0f, "Sync complete • No new threads found")
                return@withContext Result.success(0)
            }

            var syncedCount = 0

            threadItems.forEachIndexed { index, item ->
                val progress = 0.2f + (0.75f * (index + 1) / threadItems.size)
                onProgress(progress, "Syncing thread ${index + 1} of ${threadItems.size}...")

                try {
                    val threadDetail = service.getThread(
                        userId = "me",
                        threadId = item.id
                    )

                    val msgs = threadDetail.messages.orEmpty()
                    val lastMsg = msgs.lastOrNull()

                    val senderHeader = lastMsg?.payload?.headers?.firstOrNull { it.name.equals("From", ignoreCase = true) }?.value.orEmpty()
                    val subjectHeader = lastMsg?.payload?.headers?.firstOrNull { it.name.equals("Subject", ignoreCase = true) }?.value ?: "(No Subject)"

                    val parsedThread = GmailThreadEntity(
                        threadId = threadDetail.id,
                        historyId = threadDetail.historyId ?: "",
                        snippet = item.snippet ?: lastMsg?.snippet ?: "",
                        subject = subjectHeader,
                        senderSummary = senderHeader.substringBefore("<").trim().ifEmpty { senderHeader },
                        lastSenderEmail = senderHeader.substringAfter("<", "").substringBefore(">").trim().ifEmpty { senderHeader },
                        lastMessageTimestamp = lastMsg?.internalDate?.toLongOrNull() ?: System.currentTimeMillis(),
                        messageCount = msgs.size,
                        isUnread = lastMsg?.labelIds?.contains("UNREAD") == true,
                        isStarred = lastMsg?.labelIds?.contains("STARRED") == true,
                        folder = if (lastMsg?.labelIds?.contains("SENT") == true) EmailFolder.SENT else EmailFolder.INBOX,
                        category = EmailCategory.PRIMARY,
                        priority = if (lastMsg?.labelIds?.contains("IMPORTANT") == true) EmailPriority.HIGH else EmailPriority.NORMAL,
                        labels = lastMsg?.labelIds?.joinToString(", ") ?: "INBOX",
                        isSyncedOffline = true
                    )

                    val messageEntities = msgs.map { m ->
                        val from = m.payload?.headers?.firstOrNull { it.name.equals("From", ignoreCase = true) }?.value.orEmpty()
                        val to = m.payload?.headers?.firstOrNull { it.name.equals("To", ignoreCase = true) }?.value.orEmpty()
                        val sub = m.payload?.headers?.firstOrNull { it.name.equals("Subject", ignoreCase = true) }?.value ?: subjectHeader
                        val extractedBody = parseMimeBody(m.payload).ifBlank { m.snippet.orEmpty() }

                        GmailMessageEntity(
                            messageId = m.id,
                            threadId = m.threadId,
                            senderName = from.substringBefore("<").trim().ifEmpty { from },
                            senderEmail = from.substringAfter("<", "").substringBefore(">").trim().ifEmpty { from },
                            recipientEmails = to,
                            subject = sub,
                            snippet = m.snippet.orEmpty(),
                            bodyText = extractedBody,
                            internalDate = m.internalDate?.toLongOrNull() ?: System.currentTimeMillis(),
                            isRead = m.labelIds?.contains("UNREAD") != true,
                            isStarred = m.labelIds?.contains("STARRED") == true,
                            isSent = m.labelIds?.contains("SENT") == true,
                            folder = if (m.labelIds?.contains("SENT") == true) EmailFolder.SENT else EmailFolder.INBOX,
                            labelIds = m.labelIds?.joinToString(", ") ?: "INBOX",
                            isCachedLocally = true
                        )
                    }

                    repository.saveFetchedGmailThread(parsedThread, messageEntities)
                    syncedCount++
                } catch (e: Exception) {
                    SafeLogger.w("GmailApiClient", "Error fetching thread ${item.id}: ${e.message}")
                }
            }

            onProgress(1.0f, "Synced $syncedCount threads with Room database")
            Result.success(syncedCount)
        } catch (e: Exception) {
            SafeLogger.e("GmailApiClient", "Gmail sync failure: ${e.message}")
            onProgress(1.0f, "Sync Failed: ${e.message}")
            Result.failure(e)
        }
    }
}
