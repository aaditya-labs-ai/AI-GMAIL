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
    private val firestoreService: FirestoreService = FirestoreService()
) {
    // Email streams
    fun getEmailsByFolder(folder: EmailFolder): Flow<List<EmailEntity>> =
        emailDao.getEmailsByFolder(folder)

    fun getEmailsByFolderAndCategory(folder: EmailFolder, category: EmailCategory): Flow<List<EmailEntity>> =
        emailDao.getEmailsByFolderAndCategory(folder, category)

    fun getStarredEmails(): Flow<List<EmailEntity>> =
        emailDao.getStarredEmails()

    fun searchEmails(query: String): Flow<List<EmailEntity>> =
        emailDao.searchEmails(query)

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
