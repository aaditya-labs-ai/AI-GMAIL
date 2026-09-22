package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailDao {
    @Query("SELECT * FROM emails WHERE folder = :folder ORDER BY timestamp DESC")
    fun getEmailsByFolder(folder: EmailFolder): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE folder = :folder AND category = :category ORDER BY timestamp DESC")
    fun getEmailsByFolderAndCategory(folder: EmailFolder, category: EmailCategory): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredEmails(): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE id = :id")
    suspend fun getEmailById(id: Long): EmailEntity?

    @Query("SELECT * FROM emails WHERE subject LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%' OR senderName LIKE '%' || :query || '%' OR senderEmail LIKE '%' || :query || '%' OR recipientEmail LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEmails(query: String): Flow<List<EmailEntity>>

    @Query("SELECT * FROM emails WHERE tags LIKE '%' || :tag || '%' ORDER BY timestamp DESC")
    fun getEmailsByTag(tag: String): Flow<List<EmailEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    @Update
    suspend fun updateEmail(email: EmailEntity)

    @Query("UPDATE emails SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Long, isRead: Boolean)

    @Query("UPDATE emails SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarredStatus(id: Long, isStarred: Boolean)

    @Query("UPDATE emails SET folder = :folder WHERE id = :id")
    suspend fun updateFolder(id: Long, folder: EmailFolder)

    @Query("UPDATE emails SET aiSummary = :summary, aiActionItems = :actionItems WHERE id = :id")
    suspend fun updateAiSummary(id: Long, summary: String, actionItems: String?)

    @Query("UPDATE emails SET replyDraft = :draft WHERE id = :id")
    suspend fun updateReplyDraft(id: Long, draft: String?)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: Long)

    @Query("DELETE FROM emails")
    suspend fun clearAllEmails()

    @Query("SELECT COUNT(*) FROM emails WHERE folder = 'INBOX' AND isRead = 0")
    fun getUnreadCount(): Flow<Int>
}

@Dao
interface ColdMailDao {
    @Query("SELECT * FROM cold_mail_campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<ColdMailCampaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: ColdMailCampaign): Long

    @Delete
    suspend fun deleteCampaign(campaign: ColdMailCampaign)

    @Query("SELECT * FROM cold_mail_campaigns WHERE id = :id")
    suspend fun getCampaignById(id: Long): ColdMailCampaign?

    @Query("DELETE FROM cold_mail_campaigns")
    suspend fun clearAllCampaigns()
}

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun getAllRules(): Flow<List<AutomationRule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: AutomationRule): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<AutomationRule>)

    @Update
    suspend fun updateRule(rule: AutomationRule)

    @Query("UPDATE automation_rules SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setRuleEnabled(id: Long, isEnabled: Boolean)

    @Query("UPDATE automation_rules SET executionCount = executionCount + 1, lastRunTimestamp = :timestamp WHERE id = :id")
    suspend fun recordRuleExecution(id: Long, timestamp: Long)

    @Delete
    suspend fun deleteRule(rule: AutomationRule)

    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<AutomationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutomationLog)

    @Query("DELETE FROM automation_rules")
    suspend fun clearAllRules()

    @Query("DELETE FROM automation_logs")
    suspend fun clearAllLogs()
}

@Dao
interface SocialHubDao {
    @Query("SELECT * FROM social_accounts")
    fun getAllAccounts(): Flow<List<SocialAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: SocialAccount)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<SocialAccount>)

    @Query("UPDATE social_accounts SET isConnected = :connected WHERE platform = :platform")
    suspend fun updateConnection(platform: SocialPlatform, connected: Boolean)

    @Query("SELECT * FROM social_outreach_posts ORDER BY timestamp DESC")
    fun getAllOutreach(): Flow<List<SocialOutreachItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutreach(item: SocialOutreachItem): Long

    @Query("UPDATE social_outreach_posts SET isSent = 1 WHERE id = :id")
    suspend fun markOutreachSent(id: Long)

    @Query("DELETE FROM social_outreach_posts")
    suspend fun clearAllOutreach()
}

