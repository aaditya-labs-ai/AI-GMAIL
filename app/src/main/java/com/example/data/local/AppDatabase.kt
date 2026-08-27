package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `automation_rules` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `triggerType` TEXT NOT NULL,
                `triggerCondition` TEXT NOT NULL,
                `actionType` TEXT NOT NULL,
                `actionParameter` TEXT NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `executionCount` INTEGER NOT NULL,
                `lastRunTimestamp` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `automation_logs` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ruleName` TEXT NOT NULL,
                `details` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `status` TEXT NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `social_accounts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `platform` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `profileUrl` TEXT NOT NULL,
                `isConnected` INTEGER NOT NULL,
                `audienceBio` TEXT NOT NULL,
                `activeOutreachCount` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `social_outreach` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `platform` TEXT NOT NULL,
                `targetPersonOrChannel` TEXT NOT NULL,
                `messageText` TEXT NOT NULL,
                `outreachType` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `isSent` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `gmail_threads` (
                `threadId` TEXT PRIMARY KEY NOT NULL,
                `historyId` TEXT,
                `snippet` TEXT NOT NULL,
                `subject` TEXT NOT NULL,
                `senderSummary` TEXT NOT NULL,
                `lastSenderEmail` TEXT NOT NULL,
                `lastMessageTimestamp` INTEGER NOT NULL,
                `messageCount` INTEGER NOT NULL,
                `isUnread` INTEGER NOT NULL,
                `isStarred` INTEGER NOT NULL,
                `folder` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `priority` TEXT NOT NULL,
                `labels` TEXT NOT NULL,
                `aiSummary` TEXT,
                `aiActionItems` TEXT,
                `isSyncedOffline` INTEGER NOT NULL,
                `lastFetchedAt` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `gmail_messages` (
                `messageId` TEXT PRIMARY KEY NOT NULL,
                `threadId` TEXT NOT NULL,
                `senderName` TEXT NOT NULL,
                `senderEmail` TEXT NOT NULL,
                `recipientEmails` TEXT NOT NULL,
                `subject` TEXT NOT NULL,
                `snippet` TEXT NOT NULL,
                `bodyText` TEXT NOT NULL,
                `internalDate` INTEGER NOT NULL,
                `isRead` INTEGER NOT NULL,
                `isStarred` INTEGER NOT NULL,
                `isSent` INTEGER NOT NULL,
                `folder` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `priority` TEXT NOT NULL,
                `labelIds` TEXT NOT NULL,
                `hasAttachments` INTEGER NOT NULL,
                `attachmentNames` TEXT NOT NULL,
                `isCachedLocally` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `draft_messages` (
                `draftId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `threadId` TEXT,
                `recipientTo` TEXT NOT NULL,
                `recipientCc` TEXT NOT NULL,
                `recipientBcc` TEXT NOT NULL,
                `subject` TEXT NOT NULL,
                `body` TEXT NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `aiTone` TEXT NOT NULL,
                `isAiGenerated` INTEGER NOT NULL
            )
        """.trimIndent())
    }
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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
    }
}

private suspend fun populateInitialData(db: AppDatabase) {
    val emailDao = db.emailDao()
    val coldMailDao = db.coldMailDao()
    val automationDao = db.automationDao()
    val socialDao = db.socialHubDao()
    val threadDao = db.gmailThreadDao()
    val messageDao = db.gmailMessageDao()

    val now = System.currentTimeMillis()

    // 1. Initial Sample Automation Rules
    val sampleRules = listOf(
        AutomationRule(
            name = "VIP Investor & Partner Auto-Priority",
            description = "Flags and marks emails from tier-1 investors, board members, and executives as high priority.",
            triggerType = "SENDER",
            triggerCondition = "investor,partner,board,founder",
            actionType = "AUTO_LABEL",
            actionParameter = "HIGH_PRIORITY",
            isEnabled = true,
            executionCount = 24,
            lastRunTimestamp = now - 1000 * 60 * 60 * 3
        ),
        AutomationRule(
            name = "Receipts & Invoices Auto-Archive",
            description = "Automatically categorizes purchase receipts and statements under Promotions/Receipts.",
            triggerType = "KEYWORD",
            triggerCondition = "receipt,invoice,statement,billing",
            actionType = "AUTO_ARCHIVE",
            actionParameter = "MoveToCategory",
            isEnabled = true,
            executionCount = 52,
            lastRunTimestamp = now - 1000 * 60 * 60 * 48
        ),
        AutomationRule(
            name = "Morning AI Executive Briefing",
            description = "At 8:00 AM daily, synthesizes top action items and incoming email highlights.",
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

    // 2. Initial Sample Social Accounts
    val sampleSocialAccounts = listOf(
        SocialAccount(
            platform = SocialPlatform.LINKEDIN,
            username = "in/connected-profile",
            displayName = "Professional Profile",
            profileUrl = "https://linkedin.com",
            isConnected = false,
            audienceBio = "Product Engineer & AI Systems Architect",
            activeOutreachCount = 0
        ),
        SocialAccount(
            platform = SocialPlatform.TWITTER_X,
            username = "@ai_outreach",
            displayName = "AI Copilot",
            profileUrl = "https://x.com",
            isConnected = false,
            audienceBio = "AI engineering, automation, and executive workflows.",
            activeOutreachCount = 0
        ),
        SocialAccount(
            platform = SocialPlatform.GITHUB,
            username = "dev-user",
            displayName = "Developer Profile",
            profileUrl = "https://github.com",
            isConnected = false,
            audienceBio = "Open-source contributor & developer.",
            activeOutreachCount = 0
        ),
        SocialAccount(
            platform = SocialPlatform.SUBSTACK,
            username = "newsletter.substack.com",
            displayName = "AI Executive Letters",
            profileUrl = "https://substack.com",
            isConnected = false,
            audienceBio = "Weekly insights on automated workflows and technology.",
            activeOutreachCount = 0
        )
    )
    socialDao.insertAccounts(sampleSocialAccounts)

    // 3. Pre-built Cold Mail Campaigns
    val sampleCampaigns = listOf(
        ColdMailCampaign(
            title = "Partnership & Growth Outreach Template",
            targetName = "Partnership Lead",
            targetCompany = "Enterprise Cloud",
            targetRole = "Head of Global Partnerships",
            framework = "AIDA (Attention, Interest, Desire, Action)",
            subjectVariantA = "Automating outbound communication workflows",
            subjectVariantB = "AI Assistant integration proposal",
            body = "Hi there,\n\nWe built an autonomous copilot that automates outbound outreach sequences with local zero-latency intelligence and high deliverability.\n\nWould you be open to a 5-minute glance at our interactive demo this week?\n\nBest regards,\nAI Copilot Team",
            followUp1 = "Hi, bumping this in case it got buried in your inbox. Attached is a brief overview of our live agent dispatching outreach in action.",
            valueProposition = "Autonomous email assistant with verified high deliverability",
            callToAction = "5-minute demo review",
            tone = "Executive & Concise",
            deliverabilityScore = 96,
            isSaved = true
        )
    )
    for (campaign in sampleCampaigns) {
        coldMailDao.insertCampaign(campaign)
    }

    // 4. Sample Demonstration Email
    val sampleEmail = EmailEntity(
        senderName = "Welcome Bot",
        senderEmail = "welcome@ai-gmail.local",
        recipientEmail = "user@example.com",
        subject = "Welcome to AI-GMAIL Copilot",
        snippet = "Your secure personal email intelligence co-pilot is ready.",
        body = "Welcome to AI-GMAIL!\n\nConnect your Google Account to begin syncing your real Gmail threads securely. All AI operations respect your privacy, and data is protected with strict user isolation.",
        timestamp = now - 1000 * 60 * 10,
        isRead = false,
        isStarred = true,
        folder = EmailFolder.INBOX,
        category = EmailCategory.PRIMARY,
        priority = EmailPriority.HIGH,
        tags = "Welcome, Security",
        aiSummary = "Welcome message explaining setup and privacy protections.",
        aiActionItems = "• Sign in with Google\n• Authorize Gmail access to sync mailbox",
        deliveryStatus = null,
        hasAttachments = false,
        attachmentNames = ""
    )
    emailDao.insertEmail(sampleEmail)
}
