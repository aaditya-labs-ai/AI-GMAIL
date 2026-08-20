package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromFolder(value: EmailFolder): String = value.name

    @TypeConverter
    fun toFolder(value: String): EmailFolder = runCatching { EmailFolder.valueOf(value) }.getOrDefault(EmailFolder.INBOX)

    @TypeConverter
    fun fromCategory(value: EmailCategory): String = value.name

    @TypeConverter
    fun toCategory(value: String): EmailCategory = runCatching { EmailCategory.valueOf(value) }.getOrDefault(EmailCategory.PRIMARY)

    @TypeConverter
    fun fromPriority(value: EmailPriority): String = value.name

    @TypeConverter
    fun toPriority(value: String): EmailPriority = runCatching { EmailPriority.valueOf(value) }.getOrDefault(EmailPriority.NORMAL)

    @TypeConverter
    fun fromPlatform(value: SocialPlatform): String = value.name

    @TypeConverter
    fun toPlatform(value: String): SocialPlatform = runCatching { SocialPlatform.valueOf(value) }.getOrDefault(SocialPlatform.LINKEDIN)

    @TypeConverter
    fun fromDeliveryStatus(value: MessageDeliveryStatus?): String? = value?.name

    @TypeConverter
    fun toDeliveryStatus(value: String?): MessageDeliveryStatus? = value?.let { runCatching { MessageDeliveryStatus.valueOf(it) }.getOrNull() }
}

