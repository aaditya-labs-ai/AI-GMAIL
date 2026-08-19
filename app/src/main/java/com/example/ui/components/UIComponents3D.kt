package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.data.model.EmailEntity
import com.example.data.model.EmailFolder
import com.example.data.model.EmailPriority
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
            // 3D Avatar / Icon Badge
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
                            email.tags.contains("Investor") -> Gradient3dPurple
                            else -> Gradient3dPrimary
                        }
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        email.priority == EmailPriority.HIGH -> Icons.Default.PriorityHigh
                        email.tags.contains("Investor") -> Icons.Default.Work
                        email.folder == EmailFolder.SENT -> Icons.Default.Send
                        else -> Icons.Default.Mail
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
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
                        color = Text3dPrimary
                    )
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (!email.isRead) ElectricBlue else Text3dMuted
                    )
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
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (email.priority == EmailPriority.HIGH) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GmailCoralLight)
                                    .border(1.dp, GmailCoral.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "HIGH PRIORITY",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GmailCoral
                                )
                            }
                        }

                        if (email.aiSummary != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ElectricBlueLight)
                                    .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "AI Summarized",
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
