package com.example.data.firestore

import com.example.data.model.AutomationRule
import com.example.data.model.ColdMailCampaign
import com.example.data.model.NotificationPreferences
import com.example.data.model.ScheduledEmail
import com.example.data.model.SocialOutreachItem
import com.example.util.SafeLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

sealed interface CloudWriteResult {
    data object Success : CloudWriteResult
    data class Failure(val message: String) : CloudWriteResult
}

class FirestoreService {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            SafeLogger.w("FirestoreService", "Firestore instance unavailable: ${e.message}")
            null
        }
    }

    private fun getAuthenticatedUid(): String? {
        return try {
            FirebaseAuth.getInstance().currentUser?.uid?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun resolveVerifiedUid(explicitUserId: String? = null): String {
        val authUid = getAuthenticatedUid()
            ?: throw SecurityException("Unauthorized: Operation requires an authenticated Firebase session.")

        if (!explicitUserId.isNullOrBlank() && explicitUserId != authUid) {
            throw SecurityException("Security Violation: User identifier mismatch detected.")
        }
        return authUid
    }

    suspend fun saveCampaignToCloud(userId: String? = null, campaign: ColdMailCampaign): CloudWriteResult {
        val verifiedUid = try {
            resolveVerifiedUid(userId)
        } catch (e: SecurityException) {
            SafeLogger.e("FirestoreService", "Access denied for campaign save: ${e.message}")
            return CloudWriteResult.Failure(e.message ?: "Authentication required")
        }

        val db = firestore ?: return CloudWriteResult.Failure("Cloud database unavailable")

        return try {
            val docRef = db.collection("users")
                .document(verifiedUid)
                .collection("campaigns")
                .document(if (campaign.id > 0) campaign.id.toString() else System.currentTimeMillis().toString())

            val data = hashMapOf(
                "id" to campaign.id,
                "title" to campaign.title,
                "targetName" to campaign.targetName,
                "targetCompany" to campaign.targetCompany,
                "targetRole" to campaign.targetRole,
                "framework" to campaign.framework,
                "subjectVariantA" to campaign.subjectVariantA,
                "subjectVariantB" to campaign.subjectVariantB,
                "body" to campaign.body,
                "followUp1" to campaign.followUp1,
                "valueProposition" to campaign.valueProposition,
                "callToAction" to campaign.callToAction,
                "tone" to campaign.tone,
                "deliverabilityScore" to campaign.deliverabilityScore,
                "createdAt" to campaign.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            SafeLogger.d("FirestoreService", "Campaign synced to Firestore securely")
            CloudWriteResult.Success
        } catch (e: Exception) {
            SafeLogger.e("FirestoreService", "Failed to sync campaign: ${e.message}")
            CloudWriteResult.Failure(e.localizedMessage ?: "Failed to sync campaign to cloud")
        }
    }

    suspend fun saveRuleToCloud(userId: String? = null, rule: AutomationRule): CloudWriteResult {
        val verifiedUid = try {
            resolveVerifiedUid(userId)
        } catch (e: SecurityException) {
            SafeLogger.e("FirestoreService", "Access denied for rule save: ${e.message}")
            return CloudWriteResult.Failure(e.message ?: "Authentication required")
        }

        val db = firestore ?: return CloudWriteResult.Failure("Cloud database unavailable")

        return try {
            val docRef = db.collection("users")
                .document(verifiedUid)
                .collection("automation_rules")
                .document(if (rule.id > 0) rule.id.toString() else System.currentTimeMillis().toString())

            val data = hashMapOf(
                "id" to rule.id,
                "name" to rule.name,
                "description" to rule.description,
                "triggerType" to rule.triggerType,
                "triggerCondition" to rule.triggerCondition,
                "actionType" to rule.actionType,
                "actionParameter" to rule.actionParameter,
                "isEnabled" to rule.isEnabled,
                "executionCount" to rule.executionCount,
                "lastRunTimestamp" to rule.lastRunTimestamp,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            SafeLogger.d("FirestoreService", "Automation rule synced to Firestore securely")
            CloudWriteResult.Success
        } catch (e: Exception) {
            SafeLogger.e("FirestoreService", "Failed to sync rule: ${e.message}")
            CloudWriteResult.Failure(e.localizedMessage ?: "Failed to sync rule to cloud")
        }
    }

    suspend fun saveSocialPostToCloud(userId: String? = null, post: SocialOutreachItem): CloudWriteResult {
        val verifiedUid = try {
            resolveVerifiedUid(userId)
        } catch (e: SecurityException) {
            SafeLogger.e("FirestoreService", "Access denied for social post save: ${e.message}")
            return CloudWriteResult.Failure(e.message ?: "Authentication required")
        }

        val db = firestore ?: return CloudWriteResult.Failure("Cloud database unavailable")

        return try {
            val docRef = db.collection("users")
                .document(verifiedUid)
                .collection("social_outreach")
                .document(if (post.id > 0) post.id.toString() else System.currentTimeMillis().toString())

            val data = hashMapOf(
                "id" to post.id,
                "platform" to post.platform.name,
                "targetPersonOrChannel" to post.targetPersonOrChannel,
                "messageText" to post.messageText,
                "outreachType" to post.outreachType,
                "timestamp" to post.timestamp,
                "isSent" to post.isSent
            )
            docRef.set(data, SetOptions.merge()).await()
            SafeLogger.d("FirestoreService", "Social post synced to Firestore securely")
            CloudWriteResult.Success
        } catch (e: Exception) {
            SafeLogger.e("FirestoreService", "Failed to sync social post: ${e.message}")
            CloudWriteResult.Failure(e.localizedMessage ?: "Failed to sync social post to cloud")
        }
    }

    suspend fun saveScheduledEmailToCloud(userId: String? = null, email: ScheduledEmail): CloudWriteResult {
        val verifiedUid = try {
            resolveVerifiedUid(userId)
        } catch (e: SecurityException) {
            SafeLogger.e("FirestoreService", "Access denied for scheduled email save: ${e.message}")
            return CloudWriteResult.Failure(e.message ?: "Authentication required")
        }

        val db = firestore ?: return CloudWriteResult.Failure("Cloud database unavailable")

        return try {
            val docRef = db.collection("users")
                .document(verifiedUid)
                .collection("scheduled_emails")
                .document(email.id)

            val data = hashMapOf(
                "id" to email.id,
                "recipientTo" to email.recipientTo,
                "recipientCc" to email.recipientCc,
                "subject" to email.subject,
                "body" to email.body,
                "scheduledTimeEpoch" to email.scheduledTimeEpoch,
                "scheduledTimeFormatted" to email.scheduledTimeFormatted,
                "status" to email.status,
                "createdAt" to email.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            SafeLogger.d("FirestoreService", "Scheduled email synced to Firestore securely")
            CloudWriteResult.Success
        } catch (e: Exception) {
            SafeLogger.e("FirestoreService", "Failed to sync scheduled email: ${e.message}")
            CloudWriteResult.Failure(e.localizedMessage ?: "Failed to sync scheduled email to cloud")
        }
    }

    suspend fun saveNotificationPreferencesToCloud(userId: String? = null, prefs: NotificationPreferences): CloudWriteResult {
        val verifiedUid = try {
            resolveVerifiedUid(userId)
        } catch (e: SecurityException) {
            SafeLogger.e("FirestoreService", "Access denied for notification prefs save: ${e.message}")
            return CloudWriteResult.Failure(e.message ?: "Authentication required")
        }

        val db = firestore ?: return CloudWriteResult.Failure("Cloud database unavailable")

        return try {
            val docRef = db.collection("users")
                .document(verifiedUid)
                .collection("settings")
                .document("notifications")

            val data = hashMapOf(
                "notifyUrgent" to prefs.notifyUrgent,
                "notifyWork" to prefs.notifyWork,
                "notifyPersonal" to prefs.notifyPersonal,
                "notifyInvestors" to prefs.notifyInvestors,
                "notifyPromotions" to prefs.notifyPromotions,
                "notifySocial" to prefs.notifySocial,
                "quietHoursEnabled" to prefs.quietHoursEnabled,
                "quietHoursStart" to prefs.quietHoursStart,
                "quietHoursEnd" to prefs.quietHoursEnd,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).await()
            SafeLogger.d("FirestoreService", "Notification preferences synced to Firestore securely")
            CloudWriteResult.Success
        } catch (e: Exception) {
            SafeLogger.e("FirestoreService", "Failed to sync notification preferences: ${e.message}")
            CloudWriteResult.Failure(e.localizedMessage ?: "Failed to sync notification preferences to cloud")
        }
    }
}
