package com.example.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationPreferences
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: AssistantViewModel,
    preferences: NotificationPreferences,
    onDismiss: () -> Unit
) {
    var notifyUrgent by remember { mutableStateOf(preferences.notifyUrgent) }
    var notifyWork by remember { mutableStateOf(preferences.notifyWork) }
    var notifyPersonal by remember { mutableStateOf(preferences.notifyPersonal) }
    var notifyInvestors by remember { mutableStateOf(preferences.notifyInvestors) }
    var notifyPromotions by remember { mutableStateOf(preferences.notifyPromotions) }
    var notifySocial by remember { mutableStateOf(preferences.notifySocial) }
    var quietHoursEnabled by remember { mutableStateOf(preferences.quietHoursEnabled) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(48.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFCBD5E1))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .testTag("dialog_settings")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = ElectricBlue)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Gradient3dPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Notification Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Text3dPrimary
                        )
                        Text(
                            text = "Filter alerts by label to reduce clutter",
                            fontSize = 12.sp,
                            color = Text3dSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_close_settings")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Text3dMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Notification Assistant Callout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElectricBlueLight)
                    .border(1.dp, ElectricBlue.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Customize push alerts per category. Only enabled labels will trigger sound and notification popups.",
                        fontSize = 12.sp,
                        color = ElectricBlue,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "EMAIL LABELS & CHANNELS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Text3dSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Label Switches Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = ShadowAmbient)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Light3dCardSubtle)
                    .border(1.dp, Light3dBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Urgent Label Toggle
                NotificationToggleItem(
                    title = "Urgent Emails",
                    subtitle = "Action required, VIP senders, time-sensitive updates",
                    icon = Icons.Default.PriorityHigh,
                    iconTint = GmailCoral,
                    isChecked = notifyUrgent,
                    onCheckedChange = { notifyUrgent = it },
                    tag = "toggle_notify_urgent"
                )

                HorizontalDivider(color = Light3dBorder)

                // Work Label Toggle
                NotificationToggleItem(
                    title = "Work & Corporate",
                    subtitle = "Team syncs, roadmaps, sprint tickets, contracts",
                    icon = Icons.Default.BusinessCenter,
                    iconTint = ElectricBlue,
                    isChecked = notifyWork,
                    onCheckedChange = { notifyWork = it },
                    tag = "toggle_notify_work"
                )

                HorizontalDivider(color = Light3dBorder)

                // Personal Label Toggle
                NotificationToggleItem(
                    title = "Personal & Social",
                    subtitle = "Family, friends, community threads",
                    icon = Icons.Default.Person,
                    iconTint = Emerald3d,
                    isChecked = notifyPersonal,
                    onCheckedChange = { notifyPersonal = it },
                    tag = "toggle_notify_personal"
                )

                HorizontalDivider(color = Light3dBorder)

                // Investors / Opportunities Toggle
                NotificationToggleItem(
                    title = "Investors & Outreach",
                    subtitle = "Venture term sheets, pitch updates, portfolio news",
                    icon = Icons.Default.MonetizationOn,
                    iconTint = Purple3d,
                    isChecked = notifyInvestors,
                    onCheckedChange = { notifyInvestors = it },
                    tag = "toggle_notify_investors"
                )

                HorizontalDivider(color = Light3dBorder)

                // Promotions & Newsletters Toggle
                NotificationToggleItem(
                    title = "Promotions & Marketing",
                    subtitle = "Discounts, deals, product marketing digest",
                    icon = Icons.Default.LocalOffer,
                    iconTint = Amber3d,
                    isChecked = notifyPromotions,
                    onCheckedChange = { notifyPromotions = it },
                    tag = "toggle_notify_promotions"
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "QUIET HOURS & FOCUS",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Text3dSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quiet hours card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(20.dp), ambientColor = ShadowAmbient)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Light3dCardSubtle)
                    .border(1.dp, Light3dBorder, RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (quietHoursEnabled) Purple3dLight else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bedtime,
                                contentDescription = null,
                                tint = if (quietHoursEnabled) Purple3dDark else Text3dMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Quiet Hours (10:00 PM – 7:00 AM)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Text3dPrimary
                            )
                            Text(
                                text = "Mute non-urgent notifications during sleep",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )
                        }
                    }

                    Switch(
                        checked = quietHoursEnabled,
                        onCheckedChange = { quietHoursEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Purple3d
                        ),
                        modifier = Modifier.testTag("toggle_quiet_hours")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Apply Changes Button
            Button(
                onClick = {
                    val updated = preferences.copy(
                        notifyUrgent = notifyUrgent,
                        notifyWork = notifyWork,
                        notifyPersonal = notifyPersonal,
                        notifyInvestors = notifyInvestors,
                        notifyPromotions = notifyPromotions,
                        notifySocial = notifySocial,
                        quietHoursEnabled = quietHoursEnabled
                    )
                    viewModel.updateNotificationPreferences(updated)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = ElectricBlue)
                    .testTag("btn_save_notification_settings"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Preferences & Sync to Cloud", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun NotificationToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Text3dPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Text3dSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iconTint
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
