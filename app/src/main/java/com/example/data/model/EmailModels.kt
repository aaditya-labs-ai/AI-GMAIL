package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

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

enum class MessageDeliveryStatus {
    SENDING,   // In-flight
    SENT,      // Single checkmark (sent to mail server)
    DELIVERED, // Double checkmark (gray, delivered to recipient mailbox)
    OPENED     // Double checkmark (blue, opened/read by recipient)
}

data class EmailAttachment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val fileName: String,
    val fileExtension: String = "pdf", // "pdf", "png", "jpg", "xlsx", "docx", "zip", "csv"
    val fileSizeFormatted: String = "1.2 MB",
    val fileTypeDescription: String = "Document",
    val previewContent: String? = null // Text or summary preview for preview sheet
) {
    companion object {
        fun fromString(namesString: String): List<EmailAttachment> {
            if (namesString.isBlank()) return emptyList()
            return namesString.split(",").mapNotNull { raw ->
                val trimmed = raw.trim()
                if (trimmed.isBlank()) return@mapNotNull null
                val ext = trimmed.substringAfterLast(".", "").substringBefore(" ").lowercase().ifEmpty { "pdf" }
                val (size, typeDesc, preview) = when (ext) {
                    "pdf" -> Triple(
                        "2.4 MB",
                        "Adobe PDF Document",
                        "--- PDF DOCUMENT PREVIEW ---\nTitle: $trimmed\nAuthor: Verified Sender\n\nExecutive Summary:\nThis document contains official transaction terms, pitch deck highlights, verified architecture diagrams, and contractual milestones. All signatures are cryptographically authenticated."
                    )
                    "docx", "doc" -> Triple(
                        "1.1 MB",
                        "Microsoft Word Document",
                        "--- WORD DOCUMENT PREVIEW ---\nDocument: $trimmed\n\nMeeting Minutes & Action Items:\n1. Key stakeholder sign-off completed.\n2. Technical sprint roadmap aligned with Q3 OKRs.\n3. Security and compliance review passed with zero critical CVEs."
                    )
                    "xlsx", "xls", "csv" -> Triple(
                        "850 KB",
                        "Financial Spreadsheet",
                        "--- SPREADSHEET PREVIEW ---\nSheet: $trimmed\n\nColumns: [Category | Q1 Actual | Q2 Projected | Growth Rate | Margin]\n• ARR: $2.4M (↑ 42% YoY)\n• Customer Acquisition Cost: $420\n• Net Retention: 128%\n• Runway: 28 Months"
                    )
                    "png", "jpg", "jpeg" -> Triple(
                        "3.6 MB",
                        "High-Resolution Image",
                        "--- IMAGE METADATA PREVIEW ---\nFile: $trimmed\nDimensions: 3840 x 2160 (4K)\nColor Space: sRGB\nCompression: Lossless PNG\nDescription: High-fidelity system architecture diagram and UI mockup."
                    )
                    "zip", "tar", "gz" -> Triple(
                        "14.8 MB",
                        "Compressed Archive",
                        "--- ARCHIVE CONTENTS PREVIEW ---\nArchive: $trimmed\n\n• src/engine/core_service.kt\n• contracts/terms_v2.pdf\n• assets/brand_kit_vectors.svg\n• database_export_schema.sql"
                    )
                    else -> Triple(
                        "500 KB",
                        "Attachment File",
                        "--- FILE PREVIEW ---\nFile Name: $trimmed\nStatus: Verified and ready for download."
                    )
                }
                EmailAttachment(
                    fileName = trimmed,
                    fileExtension = ext,
                    fileSizeFormatted = size,
                    fileTypeDescription = typeDesc,
                    previewContent = preview
                )
            }
        }
    }
}

data class EmailThreadMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderName: String,
    val senderEmail: String,
    val recipientEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean = false,
    val body: String,
    val deliveryStatus: MessageDeliveryStatus? = null,
    val attachments: List<EmailAttachment> = emptyList()
)

data class SmartReplyOption(
    val id: String,
    val label: String,
    val iconType: String = "general", // "acknowledge", "meeting", "decline", "info"
    val fullDraft: String
)

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
    val replyDraft: String? = null,
    val deliveryStatus: MessageDeliveryStatus? = null, // for sent / outgoing emails
    val hasAttachments: Boolean = false,
    val attachmentNames: String = "" // Comma-separated or descriptor
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

data class NotificationPreferences(
    val notifyUrgent: Boolean = true,
    val notifyWork: Boolean = true,
    val notifyPersonal: Boolean = true,
    val notifyInvestors: Boolean = true,
    val notifyPromotions: Boolean = false,
    val notifySocial: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00"
)