/**
 * Data Access Object for Gmail Threads, enabling reactive queries and offline caching.
 */
@Dao
interface GmailThreadDao {
    @Query("SELECT * FROM gmail_threads WHERE folder = :folder ORDER BY lastMessageTimestamp DESC")
    fun getThreadsByFolder(folder: EmailFolder): Flow<List<GmailThreadEntity>>

    @Query("SELECT * FROM gmail_threads WHERE folder = :folder AND category = :category ORDER BY lastMessageTimestamp DESC")
    fun getThreadsByFolderAndCategory(folder: EmailFolder, category: EmailCategory): Flow<List<GmailThreadEntity>>

    @Query("SELECT * FROM gmail_threads WHERE isStarred = 1 ORDER BY lastMessageTimestamp DESC")
    fun getStarredThreads(): Flow<List<GmailThreadEntity>>

    @Transaction
    @Query("SELECT * FROM gmail_threads WHERE threadId = :threadId")
    fun getThreadWithMessages(threadId: String): Flow<GmailThreadWithMessages?>

    @Transaction
    @Query("SELECT * FROM gmail_threads WHERE folder = :folder ORDER BY lastMessageTimestamp DESC")
    fun getThreadsWithMessagesByFolder(folder: EmailFolder): Flow<List<GmailThreadWithMessages>>

    @Query("SELECT * FROM gmail_threads WHERE threadId = :threadId")
    suspend fun getThreadById(threadId: String): GmailThreadEntity?

    @Query("SELECT * FROM gmail_threads WHERE subject LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%' OR senderSummary LIKE '%' || :query || '%' ORDER BY lastMessageTimestamp DESC")
    fun searchThreads(query: String): Flow<List<GmailThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: GmailThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThreads(threads: List<GmailThreadEntity>)

    @Update
    suspend fun updateThread(thread: GmailThreadEntity)

    @Query("UPDATE gmail_threads SET isUnread = :isUnread WHERE threadId = :threadId")
    suspend fun updateThreadUnread(threadId: String, isUnread: Boolean)

    @Query("UPDATE gmail_threads SET isStarred = :isStarred WHERE threadId = :threadId")
    suspend fun updateThreadStarred(threadId: String, isStarred: Boolean)

    @Query("UPDATE gmail_threads SET folder = :folder WHERE threadId = :threadId")
    suspend fun updateThreadFolder(threadId: String, folder: EmailFolder)

    @Query("UPDATE gmail_threads SET category = :category WHERE threadId = :threadId")
    suspend fun updateThreadCategory(threadId: String, category: EmailCategory)

    @Query("UPDATE gmail_threads SET aiSummary = :summary, aiActionItems = :actionItems WHERE threadId = :threadId")
    suspend fun updateThreadAiInsights(threadId: String, summary: String?, actionItems: String?)

    @Query("DELETE FROM gmail_threads WHERE threadId = :threadId")
    suspend fun deleteThreadById(threadId: String)

    @Query("DELETE FROM gmail_threads")
    suspend fun clearAllThreads()

    @Query("SELECT COUNT(*) FROM gmail_threads WHERE folder = 'INBOX' AND isUnread = 1")
    fun getUnreadThreadCount(): Flow<Int>
}

/**
 * Data Access Object for individual Gmail Messages, supporting thread fetching and offline reads.
 */
@Dao
interface GmailMessageDao {
    @Query("SELECT * FROM gmail_messages WHERE threadId = :threadId ORDER BY internalDate ASC")
    fun getMessagesForThread(threadId: String): Flow<List<GmailMessageEntity>>

    @Query("SELECT * FROM gmail_messages WHERE threadId = :threadId ORDER BY internalDate ASC")
    suspend fun getMessagesForThreadSync(threadId: String): List<GmailMessageEntity>

    @Query("SELECT * FROM gmail_messages WHERE messageId = :messageId")
    fun getMessageById(messageId: String): Flow<GmailMessageEntity?>

    @Query("SELECT * FROM gmail_messages WHERE messageId = :messageId")
    suspend fun getMessageByIdDirect(messageId: String): GmailMessageEntity?

