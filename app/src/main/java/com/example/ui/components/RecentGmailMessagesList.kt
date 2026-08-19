package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmailCategory
import com.example.data.model.EmailEntity
import com.example.data.model.EmailPriority
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * A clean, polished Jetpack Compose component that displays a list of recent Gmail messages
 * using a LazyColumn, including the sender name, subject line, and a preview of the email body.
 */
@Composable
fun RecentGmailMessagesList(
    emails: List<EmailEntity>,
    onEmailClick: (EmailEntity) -> Unit,
    onToggleStar: (EmailEntity) -> Unit = {},
    onArchiveEmail: (EmailEntity) -> Unit = {},
    onDeleteEmail: (EmailEntity) -> Unit = {},
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    emptyContent: @Composable () -> Unit = { DefaultEmptyRecentEmailsView() }
) {
    if (emails.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            emptyContent()
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .testTag("recent_gmail_messages_list"),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = emails,
                key = { it.id }
            ) { email ->
                GmailMessageCard(
                    email = email,
                    onClick = { onEmailClick(email) },
                    onToggleStar = { onToggleStar(email) },
                    onArchive = { onArchiveEmail(email) },
                    onDelete = { onDeleteEmail(email) }
                )
            }
        }
    }
}

/**
 * Individual Card representing a Gmail Message with Sender, Subject, Body Preview, and Actions.
 */
@Composable
fun GmailMessageCard(
    email: EmailEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(email.timestamp) { dateFormat.format(Date(email.timestamp)) }
    val initials = remember(email.senderName) {
        val parts = email.senderName.trim().split(" ")
        if (parts.size >= 2) "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        else email.senderName.take(2).uppercase()
    }

    // Dynamic avatar gradient based on sender name hash
    val avatarGradients = listOf(Gradient3dPrimary, Gradient3dPurple, Gradient3dCoral)
    val avatarGradient = remember(email.senderName) {
        val index = kotlin.math.abs(email.senderName.hashCode()) % avatarGradients.size
        avatarGradients[index]
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (!email.isRead) 5.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (!email.isRead) ShadowBlueGlow else ShadowAmbient,
                spotColor = if (!email.isRead) ElectricBlue else ShadowSpot
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = 1.2.dp,
                brush = if (!email.isRead) {
                    Brush.verticalGradient(listOf(Color.White, ElectricBlue.copy(alpha = 0.35f)))
                } else {
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFE5E9F0)))
                },
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("gmail_message_card_${email.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3D Avatar with Sender Initials
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        spotColor = if (!email.isRead) ElectricBlue else Color.Gray
                    )
                    .clip(CircleShape)
                    .background(avatarGradient)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Main Content: Sender Name, Subject, and Body Preview
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header Row: Sender Name + Formatted Timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = email.senderName,
                            fontWeight = if (!email.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Text3dPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Unread Status Dot Indicator
                        if (!email.isRead) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ElectricBlue)
                            )
                        }
                    }

                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Normal,
                        color = if (!email.isRead) ElectricBlue else Text3dSecondary
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Subject Line
                Text(
                    text = email.subject,
                    fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = if (!email.isRead) Text3dPrimary else Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email Body Preview (2-line clamp)
                Text(
                    text = email.snippet.ifBlank { email.body },
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = Text3dSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Metadata Badges & Quick Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Priority / AI Category Tag
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (email.priority) {
                            EmailPriority.HIGH -> {
                                PriorityBadge(
                                    label = "High Priority",
                                    color = GmailCoral,
                                    bgColor = GmailCoralLight
                                )
                            }
                            EmailPriority.NORMAL -> {
                                PriorityBadge(
                                    label = "Normal",
                                    color = ElectricBlue,
                                    bgColor = Light3dCardSubtle
                                )
                            }
                            EmailPriority.LOW -> {
                                PriorityBadge(
                                    label = "Low",
                                    color = Text3dSecondary,
                                    bgColor = Light3dCardSubtle
                                )
                            }
                        }

                        if (email.category != EmailCategory.PRIMARY) {
                            val categoryLabel = when (email.category) {
                                EmailCategory.UPDATES -> "Updates"
                                EmailCategory.PROMOTIONS -> "Promotions"
                                EmailCategory.SOCIAL -> "Social"
                                EmailCategory.PRIMARY -> "Primary"
                            }
                            PriorityBadge(
                                label = categoryLabel,
                                color = Purple3d,
                                bgColor = Purple3dLight
                            )
                        }
                    }

                    // Action Icons (Star & Quick Archive)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onToggleStar,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_star_email_${email.id}")
                        ) {
                            Icon(
                                imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (email.isStarred) "Starred" else "Not Starred",
                                tint = if (email.isStarred) Amber3d else Text3dSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_archive_email_${email.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Archive,
                                contentDescription = "Archive Email",
                                tint = Text3dSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Polished 3D Pill Badge for Priority & Categories
 */
@Composable
fun PriorityBadge(
    label: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(0.8.dp, color.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Clean Empty State Illustration View
 */
@Composable
fun DefaultEmptyRecentEmailsView(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape, spotColor = ElectricBlue)
                .clip(CircleShape)
                .background(Gradient3dPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MarkEmailRead,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Inbox Zero",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Text3dPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "All recent Gmail messages have been processed and triaged.",
            fontSize = 13.sp,
            color = Text3dSecondary,
            lineHeight = 18.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
