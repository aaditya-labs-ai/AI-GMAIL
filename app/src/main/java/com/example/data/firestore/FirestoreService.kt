package com.example.data.firestore

import android.util.Log
import com.example.data.model.AutomationRule
import com.example.data.model.ColdMailCampaign
import com.example.data.model.NotificationPreferences
import com.example.data.model.ScheduledEmail
import com.example.data.model.SocialOutreachItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FirestoreService", "Firestore instance unavailable: ${e.message}")
            null
        }
    }

    private fun getVerifiedCurrentUid(): String? {
        val auth = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
        return auth?.currentUser?.uid?.takeIf { it.isNotBlank() }
    }

    private fun validateAccess(requestedUserId: String): String {
        val currentUid = getVerifiedCurrentUid()
            ?: throw SecurityException("Unauthorized Firestore access: User is not authenticated.")
        if (requestedUserId.isBlank() || requestedUserId != currentUid) {
            throw SecurityException("Access Denied: Cannot access data for user '$requestedUserId' from authenticated session '$currentUid'.")
        }
        return currentUid
    }

    suspend fun saveCampaignToCloud(userId: String, campaign: ColdMailCampaign) {
        val verifiedUid = try {
            validateAccess(userId)
        } catch (e: SecurityException) {
            Log.e("FirestoreService", "Security violation: ${e.message}")
            return
        }
        val db = firestore ?: return
        try {
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
            Log.d("FirestoreService", "Campaign synced to Firestore securely for user $verifiedUid")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync campaign: ${e.message}")
        }
    }

    suspend fun saveRuleToCloud(userId: String, rule: AutomationRule) {
        val verifiedUid = try {
            validateAccess(userId)
        } catch (e: SecurityException) {
            Log.e("FirestoreService", "Security violation: ${e.message}")
            return
        }
        val db = firestore ?: return
        try {
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
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync rule: ${e.message}")
        }
    }

    suspend fun saveSocialPostToCloud(userId: String, post: SocialOutreachItem) {
        val verifiedUid = try {
            validateAccess(userId)
        } catch (e: SecurityException) {
            Log.e("FirestoreService", "Security violation: ${e.message}")
            return
        }
        val db = firestore ?: return
        try {
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
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync social post: ${e.message}")
        }
    }

    suspend fun saveScheduledEmailToCloud(userId: String, email: ScheduledEmail) {
        val verifiedUid = try {
            validateAccess(userId)
        } catch (e: SecurityException) {
            Log.e("FirestoreService", "Security violation: ${e.message}")
            return
        }
        val db = firestore ?: return
        try {
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
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync scheduled email: ${e.message}")
        }
    }

    suspend fun saveNotificationPreferencesToCloud(userId: String, prefs: NotificationPreferences) {
        val verifiedUid = try {
            validateAccess(userId)
        } catch (e: SecurityException) {
            Log.e("FirestoreService", "Security violation: ${e.message}")
            return
        }
        val db = firestore ?: return
        try {
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
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync notification preferences: ${e.message}")
        }
    }
}