    @Query("SELECT * FROM gmail_messages WHERE folder = :folder ORDER BY internalDate DESC")
    fun getMessagesByFolder(folder: EmailFolder): Flow<List<GmailMessageEntity>>

    @Query("SELECT * FROM gmail_messages WHERE isRead = 0 ORDER BY internalDate DESC")
    fun getUnreadMessages(): Flow<List<GmailMessageEntity>>

    @Query("SELECT * FROM gmail_messages WHERE subject LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%' OR bodyText LIKE '%' || :query || '%' OR senderName LIKE '%' || :query || '%' ORDER BY internalDate DESC")
    fun searchMessages(query: String): Flow<List<GmailMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: GmailMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<GmailMessageEntity>)

    @Update
    suspend fun updateMessage(message: GmailMessageEntity)

    @Query("UPDATE gmail_messages SET isRead = :isRead WHERE messageId = :messageId")
    suspend fun updateMessageReadStatus(messageId: String, isRead: Boolean)

    @Query("UPDATE gmail_messages SET isStarred = :isStarred WHERE messageId = :messageId")
    suspend fun updateMessageStarredStatus(messageId: String, isStarred: Boolean)

    @Query("UPDATE gmail_messages SET folder = :folder WHERE messageId = :messageId")
    suspend fun updateMessageFolder(messageId: String, folder: EmailFolder)

    @Query("UPDATE gmail_messages SET replyDraft = :draft WHERE messageId = :messageId")
    suspend fun updateMessageReplyDraft(messageId: String, draft: String?)

    @Query("UPDATE gmail_messages SET aiSummary = :summary, aiActionItems = :actionItems WHERE messageId = :messageId")
    suspend fun updateMessageAiInsights(messageId: String, summary: String?, actionItems: String?)

    @Query("DELETE FROM gmail_messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM gmail_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesByThreadId(threadId: String)

    @Query("DELETE FROM gmail_messages")
    suspend fun clearAllMessages()

    @Query("SELECT COUNT(*) FROM gmail_messages WHERE isCachedLocally = 1")
    fun getCachedMessageCount(): Flow<Int>
}

/**
 * Data Access Object for local drafts and offline queueing.
 */
@Dao
interface DraftMessageDao {
    @Query("SELECT * FROM draft_messages ORDER BY lastSavedTimestamp DESC")
    fun getAllDrafts(): Flow<List<DraftMessageEntity>>

    @Query("SELECT * FROM draft_messages WHERE draftId = :draftId")
    fun getDraftById(draftId: Long): Flow<DraftMessageEntity?>

    @Query("SELECT * FROM draft_messages WHERE threadId = :threadId ORDER BY lastSavedTimestamp DESC LIMIT 1")
    fun getDraftForThread(threadId: String): Flow<DraftMessageEntity?>

    @Query("SELECT * FROM draft_messages WHERE threadId = :threadId ORDER BY lastSavedTimestamp DESC LIMIT 1")
    suspend fun getDraftForThreadSync(threadId: String): DraftMessageEntity?

    @Query("SELECT * FROM draft_messages WHERE syncStatus = 'QUEUED_TO_SEND' ORDER BY lastSavedTimestamp ASC")
    fun getQueuedOutboxMessages(): Flow<List<DraftMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDraft(draft: DraftMessageEntity): Long

    @Update
    suspend fun updateDraft(draft: DraftMessageEntity)

    @Query("UPDATE draft_messages SET syncStatus = :status, lastSavedTimestamp = :timestamp WHERE draftId = :draftId")
    suspend fun updateDraftSyncStatus(draftId: Long, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM draft_messages WHERE draftId = :draftId")
    suspend fun deleteDraftById(draftId: Long)

    @Query("DELETE FROM draft_messages WHERE threadId = :threadId")
    suspend fun deleteDraftsForThread(threadId: String)

    @Query("DELETE FROM draft_messages")
    suspend fun clearAllDrafts()

    @Query("SELECT COUNT(*) FROM draft_messages WHERE syncStatus = 'LOCAL_DRAFT'")
    fun getDraftCount(): Flow<Int>
}


