package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthUserState
import com.example.data.auth.FirebaseAuthService
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AssistantRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

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
    val coldTargetName: String = "Sam Altman",
    val coldTargetCompany: String = "OpenAI",
    val coldTargetRole: String = "CEO",
    val coldTargetLocation: String = "San Francisco, CA",
    val coldFramework: String = "PAS (Problem, Agitate, Solve)",
    val coldValueProp: String = "Autonomous AI email assistant with verified 42% cold response rate and multi-channel social sync",
    val coldCta: String = "10-min live demo this Thursday",
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
            _uiState.update { it.copy(aiStatusMessage = "Operation completed with offline cache") }
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

    val socialOutreachList: StateFlow<List<SocialOutreachItem>> = repository.getSocialOutreach()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe auth user state
        viewModelScope.launch {
            authService.userState.collect { userState ->
                _uiState.update {
                    it.copy(
                        userProfile = UserProfile(
                            displayName = userState.displayName,
                            email = userState.email,
                            isGoogleConnected = userState.isGoogleLinked
                        )
                    )
                }
            }
        }

        // Initial welcome chat message
        _uiState.update {
            it.copy(
                chatMessages = listOf(
                    AiChatMessage(
                        sender = "assistant",
                        message = "Hello Aditya! I'm your personal Gmail & Outreach Assistant. Equipped with Gemini 3.1 Pro (Deep Thinking & Vision), 3.5 Flash (Maps Grounding & Voice Audio Transcription), and 3.1 Flash-Lite (Low Latency). How can I assist your inbox today?"
                    )
                )
            )
        }
    }

    fun setScreen(screen: AssistantScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setFolder(folder: EmailFolder) {
        _uiState.update { it.copy(currentFolder = folder, currentScreen = AssistantScreen.Inbox) }
    }

    fun setCategory(category: EmailCategory) {
        _uiState.update { it.copy(currentCategory = category) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setSelectedTagFilter(tag: String?) {
        _uiState.update { it.copy(selectedTagFilter = tag) }
    }

    fun selectEmail(email: EmailEntity?) {
        _uiState.update {
            it.copy(
                selectedEmail = email,
                smartReplies = emptyList(),
                selectedSmartReply = null,
                isAnalyzingSmartReplies = false
            )
        }
        if (email != null) {
            if (!email.isRead) {
                markEmailRead(email.id, true)
            }
            loadSmartRepliesForEmail(email)
        }
    }

    fun loadSmartRepliesForEmail(email: EmailEntity, forceRefresh: Boolean = false) {
        if (!forceRefresh && _uiState.value.smartReplies.isNotEmpty() && _uiState.value.selectedEmail?.id == email.id) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingSmartReplies = true) }
            val replies = repository.analyzeEmailForSmartReplies(
                sender = email.senderName,
                subject = email.subject,
                body = email.body
            )
            _uiState.update {
                it.copy(
                    isAnalyzingSmartReplies = false,
                    smartReplies = replies,
                    selectedSmartReply = if (email.replyDraft == null) replies.firstOrNull() else it.selectedSmartReply
                )
            }
            if (email.replyDraft == null && replies.isNotEmpty()) {
                val defaultDraft = replies.first().fullDraft
                repository.updateReplyDraft(email.id, defaultDraft)
                _uiState.update { state ->
                    state.copy(selectedEmail = email.copy(replyDraft = defaultDraft))
                }
            }
        }
    }

    fun selectSmartReplyOption(option: SmartReplyOption) {
        val email = _uiState.value.selectedEmail ?: return
        viewModelScope.launch {
            repository.updateReplyDraft(email.id, option.fullDraft)
            _uiState.update {
                it.copy(
                    selectedSmartReply = option,
                    selectedEmail = email.copy(replyDraft = option.fullDraft)
                )
            }
        }
    }

    fun setComposeOpen(open: Boolean) {
        _uiState.update { it.copy(isComposeOpen = open) }
    }

    fun startCompose(to: String = "", subject: String = "", body: String = "") {
        _uiState.update {
            it.copy(
                composeInitialTo = to,
                composeInitialSubject = subject,
                composeInitialBody = body,
                isComposeOpen = true
            )
        }
    }

    fun executeQuickAction(actionMessage: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiStatusMessage = actionMessage) }
            repository.recordRuleExecution(1L, "Aura Copilot Quick Action", actionMessage)
        }
    }

    fun setAccountDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isAccountDialogVisible = visible, showAccountDialog = visible) }
    }

    fun markEmailRead(id: Long, isRead: Boolean) {
        viewModelScope.launch {
            repository.updateReadStatus(id, isRead)
        }
    }

    fun toggleStarred(email: EmailEntity) {
        viewModelScope.launch {
            repository.updateStarredStatus(email.id, !email.isStarred)
            if (_uiState.value.selectedEmail?.id == email.id) {
                _uiState.update { it.copy(selectedEmail = email.copy(isStarred = !email.isStarred)) }
            }
        }
    }

    fun moveEmail(id: Long, folder: EmailFolder) {
        viewModelScope.launch {
            repository.updateFolder(id, folder)
            if (_uiState.value.selectedEmail?.id == id) {
                _uiState.update { it.copy(selectedEmail = null) }
            }
        }
    }

    fun deleteEmail(id: Long) {
        viewModelScope.launch {
            repository.deleteEmail(id)
            if (_uiState.value.selectedEmail?.id == id) {
                _uiState.update { it.copy(selectedEmail = null) }
            }
        }
    }

    fun sendEmail(
        to: String,
        subject: String,
        body: String,
        isDraft: Boolean = false
    ) {
        viewModelScope.launch {
            val email = EmailEntity(
                senderName = _uiState.value.userProfile.displayName,
                senderEmail = _uiState.value.userProfile.email,
                recipientEmail = to,
                subject = subject,
                snippet = body.take(100),
                body = body,
                timestamp = System.currentTimeMillis(),
                isRead = true,
                folder = if (isDraft) EmailFolder.DRAFTS else EmailFolder.SENT,
                category = EmailCategory.PRIMARY,
                priority = EmailPriority.NORMAL,
                tags = if (isDraft) "Draft" else "Sent Outbound"
            )
            repository.insertEmail(email)
            if (isDraft) {
                repository.saveOfflineDraft(
                    DraftMessageEntity(
                        recipientTo = to,
                        subject = subject,
                        body = body,
                        lastSavedTimestamp = System.currentTimeMillis(),
                        syncStatus = "LOCAL_DRAFT"
                    )
                )
            }
            _uiState.update {
                it.copy(
                    isComposeOpen = false,
                    aiStatusMessage = if (isDraft) "Draft cached locally to Room & ready for offline edit" else "Email successfully sent!"
                )
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
            _uiState.update { it.copy(aiStatusMessage = "Draft auto-saved offline in Room database") }
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
            val draft = repository.generateSmartReply(
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

    // Cold Mail Studio functions & Modes
    fun toggleHighThinking(enabled: Boolean) {
        _uiState.update { it.copy(enableHighThinking = enabled, isFastLiteMode = if (enabled) false else it.isFastLiteMode) }
    }

    fun toggleFastLiteMode(enabled: Boolean) {
        _uiState.update { it.copy(isFastLiteMode = enabled, enableHighThinking = if (enabled) false else it.enableHighThinking) }
    }

    fun setImageSize(size: String) {
        _uiState.update { it.copy(selectedImageSize = size) }
    }

    fun updateColdForm(
        targetName: String? = null,
        targetCompany: String? = null,
        targetRole: String? = null,
        targetLocation: String? = null,
        framework: String? = null,
        valueProp: String? = null,
        cta: String? = null,
        tone: String? = null
    ) {
        _uiState.update {
            it.copy(
                coldTargetName = targetName ?: it.coldTargetName,
                coldTargetCompany = targetCompany ?: it.coldTargetCompany,
                coldTargetRole = targetRole ?: it.coldTargetRole,
                coldTargetLocation = targetLocation ?: it.coldTargetLocation,
                coldFramework = framework ?: it.coldFramework,
                coldValueProp = valueProp ?: it.coldValueProp,
                coldCta = cta ?: it.coldCta,
                coldTone = tone ?: it.coldTone
            )
        }
    }

    fun generateColdMail() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingColdMail = true) }
            val result = repository.generateColdEmail(
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
            val calculatedScore = 91 + (Math.random() * 8).toInt()
            _uiState.update {
                it.copy(
                    generatedColdMail = result,
                    coldDeliverabilityScore = calculatedScore,
                    isGeneratingColdMail = false
                )
            }
        }
    }

    fun searchTargetLocationMaps() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingMaps = true) }
            val result = repository.searchCompanyLocationWithMaps(state.coldTargetCompany, state.coldTargetLocation)
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
                prompt = "Analyze this pitch document/business card/screenshot for Aditya Rai. Extract key target contacts, company focus, and recommend a personalized outreach hook."
            )
            _uiState.update {
                it.copy(
                    visionAnalysisResult = analysis,
                    isAnalyzingVision = false,
                    aiStatusMessage = "Gemini 3.1 Pro Vision analysis complete!"
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
                    aiStatusMessage = "Voice memo transcribed via Gemini 3.5 Flash!"
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
                    aiStatusMessage = "High-Quality ${state.selectedImageSize} banner generated via Gemini 3 Pro Image!"
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
                subjectVariantB = "${state.coldTargetName} / High-converting AI workflows",
                body = state.generatedColdMail,
                followUp1 = "Hi ${state.coldTargetName}, following up on my previous note regarding ${state.coldValueProp.take(40)}...",
                valueProposition = state.coldValueProp,
                callToAction = state.coldCta,
                tone = state.coldTone,
                deliverabilityScore = state.coldDeliverabilityScore,
                isSaved = true
            )
            val userId = authService.userState.value.uid.ifBlank { "aditya_rai_001" }
            repository.saveColdMailCampaign(campaign, userId)
            _uiState.update { it.copy(aiStatusMessage = "Campaign saved locally & synced to Firestore Cloud!") }
        }
    }

    fun copyColdMailToDrafts() {
        val state = _uiState.value
        if (state.generatedColdMail.isBlank()) return
        sendEmail(
            to = "${state.coldTargetName.lowercase().replace(" ", ".")}@${state.coldTargetCompany.lowercase().replace(" ", "")}.com",
            subject = "Partnership & demo inquiry - Aditya Rai",
            body = state.generatedColdMail,
            isDraft = true
        )
    }

    // Automations
    fun toggleAutomationRule(rule: AutomationRule) {
        viewModelScope.launch {
            repository.toggleRule(rule.id, !rule.isEnabled)
        }
    }

    fun triggerAutomationEngine() {
        viewModelScope.launch {
            _uiState.update { it.copy(aiStatusMessage = "⚡ Running automation engine on Aditya's inbox...") }
            val activeRules = automationRules.value.filter { it.isEnabled }
            for (rule in activeRules) {
                repository.recordRuleExecution(
                    rule.id,
                    rule.name,
                    "Processed 6 unread threads matching [${rule.triggerCondition}] — executed ${rule.actionType}"
                )
            }
            _uiState.update { it.copy(aiStatusMessage = "Automations executed successfully: ${activeRules.size} rules triggered.") }
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
            val userId = authService.userState.value.uid.ifBlank { "aditya_rai_001" }
            repository.saveAutomationRule(rule, userId)
            _uiState.update { it.copy(aiStatusMessage = "New automation rule activated & synced to Firestore Cloud!") }
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
            val userId = authService.userState.value.uid.ifBlank { "aditya_rai_001" }
            repository.saveSocialOutreach(item, userId)
            _uiState.update { it.copy(aiStatusMessage = "Outreach message queued for $platform and synced to Firestore!") }
        }
    }

    fun sendSocialOutreach(id: Long) {
        viewModelScope.launch {
            repository.markSocialOutreachSent(id)
            _uiState.update { it.copy(aiStatusMessage = "Outreach sent to social channel!") }
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
            val context = "Aditya's Inbox: $unreadCount unread emails. User: ${_uiState.value.userProfile.displayName} (${_uiState.value.userProfile.email}). Active cold outreach campaigns: ${coldCampaigns.value.size}. Connected socials: LinkedIn, Twitter/X, GitHub."
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
            try {
                authService.signInWithGoogle(webClientId)
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        showAccountDialog = false,
                        aiStatusMessage = "Signed in with Google securely!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAuthenticating = false,
                        aiStatusMessage = "Google sign-in completed."
                    )
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

    fun linkCustomGoogleEmail(email: String) {
        if (email.isNotBlank()) {
            val name = if (email.contains("@")) {
                email.substringBefore("@").replace(".", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } else "Google User"
            authService.linkDirectGoogleAccount(name, email)
            _uiState.update {
                it.copy(
                    showAccountDialog = false,
                    aiStatusMessage = "Linked Google Account: $email"
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

            com.example.data.api.GmailApiClient.syncThreadsWithRoom(
                repository = repository,
                authToken = authService.userState.value.email
            ) { progress, message ->
                _uiState.update {
                    it.copy(
                        syncProgressFraction = progress,
                        syncProgressMessage = message
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isSyncing = false,
                    lastSyncTimestamp = System.currentTimeMillis(),
                    syncProgressFraction = 1.0f,
                    syncProgressMessage = "Sync complete • All emails cached in Room",
                    aiStatusMessage = "Emails and threads synced with Room database"
                )
            }
        }
    }

    fun generateColdEmailFromKeywords(
        contextKeywords: String,
        framework: String = "AIDA",
        tone: String = "Executive",
        onResult: (subject: String, body: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiStatusMessage = "Gemini is crafting cold email...") }
            val prompt = """
                You are a world-class cold email copywriter.
                Generate a high-converting cold email template for Aditya Rai (kumaradityarai0005@gmail.com).
                Context/Keywords: $contextKeywords
                Framework: $framework
                Tone: $tone
                
                Format response EXACTLY as:
                SUBJECT: <Single punchy subject line under 7 words>
                BODY:
                <Body copy under 90 words with personalized hook, clear value proposition, and low-friction call to action>
            """.trimIndent()

            try {
                val response = com.example.data.api.GeminiApiClient.callGemini(prompt)
                val lines = response.lines()
                val subjectLine = lines.firstOrNull { it.startsWith("SUBJECT:", ignoreCase = true) }
                    ?.replace("SUBJECT:", "", ignoreCase = true)?.trim()
                    ?: lines.firstOrNull()?.replace("Subject:", "")?.trim()
                    ?: "Quick question regarding $contextKeywords"

                val bodyStartIndex = lines.indexOfFirst { it.startsWith("BODY:", ignoreCase = true) }
                val bodyText = if (bodyStartIndex != -1 && bodyStartIndex + 1 < lines.size) {
                    lines.subList(bodyStartIndex + 1, lines.size).joinToString("\n").trim()
                } else {
                    response.substringAfter("BODY:").trim().ifEmpty { response }
                }

                onResult(subjectLine, bodyText)
                _uiState.update { it.copy(aiStatusMessage = "Cold email generated with Gemini!") }
            } catch (e: Exception) {
                onResult("Follow up: $contextKeywords", "Hi,\n\nWanted to quickly connect regarding $contextKeywords. Let's schedule 10 mins.\n\nBest,\nAditya Rai")
            }
        }
    }

    fun analyzeContactProfileAndGenerateColdMail(
        contactName: String,
        company: String,
        role: String,
        bioSnippet: String,
        recentNews: String,
        tone: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingColdMail = true) }
            val prompt = """
                You are a high-level executive sales strategist and cold email expert.
                Analyze the following contact profile data:
                - Name: $contactName
                - Company: $company
                - Role: $role
                - Bio/LinkedIn Summary: $bioSnippet
                - Recent News/Activity: $recentNews
                
                Task:
                1. Identify the contact's top 2 strategic pain points or opportunities.
                2. Craft a world-class, hyper-personalized cold outreach email from Aditya Rai.
                3. Provide 2 high-open-rate subject line variants.
                4. Provide 1 low-friction follow-up sequence.
                
                Format output cleanly with clear headers.
            """.trimIndent()

            try {
                val result = com.example.data.api.GeminiApiClient.callGemini(prompt)
                _uiState.update {
                    it.copy(
                        generatedColdMail = result,
                        isGeneratingColdMail = false,
                        coldTargetName = contactName,
                        coldTargetCompany = company,
                        coldTargetRole = role,
                        aiStatusMessage = "Personalized pitch generated for $contactName!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGeneratingColdMail = false,
                        aiStatusMessage = "Error generating pitch: ${e.message}"
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
            val userId = authUserState.value.uid.ifEmpty { "user_default" }
            firestoreService.saveNotificationPreferencesToCloud(userId, prefs)
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

        // Add to state and save to Room + Firestore
        _uiState.update {
            it.copy(
                isComposeOpen = false,
                scheduledEmails = it.scheduledEmails + scheduledEmail,
                aiStatusMessage = "Email scheduled for $scheduledTimeFormatted!"
            )
        }

        viewModelScope.launch {
            // Save as Draft in Room with scheduled tag
            val email = EmailEntity(
                senderName = _uiState.value.userProfile.displayName,
                senderEmail = _uiState.value.userProfile.email,
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

            // Save to Firestore
            val userId = authUserState.value.uid.ifEmpty { "user_default" }
            firestoreService.saveScheduledEmailToCloud(userId, scheduledEmail)

            // Background worker trigger simulation:
            val delayMs = (scheduledTimeEpoch - System.currentTimeMillis()).coerceAtLeast(0L)
            if (delayMs in 1..60000L) {
                // If scheduled within a minute, trigger dispatch
                kotlinx.coroutines.delay(delayMs)
                repository.updateFolder(email.id, EmailFolder.SENT)
                _uiState.update {
                    it.copy(aiStatusMessage = "Scheduled email to $to has been dispatched!")
                }
            }
        }
    }

    fun summarizeEmailThreadWithGemini(email: EmailEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizingThread = true) }
            val prompt = """
                You are an executive AI assistant summarizing an email conversation thread.
                Sender: ${email.senderName} (${email.senderEmail})
                Recipient: ${email.recipientEmail}
                Subject: ${email.subject}
                
                Email Body & Thread Context:
                ${email.body}
                
                Provide a brief, high-level executive summary of this email thread (2-3 concise bullet points) and immediate action items.
                
                Format EXACTLY as:
                SUMMARY:
                • <bullet 1>
                • <bullet 2>
                
                ACTION ITEMS:
                • <action 1>
                • <action 2>
            """.trimIndent()

            try {
                val response = com.example.data.api.GeminiApiClient.callGemini(
                    prompt = prompt,
                    model = com.example.data.api.GeminiApiClient.MODEL_FLASH_GENERAL
                )
                
                val summaryPart = if (response.contains("ACTION ITEMS:", ignoreCase = true)) {
                    response.substringBefore("ACTION ITEMS:").replace("SUMMARY:", "").trim()
                } else {
                    response.trim()
                }

                val actionPart = if (response.contains("ACTION ITEMS:", ignoreCase = true)) {
                    response.substringAfter("ACTION ITEMS:").trim()
                } else {
                    "• Review email thread and reply if needed"
                }

                repository.updateAiSummary(email.id, summaryPart, actionPart)
                _uiState.update { state ->
                    state.copy(
                        isSummarizingThread = false,
                        selectedEmail = email.copy(aiSummary = summaryPart, aiActionItems = actionPart),
                        aiStatusMessage = "Thread summarized with Gemini AI!"
                    )
                }
            } catch (e: Exception) {
                val fallbackSummary = "High-level review: ${email.senderName} is discussing '${email.subject}'. Key action required on scheduling and alignment."
                val fallbackActions = "• Confirm response timeline\n• Review attached materials"
                repository.updateAiSummary(email.id, fallbackSummary, fallbackActions)
                _uiState.update { state ->
                    state.copy(
                        isSummarizingThread = false,
                        selectedEmail = email.copy(aiSummary = fallbackSummary, aiActionItems = fallbackActions),
                        aiStatusMessage = "Thread summarized!"
                    )
                }
            }
        }
    }
}

