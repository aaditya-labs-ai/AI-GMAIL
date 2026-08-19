package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmailEntity
import com.example.data.model.EmailFolder
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Light3dBorder) },
        modifier = Modifier.fillMaxHeight(0.92f)
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

            // Subject
            Text(
                text = email.subject,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Text3dPrimary,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sender Details with 3D Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = ElectricBlue)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Gradient3dPrimary)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = email.senderName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = email.senderName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Text3dPrimary
                    )
                    Text(
                        text = "From: ${email.senderEmail}",
                        fontSize = 12.sp,
                        color = Text3dSecondary
                    )
                    Text(
                        text = "To: Aditya Rai <${email.recipientEmail}>",
                        fontSize = 11.sp,
                        color = Text3dMuted
                    )
                }

                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = Text3dSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3D AI Executive Summary Card
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
                        Text(
                            text = "AI EXECUTIVE SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )

                        if (email.aiSummary == null) {
                            Button(
                                onClick = { viewModel.summarizeSelectedEmail() },
                                enabled = !isAnalyzing,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_summarize_email")
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Summarize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (email.aiSummary != null) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // 3D Email Body Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(20.dp), ambientColor = ShadowAmbient)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = email.body,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = Text3dPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3D Instant Smart Replies
            Text(
                text = "SMART REPLIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Text3dSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.generateSmartReplyForSelected("Accept meeting and propose Thursday 2:00 PM") },
                    label = { Text("Accept Meet", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(14.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Light3dCardSubtle,
                        labelColor = Text3dPrimary
                    ),
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { viewModel.generateSmartReplyForSelected("Polite executive decline") },
                    label = { Text("Decline", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(14.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Light3dCardSubtle,
                        labelColor = Text3dPrimary
                    ),
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { viewModel.generateSmartReplyForSelected("Request technical specs") },
                    label = { Text("Request Specs", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(14.dp),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Light3dCardSubtle,
                        labelColor = Text3dPrimary
                    ),
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.weight(1f)
                )
            }

            // Generated Reply Draft Card
            AnimatedVisibility(visible = email.replyDraft != null) {
                email.replyDraft?.let { draft ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Purple3d)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .border(1.5.dp, Purple3d.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Generated Reply Draft",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Purple3d
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = draft,
                                    fontSize = 13.sp,
                                    color = Text3dPrimary,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            viewModel.sendEmail(
                                                to = email.senderEmail,
                                                subject = "Re: ${email.subject}",
                                                body = draft,
                                                isDraft = true
                                            )
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Light3dCardSubtle,
                                            contentColor = Text3dPrimary
                                        ),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("Save Draft", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.sendEmail(
                                                to = email.senderEmail,
                                                subject = "Re: ${email.subject}",
                                                body = draft,
                                                isDraft = false
                                            )
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White)
                                    ) {
                                        Text("Send Reply", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