@Database(
    entities = [
        EmailEntity::class,
        GmailThreadEntity::class,
        GmailMessageEntity::class,
        DraftMessageEntity::class,
        ColdMailCampaign::class,
        AutomationRule::class,
        AutomationLog::class,
        SocialAccount::class,
        SocialOutreachItem::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun emailDao(): EmailDao
    abstract fun gmailThreadDao(): GmailThreadDao
    abstract fun gmailMessageDao(): GmailMessageDao
    abstract fun draftMessageDao(): DraftMessageDao
    abstract fun coldMailDao(): ColdMailDao
    abstract fun automationDao(): AutomationDao
    abstract fun socialHubDao(): SocialHubDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gmail_assistant_db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(db: AppDatabase) {
    val emailDao = db.emailDao()
    val automationDao = db.automationDao()
    val socialDao = db.socialHubDao()
    val coldMailDao = db.coldMailDao()

    val now = System.currentTimeMillis()

    // 1. Initial Emails
    val sampleEmails = listOf(
        EmailEntity(
            senderName = "Marc Andreessen",
            senderEmail = "marc@a16z.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "Re: AI Developer Platform & Automation Agent Architecture",
            snippet = "Loved the breakdown you sent over on autonomous email pipelines. Let's schedule 20 mins this Thursday.",
            body = "Hi Aditya,\n\nI went through your deck and the live prototype demo you shared regarding the personal agentic orchestration layer.\n\nThe response latency and intelligent context management look substantially ahead of conventional wrappers. Our enterprise partners have been asking for this exact workflow.\n\nAre you free for a quick 20-minute Zoom call this Thursday at 2:00 PM PST? Let me know or send a calendar invite directly.\n\nBest,\nMarc\nGeneral Partner, a16z",
            timestamp = now - 1000 * 60 * 35, // 35 mins ago
            isRead = false,
            isStarred = true,
            folder = EmailFolder.INBOX,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.HIGH,
            tags = "Work, Urgent, Investor",
            aiSummary = "Marc (a16z) reviewed your prototype demo deck, praised the AI architecture, and invited you to a 20-min Zoom call on Thursday at 2:00 PM PST.",
            aiActionItems = "• Reply with calendar invite or confirm Thursday 2:00 PM PST\n• Attach updated metrics/deck\n• Prepare 3-slide product architecture overview",
            replyDraft = "Hi Marc,\n\nThank you for the note. I would love to connect this Thursday at 2:00 PM PST. I've sent a calendar invite to your email with a Google Meet link.\n\nLooking forward to speaking.\n\nBest regards,\nAditya Rai",
            deliveryStatus = null,
            hasAttachments = true,
            attachmentNames = "a16z_Seed_Evaluation_Rubric.pdf, Pitch_Deck_Annotated.pdf"
        ),
        EmailEntity(
            senderName = "Security Operations",
            senderEmail = "security@cloud.google.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "URGENT: Production SSL Certificate Expiration in 24 Hours",
            snippet = "Action required: Your production API service certificate is set to expire tomorrow. Immediate rotation needed.",
            body = "Dear Developer,\n\nThis is an automated high-priority alert. The SSL/TLS certificate for your production API gateway endpoint is scheduled to expire in less than 24 hours.\n\nPlease verify DNS records and confirm auto-renewal via the Cloud Console immediately to avoid connection interruptions.\n\nGoogle Cloud Security Team",
            timestamp = now - 1000 * 60 * 75, // 75 mins ago
            isRead = false,
            isStarred = true,
            folder = EmailFolder.INBOX,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.HIGH,
            tags = "Urgent, Work, Security",
            aiSummary = "Automated high-priority alert regarding SSL certificate renewal required within 24 hours to prevent API downtime.",
            aiActionItems = "• Open Cloud Console\n• Trigger DNS validation and certificate rotation\n• Test gateway endpoints",
            replyDraft = null,
            deliveryStatus = null,
            hasAttachments = true,
            attachmentNames = "SSL_Renewal_Certificate_Guide.pdf"
        ),
        EmailEntity(
            senderName = "Satya Nadella",
            senderEmail = "satya@microsoft.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "Partnership opportunity with Azure AI Ecosystem",
            snippet = "Our Developer Relations team flagged your cold outreach suite and multi-agent hub. We'd like to feature it.",
            body = "Hello Aditya,\n\nOur Copilot Ecosystem team brought your multi-channel cold mail & workflow assistant to my attention. We are actively expanding developer tools built with native agentic loops.\n\nWe would love to discuss a developer grant and feature spot at the upcoming AI Innovators Keynote.\n\nMy chief of staff is copied to coordinate the briefing.\n\nWarm regards,\nSatya Nadella\nMicrosoft",
            timestamp = now - 1000 * 60 * 180, // 3 hrs ago
            isRead = false,
            isStarred = true,
            folder = EmailFolder.INBOX,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.HIGH,
            tags = "Work, Urgent, Partnership",
            aiSummary = "Microsoft Developer Relations flagged your app for an Azure AI Ecosystem developer grant and potential keynote showcase spot.",
            aiActionItems = "• Coordinate with Chief of Staff\n• Prepare technical executive brief\n• Confirm demo availability",
            replyDraft = "Hi Satya,\n\nHonored by the outreach. We would be thrilled to explore this partnership and participate in the AI Innovators Keynote.\n\nI will coordinate timing directly with your team.\n\nBest regards,\nAditya Rai",
            deliveryStatus = null,
            hasAttachments = true,
            attachmentNames = "Azure_AI_Foundry_Keynote_Brief.docx, Partnership_Terms.pdf"
        ),
        EmailEntity(
            senderName = "Alex Chen",
            senderEmail = "alex.chen@gmail.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "Weekend hiking trail & dinner catch-up in Tahoe",
            snippet = "Hey Aditya! Are we still good for the Emerald Bay trail run this Saturday morning? Let me know if 9 AM works.",
            body = "Hey Aditya,\n\nHope your launch sprint is going smoothly! Are we still on for the trail run around Emerald Bay in Tahoe this Saturday morning?\n\nThinking we can start around 9:00 AM, grab lunch by the lake, and do dinner with the crew.\n\nLet me know if you want me to drive!\n\nCheers,\nAlex",
            timestamp = now - 1000 * 60 * 300, // 5 hrs ago
            isRead = true,
            isStarred = false,
            folder = EmailFolder.INBOX,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.NORMAL,
            tags = "Personal, Friends, Weekend",
            aiSummary = "Alex asked to confirm Saturday 9 AM Emerald Bay trail run and lunch in Tahoe.",
            aiActionItems = "• Confirm 9 AM time\n• Decide who is driving",
            replyDraft = "Hey Alex! Absolutely, 9 AM works great for me. I'd love to drive this time. See you Saturday!\n\nBest,\nAditya",
            deliveryStatus = null,
            hasAttachments = true,
            attachmentNames = "Tahoe_Trail_Map_GPS.png"
        ),
        EmailEntity(
            senderName = "TechCrunch Editorial",
            senderEmail = "news@techcrunch.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "Exclusive coverage pitch: AI Personal Gmail Assistant launch",
            snippet = "We would like to feature your personal AI cold outreach assistant in tomorrow's startup spotlight.",
            body = "Hi Aditya,\n\nWe saw the early benchmark numbers you published on cold email response conversion rates using your custom AIDA & PAS model fine-tunes.\n\nWould you be open to an embargoed Q&A interview before the public launch?\n\nPlease share high-res product screenshots and founder bio by 5 PM today.\n\nThanks,\nSarah Perez\nTechCrunch",
            timestamp = now - 1000 * 60 * 60 * 8, // 8 hrs ago
            isRead = true,
            isStarred = false,
            folder = EmailFolder.INBOX,
            category = EmailCategory.UPDATES,
            priority = EmailPriority.NORMAL,
            tags = "Work, Press, Media",
            aiSummary = "TechCrunch requested an embargoed Q&A interview for tomorrow's startup spotlight, requesting screenshots and founder bio by 5 PM.",
            aiActionItems = "• Send product screenshots\n• Attach founder bio\n• Review interview questions",
            replyDraft = null,
            deliveryStatus = null,
            hasAttachments = false,
            attachmentNames = ""
        ),
        EmailEntity(
            senderName = "GitHub Notifications",
            senderEmail = "notifications@github.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "[kumaradityarai/gmail-ai-assistant] 12 new stars & PR #4 merged",
            snippet = "Your repository has surpassed 2,400 stars on GitHub! 3 pull requests merged automatically.",
            body = "Hey kumaradityarai,\n\nGreat news! Your repo 'gmail-ai-assistant' is trending in the Kotlin / Jetpack Compose category today.\n\n• New stars: +12\n• Active forks: 84\n• Open issues: 2 (Automations scheduled)\n\nKeep up the great work!\nGitHub Team",
            timestamp = now - 1000 * 60 * 60 * 22,
            isRead = true,
            isStarred = false,
            folder = EmailFolder.INBOX,
            category = EmailCategory.UPDATES,
            priority = EmailPriority.LOW,
            tags = "Work, Dev, Trending",
            deliveryStatus = null,
            hasAttachments = false,
            attachmentNames = ""
        ),
        EmailEntity(
            senderName = "Stripe Billing",
            senderEmail = "receipts@stripe.com",
            recipientEmail = "kumaradityarai0005@gmail.com",
            subject = "Invoice #INV-2026-894 Paid - $149.00",
            snippet = "Payment of $149.00 for Gemini API Infrastructure Tier was processed successfully.",
            body = "Your invoice #INV-2026-894 has been successfully paid via card ending in •••• 4242.\n\nAmount: $149.00 USD\nDescription: High-Throughput Token Allocation\nDate: August 2026\n\nThank you for using Stripe.",
            timestamp = now - 1000 * 60 * 60 * 48,
            isRead = true,
            isStarred = false,
            folder = EmailFolder.INBOX,
            category = EmailCategory.PROMOTIONS,
            priority = EmailPriority.LOW,
            tags = "Personal, Finance, Receipt",
            deliveryStatus = null,
            hasAttachments = true,
            attachmentNames = "Invoice_INV_2026_894.pdf, Stripe_Tax_Summary.xlsx"
        ),
        EmailEntity(
            senderName = "Aditya Rai",
            senderEmail = "kumaradityarai0005@gmail.com",
            recipientEmail = "marc@a16z.com",
            subject = "Re: AI Developer Platform & Automation Agent Architecture",
            snippet = "Thanks Marc, calendar invite sent for Thursday 2:00 PM PST with our product deck.",
            body = "Hi Marc,\n\nThank you for the note. I would love to connect this Thursday at 2:00 PM PST. I've sent a calendar invite to your email with a Google Meet link.\n\nAttached is our updated 3-slide product architecture overview and live metrics summary.\n\nLooking forward to speaking.\n\nBest regards,\nAditya Rai",
            timestamp = now - 1000 * 60 * 15, // 15 mins ago
            isRead = true,
            isStarred = true,
            folder = EmailFolder.SENT,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.HIGH,
            tags = "Work, Urgent, Investor",
            aiSummary = "Confirmation sent to Marc Andreessen with calendar invite and product architecture overview for Thursday 2:00 PM PST.",
            aiActionItems = null,
            replyDraft = null,
            deliveryStatus = MessageDeliveryStatus.OPENED, // Double blue checkmark (Opened 10m ago)
            hasAttachments = true,
            attachmentNames = "AI_Engine_Architecture_v2.pdf"
        ),
        EmailEntity(
            senderName = "Aditya Rai",
            senderEmail = "kumaradityarai0005@gmail.com",
            recipientEmail = "partnerships@anthropic.com",
            subject = "Claude API Model Integration Benchmark Results",
            snippet = "Delivered: Benchmarks for the autonomous multi-agent routing layer.",
            body = "Hi Partnerships Team,\n\nSharing the latest latency and reasoning benchmark results on our hybrid Gemini & Claude orchestration framework.\n\nLet us know if you would like to explore enterprise sandbox access.\n\nBest regards,\nAditya Rai",
            timestamp = now - 1000 * 60 * 120, // 2 hrs ago
            isRead = true,
            isStarred = false,
            folder = EmailFolder.SENT,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.NORMAL,
            tags = "Work, Partnership",
            aiSummary = "Sent technical benchmark results to Anthropic Partnerships team.",
            aiActionItems = null,
            replyDraft = null,
            deliveryStatus = MessageDeliveryStatus.DELIVERED, // Double gray checkmark (Delivered)
            hasAttachments = true,
            attachmentNames = "Model_Benchmark_Results.xlsx"
        ),
        EmailEntity(
            senderName = "Aditya Rai (Draft)",
            senderEmail = "kumaradityarai0005@gmail.com",
            recipientEmail = "sam@openai.com",
            subject = "Cold Pitch: Enterprise Autonomous Mail Pipeline Integration",
            snippet = "Sam, quick question on how your team is tackling multi-turn outbound personalization...",
            body = "Hi Sam,\n\nLoved your recent remarks on compound AI systems.\n\nWe built an autonomous personal assistant engine that crafts context-rich cold emails and automates outbound loops with a 41% verified reply rate.\n\nWould you be open to checking out a 90-second loom or testing the APK directly?\n\nBest,\nAditya Rai",
            timestamp = now - 1000 * 60 * 60 * 12,
            isRead = true,
            isStarred = false,
            folder = EmailFolder.DRAFTS,
            category = EmailCategory.PRIMARY,
            priority = EmailPriority.NORMAL,
            tags = "Work, Draft, Cold Mail",
            deliveryStatus = null,
            hasAttachments = false,
            attachmentNames = ""
        )
    )
    emailDao.insertEmails(sampleEmails)

    // Populate Initial Gmail Threads & Constituent Messages for offline cache
    val threadDao = db.gmailThreadDao()
    val messageDao = db.gmailMessageDao()

    val thread1 = GmailThreadEntity(
        threadId = "18f1a23b4c5d6e01",
        historyId = "1098472",
        snippet = "Loved the breakdown you sent over on autonomous email pipelines. Let's schedule 20 mins this Thursday.",
        subject = "Re: AI Developer Platform & Automation Agent Architecture",
        senderSummary = "Marc Andreessen",
        lastSenderEmail = "marc@a16z.com",
        lastMessageTimestamp = now - 1000 * 60 * 35,
        messageCount = 2,
        isUnread = true,
        isStarred = true,
        folder = EmailFolder.INBOX,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        labels = "INBOX, IMPORTANT, UNREAD, a16z",
        aiSummary = "Marc (a16z) praised the AI architecture and invited you to a 20-min Zoom call on Thursday at 2:00 PM PST.",
        aiActionItems = "• Reply confirming Thursday 2:00 PM PST\n• Attach updated architecture overview deck",
        isSyncedOffline = true
    )

    val message1A = GmailMessageEntity(
        messageId = "msg_001_initial_pitch",
        threadId = "18f1a23b4c5d6e01",
        senderName = "Aditya Rai",
        senderEmail = "kumaradityarai0005@gmail.com",
        recipientEmails = "marc@a16z.com",
        subject = "AI Developer Platform & Automation Agent Architecture",
        snippet = "Hi Marc, wanted to share our latest architecture on autonomous email triage and cold pipeline automation...",
        bodyText = "Hi Marc,\n\nWanted to share our latest architecture on autonomous email triage and cold pipeline automation. We achieved sub-200ms processing latency on local embeddings and built direct multi-channel social sync.\n\nBest,\nAditya Rai",
        internalDate = now - 1000 * 60 * 60 * 24,
        isRead = true,
        isStarred = false,
        isSent = true,
        folder = EmailFolder.SENT,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        labelIds = "SENT, IMPORTANT"
    )

    val message1B = GmailMessageEntity(
        messageId = "msg_002_marc_reply",
        threadId = "18f1a23b4c5d6e01",
        senderName = "Marc Andreessen",
        senderEmail = "marc@a16z.com",
        recipientEmails = "kumaradityarai0005@gmail.com",
        subject = "Re: AI Developer Platform & Automation Agent Architecture",
        snippet = "Loved the breakdown you sent over on autonomous email pipelines. Let's schedule 20 mins this Thursday.",
        bodyText = "Hi Aditya,\n\nI went through your deck and the live prototype demo you shared regarding the personal agentic orchestration layer.\n\nThe response latency and intelligent context management look substantially ahead of conventional wrappers. Our enterprise partners have been asking for this exact workflow.\n\nAre you free for a quick 20-minute Zoom call this Thursday at 2:00 PM PST? Let me know or send a calendar invite directly.\n\nBest,\nMarc\nGeneral Partner, a16z",
        internalDate = now - 1000 * 60 * 35,
        isRead = false,
        isStarred = true,
        folder = EmailFolder.INBOX,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        labelIds = "INBOX, IMPORTANT, UNREAD",
        aiSummary = "Marc (a16z) requested a 20-min Zoom call on Thursday at 2:00 PM PST.",
        aiActionItems = "• Reply confirming Thursday 2:00 PM PST\n• Attach updated deck",
        replyDraft = "Hi Marc,\n\nThank you for the note. I would love to connect this Thursday at 2:00 PM PST. I've sent a calendar invite with a Google Meet link.\n\nBest regards,\nAditya Rai"
    )

    val thread2 = GmailThreadEntity(
        threadId = "18f1a23b4c5d6e02",
        historyId = "1098480",
        snippet = "Our Developer Relations team flagged your cold outreach suite and multi-agent hub. We'd like to feature it.",
        subject = "Partnership opportunity with Azure AI Ecosystem",
        senderSummary = "Satya Nadella",
        lastSenderEmail = "satya@microsoft.com",
        lastMessageTimestamp = now - 1000 * 60 * 180,
        messageCount = 1,
        isUnread = true,
        isStarred = true,
        folder = EmailFolder.INBOX,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        labels = "INBOX, IMPORTANT, UNREAD, Microsoft",
        aiSummary = "Satya proposed an Azure AI Ecosystem developer grant and keynote feature spot.",
        aiActionItems = "• Coordinate with Chief of Staff\n• Prepare keynote demo materials"
    )

    val message2A = GmailMessageEntity(
        messageId = "msg_003_satya_invite",
        threadId = "18f1a23b4c5d6e02",
        senderName = "Satya Nadella",
        senderEmail = "satya@microsoft.com",
        recipientEmails = "kumaradityarai0005@gmail.com",
        subject = "Partnership opportunity with Azure AI Ecosystem",
        snippet = "Our Developer Relations team flagged your cold outreach suite and multi-agent hub. We'd like to feature it.",
        bodyText = "Hello Aditya,\n\nOur Copilot Ecosystem team brought your multi-channel cold mail & workflow assistant to my attention. We are actively expanding developer tools built with native agentic loops.\n\nWe would love to discuss a developer grant and feature spot at the upcoming AI Innovators Keynote.\n\nMy chief of staff is copied to coordinate the briefing.\n\nWarm regards,\nSatya Nadella\nMicrosoft",
        internalDate = now - 1000 * 60 * 180,
        isRead = false,
        isStarred = true,
        folder = EmailFolder.INBOX,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        labelIds = "INBOX, IMPORTANT, UNREAD",
        aiSummary = "Microsoft offered an Azure AI Ecosystem developer grant and keynote showcase.",
        replyDraft = "Hi Satya,\n\nHonored by the outreach. We would be thrilled to explore this partnership and participate in the AI Innovators Keynote.\n\nBest regards,\nAditya Rai"
    )

    threadDao.insertThreads(listOf(thread1, thread2))
    messageDao.insertMessages(listOf(message1A, message1B, message2A))

    val draftDao = db.draftMessageDao()
    val sampleDraft = DraftMessageEntity(
        threadId = "18f1a23b4c5d6e01",
        inReplyToMessageId = "msg_002_marc_reply",
        recipientTo = "marc@a16z.com",
        recipientCc = "",
        recipientBcc = "",
        subject = "Re: AI Developer Platform & Automation Agent Architecture",
        body = "Hi Marc,\n\nThank you for the note. I would love to connect this Thursday at 2:00 PM PST. I've sent a calendar invite with a Google Meet link.\n\nLooking forward to speaking.\n\nBest regards,\nAditya Rai",
        isAiGenerated = true,
        aiPromptUsed = "Auto-drafted calendar acceptance response for Marc Andreessen",
        lastSavedTimestamp = now - 1000 * 60 * 10,
        syncStatus = "LOCAL_DRAFT"
    )
    draftDao.insertOrUpdateDraft(sampleDraft)

    // 2. Automation Rules
    val sampleRules = listOf(
        AutomationRule(
            name = "Auto-Flag High Priority Investors & Execs",
            description = "Flags emails from tier-1 domains (a16z, sequoia, yc, microsoft, google) with High Priority tag and notifies AI Copilot.",
            triggerType = "DOMAIN",
            triggerCondition = "a16z.com,sequoia.com,ycombinator.com,microsoft.com,google.com",
            actionType = "AUTO_LABEL",
            actionParameter = "Investor/Exec Priority",
            isEnabled = true,
            executionCount = 14,
            lastRunTimestamp = now - 1000 * 60 * 35
        ),
        AutomationRule(
            name = "Smart Auto-Draft for Meeting Inquiries",
            description = "When someone asks for a Zoom/Google Meet or call, automatically prepare an executive AI reply draft with Aditya's calendar link.",
            triggerType = "KEYWORD",
            triggerCondition = "zoom,call,schedule,calendar,20 mins,meet",
            actionType = "AUTO_DRAFT",
            actionParameter = "Executive Meeting Scheduler",
            isEnabled = true,
            executionCount = 28,
            lastRunTimestamp = now - 1000 * 60 * 35
        ),
        AutomationRule(
            name = "Cold Mail Follow-Up 3-Day Nudge",
            description = "Checks sent cold emails with no reply after 72 hours and generates a high-converting value-add follow-up snippet.",
            triggerType = "NO_REPLY",
            triggerCondition = "3_DAYS",
            actionType = "FOLLOW_UP_NUDGE",
            actionParameter = "PAS Framework Angle",
            isEnabled = true,
            executionCount = 9,
            lastRunTimestamp = now - 1000 * 60 * 60 * 14
        ),
        AutomationRule(
            name = "Auto-Archive Receipts & Newsletters",
            description = "Automatically organizes receipts from Stripe/AWS and moves promotional digests out of Primary tab.",
            triggerType = "KEYWORD",
            triggerCondition = "receipt,invoice,unsubscribe,newsletter",
            actionType = "AUTO_ARCHIVE",
            actionParameter = "MoveToCategory",
            isEnabled = true,
            executionCount = 52,
            lastRunTimestamp = now - 1000 * 60 * 60 * 48
        ),
        AutomationRule(
            name = "Morning AI Executive Briefing",
            description = "At 8:00 AM daily, synthesizes top 3 action items and incoming cold mail stats into an audio/text briefing.",
            triggerType = "TIME_SCHEDULE",
            triggerCondition = "08:00 AM",
            actionType = "DAILY_BRIEFING",
            actionParameter = "Daily Priority Brief",
            isEnabled = true,
            executionCount = 18,
            lastRunTimestamp = now - 1000 * 60 * 60 * 16
        )
    )
    automationDao.insertRules(sampleRules)

    // 3. Social Accounts
    val sampleSocialAccounts = listOf(
        SocialAccount(
            platform = SocialPlatform.LINKEDIN,
            username = "in/adityarai-dev",
            displayName = "Aditya Rai",
            profileUrl = "https://linkedin.com/in/adityarai-dev",
            isConnected = true,
            audienceBio = "Product Engineer & AI Architect | Building next-gen autonomous agent workflows",
            activeOutreachCount = 14
        ),
        SocialAccount(
            platform = SocialPlatform.TWITTER_X,
            username = "@adityarai_ai",
            displayName = "Aditya Rai ⚡",
            profileUrl = "https://x.com/adityarai_ai",
            isConnected = true,
            audienceBio = "Shipping daily. Autonomous agents, Kotlin, Jetpack Compose & LLMs.",
            activeOutreachCount = 22
        ),
        SocialAccount(
            platform = SocialPlatform.GITHUB,
            username = "kumaradityarai",
            displayName = "Aditya Rai (kumaradityarai)",
            profileUrl = "https://github.com/kumaradityarai",
            isConnected = true,
            audienceBio = "Open source contributor & agent systems builder.",
            activeOutreachCount = 8
        ),
        SocialAccount(
            platform = SocialPlatform.SUBSTACK,
            username = "adityarai.substack.com",
            displayName = "Aditya's AI Letters",
            profileUrl = "https://adityarai.substack.com",
            isConnected = true,
            audienceBio = "Weekly breakdowns on cold email psychology and AI systems.",
            activeOutreachCount = 5
        )
    )
    socialDao.insertAccounts(sampleSocialAccounts)

    // 4. Pre-built Cold Mail Campaigns
    val sampleCampaigns = listOf(
        ColdMailCampaign(
            title = "Tier-1 VC Seed Round Cold Outreach",
            targetName = "Garry Tan",
            targetCompany = "Y Combinator",
            targetRole = "President & CEO",
            framework = "AIDA (Attention, Interest, Desire, Action)",
            subjectVariantA = "Quick question on YC's autonomous agent thesis",
            subjectVariantB = "Aditya Rai / Autonomous Gmail AI Assistant (Live Demo)",
            body = "Hi Garry,\n\nLoved your recent breakdown on how developer tooling will collapse into hyper-personalized agentic workflows.\n\nWe built a personal Gmail copilot that automates outbound outreach sequences with a verified 42% reply rate and local zero-latency intelligence.\n\nWe just surpassed 2,400 developer stars and are preparing for our next major release.\n\nWould you be open to a 5-minute glance at our interactive demo this week?\n\nBest,\nAditya Rai\nkumaradityarai0005@gmail.com",
            followUp1 = "Hi Garry, bumping this in case it got buried in your inbox. Attached is a 60-second video demo of our live agent dispatching cold outreach in action. Let me know if you'd like an APK build to test.",
            valueProposition = "Autonomous email assistant with verified 42% cold mail reply rate",
            callToAction = "5-minute demo review",
            tone = "Executive & Concise",
            deliverabilityScore = 96,
            isSaved = true
        ),
        ColdMailCampaign(
            title = "B2B SaaS Enterprise Partnership Pitch",
            targetName = "Elena Vance",
            targetCompany = "Stripe",
            targetRole = "Head of Global Partnerships",
            framework = "PAS (Problem, Agitate, Solve)",
            subjectVariantA = "Eliminating manual cold outreach friction for Stripe Partners",
            subjectVariantB = "Stripe + AI Assistant integration proposal",
            body = "Hi Elena,\n\nMost outbound sales teams spend 14+ hours a week manually drafting personalized emails, yet average cold reply rates remain under 3%.\n\nThis friction slows deal velocity and leaves high-value pipeline opportunities cold.\n\nWe solved this by pairing behavioral cold-mail frameworks with multi-channel social sync (LinkedIn & X), boosting verified reply rates to 38%.\n\nOpen to exploring a lightweight integration for Stripe partner developers next Tuesday?\n\nBest,\nAditya Rai",
            followUp1 = "Hi Elena, thought of you while reviewing our latest case study showing a 3.4x boost in B2B response rates. Happy to send over the 1-page PDF if useful.",
            valueProposition = "Cut outreach time by 80% while boosting response rates to 38%",
            callToAction = "15-minute sync next Tuesday",
            tone = "Persuasive & Direct",
            deliverabilityScore = 94,
            isSaved = true
        )
    )
    for (campaign in sampleCampaigns) {
        coldMailDao.insertCampaign(campaign)
    }
}
