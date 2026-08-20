package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AttachmentPreviewDialog
import com.example.ui.components.AttachmentThumbnailChip
import com.example.ui.components.DeliveryStatusIndicator
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailDetailSheet(
    email: EmailEntity,
    viewModel: AssistantViewModel,
    isAnalyzing: Boolean,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }
    val timeFormatted = remember(email.timestamp) { dateFormat.format(Date(email.timestamp)) }
    val uiState by viewModel.uiState.collectAsState()

    var selectedAttachmentForPreview by remember { mutableStateOf<EmailAttachment?>(null) }
    var threadQuickReplyText by remember { mutableStateOf("") }
    var isReplyingInThread by remember { mutableStateOf(false) }

    // Parse attachments from email
    val emailAttachments = remember(email.attachmentNames) {
        EmailAttachment.fromString(email.attachmentNames)
    }

    // Build thread messages
    val threadMessages = remember(email) {
        val list = mutableListOf<EmailThreadMessage>()
        val isSentFolder = email.folder == EmailFolder.SENT

        if (isSentFolder) {
            // Outgoing initial email
            list.add(
                EmailThreadMessage(
                    id = "msg_${email.id}_out",
                    senderName = "Aditya Rai",
                    senderEmail = email.senderEmail,
                    recipientEmail = email.recipientEmail,
                    timestamp = email.timestamp,
                    isOutgoing = true,
                    body = email.body,
                    deliveryStatus = email.deliveryStatus ?: MessageDeliveryStatus.OPENED,
                    attachments = emailAttachments
                )
            )
        } else {
            // Incoming initial message
            list.add(
                EmailThreadMessage(
                    id = "msg_${email.id}_in",
                    senderName = email.senderName,
                    senderEmail = email.senderEmail,
                    recipientEmail = email.recipientEmail,
                    timestamp = email.timestamp - 7200000L, // 2 hours prior if part of thread
                    isOutgoing = false,
                    body = email.body,
                    deliveryStatus = null,
                    attachments = emailAttachments
                )
            )

            // If there's an existing draft reply or simulated previous context
            if (!email.replyDraft.isNullOrBlank()) {
                list.add(
                    EmailThreadMessage(
                        id = "msg_${email.id}_out_prev",
                        senderName = "Aditya Rai",
                        senderEmail = "kumaradityarai0005@gmail.com",
                        recipientEmail = email.senderEmail,
                        timestamp = email.timestamp,
                        isOutgoing = true,
                        body = email.replyDraft,
                        deliveryStatus = MessageDeliveryStatus.OPENED,
                        attachments = emptyList()
                    )
                )
            }
        }
        list
    }

    // Trigger Smart Reply analysis if not yet loaded for this email
    LaunchedEffect(email.id) {
        viewModel.loadSmartRepliesForEmail(email)
    }

    // Attachment Preview Dialog
    selectedAttachmentForPreview?.let { attachment ->
        AttachmentPreviewDialog(
            attachment = attachment,
            onDismiss = { selectedAttachmentForPreview = null }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Light3dBorder) },
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Text3dSecondary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = { viewModel.toggleStarred(email) }) {
                        Icon(
                            imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star",
                            tint = if (email.isStarred) Amber3d else Text3dMuted
                        )
                    }
                    IconButton(onClick = {
                        viewModel.moveEmail(email.id, EmailFolder.ARCHIVE)
                        onDismiss()
                    }) {
                        Icon(Icons.Outlined.Archive, contentDescription = "Archive", tint = Text3dSecondary)
                    }
                    IconButton(onClick = {
                        viewModel.deleteEmail(email.id)
                        onDismiss()
                    }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = GmailCoral)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject with Thread Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = email.subject,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text3dPrimary,
                    lineHeight = 26.sp,
                    modifier = Modifier.weight(1f)
                )

                // Thread count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBlueLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${threadMessages.size} msgs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Executive Summary Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = ElectricBlue)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Gradient3dHero)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.25f), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "AI THREAD INTELLIGENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                letterSpacing = 1.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (email.aiSummary != null) {
                                OutlinedButton(
                                    onClick = { viewModel.summarizeEmailThreadWithGemini(email) },
                                    enabled = !isAnalyzing && !uiState.isSummarizingThread,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp).testTag("btn_refresh_summary")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Regenerate with Gemini",
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Regenerate", fontSize = 10.sp, color = ElectricBlue, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.summarizeEmailThreadWithGemini(email) },
                                    enabled = !isAnalyzing && !uiState.isSummarizingThread,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("btn_summarize_email")
                                ) {
                                    if (isAnalyzing || uiState.isSummarizingThread) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text("AI Summarize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (isAnalyzing || uiState.isSummarizingThread) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = ElectricBlue,
                                strokeWidth = 2.dp
                            )
                            Text(
                                "Gemini AI is analyzing email thread & extracting action items...",
                                fontSize = 12.sp,
                                color = ElectricBlue,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    } else if (email.aiSummary != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = email.aiSummary,
                            fontSize = 13.sp,
                            color = Text3dPrimary,
                            lineHeight = 18.sp
                        )

                        if (!email.aiActionItems.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Light3dBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Action Items for Aditya:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = ElectricBlue
                            )
                            Text(
                                text = email.aiActionItems,
                                fontSize = 12.sp,
                                color = Text3dSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Thread Messages Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CONVERSATION THREAD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Text3dSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Chronological • Threaded",
                    fontSize = 11.sp,
                    color = Text3dMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thread Messages Lazy List Rendering
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                threadMessages.forEachIndexed { index, msg ->
                    EmailThreadMessageCard(
                        message = msg,
                        dateFormat = dateFormat,
                        onAttachmentClick = { attachment ->
                            selectedAttachmentForPreview = attachment
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3D Smart Reply Section (Gemini AI Powered Context-Aware Quick Replies)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = ShadowAmbient,
                        spotColor = Purple3d
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(1.2.dp, Purple3d.copy(alpha = 0.3f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Purple3d)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Gradient3dPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "SMART REPLIES",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple3d,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Gemini context-aware instant reply drafts",
                                    fontSize = 10.sp,
                                    color = Text3dSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadSmartRepliesForEmail(email, forceRefresh = true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Regenerate smart replies",
                                tint = Purple3d,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isAnalyzingSmartReplies) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Purple3dLight)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Purple3d,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini is generating 3 smart replies...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Purple3dDark
                            )
                        }
                    } else {
                        // 3 Context-Aware Quick-Reply Buttons
                        val replies = if (uiState.smartReplies.isNotEmpty()) {
                            uiState.smartReplies
                        } else {
                            listOf(
                                SmartReplyOption("1", "Acknowledge", "acknowledge", "Hi ${email.senderName},\n\nThanks for reaching out! I've received your email and will review the details shortly.\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com"),
                                SmartReplyOption("2", "Request Meeting", "meeting", "Hi ${email.senderName},\n\nThanks for following up! Let's schedule a brief 15-minute call to discuss this. Are you available this Thursday afternoon?\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com"),
                                SmartReplyOption("3", "Decline", "decline", "Hi ${email.senderName},\n\nThank you for reaching out. We are focusing on active commitments and won't be able to proceed at this time.\n\nBest regards,\nAditya Rai\nkumaradityarai0005@gmail.com")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            replies.forEach { option ->
                                val isSelected = uiState.selectedSmartReply?.id == option.id || (uiState.selectedSmartReply == null && option == replies.firstOrNull())
                                val (icon, chipColor, textColor, borderColor) = getSmartReplyTheme(option.iconType, isSelected)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(
                                            elevation = if (isSelected) 3.dp else 1.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            spotColor = if (isSelected) Purple3d else ShadowAmbient
                                        )
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipColor)
                                        .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                        .clickable { viewModel.selectSmartReplyOption(option) }
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = option.label,
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = option.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Active Smart Reply Draft Preview Card
                        val currentDraft = email.replyDraft ?: uiState.selectedSmartReply?.fullDraft ?: replies.firstOrNull()?.fullDraft

                        if (!currentDraft.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = Purple3d)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Light3dCardSubtle)
                                    .border(1.dp, Purple3d.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Tailored Reply Draft Preview",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Purple3dDark
                                        )
                                        Text(
                                            text = "Ready to send or edit",
                                            fontSize = 10.sp,
                                            color = Text3dSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = currentDraft,
                                        fontSize = 12.sp,
                                        color = Text3dPrimary,
                                        lineHeight = 17.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Save as Draft button
                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.sendEmail(
                                                    to = email.senderEmail,
                                                    subject = if (email.subject.startsWith("Re:", ignoreCase = true)) email.subject else "Re: ${email.subject}",
                                                    body = currentDraft,
                                                    isDraft = true
                                                )
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color.White,
                                                contentColor = Text3dPrimary
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Save Draft", fontSize = 11.sp)
                                        }

                                        // Open in full Compose button
                                        FilledTonalButton(
                                            onClick = {
                                                viewModel.openComposeWithContent(
                                                    to = email.senderEmail,
                                                    subject = if (email.subject.startsWith("Re:", ignoreCase = true)) email.subject else "Re: ${email.subject}",
                                                    body = currentDraft
                                                )
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color.White,
                                                contentColor = Purple3d
                                            ),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Edit", fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.weight(1f))

                                        // Quick Send Reply button
                                        Button(
                                            onClick = {
                                                viewModel.sendEmail(
                                                    to = email.senderEmail,
                                                    subject = if (email.subject.startsWith("Re:", ignoreCase = true)) email.subject else "Re: ${email.subject}",
                                                    body = currentDraft,
                                                    isDraft = false
                                                )
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .height(34.dp)
                                                .testTag("btn_send_smart_reply")
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Send", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/**
 * Message Card in the Threaded View, visually distinguishing Incoming vs Outgoing messages
 */
@Composable
fun EmailThreadMessageCard(
    message: EmailThreadMessage,
    dateFormat: SimpleDateFormat,
    onAttachmentClick: (EmailAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatted = remember(message.timestamp) { dateFormat.format(Date(message.timestamp)) }
    val isOutgoing = message.isOutgoing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("thread_msg_${message.id}"),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Bubble Container with 3D styling
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isOutgoing) 0.94f else 1f)
                .shadow(
                    elevation = if (isOutgoing) 4.dp else 2.dp,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isOutgoing) 20.dp else 6.dp,
                        bottomEnd = if (isOutgoing) 6.dp else 20.dp
                    ),
                    ambientColor = if (isOutgoing) ShadowBlueGlow else ShadowAmbient,
                    spotColor = if (isOutgoing) ElectricBlue else ShadowSpot
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isOutgoing) 20.dp else 6.dp,
                        bottomEnd = if (isOutgoing) 6.dp else 20.dp
                    )
                )
                .background(
                    if (isOutgoing) Color(0xFFF0F7FF) // Outgoing light blue tint
                    else Color.White
                )
                .border(
                    width = 1.2.dp,
                    color = if (isOutgoing) ElectricBlue.copy(alpha = 0.35f) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isOutgoing) 20.dp else 6.dp,
                        bottomEnd = if (isOutgoing) 6.dp else 20.dp
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Header: Sender Avatar & Name + Outgoing/Incoming Badge + Delivery Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 3D Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = if (isOutgoing) ElectricBlue else ShadowSpot)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOutgoing) Gradient3dPrimary else Gradient3dDark)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = message.senderName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isOutgoing) "Aditya Rai (You)" else message.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Text3dPrimary
                                )

                                // Incoming / Outgoing pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isOutgoing) ElectricBlueLight else Color(0xFFF1F5F9))
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = if (isOutgoing) "Outgoing" else "Incoming",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOutgoing) ElectricBlue else Text3dSecondary
                                    )
                                }
                            }

                            Text(
                                text = message.senderEmail,
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )
                        }
                    }

                    // Timestamp & Delivery Status
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = timeFormatted,
                            fontSize = 10.sp,
                            color = Text3dMuted
                        )

                        // Outgoing Delivery Status Checkmarks (Sent, Delivered, Opened)
                        if (isOutgoing || message.deliveryStatus != null) {
                            DeliveryStatusIndicator(
                                status = message.deliveryStatus ?: MessageDeliveryStatus.OPENED,
                                showLabel = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = if (isOutgoing) ElectricBlue.copy(alpha = 0.15f) else Light3dBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Message Body
                Text(
                    text = message.body,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = Text3dPrimary
                )

                // Attachment Thumbnails / Files Rendering
                if (message.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Attachments (${message.attachments.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        message.attachments.forEach { attachment ->
                            AttachmentThumbnailChip(
                                attachment = attachment,
                                onClick = { onAttachmentClick(attachment) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns icon, background color, text color, and border color for a smart reply choice
 */
private fun getSmartReplyTheme(iconType: String, isSelected: Boolean): SmartReplyThemeConfig {
    return when (iconType.lowercase()) {
        "acknowledge", "ack" -> {
            SmartReplyThemeConfig(
                icon = Icons.Default.CheckCircle,
                backgroundColor = if (isSelected) Emerald3dLight else Color.White,
                textColor = if (isSelected) Color(0xFF065F46) else Text3dPrimary,
                borderColor = if (isSelected) Emerald3d else Light3dBorder
            )
        }
        "meeting", "meet", "schedule" -> {
            SmartReplyThemeConfig(
                icon = Icons.Default.Event,
                backgroundColor = if (isSelected) ElectricBlueLight else Color.White,
                textColor = if (isSelected) ElectricBlue else Text3dPrimary,
                borderColor = if (isSelected) ElectricBlue else Light3dBorder
            )
        }
        "decline", "reject", "pass" -> {
            SmartReplyThemeConfig(
                icon = Icons.Default.Cancel,
                backgroundColor = if (isSelected) GmailCoralLight else Color.White,
                textColor = if (isSelected) GmailCoral else Text3dPrimary,
                borderColor = if (isSelected) GmailCoral else Light3dBorder
            )
        }
        else -> {
            SmartReplyThemeConfig(
                icon = Icons.Default.ChatBubbleOutline,
                backgroundColor = if (isSelected) Purple3dLight else Color.White,
                textColor = if (isSelected) Purple3dDark else Text3dPrimary,
                borderColor = if (isSelected) Purple3d else Light3dBorder
            )
        }
    }
}

private data class SmartReplyThemeConfig(
    val icon: ImageVector,
    val backgroundColor: Color,
    val textColor: Color,
    val borderColor: Color
)
