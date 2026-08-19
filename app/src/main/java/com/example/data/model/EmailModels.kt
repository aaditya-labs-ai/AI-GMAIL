package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EmailFolder {
    INBOX,
    SENT,
    DRAFTS,
    STARRED,
    ARCHIVE,
    TRASH
}

enum class EmailCategory {
    PRIMARY,
    UPDATES,
    PROMOTIONS,
    SOCIAL
}

enum class EmailPriority {
    HIGH,
    NORMAL,
    LOW
}

@Entity(tableName = "emails")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String,
    val subject: String,
    val snippet: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val folder: EmailFolder = EmailFolder.INBOX,
    val category: EmailCategory = EmailCategory.PRIMARY,
    val priority: EmailPriority = EmailPriority.NORMAL,
    val tags: String = "", // Comma-separated tags
    val aiSummary: String? = null,
    val aiActionItems: String? = null,
    val replyDraft: String? = null
)

@Entity(tableName = "cold_mail_campaigns")
data class ColdMailCampaign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetName: String,
    val targetCompany: String,
    val targetRole: String,
    val framework: String, // PAS, AIDA, BAB, QuickPitch
    val subjectVariantA: String,
    val subjectVariantB: String,
    val body: String,
    val followUp1: String,
    val valueProposition: String,
    val callToAction: String,
    val tone: String,
    val deliverabilityScore: Int = 92,
    val createdAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = true
)

@Entity(tableName = "automation_rules")
data class AutomationRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val triggerType: String, // KEYWORD, DOMAIN, NO_REPLY, TIME_SCHEDULE
    val triggerCondition: String,
    val actionType: String, // AUTO_LABEL, AUTO_DRAFT, AUTO_ARCHIVE, FOLLOW_UP_NUDGE, DAILY_BRIEFING
    val actionParameter: String,
    val isEnabled: Boolean = true,
    val executionCount: Int = 0,
    val lastRunTimestamp: Long = 0
)

@Entity(tableName = "automation_logs")
data class AutomationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)

enum class SocialPlatform {
    LINKEDIN,
    TWITTER_X,
    GITHUB,
    INSTAGRAM,
    SUBSTACK
}

@Entity(tableName = "social_accounts")
data class SocialAccount(
    @PrimaryKey
    val platform: SocialPlatform,
    val username: String,
    val displayName: String,
    val profileUrl: String,
    val isConnected: Boolean = true,
    val audienceBio: String = "",
    val activeOutreachCount: Int = 0
)

@Entity(tableName = "social_outreach_posts")
data class SocialOutreachItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: SocialPlatform,
    val targetPersonOrChannel: String,
    val messageText: String,
    val outreachType: String, // "Connection Note", "InMail DM", "Tweet Pitch", "Collab DM"
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = false
)

data class UserProfile(
    val displayName: String = "Aditya Rai",
    val email: String = "kumaradityarai0005@gmail.com",
    val title: String = "Product & AI Engineer",
    val signature: String = "Best regards,\nAditya Rai\nkumaradityarai0005@gmail.com",
    val primaryGoal: String = "High-conversion cold outreach & inbox zero automation",
    val isGoogleConnected: Boolean = true
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "assistant"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isActionCard: Boolean = false,
    val actionTitle: String? = null,
    val actionPayload: String? = null
)
