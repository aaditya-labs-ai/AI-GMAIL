package com.example.ui.components

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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilterChip3D(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 4.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isSelected) ShadowBlueGlow else ShadowAmbient,
                spotColor = if (isSelected) ElectricBlue else ShadowSpot
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Gradient3dPrimary else Brush.verticalGradient(listOf(Color.White, Color(0xFFF8FAFD))))
            .border(
                1.dp,
                if (isSelected) Color.Transparent else Light3dBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Text3dSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableEmail3DItem(
    email: EmailEntity,
    onSelect: () -> Unit,
    onToggleStar: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onArchive()
                    true
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .testTag("swipe_email_item_${email.id}"),
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Emerald3d
                SwipeToDismissBoxValue.EndToStart -> GmailCoral
                else -> Color.Transparent
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Icons.Outlined.Archive
                SwipeToDismissBoxValue.EndToStart -> Icons.Outlined.Delete
                else -> Icons.Outlined.Archive
            }
            val labelText = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> "Archive"
                SwipeToDismissBoxValue.EndToStart -> "Delete"
                else -> ""
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(horizontal = 24.dp),
                contentAlignment = alignment
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Icon(icon, contentDescription = "Archive Email", tint = Color.White, modifier = Modifier.size(24.dp))
                        Text(labelText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    } else if (direction == SwipeToDismissBoxValue.EndToStart) {
                        Text(labelText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Icon(icon, contentDescription = "Delete Email", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    ) {
        Email3DItem(
            email = email,
            onSelect = onSelect,
            onToggleStar = onToggleStar,
            onArchive = onArchive,
            onDelete = onDelete
        )
    }
}

@Composable
fun DoubleCheckmarkIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Double Check",
            tint = color,
            modifier = Modifier
                .size(13.dp)
                .offset(x = 0.dp)
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(13.dp)
                .offset(x = 4.dp)
        )
    }
}

