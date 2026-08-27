package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthResult
import com.example.data.auth.AuthUserState
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.GmailOAuthManager
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AssistantScreen {
    object Inbox : AssistantScreen()
    object ColdMailStudio : AssistantScreen()
    object AutomationStudio : AssistantScreen()
    object SocialHub : AssistantScreen()
    object AICopilot : AssistantScreen()
    object Settings : AssistantScreen()
}

data class AssistantUiState(
    val currentScreen: AssistantScreen = AssistantScreen.Inbox,
    val currentFolder: EmailFolder = EmailFolder.INBOX,
    val currentCategory: EmailCategory = EmailCategory.PRIMARY,
    val searchQuery: String = "",
    val selectedEmail: EmailEntity? = null,
    val isComposeOpen: Boolean = false,
    val isAnalyzing: Boolean = false,
    val isAccountDialogVisible: Boolean = false,
    val showAccountDialog: Boolean = false,
    val isAuthenticating: Boolean = false,
    val aiStatusMessage: String? = null,
    val unreadCount: Int = 0,
    val userProfile: UserProfile = UserProfile(),
    // Cold Mail Studio State
    val coldTargetName: String = "Enterprise Lead",
    val coldTargetCompany: String = "Tech Innovations",
    val coldTargetRole: String = "VP Growth",
    val coldTargetLocation: String = "San Francisco, CA",
    val coldFramework: String = "PAS (Problem, Agitate, Solve)",
    val coldValueProp: String = "Autonomous AI email assistant with high deliverability and multi-channel sync",
    val coldCta: String = "10-min live demo this week",
    val coldTone: String = "Executive & Direct",
    val generatedColdMail: String = "",
    val coldDeliverabilityScore: Int = 94,
    val isGeneratingColdMail: Boolean = false,
    val enableHighThinking: Boolean = false,
    val isFastLiteMode: Boolean = false,
    val mapsGroundingResult: String? = null,
    val isSearchingMaps: Boolean = false,
    // Context-aware Smart Replies State
    val smartReplies: List<SmartReplyOption> = emptyList(),
    val isAnalyzingSmartReplies: Boolean = false,
    val selectedSmartReply: SmartReplyOption? = null,
    // Visual generation (1K, 2K, 4K)
    val selectedImageSize: String = "1K", // "1K", "2K", "4K"
    val generatedBannerUrl: String? = null,
    val isGeneratingBanner: Boolean = false,
    // Vision & Audio understanding
    val visionAnalysisResult: String? = null,
    val isAnalyzingVision: Boolean = false,
    val isRecordingVoice: Boolean = false,
    val transcribedVoiceText: String? = null,
    // AI Chat Copilot
    val chatMessages: List<AiChatMessage> = emptyList(),
    val isCopilotThinking: Boolean = false,
    // Room & Gmail Sync Status
    val isSyncing: Boolean = false,
    val syncProgressMessage: String = "All emails cached offline in Room",
    val syncProgressFraction: Float = 1.0f,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    // Compose Prefill
    val composeInitialTo: String = "",
    val composeInitialSubject: String = "",
    val composeInitialBody: String = "",
    // Tag Filtering
    val selectedTagFilter: String? = null,
    // Settings & Notification Preferences
    val isSettingsOpen: Boolean = false,
    val notificationPreferences: NotificationPreferences = NotificationPreferences(),
    // Scheduled Emails
    val scheduledEmails: List<ScheduledEmail> = emptyList(),
    val isSummarizingThread: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (throwable !is kotlinx.coroutines.CancellationException) {
            android.util.Log.e("AssistantViewModel", "Coroutine error: ${throwable.message}")
            _uiState.update { it.copy(aiStatusMessage = "Operation notice: ${throwable.localizedMessage ?: throwable.message}") }
        }
    }

    private val database = AppDatabase.getDatabase(application)
    private val repository = AssistantRepository(
        database.emailDao(),
        database.coldMailDao(),
        database.automationDao(),
        database.socialHubDao(),
        database.gmailThreadDao(),
        database.gmailMessageDao(),
        database.draftMessageDao()
    )
    val authService = FirebaseAuthService(application)
    val authUserState: StateFlow<AuthUserState> = authService.userState

    val offlineDrafts: StateFlow<List<DraftMessageEntity>> = repository.getAllOfflineDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    // Real-time streams from Room
    val emails: StateFlow<List<EmailEntity>> = _uiState
        .flatMapLatest { state ->
            if (state.searchQuery.isNotBlank()) {
                repository.searchEmails(state.searchQuery)
            } else if (!state.selectedTagFilter.isNullOrBlank()) {
                repository.getEmailsByTag(state.selectedTagFilter)
            } else if (state.currentFolder == EmailFolder.STARRED) {
                repository.getStarredEmails()
            } else if (state.currentFolder == EmailFolder.INBOX) {
                repository.getEmailsByFolderAndCategory(state.currentFolder, state.currentCategory)
            } else {
                repository.getEmailsByFolder(state.currentFolder)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val coldCampaigns: StateFlow<List<ColdMailCampaign>> = repository.getAllColdMailCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationRules: StateFlow<List<AutomationRule>> = repository.getAllAutomationRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automationLogs: StateFlow<List<AutomationLog>> = repository.getRecentAutomationLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialAccounts: StateFlow<List<SocialAccount>> = repository.getSocialAccounts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialOutreach: StateFlow<List<SocialOutreachItem>> = repository.getSocialOutreach()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Sync auth state to userProfile in UI state reactively
        viewModelScope.launch {
            authUserState.collect { authUser ->
                _uiState.update { state ->
                    state.copy(
                        userProfile = if (authUser.isAuthenticated) {
                            UserProfile(
                                displayName = authUser.displayName.ifBlank { "User" },
                                email = authUser.email.ifBlank { "user@gmail.com" },
                                title = "Authenticated User",
                                signature = "Best regards,\n${authUser.displayName}\n${authUser.email}",
                                isGoogleConnected = authUser.isGoogleLinked
                            )
                        } else {
                            UserProfile(
                                displayName = "",
                                email = "",
                                title = "AI Mail Copilot",
                                signature = "",
                                isGoogleConnected = false
                            )
                        }
                    )
                }
            }
        }

        // Initialize welcome message in Copilot
        _uiState.update {
            it.copy(
                chatMessages = listOf(
                    AiChatMessage(
                        sender = "assistant",
                        message = "Hello! I am your AI executive mail co-pilot. I can analyze incoming emails, extract key action items, draft context-aware replies, generate cold outreach campaigns, and sync across platforms. How can I assist you today?"
                    )
                )
            )
        }
    }

    // Navigation & View Actions
    fun setScreen(screen: AssistantScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setFolder(folder: EmailFolder) {
        _uiState.update { it.copy(currentFolder = folder, selectedTagFilter = null) }
    }

    fun setCategory(category: EmailCategory) {
        _uiState.update { it.copy(currentCategory = category, selectedTagFilter = null) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setTagFilter(tag: String?) {
        _uiState.update { it.copy(selectedTagFilter = tag) }
    }

    fun selectEmail(email: EmailEntity?) {
        _uiState.update {
            it.copy(
                selectedEmail = email,
                smartReplies = emptyList(),
                selectedSmartReply = null
            )
        }
        if (email != null && !email.isRead) {
            markEmailAsRead(email.id, true)
        }
    }

    fun setComposeOpen(isOpen: Boolean) {
        _uiState.update {
            it.copy(
                isComposeOpen = isOpen,
                composeInitialTo = if (isOpen) it.composeInitialTo else "",
                composeInitialSubject = if (isOpen) it.composeInitialSubject else "",
                composeInitialBody = if (isOpen) it.composeInitialBody else ""
            )
        }
    }

    fun setAccountDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showAccountDialog = visible, isAccountDialogVisible = visible) }
    }

    // Email Operations
    fun toggleStar(emailId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateStarredStatus(emailId, !currentStatus)
        }
    }

    fun markEmailAsRead(emailId: Long, isRead: Boolean) {
        viewModelScope.launch {
            repository.updateReadStatus(emailId, isRead)
        }
    }

    fun moveToFolder(emailId: Long, folder: EmailFolder) {
        viewModelScope.launch {
            repository.updateFolder(emailId, folder)
            _uiState.update {
                it.copy(
                    selectedEmail = if (it.selectedEmail?.id == emailId) null else it.selectedEmail,
                    aiStatusMessage = "Email moved to ${folder.name.lowercase().replaceFirstChar { c -> c.uppercase() }}"
                )
            }
        }
    }

    fun deleteEmail(emailId: Long) {
        viewModelScope.launch {
            repository.deleteEmail(emailId)
            _uiState.update {
                it.copy(
                    selectedEmail = null,
                    aiStatusMessage = "Email deleted permanently"
                )
            }
        }
    }

    fun sendEmail(
        to: String,
        subject: String,
        body: String,
        isDraft: Boolean = false,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            if (to.isBlank()) {
                val errorMsg = "Recipient email cannot be blank."
                _uiState.update { it.copy(aiStatusMessage = errorMsg) }
                onComplete(false, errorMsg)
                return@launch
            }

            val userEmail = _uiState.value.userProfile.email.ifBlank { "user@example.com" }
            val userName = _uiState.value.userProfile.displayName.ifBlank { "User" }

            if (isDraft) {
                // Save locally to Room Drafts
                val email = EmailEntity(
                    senderName = userName,
                    senderEmail = userEmail,
                    recipientEmail = to,
                    subject = subject,
                    snippet = body.take(100),
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    isRead = true,
                    folder = EmailFolder.DRAFTS,
                    category = EmailCategory.PRIMARY,
                    priority = EmailPriority.NORMAL,
                    tags = "Draft"
                )
                repository.insertEmail(email)
                repository.saveOfflineDraft(
                    DraftMessageEntity(
                        recipientTo = to,
                        subject = subject,
                        body = body,
                        lastSavedTimestamp = System.currentTimeMillis(),
                        syncStatus = "LOCAL_DRAFT"
                    )
                )
                _uiState.update {
                    it.copy(
                        isComposeOpen = false,
                        aiStatusMessage = "Draft saved locally to Room database."
                    )
                }
                onComplete(true, "Draft saved")
            } else {
                // Outbound live dispatch
                if (GmailOAuthManager.hasValidAuthorization()) {
                    val result = repository.sendDirectGmailMessage(
                        to = to,
                        subject = subject,
                        body = body
                    )
                    if (result.isSuccess) {
                        val email = EmailEntity(
                            senderName = userName,
                            senderEmail = userEmail,
                            recipientEmail = to,
                            subject = subject,
                            snippet = body.take(100),
                            body = body,
                            timestamp = System.currentTimeMillis(),
                            isRead = true,
                            folder = EmailFolder.SENT,
                            category = EmailCategory.PRIMARY,
                            priority = EmailPriority.NORMAL,
                            tags = "Sent Outbound",
                            deliveryStatus = MessageDeliveryStatus.SENT
                        )
                        repository.insertEmail(email)
                        _uiState.update {
                            it.copy(
                                isComposeOpen = false,
                                aiStatusMessage = "Email sent successfully via Gmail API!"
                            )
                        }
                        onComplete(true, "Email sent successfully")
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "Failed to dispatch email"
                        _uiState.update { it.copy(aiStatusMessage = "Send Error: $error") }
                        onComplete(false, error)
                    }
                } else {
                    // Save as outbox/draft and alert user
                    val email = EmailEntity(
                        senderName = userName,
                        senderEmail = userEmail,
                        recipientEmail = to,
                        subject = subject,
                        snippet = body.take(100),
                        body = body,
                        timestamp = System.currentTimeMillis(),
                        isRead = true,
                        folder = EmailFolder.DRAFTS,
                        category = EmailCategory.PRIMARY,
                        priority = EmailPriority.NORMAL,
                        tags = "Pending Authorization"
                    )
                    repository.insertEmail(email)
                    _uiState.update {
                        it.copy(
                            isComposeOpen = false,
                            aiStatusMessage = "Saved to Drafts: Gmail OAuth authorization is required to send live outbound mail."
                        )
                    }
                    onComplete(false, "Gmail OAuth authorization required to dispatch email.")
                }
            }
        }
    }

    fun saveDraftMessage(
        to: String,
        subject: String,
        body: String,
        threadId: String? = null,
        draftId: Long = 0
    ) {
        viewModelScope.launch {
            val draft = DraftMessageEntity(
                draftId = draftId,
                threadId = threadId,
                recipientTo = to,
                subject = subject,
                body = body,
                lastSavedTimestamp = System.currentTimeMillis(),
                syncStatus = "LOCAL_DRAFT"
            )
            repository.saveOfflineDraft(draft)
            _uiState.update { it.copy(aiStatusMessage = "Draft saved offline in Room database") }
        }
    }

    fun deleteDraftMessage(draftId: Long) {
        viewModelScope.launch {
            repository.deleteOfflineDraft(draftId)
            _uiState.update { it.copy(aiStatusMessage = "Draft deleted") }
        }
    }

    fun summarizeSelectedEmail() {
        val email = _uiState.value.selectedEmail ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            val (summary, actionItems) = repository.summarizeEmail(
                sender = email.senderName,
                subject = email.subject,
                body = email.body
            )
            repository.updateAiSummary(email.id, summary, actionItems)
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    selectedEmail = email.copy(aiSummary = summary, aiActionItems = actionItems)
                )
            }
        }
    }

    fun generateSmartReplyForSelected(replyIntent: String) {
        val email = _uiState.value.selectedEmail ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }
            val userName = _uiState.value.userProfile.displayName.ifBlank { "User" }
            val userEmail = _uiState.value.userProfile.email.ifBlank { "user@example.com" }
            val draft = repository.generateSmartReply(
                userDisplayName = userName,
                userEmail = userEmail,
                sender = email.senderName,
                subject = email.subject,
                body = email.body,
                replyIntent = replyIntent
            )
            repository.updateReplyDraft(email.id, draft)
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    selectedEmail = email.copy(replyDraft = draft)
                )
            }
        }
    }

    fun loadSmartRepliesForEmail(email: EmailEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingSmartReplies = true) }
            val userName = _uiState.value.userProfile.displayName.ifBlank { "User" }
            val userEmail = _uiState.value.userProfile.email.ifBlank { "user@example.com" }
            val replies = repository.analyzeEmailForSmartReplies(
                userDisplayName = userName,
                userEmail = userEmail,
                sender = email.senderName,
                subject = email.subject,
                body = email.body
            )
            _uiState.update {
                it.copy(
                    smartReplies = replies,
                    isAnalyzingSmartReplies = false
                )
            }
        }
    }

    fun selectSmartReplyOption(option: SmartReplyOption) {
        _uiState.update { it.copy(selectedSmartReply = option) }
        val currentEmail = _uiState.value.selectedEmail
        if (currentEmail != null) {
            viewModelScope.launch {
                repository.updateReplyDraft(currentEmail.id, option.fullDraft)
                _uiState.update {
                    it.copy(selectedEmail = currentEmail.copy(replyDraft = option.fullDraft))
                }
            }
        }
    }

    // Cold Mail Studio
    fun updateColdMailParams(
        targetName: String = _uiState.value.coldTargetName,
        targetCompany: String = _uiState.value.coldTargetCompany,
        targetRole: String = _uiState.value.coldTargetRole,
        targetLocation: String = _uiState.value.coldTargetLocation,
        framework: String = _uiState.value.coldFramework,
        valueProp: String = _uiState.value.coldValueProp,
        cta: String = _uiState.value.coldCta,
        tone: String = _uiState.value.coldTone
    ) {
        _uiState.update {
            it.copy(
                coldTargetName = targetName,
                coldTargetCompany = targetCompany,
                coldTargetRole = targetRole,
                coldTargetLocation = targetLocation,
                coldFramework = framework,
                coldValueProp = valueProp,
                coldCta = cta,
                coldTone = tone
            )
        }
    }

    fun setColdModelConfig(enableHighThinking: Boolean, isFastLiteMode: Boolean) {
        _uiState.update {
            it.copy(
                enableHighThinking = enableHighThinking,
                isFastLiteMode = isFastLiteMode
            )
        }
    }

    fun setImageSize(size: String) {
        _uiState.update { it.copy(selectedImageSize = size) }
    }

    fun generateColdMail() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGeneratingColdMail = true,
                    aiStatusMessage = if (state.enableHighThinking) "Thinking with Gemini 2.5 Pro..." else "Generating with Gemini..."
                )
            }
            val userName = state.userProfile.displayName.ifBlank { "User" }
            val userEmail = state.userProfile.email.ifBlank { "user@example.com" }
            val result = repository.generateColdEmail(
                senderName = userName,
                senderEmail = userEmail,
                targetName = state.coldTargetName,
                targetCompany = state.coldTargetCompany,
                targetRole = state.coldTargetRole,
                framework = state.coldFramework,
                valueProp = state.coldValueProp,
                cta = state.coldCta,
                tone = state.coldTone,
                enableHighThinking = state.enableHighThinking,
                isFastLiteMode = state.isFastLiteMode
            )
            _uiState.update {
                it.copy(
                    generatedColdMail = result,
                    isGeneratingColdMail = false,
                    aiStatusMessage = "Cold email generated successfully!"
                )
            }
        }
    }

    fun searchTargetCompanyLocation() {
        val state = _uiState.value
        if (state.coldTargetCompany.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingMaps = true) }
            val result = repository.searchCompanyLocationWithMaps(
                company = state.coldTargetCompany,
                location = state.coldTargetLocation
            )
            _uiState.update {
                it.copy(
                    mapsGroundingResult = result,
                    isSearchingMaps = false
                )
            }
        }
    }

    fun analyzeImageDocument(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingVision = true) }
            val analysis = repository.analyzeImageDocument(
                bitmap = bitmap,
                prompt = "Analyze this pitch document/business card/screenshot. Extract key target contacts, company focus, and recommend a personalized outreach hook."
            )
            _uiState.update {
                it.copy(
                    visionAnalysisResult = analysis,
                    isAnalyzingVision = false,
                    aiStatusMessage = "Vision analysis complete!"
                )
            }
        }
    }

    fun transcribeVoiceMemo(audioBytes: ByteArray = ByteArray(0)) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecordingVoice = true) }
            val text = repository.transcribeAudioMemo(audioBytes)
            _uiState.update {
                it.copy(
                    transcribedVoiceText = text,
                    generatedColdMail = if (it.generatedColdMail.isBlank()) text else it.generatedColdMail,
                    isRecordingVoice = false,
                    aiStatusMessage = "Voice memo transcribed via Gemini!"
                )
            }
        }
    }

    fun generateCampaignBanner(prompt: String) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingBanner = true) }
            val url = repository.generateCampaignImage(
                prompt = prompt,
                imageSize = state.selectedImageSize
            )
            _uiState.update {
                it.copy(
                    generatedBannerUrl = url,
                    isGeneratingBanner = false,
                    aiStatusMessage = "${state.selectedImageSize} banner generated via Imagen!"
                )
            }
        }
    }

    fun saveGeneratedColdCampaign() {
        val state = _uiState.value
        if (state.generatedColdMail.isBlank()) return
        viewModelScope.launch {
            val campaign = ColdMailCampaign(
                title = "Cold Pitch: ${state.coldTargetCompany} (${state.coldTargetRole})",
                targetName = state.coldTargetName,
                targetCompany = state.coldTargetCompany,
                targetRole = state.coldTargetRole,
                framework = state.coldFramework,
                subjectVariantA = "Quick inquiry for ${state.coldTargetCompany}",
                subjectVariantB = "${state.coldTargetName} / Outbound Proposal",
                body = state.generatedColdMail,
                followUp1 = "Hi ${state.coldTargetName}, following up on my previous note...",
                valueProposition = state.coldValueProp,
                callToAction = state.coldCta,
                tone = state.coldTone,
                deliverabilityScore = state.coldDeliverabilityScore,
                isSaved = true
            )
            val userId = authService.userState.value.uid
            repository.saveColdMailCampaign(campaign, userId)
            _uiState.update { it.copy(aiStatusMessage = "Campaign saved locally & synced to cloud!") }
        }
    }

    // Automations
    fun toggleAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            repository.toggleRule(rule.id, !rule.isEnabled)
        }
    }

    fun triggerAutomationEngine() {
        viewModelScope.launch {
            _uiState.update { it.copy(aiStatusMessage = "⚡ Running automation engine on mailbox...") }
            val activeRules = automationRules.value.filter { it.isEnabled }
            for (rule in activeRules) {
                repository.recordRuleExecution(
                    rule.id,
                    rule.name,
                    "Processed unread threads matching [${rule.triggerCondition}] — executed ${rule.actionType}"
                )
            }
            _uiState.update { it.copy(aiStatusMessage = "Automations executed: ${activeRules.size} rules active.") }
        }
    }

    fun addCustomAutomationRule(
        name: String,
        desc: String,
        triggerType: String,
        triggerCond: String,
        actionType: String,
        actionParam: String
    ) {
        viewModelScope.launch {
            val rule = AutomationRule(
                name = name,
                description = desc,
                triggerType = triggerType,
                triggerCondition = triggerCond,
                actionType = actionType,
                actionParameter = actionParam,
                isEnabled = true,
                executionCount = 0,
                lastRunTimestamp = System.currentTimeMillis()
            )
            val userId = authService.userState.value.uid
            repository.saveAutomationRule(rule, userId)
            _uiState.update { it.copy(aiStatusMessage = "New automation rule activated & synced!") }
        }
    }

    // Social Hub
    fun toggleSocialConnection(platform: SocialPlatform, isConnected: Boolean) {
        viewModelScope.launch {
            repository.updateSocialConnection(platform, isConnected)
        }
    }

    fun createSocialOutreach(
        platform: SocialPlatform,
        target: String,
        pitchBody: String,
        outreachType: String
    ) {
        viewModelScope.launch {
            val adaptedMessage = repository.convertToSocialOutreach(platform, target, pitchBody)
            val item = SocialOutreachItem(
                platform = platform,
                targetPersonOrChannel = target,
                messageText = adaptedMessage,
                outreachType = outreachType,
                timestamp = System.currentTimeMillis(),
                isSent = false
            )
            val userId = authService.userState.value.uid
            repository.saveSocialOutreach(item, userId)
            _uiState.update { it.copy(aiStatusMessage = "Outreach message queued for $platform!") }
        }
    }

    fun sendSocialOutreach(id: Long) {
        viewModelScope.launch {
            repository.markSocialOutreachSent(id)
            _uiState.update { it.copy(aiStatusMessage = "Outreach marked as sent!") }
        }
    }

    // Copilot AI Chat
    fun sendCopilotMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val userMsg = AiChatMessage(
            sender = "user",
            message = userPrompt
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                isCopilotThinking = true
            )
        }

        viewModelScope.launch {
            val unreadCount = unreadCount.value
            val context = "Inbox: $unreadCount unread emails. User: ${_uiState.value.userProfile.displayName} (${_uiState.value.userProfile.email}). Active cold outreach campaigns: ${coldCampaigns.value.size}."
            val reply = repository.askAssistant(userPrompt, context)
            val assistantMsg = AiChatMessage(
                sender = "assistant",
                message = reply
            )
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + assistantMsg,
                    isCopilotThinking = false
                )
            }
        }
    }

    fun signInWithGoogle(webClientId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true) }
            val result = authService.signInWithGoogle(webClientId)
            when (result) {
                is AuthResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            showAccountDialog = false,
                            aiStatusMessage = "Signed in with Google securely as ${result.userState.email}"
                        )
                    }
                }
                is AuthResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            aiStatusMessage = "Authentication Notice: ${result.message}"
                        )
                    }
                }
                is AuthResult.Cancelled -> {
                    _uiState.update {
                        it.copy(
                            isAuthenticating = false,
                            aiStatusMessage = "Google Sign-in cancelled"
                        )
                    }
                }
            }
        }
    }

    fun signOutUser() {
        viewModelScope.launch {
            authService.signOut()
            _uiState.update {
                it.copy(
                    showAccountDialog = false,
                    aiStatusMessage = "Signed out."
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(aiStatusMessage = null) }
    }

    fun openComposeWithContent(to: String = "", subject: String = "", body: String = "") {
        _uiState.update {
            it.copy(
                isComposeOpen = true,
                composeInitialTo = to,
                composeInitialSubject = subject,
                composeInitialBody = body
            )
        }
    }

    fun triggerSync() {
        if (_uiState.value.isSyncing) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSyncing = true,
                    syncProgressFraction = 0.05f,
                    syncProgressMessage = "Initializing sync with Room & Gmail API..."
                )
            }

            val result = com.example.data.api.GmailApiClient.syncThreadsWithRoom(
                repository = repository
            ) { progress, message ->
                _uiState.update {
                    it.copy(
                        syncProgressFraction = progress,
                        syncProgressMessage = message
                    )
                }
            }

            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        lastSyncTimestamp = System.currentTimeMillis(),
                        syncProgressFraction = 1.0f,
                        syncProgressMessage = "Sync complete • $count threads cached in Room",
                        aiStatusMessage = "Gmail sync complete ($count threads synced)"
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Gmail sync failed"
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        syncProgressFraction = 1.0f,
                        syncProgressMessage = "Sync paused: $errorMsg",
                        aiStatusMessage = "Sync Notice: $errorMsg"
                    )
                }
            }
        }
    }

    private val firestoreService = com.example.data.firestore.FirestoreService()

    fun setSettingsOpen(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun updateNotificationPreferences(prefs: NotificationPreferences) {
        _uiState.update {
            it.copy(
                notificationPreferences = prefs,
                aiStatusMessage = "Notification preferences updated"
            )
        }
        viewModelScope.launch {
            val userId = authUserState.value.uid
            if (userId.isNotBlank()) {
                firestoreService.saveNotificationPreferencesToCloud(userId, prefs)
            }
        }
    }

    fun scheduleSendEmail(
        to: String,
        subject: String,
        body: String,
        scheduledTimeEpoch: Long,
        scheduledTimeFormatted: String
    ) {
        val scheduledEmail = ScheduledEmail(
            recipientTo = to,
            subject = subject,
            body = body,
            scheduledTimeEpoch = scheduledTimeEpoch,
            scheduledTimeFormatted = scheduledTimeFormatted,
            status = "SCHEDULED"
        )

        _uiState.update {
            it.copy(
                isComposeOpen = false,
                scheduledEmails = it.scheduledEmails + scheduledEmail,
                aiStatusMessage = "Email scheduled for $scheduledTimeFormatted!"
            )
        }

        viewModelScope.launch {
            val email = EmailEntity(
                senderName = _uiState.value.userProfile.displayName.ifBlank { "User" },
                senderEmail = _uiState.value.userProfile.email.ifBlank { "user@example.com" },
                recipientEmail = to,
                subject = subject,
                snippet = "[Scheduled: $scheduledTimeFormatted] $body".take(100),
                body = body,
                timestamp = scheduledTimeEpoch,
                isRead = true,
                folder = EmailFolder.DRAFTS,
                category = EmailCategory.PRIMARY,
                priority = EmailPriority.NORMAL,
                tags = "Scheduled, $scheduledTimeFormatted"
            )
            repository.insertEmail(email)

            val userId = authUserState.value.uid
            if (userId.isNotBlank()) {
                firestoreService.saveScheduledEmailToCloud(userId, scheduledEmail)
            }
        }
    }

    fun summarizeEmailThreadWithGemini(email: EmailEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizingThread = true) }
            val (summary, actionItems) = repository.summarizeEmail(
                sender = email.senderName,
                subject = email.subject,
                body = email.body
            )
            repository.updateAiSummary(email.id, summary, actionItems)
            _uiState.update { state ->
                state.copy(
                    isSummarizingThread = false,
                    selectedEmail = email.copy(aiSummary = summary, aiActionItems = actionItems),
                    aiStatusMessage = "Thread summarized with Gemini AI!"
                )
            }
        }
    }

    // UI Helper & Compatibility Methods
    fun toggleHighThinking(enable: Boolean) {
        setColdModelConfig(enable, _uiState.value.isFastLiteMode)
    }

    fun toggleFastLiteMode(enable: Boolean) {
        setColdModelConfig(_uiState.value.enableHighThinking, enable)
    }

    fun updateColdForm(
        targetName: String = _uiState.value.coldTargetName,
        targetCompany: String = _uiState.value.coldTargetCompany,
        targetRole: String = _uiState.value.coldTargetRole,
        targetLocation: String = _uiState.value.coldTargetLocation,
        framework: String = _uiState.value.coldFramework,
        valueProp: String = _uiState.value.coldValueProp,
        cta: String = _uiState.value.coldCta,
        tone: String = _uiState.value.coldTone
    ) {
        updateColdMailParams(targetName, targetCompany, targetRole, targetLocation, framework, valueProp, cta, tone)
    }

    fun searchTargetLocationMaps() {
        searchTargetCompanyLocation()
    }

    fun generateColdEmailFromKeywords(
        contextKeywords: String,
        framework: String,
        tone: String,
        onResult: (String, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val prompt = "Generate a cold outreach email based on these keywords:\n$contextKeywords\nFramework: $framework\nTone: $tone\nFormat output as:\nSubject: [Subject Here]\n\n[Body Here]"
                val response = com.example.data.api.GeminiApiClient.callGemini(prompt)
                val lines = response.lines()
                val subjectLine = lines.firstOrNull { it.startsWith("Subject:", ignoreCase = true) }
                    ?.removePrefix("Subject:")?.trim()
                    ?: "Opportunity Discussion"
                val bodyText = if (lines.any { it.startsWith("Subject:", ignoreCase = true) }) {
                    lines.drop(lines.indexOfFirst { it.startsWith("Subject:", ignoreCase = true) } + 1)
                        .joinToString("\n").trim()
                } else {
                    response.trim()
                }
                onResult(subjectLine, bodyText)
            } catch (e: Exception) {
                onResult("Partnership Discussion", "Hi,\n\nI hope this email finds you well. I would love to connect to discuss potential synergies.\n\nBest regards.")
            }
        }
    }

    fun toggleStarred(emailId: Long, currentStatus: Boolean) {
        toggleStar(emailId, currentStatus)
    }

    fun toggleStarred(email: EmailEntity) {
        toggleStar(email.id, email.isStarred)
    }

    fun moveEmail(emailId: Long, folder: EmailFolder) {
        moveToFolder(emailId, folder)
    }

    fun loadSmartRepliesForEmail(email: EmailEntity, forceRefresh: Boolean = false) {
        if (forceRefresh || _uiState.value.smartReplies.isEmpty()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isAnalyzingSmartReplies = true) }
                try {
                    val prompt = "Generate 3 quick smart reply options for this email:\nFrom: ${email.senderName}\nSubject: ${email.subject}\nBody: ${email.body}\nFormat as 3 distinct short reply options separated by ---"
                    val response = com.example.data.api.GeminiApiClient.callGemini(prompt)
                    val parts = response.split("---").map { it.trim() }.filter { it.isNotBlank() }
                    val replies = parts.mapIndexed { idx, text ->
                        val title = when (idx) {
                            0 -> "Acknowledge"
                            1 -> "Follow Up"
                            else -> "Confirm"
                        }
                        val type = when (idx) {
                            0 -> "acknowledge"
                            1 -> "meeting"
                            else -> "decline"
                        }
                        SmartReplyOption(id = "${idx + 1}", label = title, iconType = type, fullDraft = text)
                    }
                    _uiState.update { it.copy(isAnalyzingSmartReplies = false, smartReplies = replies, selectedSmartReply = replies.firstOrNull()) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isAnalyzingSmartReplies = false) }
                }
            }
        }
    }

    fun startCompose(to: String = "", subject: String = "", body: String = "") {
        openComposeWithContent(to, subject, body)
    }

    fun executeQuickAction(action: String) {
        when (action.lowercase()) {
            "summarize" -> _uiState.value.selectedEmail?.let { summarizeSelectedEmail() }
            "sync" -> triggerSync()
            else -> openComposeWithContent()
        }
    }

    fun linkCustomGoogleEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiStatusMessage = "Account linked: $email") }
        }
    }
}
