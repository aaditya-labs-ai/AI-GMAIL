package com.example.data.firestore

import android.util.Log
import com.example.data.model.AutomationRule
import com.example.data.model.ColdMailCampaign
import com.example.data.model.EmailEntity
import com.example.data.model.SocialOutreachItem
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

    suspend fun saveCampaignToCloud(userId: String, campaign: ColdMailCampaign) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            val docRef = db.collection("users")
                .document(userId)
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
            Log.d("FirestoreService", "Campaign synced to Firestore: ${campaign.title}")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync campaign: ${e.message}")
        }
    }

    suspend fun saveRuleToCloud(userId: String, rule: AutomationRule) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            val docRef = db.collection("users")
                .document(userId)
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
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            val docRef = db.collection("users")
                .document(userId)
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

    suspend fun saveScheduledEmailToCloud(userId: String, email: com.example.data.model.ScheduledEmail) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            val docRef = db.collection("users")
                .document(userId)
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
            Log.d("FirestoreService", "Scheduled email synced to Firestore: ${email.subject} for ${email.scheduledTimeFormatted}")
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to sync scheduled email: ${e.message}")
        }
    }

    suspend fun saveNotificationPreferencesToCloud(userId: String, prefs: com.example.data.model.NotificationPreferences) {
        if (userId.isBlank()) return
        val db = firestore ?: return
        try {
            val docRef = db.collection("users")
                .document(userId)
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