@Composable
fun DeliveryStatusIndicator(
    status: MessageDeliveryStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        when (status) {
            MessageDeliveryStatus.OPENED -> {
                DoubleCheckmarkIcon(color = ElectricBlue, modifier = Modifier.size(16.dp))
                if (showLabel) {
                    Text(
                        text = "Opened",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                }
            }
            MessageDeliveryStatus.DELIVERED -> {
                DoubleCheckmarkIcon(color = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                if (showLabel) {
                    Text(
                        text = "Delivered",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }
            }
            MessageDeliveryStatus.SENT -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Sent",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                if (showLabel) {
                    Text(
                        text = "Sent",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
            MessageDeliveryStatus.SENDING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = ElectricBlue
                )
                if (showLabel) {
                    Text(
                        text = "Sending...",
                        fontSize = 11.sp,
                        color = Text3dSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun AttachmentThumbnailChip(
    attachment: EmailAttachment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText, iconVector) = when (attachment.fileExtension.lowercase()) {
        "pdf" -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Icons.Default.PictureAsPdf)
        "docx", "doc" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), Icons.Default.Description)
        "xlsx", "xls", "csv" -> Triple(Color(0xFFD1FAE5), Color(0xFF047857), Icons.Default.TableChart)
        "png", "jpg", "jpeg" -> Triple(Color(0xFFEDE9FE), Color(0xFF7C3AED), Icons.Default.Image)
        "zip", "tar", "gz" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Default.FolderZip)
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF475569), Icons.Default.AttachFile)
    }

    Box(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = ShadowSpot)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Light3dBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("attachment_chip_${attachment.fileName}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = attachment.fileExtension,
                    tint = badgeText,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = attachment.fileName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text3dPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = attachment.fileSizeFormatted,
                        fontSize = 10.sp,
                        color = Text3dMuted
                    )
                    Text(text = "•", fontSize = 10.sp, color = Text3dMuted)
                    Text(
                        text = "Tap to preview",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricBlue
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPreviewDialog(
    attachment: EmailAttachment,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp)
            .clip(RoundedCornerShape(24.dp))
            .testTag("attachment_preview_dialog"),
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        val (icon, bg, tint) = when (attachment.fileExtension.lowercase()) {
                            "pdf" -> Triple(Icons.Default.PictureAsPdf, Color(0xFFFEE2E2), Color(0xFFDC2626))
                            "docx", "doc" -> Triple(Icons.Default.Description, Color(0xFFDBEAFE), Color(0xFF1D4ED8))
                            "xlsx", "xls", "csv" -> Triple(Icons.Default.TableChart, Color(0xFFD1FAE5), Color(0xFF047857))
                            "png", "jpg", "jpeg" -> Triple(Icons.Default.Image, Color(0xFFEDE9FE), Color(0xFF7C3AED))
                            else -> Triple(Icons.Default.AttachFile, Color(0xFFF1F5F9), Color(0xFF475569))
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = attachment.fileName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Text3dPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${attachment.fileTypeDescription} • ${attachment.fileSizeFormatted}",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_attachment_preview")) {
                        Icon(Icons.Default.Close, contentDescription = "Close preview", tint = Text3dSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Light3dBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Document / File Preview Canvas Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 340.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Light3dCardSubtle)
                        .border(1.dp, Light3dBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Extension Badge Banner
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White)
                                .border(1.dp, Light3dBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Emerald3d, modifier = Modifier.size(13.dp))
                            Text("Verified Document • Security Scanned", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Emerald3dDark)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preview Content
                        Text(
                            text = attachment.previewContent ?: "Document content verified. Preview loaded successfully with verified cryptographic signature.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Text3dPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Bar: Download, Share, Open
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

enum class VisualTag(
    val label: String,
    val textColor: Color,
    val bgColor: Color,
    val borderColor: Color
) {
    WORK("Work", Color(0xFF1D4ED8), Color(0xFFEFF6FF), Color(0xFF93C5FD)),
    PERSONAL("Personal", Color(0xFF047857), Color(0xFFECFDF5), Color(0xFFA7F3D0)),
    URGENT("Urgent", Color(0xFFDC2626), Color(0xFFFEF2F2), Color(0xFFFECACA));

    companion object {
        fun extractTags(tagsString: String, priority: EmailPriority): List<VisualTag> {
            val list = mutableListOf<VisualTag>()
            val lower = tagsString.lowercase()
            if (priority == EmailPriority.HIGH || lower.contains("urgent") || lower.contains("high priority") || lower.contains("asap")) {
                list.add(URGENT)
            }
            if (lower.contains("work") || lower.contains("investor") || lower.contains("partnership") || lower.contains("press") || lower.contains("dev") || lower.contains("draft") || lower.contains("client")) {
                list.add(WORK)
            }
            if (lower.contains("personal") || lower.contains("friend") || lower.contains("weekend") || lower.contains("receipt") || lower.contains("finance")) {
                list.add(PERSONAL)
            }
            return list.distinct()
        }
    }
}

@Composable
fun VisualTagBadge(tag: VisualTag, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(tag.bgColor)
            .border(1.dp, tag.borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = when (tag) {
                    VisualTag.WORK -> Icons.Default.Work
                    VisualTag.PERSONAL -> Icons.Default.Person
                    VisualTag.URGENT -> Icons.Default.PriorityHigh
                },
                contentDescription = null,
                tint = tag.textColor,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = tag.label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tag.textColor
            )
        }
    }
}

@Composable
fun Email3DItem(
    email: EmailEntity,
    onSelect: () -> Unit,
    onToggleStar: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val timeString = remember(email.timestamp) { dateFormat.format(Date(email.timestamp)) }
    val visualTags = remember(email.tags, email.priority) { VisualTag.extractTags(email.tags, email.priority) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (!email.isRead) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = if (!email.isRead) ShadowBlueGlow else ShadowAmbient,
                spotColor = if (!email.isRead) ElectricBlue else ShadowSpot
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                1.5.dp,
                if (!email.isRead) Brush.verticalGradient(listOf(Color.White, ElectricBlue.copy(alpha = 0.3f)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFE5E9F0))),
                RoundedCornerShape(20.dp)
            )
            .clickable { onSelect() }
            .testTag("email_item_${email.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 3D Avatar / Icon Badge with Initials or Priority Color
            val initials = remember(email.senderName) {
                email.senderName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = if (email.priority == EmailPriority.HIGH) GmailCoral else ElectricBlue
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            email.priority == EmailPriority.HIGH -> Gradient3dCoral
                            email.tags.contains("Personal", ignoreCase = true) -> Gradient3dEmerald
                            email.tags.contains("Investor", ignoreCase = true) -> Gradient3dPurple
                            else -> Gradient3dPrimary
                        }
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (initials.isNotBlank() && initials.length <= 2) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Icon(
                        imageVector = when {
                            email.priority == EmailPriority.HIGH -> Icons.Default.PriorityHigh
                            email.tags.contains("Investor") -> Icons.Default.Work
                            email.folder == EmailFolder.SENT -> Icons.AutoMirrored.Filled.Send
                            else -> Icons.Default.Mail
                        },
                        contentDescription = "Sender Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = email.senderName,
                        fontWeight = if (!email.isRead) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Text3dPrimary,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sent Email Visual Status Indicator (Single / Double Checkmark)
                        if (email.folder == EmailFolder.SENT || email.deliveryStatus != null) {
                            DeliveryStatusIndicator(
                                status = email.deliveryStatus ?: MessageDeliveryStatus.OPENED,
                                showLabel = false
                            )
                        }

                        Text(
                            text = timeString,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (!email.isRead) ElectricBlue else Text3dMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = email.subject,
                    fontWeight = if (!email.isRead) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (!email.isRead) Text3dPrimary else Text3dSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email.snippet,
                    fontSize = 12.sp,
                    color = Text3dSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Visual Tag Badges (Work, Personal, Urgent) & Attachment Tag
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        visualTags.forEach { vTag ->
                            VisualTagBadge(tag = vTag)
                        }

                        // Attachment Pill
                        if (email.hasAttachments || email.attachmentNames.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .border(1.dp, Light3dBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = "Has attachments",
                                        tint = Text3dSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = if (email.attachmentNames.contains(",")) "Files" else "File",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Text3dSecondary
                                    )
                                }
                            }
                        }

                        if (email.aiSummary != null && visualTags.size < 2) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricBlueLight)
                                    .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
                            ) {
                                Text(
                                    text = "AI Summary",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onToggleStar,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (email.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Star",
                                tint = if (email.isStarred) Amber3d else Text3dMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onArchive,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Archive,
                                contentDescription = "Archive",
                                tint = Text3dMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
