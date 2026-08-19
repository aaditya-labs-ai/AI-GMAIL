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

    @Query("SELECT * FROM emails WHERE subject LIKE '%' || :query || '%' OR snippet LIKE '%' || :query || '%' OR senderName LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEmails(query: String): Flow<List<EmailEntity>>

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
}
