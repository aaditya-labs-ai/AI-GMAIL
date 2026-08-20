package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmailEntity
import com.example.ui.theme.*

/**
 * Summary stats card visualizing email distribution across categories ('Work', 'Personal', 'Urgent')
 * using a D3-styled bar chart canvas.
 */
@Composable
fun InboxCategoryStatsCard(
    emails: List<EmailEntity>,
    selectedFilter: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = emails.size.coerceAtLeast(1)
    
    val workCount = remember(emails) {
        emails.count { email ->
            email.tags.contains("Work", ignoreCase = true) ||
            email.category.name.equals("WORK", ignoreCase = true) ||
            email.subject.contains("sprint", ignoreCase = true) ||
            email.subject.contains("roadmap", ignoreCase = true) ||
            email.subject.contains("meeting", ignoreCase = true) ||
            email.senderName.contains("Sarah", ignoreCase = true) ||
            email.senderName.contains("Elena", ignoreCase = true) ||
            email.senderName.contains("Marcus", ignoreCase = true)
        }
    }

    val urgentCount = remember(emails) {
        emails.count { email ->
            email.tags.contains("Urgent", ignoreCase = true) ||
            email.tags.contains("Action Required", ignoreCase = true) ||
            email.priority.name.equals("HIGH", ignoreCase = true) ||
            email.priority.name.equals("URGENT", ignoreCase = true) ||
            email.subject.contains("urgent", ignoreCase = true) ||
            email.subject.contains("critical", ignoreCase = true)
        }
    }

    val personalCount = remember(emails) {
        emails.count { email ->
            email.tags.contains("Personal", ignoreCase = true) ||
            email.tags.contains("Community", ignoreCase = true) ||
            email.category.name.equals("PERSONAL", ignoreCase = true) ||
            email.category.name.equals("SOCIAL", ignoreCase = true) ||
            (!email.tags.contains("Work", ignoreCase = true) && !email.tags.contains("Urgent", ignoreCase = true) && !email.subject.contains("sprint", ignoreCase = true))
        }
    }

    val maxCategoryCount = remember(workCount, personalCount, urgentCount) {
        maxOf(workCount, personalCount, urgentCount, 1).toFloat()
    }

    // Animated bar fractions for fluid D3-like rendering
    val workAnimFraction by animateFloatAsState(
        targetValue = (workCount / maxCategoryCount).coerceIn(0.08f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "work_bar_anim"
    )
    val personalAnimFraction by animateFloatAsState(
        targetValue = (personalCount / maxCategoryCount).coerceIn(0.08f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "personal_bar_anim"
    )
    val urgentAnimFraction by animateFloatAsState(
        targetValue = (urgentCount / maxCategoryCount).coerceIn(0.08f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "urgent_bar_anim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = ShadowAmbient,
                spotColor = ShadowSpot
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(
                1.2.dp,
                Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))),
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
            .testTag("card_category_stats")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Category Stats Chart",
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CATEGORY INTELLIGENCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ElectricBlue,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Email Volume by Classification",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Text3dPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Total: ${emails.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D3 Bar Chart Canvas & Interactive Metric Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Work Bar
                CategoryStatBarRow(
                    label = "Work",
                    count = workCount,
                    total = totalCount,
                    fraction = workAnimFraction,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF60A5FA))),
                    icon = Icons.Default.BusinessCenter,
                    color = ElectricBlue,
                    isSelected = selectedFilter == "Work",
                    onClick = {
                        if (selectedFilter == "Work") onCategorySelected(null)
                        else onCategorySelected("Work")
                    },
                    testTag = "stat_bar_work"
                )

                // 2. Personal Bar
                CategoryStatBarRow(
                    label = "Personal",
                    count = personalCount,
                    total = totalCount,
                    fraction = personalAnimFraction,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF34D399))),
                    icon = Icons.Default.Person,
                    color = Emerald3d,
                    isSelected = selectedFilter == "Personal",
                    onClick = {
                        if (selectedFilter == "Personal") onCategorySelected(null)
                        else onCategorySelected("Personal")
                    },
                    testTag = "stat_bar_personal"
                )

                // 3. Urgent Bar
                CategoryStatBarRow(
                    label = "Urgent",
                    count = urgentCount,
                    total = totalCount,
                    fraction = urgentAnimFraction,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFFDC2626), Color(0xFFF87171))),
                    icon = Icons.Default.PriorityHigh,
                    color = GmailCoral,
                    isSelected = selectedFilter == "Urgent",
                    onClick = {
                        if (selectedFilter == "Urgent") onCategorySelected(null)
                        else onCategorySelected("Urgent")
                    },
                    testTag = "stat_bar_urgent"
                )
            }
        }
    }
}

@Composable
private fun CategoryStatBarRow(
    label: String,
    count: Int,
    total: Int,
    fraction: Float,
    barBrush: Brush,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val percentage = if (total > 0) ((count.toFloat() / total) * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Text3dPrimary
                )
                if (isSelected) {
                    Text(
                        text = "• Filtered",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$count emails",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text3dPrimary
                )
                Text(
                    text = "($percentage%)",
                    fontSize = 10.sp,
                    color = Text3dMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // D3 Bar Track & Fill Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            val width = size.width
            val height = size.height

            // Background Track
            drawRoundRect(
                color = Color(0xFFF1F5F9),
                size = Size(width, height),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Animated Bar Value
            val barWidth = (width * fraction).coerceAtLeast(14f)
            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(0f, 0f),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(10f, 10f)
            )
        }
    }
}