data class ScheduledEmail(
    val id: String = java.util.UUID.randomUUID().toString(),
    val recipientTo: String,
    val recipientCc: String = "",
    val subject: String,
    val body: String,
    val scheduledTimeEpoch: Long,
    val scheduledTimeFormatted: String,
    val status: String = "SCHEDULED", // "SCHEDULED", "SENT", "CANCELLED"
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val title: String = "AI Mail Copilot",
    val signature: String = "",
    val primaryGoal: String = "Executive email & outreach automation",
    val isGoogleConnected: Boolean = false
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

/**
 * Room Entity storing Gmail Threads for offline access, search, and categorization.
 */
@Entity(
    tableName = "gmail_threads",
    indices = [
        Index(value = ["threadId"], unique = true),
        Index(value = ["lastMessageTimestamp"]),
        Index(value = ["folder"]),
        Index(value = ["category"])
    ]
)
data class GmailThreadEntity(
    @PrimaryKey
    val threadId: String,
    val historyId: String? = null,
    val snippet: String = "",
    val subject: String = "(No Subject)",
    val senderSummary: String = "", // e.g., "Marc Andreessen, Satya Nadella"
    val lastSenderEmail: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val messageCount: Int = 1,
    val isUnread: Boolean = false,
    val isStarred: Boolean = false,
    val folder: EmailFolder = EmailFolder.INBOX,
    val category: EmailCategory = EmailCategory.PRIMARY,
    val priority: EmailPriority = EmailPriority.NORMAL,
    val labels: String = "INBOX", // Comma-separated label IDs e.g. "INBOX, IMPORTANT, UNREAD"
    val aiSummary: String? = null,
    val aiActionItems: String? = null,
    val isSyncedOffline: Boolean = true,
    val lastFetchedAt: Long = System.currentTimeMillis()
)

/**
 * Room Entity storing individual Gmail Messages with full bodies, attachments metadata, and AI tags.
 */
@Entity(
    tableName = "gmail_messages",
    indices = [
        Index(value = ["messageId"], unique = true),
        Index(value = ["threadId"]),
        Index(value = ["internalDate"]),
        Index(value = ["senderEmail"])
    ]
)
data class GmailMessageEntity(
    @PrimaryKey
    val messageId: String,
    val threadId: String,
    val senderName: String,
    val senderEmail: String,
    val recipientEmails: String, // Comma-separated
    val ccEmails: String = "",
    val bccEmails: String = "",
    val subject: String = "",
    val snippet: String = "",
    val bodyText: String = "",
    val bodyHtml: String = "",
    val internalDate: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isDraft: Boolean = false,
    val isSent: Boolean = false,
    val isTrash: Boolean = false,
    val isSpam: Boolean = false,
    val folder: EmailFolder = EmailFolder.INBOX,
    val category: EmailCategory = EmailCategory.PRIMARY,
    val priority: EmailPriority = EmailPriority.NORMAL,
    val labelIds: String = "INBOX", // Comma-separated Gmail labels
    val hasAttachments: Boolean = false,
    val attachmentNames: String = "", // Comma-separated attachment filenames
    val aiSummary: String? = null,
    val aiActionItems: String? = null,
    val replyDraft: String? = null,
    val isCachedLocally: Boolean = true,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * 1-to-Many Relationship joining a Gmail Thread with all of its constituent Messages.
 */
data class GmailThreadWithMessages(
    @Embedded
    val thread: GmailThreadEntity,

    @Relation(
        parentColumn = "threadId",
        entityColumn = "threadId"
    )
    val messages: List<GmailMessageEntity>
)

/**
 * Room Entity specifically designed for local offline drafting, autosaving, and queueing outbound emails.
 */
@Entity(
    tableName = "draft_messages",
    indices = [
        Index(value = ["threadId"]),
        Index(value = ["lastSavedTimestamp"]),
        Index(value = ["syncStatus"])
    ]
)
data class DraftMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val draftId: Long = 0,
    val threadId: String? = null,
    val inReplyToMessageId: String? = null,
    val recipientTo: String = "",
    val recipientCc: String = "",
    val recipientBcc: String = "",
    val subject: String = "",
    val body: String = "",
    val attachments: String = "",
    val isAiGenerated: Boolean = false,
    val aiPromptUsed: String? = null,
    val lastSavedTimestamp: Long = System.currentTimeMillis(),
    val syncStatus: String = "LOCAL_DRAFT" // "LOCAL_DRAFT", "QUEUED_TO_SEND", "SYNCED_TO_GMAIL"
)


