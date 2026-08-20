package com.example.data.repository

import android.graphics.Bitmap
import com.example.data.api.GeminiApiClient
import com.example.data.firestore.FirestoreService
import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AssistantRepository(
    private val emailDao: EmailDao,
    private val coldMailDao: ColdMailDao,
    private val automationDao: AutomationDao,
    private val socialDao: SocialHubDao,
    private val gmailThreadDao: GmailThreadDao? = null,
    private val gmailMessageDao: GmailMessageDao? = null,
    private val draftMessageDao: DraftMessageDao? = null,
    private val firestoreService: FirestoreService = FirestoreService()
) {
    // Local Offline Drafts Cache
    fun getAllOfflineDrafts(): Flow<List<DraftMessageEntity>> =
        draftMessageDao?.getAllDrafts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getOfflineDraftForThread(threadId: String): Flow<DraftMessageEntity?> =
        draftMessageDao?.getDraftForThread(threadId) ?: kotlinx.coroutines.flow.flowOf(null)

    suspend fun saveOfflineDraft(draft: DraftMessageEntity): Long =
        draftMessageDao?.insertOrUpdateDraft(draft) ?: 0L

    suspend fun deleteOfflineDraft(draftId: Long) =
        draftMessageDao?.deleteDraftById(draftId)

    suspend fun deleteOfflineDraftsForThread(threadId: String) =
        draftMessageDao?.deleteDraftsForThread(threadId)

    // Gmail Threads & Messages Offline Cache streams
    fun getGmailThreadsByFolder(folder: EmailFolder): Flow<List<GmailThreadEntity>> =
        gmailThreadDao?.getThreadsByFolder(folder) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getGmailThreadsByCategory(folder: EmailFolder, category: EmailCategory): Flow<List<GmailThreadEntity>> =
        gmailThreadDao?.getThreadsByFolderAndCategory(folder, category) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getGmailThreadWithMessages(threadId: String): Flow<GmailThreadWithMessages?> =
        gmailThreadDao?.getThreadWithMessages(threadId) ?: kotlinx.coroutines.flow.flowOf(null)

    fun getMessagesForThread(threadId: String): Flow<List<GmailMessageEntity>> =
        gmailMessageDao?.getMessagesForThread(threadId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun saveFetchedGmailThread(thread: GmailThreadEntity, messages: List<GmailMessageEntity>) {
        gmailThreadDao?.insertThread(thread)
        gmailMessageDao?.insertMessages(messages)
    }

    suspend fun updateThreadUnread(threadId: String, isUnread: Boolean) {
        gmailThreadDao?.updateThreadUnread(threadId, isUnread)
    }

    suspend fun updateThreadStarred(threadId: String, isStarred: Boolean) {
        gmailThreadDao?.updateThreadStarred(threadId, isStarred)
    }

    suspend fun updateThreadFolder(threadId: String, folder: EmailFolder) {
        gmailThreadDao?.updateThreadFolder(threadId, folder)
    }

    // Email streams
    fun getEmailsByFolder(folder: EmailFolder): Flow<List<EmailEntity>> =
        emailDao.getEmailsByFolder(folder)

    fun getEmailsByFolderAndCategory(folder: EmailFolder, category: EmailCategory): Flow<List<EmailEntity>> =
        emailDao.getEmailsByFolderAndCategory(folder, category)

    fun getStarredEmails(): Flow<List<EmailEntity>> =
        emailDao.getStarredEmails()

    fun searchEmails(query: String): Flow<List<EmailEntity>> =
        emailDao.searchEmails(query)

    fun getEmailsByTag(tag: String): Flow<List<EmailEntity>> =
        emailDao.getEmailsByTag(tag)

    fun getUnreadCount(): Flow<Int> =
        emailDao.getUnreadCount()

    suspend fun getEmailById(id: Long): EmailEntity? =
        emailDao.getEmailById(id)

    suspend fun insertEmail(email: EmailEntity): Long =
        emailDao.insertEmail(email)

    suspend fun updateReadStatus(id: Long, isRead: Boolean) =
        emailDao.updateReadStatus(id, isRead)

    suspend fun updateStarredStatus(id: Long, isStarred: Boolean) =
        emailDao.updateStarredStatus(id, isStarred)

    suspend fun updateFolder(id: Long, folder: EmailFolder) =
        emailDao.updateFolder(id, folder)

    suspend fun deleteEmail(id: Long) =
        emailDao.deleteEmailById(id)

    suspend fun updateAiSummary(id: Long, summary: String, actionItems: String?) =
        emailDao.updateAiSummary(id, summary, actionItems)

    suspend fun updateReplyDraft(id: Long, draft: String?) =
        emailDao.updateReplyDraft(id, draft)

    // Cold Mail Campaigns (Local + Firestore)
    fun getAllColdMailCampaigns(): Flow<List<ColdMailCampaign>> =
        coldMailDao.getAllCampaigns()

    suspend fun saveColdMailCampaign(campaign: ColdMailCampaign, userId: String = "aditya_rai_001"): Long {
        val id = coldMailDao.insertCampaign(campaign)
        firestoreService.saveCampaignToCloud(userId, campaign.copy(id = id))
        return id
    }

    suspend fun deleteColdMailCampaign(campaign: ColdMailCampaign) =
        coldMailDao.deleteCampaign(campaign)

    // Automations (Local + Firestore)
    fun getAllAutomationRules(): Flow<List<AutomationRule>> =
        automationDao.getAllRules()

    fun getRecentAutomationLogs(): Flow<List<AutomationLog>> =
        automationDao.getRecentLogs()

    suspend fun saveAutomationRule(rule: AutomationRule, userId: String = "aditya_rai_001"): Long {
        val id = automationDao.insertRule(rule)
        firestoreService.saveRuleToCloud(userId, rule.copy(id = id))
        return id
    }

    suspend fun toggleRule(id: Long, isEnabled: Boolean) =
        automationDao.setRuleEnabled(id, isEnabled)

    suspend fun recordRuleExecution(id: Long, ruleName: String, detail: String) {
        val now = System.currentTimeMillis()
        automationDao.recordRuleExecution(id, now)
        automationDao.insertLog(
            AutomationLog(
                ruleName = ruleName,
                details = detail,
                timestamp = now,
                status = "SUCCESS"
            )
        )
    }

    suspend fun deleteAutomationRule(rule: AutomationRule) =
        automationDao.deleteRule(rule)

    // Social Hub (Local + Firestore)
    fun getSocialAccounts(): Flow<List<SocialAccount>> =
        socialDao.getAllAccounts()

    suspend fun updateSocialConnection(platform: SocialPlatform, connected: Boolean) =
        socialDao.updateConnection(platform, connected)

    fun getSocialOutreach(): Flow<List<SocialOutreachItem>> =
        socialDao.getAllOutreach()

    suspend fun saveSocialOutreach(item: SocialOutreachItem, userId: String = "aditya_rai_001"): Long {
        val id = socialDao.insertOutreach(item)
        firestoreService.saveSocialPostToCloud(userId, item.copy(id = id))
        return id
    }

    suspend fun markSocialOutreachSent(id: Long) =
        socialDao.markOutreachSent(id)

    // Gemini Model Specific AI Generation

    /**
     * Cold email generation with model selection (Thinking / Flash / Flash-Lite)
     */
    suspend fun generateColdEmail(
        targetName: String,
        targetCompany: String,
        targetRole: String,
        framework: String,
        valueProp: String,
        cta: String,
        tone: String,
        enableHighThinking: Boolean = false,
        isFastLiteMode: Boolean = false
    ): String {
        val prompt = """
        Write a world-class, high-converting cold email for Aditya Rai (kumaradityarai0005@gmail.com).
        Target Recipient: $targetName
        Company: $targetCompany
        Role: $targetRole
        Outreach Framework: $framework
        Core Value Proposition / Offer: $valueProp
        Call to Action: $cta
        Desired Tone: $tone

        Requirements:
        1. Give 2 irresistible, high-open subject lines (under 7 words).
        2. Write a punchy, ultra-concise email body (under 120 words) that hooks attention immediately, establishes credibility, addresses the recipient's pain point, presents the solution, and ends with a low-friction CTA.
        3. Include a 2-line high-impact Follow-Up email for Day 3.
        4. Provide an estimated deliverability and spam risk score.
        """.trimIndent()

        return when {
            enableHighThinking -> GeminiApiClient.callHighThinking(prompt)
            isFastLiteMode -> GeminiApiClient.callLowLatency(prompt)
            else -> GeminiApiClient.callGemini(prompt, model = GeminiApiClient.MODEL_FLASH_GENERAL)
        }
    }

    /**
     * Maps Grounding via gemini-3.5-flash with googleMaps tool
     */
    suspend fun searchCompanyLocationWithMaps(company: String, location: String): String {
        val query = "$company headquarters and offices in $location"
        return GeminiApiClient.callWithMapsGrounding(query)
    }

    /**
     * Image understanding & document analysis via gemini-3.1-pro-preview
     */
    suspend fun analyzeImageDocument(bitmap: Bitmap, prompt: String): String {
        return GeminiApiClient.analyzeImage(bitmap, prompt)
    }

    /**
     * Audio voice memo transcription via gemini-3.5-flash
     */
    suspend fun transcribeAudioMemo(audioBytes: ByteArray): String {
        return GeminiApiClient.transcribeAudio(audioBytes)
    }

    /**
     * Image generation via gemini-3-pro-image-preview with user selectable size (1K, 2K, 4K)
     */
    suspend fun generateCampaignImage(prompt: String, imageSize: String): String {
        return GeminiApiClient.generateImage(prompt, imageSize = imageSize)
    }

    /**
     * Context-aware Smart Reply feature using Gemini API
     * Analyzes incoming email body and returns 3 tailored quick replies (e.g. Acknowledge, Request Meeting, Decline).
     */
    suspend fun analyzeEmailForSmartReplies(sender: String, subject: String, body: String): List<SmartReplyOption> {
        val prompt = """
        Analyze the incoming email below for Aditya Rai (kumaradityarai0005@gmail.com).
        Generate exactly 3 distinct, context-aware smart quick-reply choices with drafted responses:
        1. An acknowledgement / confirmation response (e.g. "Acknowledge", "Confirm Received")
        2. An actionable / scheduling / follow-up response (e.g. "Request Meeting", "Propose Call", "Send Info")
        3. A polite decline / deferral response (e.g. "Decline", "Politely Pass", "Not at this time")

        Incoming Email Sender: $sender
        Subject: $subject
        Body:
        $body

        Respond in the following clean format with three sections separated by '---REPLY---':
        LABEL: <Short button title, 2-4 words, e.g. 'Acknowledge Receipt'>
        TYPE: <acknowledge | meeting | decline>
        BODY:
        <Complete, professional email reply signed off as Aditya Rai (kumaradityarai0005@gmail.com)>
        ---REPLY---
        LABEL: <Short button title, 2-4 words, e.g. 'Request Meeting'>
        TYPE: <meeting>
        BODY:
        <Complete, professional email reply signed off as Aditya Rai (kumaradityarai0005@gmail.com)>
        ---REPLY---
        LABEL: <Short button title, 2-4 words, e.g. 'Politely Decline'>
        TYPE: <decline>
        BODY:
        <Complete, professional email reply signed off as Aditya Rai (kumaradityarai0005@gmail.com)>
        """.trimIndent()

        try {
            val response = GeminiApiClient.callLowLatency(prompt)
            val replyBlocks = response.split("---REPLY---").map { it.trim() }.filter { it.isNotBlank() }
            val options = mutableListOf<SmartReplyOption>()

            for ((index, block) in replyBlocks.withIndex()) {
                val labelLine = block.lines().firstOrNull { it.startsWith("LABEL:", ignoreCase = true) }
                val typeLine = block.lines().firstOrNull { it.startsWith("TYPE:", ignoreCase = true) }
                val label = labelLine?.substringAfter(":")?.trim()
                    ?: when (index) {
                        0 -> "Acknowledge"
                        1 -> "Request Meeting"
                        else -> "Decline"
                    }
                val iconType = typeLine?.substringAfter(":")?.trim()?.lowercase()
                    ?: when (index) {
                        0 -> "acknowledge"
                        1 -> "meeting"
                        else -> "decline"
                    }
                val bodyIndex = block.indexOf("BODY:", ignoreCase = true)
                val replyBody = if (bodyIndex != -1) {
                    block.substring(bodyIndex + 5).trim()
                } else {
                    block.lines().drop(2).joinToString("\n").trim()
                }

                if (label.isNotBlank() && replyBody.isNotBlank()) {
                    options.add(
                        SmartReplyOption(
                            id = "smart_reply_${index + 1}",
                            label = label,
                            iconType = iconType,
                            fullDraft = replyBody
                        )
                    )
                }
            }

            if (options.size >= 3) {
                return options.take(3)
            }
        } catch (e: Exception) {
            android.util.Log.e("AssistantRepository", "analyzeEmailForSmartReplies error: ${e.message}")
        }

        // Robust context-aware fallbacks if offline or error
        return listOf(
            SmartReplyOption(
                id = "smart_reply_1",
                label = "Acknowledge",
                iconType = "acknowledge",
                fullDraft = "Hi $sender,\n\nThanks for following up on \"$subject\". I have received your email and will review the details shortly.\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com"
            ),
            SmartReplyOption(
                id = "smart_reply_2",
                label = "Request Meeting",
                iconType = "meeting",
                fullDraft = "Hi $sender,\n\nThanks for reaching out! Let's schedule a brief 15-minute call to discuss this further. Are you available this Thursday afternoon or Friday morning?\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com"
            ),
            SmartReplyOption(
                id = "smart_reply_3",
                label = "Decline",
                iconType = "decline",
                fullDraft = "Hi $sender,\n\nThank you for reaching out regarding this opportunity. Unfortunately, due to current project priorities, we will not be able to proceed at this time. I'll be sure to keep your details in mind for future collaboration.\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com"
            )
        )
    }

    /**
     * Low latency smart reply via gemini-3.1-flash-lite
     */
    suspend fun generateSmartReply(sender: String, subject: String, body: String, replyIntent: String): String {
        val prompt = """
        Draft a high-caliber email reply from Aditya Rai (kumaradityarai0005@gmail.com).
        Incoming Sender: $sender
        Subject: $subject
        Original Email:
        $body

        Reply Intent: $replyIntent
        Sign-off as Aditya Rai with email kumaradityarai0005@gmail.com.
        """.trimIndent()
        return GeminiApiClient.callLowLatency(prompt)
    }

    suspend fun summarizeEmail(sender: String, subject: String, body: String): Pair<String, String> {
        val prompt = """
        Analyze this email for Aditya Rai and provide:
        1. A concise 2-sentence summary (TL;DR).
        2. Extracted actionable bullet points (Action Items).

        Email from: $sender
        Subject: $subject
        Content:
        $body
        """.trimIndent()
        val result = GeminiApiClient.callGemini(prompt, model = GeminiApiClient.MODEL_FLASH_GENERAL)
        val parts = result.split("Action Items:", "Action points:", ignoreCase = true)
        val summary = parts.getOrNull(0)?.replace("Summary:", "", ignoreCase = true)?.trim() ?: result
        val actionItems = if (parts.size > 1) parts[1].trim() else "• Review request and reply"
        return Pair(summary, actionItems)
    }

    suspend fun askAssistant(userQuery: String, contextInfo: String): String {
        val prompt = """
        User Query: $userQuery

        Context of Aditya's Gmail & Outreach Assistant:
        $contextInfo

        Answer concisely, professionally, and provide actionable next steps or generated content.
        """.trimIndent()
        return GeminiApiClient.callGemini(prompt, model = GeminiApiClient.MODEL_FLASH_GENERAL)
    }

    suspend fun convertToSocialOutreach(platform: SocialPlatform, recipient: String, emailContent: String): String {
        val platformName = when (platform) {
            SocialPlatform.LINKEDIN -> "LinkedIn InMail / Connection Request (under 300 characters)"
            SocialPlatform.TWITTER_X -> "Twitter / X DM (punchy and conversational)"
            SocialPlatform.GITHUB -> "GitHub Discussion / PR collaboration message"
            SocialPlatform.INSTAGRAM -> "Instagram Direct Message for creator collab"
            SocialPlatform.SUBSTACK -> "Substack guest feature note"
        }
        val prompt = """
        Adapt this email cold pitch into a high-converting $platformName tailored for $recipient:
        Original pitch:
        $emailContent
        """.trimIndent()
        return GeminiApiClient.callGemini(prompt, model = GeminiApiClient.MODEL_FLASH_GENERAL)
    }
}
