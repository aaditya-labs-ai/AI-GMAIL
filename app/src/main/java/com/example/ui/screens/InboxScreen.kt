package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.emails.collectAsStateWithLifecycle()
    val authUserState by viewModel.authUserState.collectAsStateWithLifecycle()

    // Default selected email for Copilot presentation (e.g. Eleanor Vance or first unread)
    var selectedEmailId by remember { mutableStateOf<Long?>(null) }
    var isCopilotExpanded by remember { mutableStateOf(true) }

    // Synchronize initial selected email
    LaunchedEffect(emails) {
        if (selectedEmailId == null && emails.isNotEmpty()) {
            val defaultEleanor = emails.firstOrNull { it.senderName.contains("Eleanor", ignoreCase = true) }
            selectedEmailId = defaultEleanor?.id ?: emails.first().id
        }
    }

    val activeEmail = remember(emails, selectedEmailId) {
        emails.firstOrNull { it.id == selectedEmailId } ?: emails.firstOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Top Bar: Aura Mail Logo + Profile Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Aura Mail Brand Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { viewModel.setAccountDialogVisible(true) }
                ) {
                    // Official Mail Logo Emblem
                    Image(
                        painter = painterResource(id = R.drawable.mail_logo),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(32.dp),
                        contentScale = ContentScale.Fit
                    )

                    Column {
                        Text(
                            text = "Aura Mail",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = AuraDark
                        )
                    }
                }

                // Profile Avatar / Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { viewModel.setAccountDialogVisible(true) }
                        .padding(2.dp)
                        .testTag("btn_aura_profile")
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraBorderSelected)
                            .border(1.5.dp, AuraBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SJ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Search & User Filter Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AuraSearchBg)
                    .border(1.dp, AuraBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = AuraMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.searchQuery.isEmpty()) {
                                Text(
                                    text = "Search",
                                    fontSize = 14.sp,
                                    color = AuraMuted
                                )
                            }
                            androidx.compose.foundation.text.BasicTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 14.sp,
                                    color = AuraDark,
                                    fontFamily = FontFamily.SansSerif
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_aura_search")
                            )
                        }
                    }

                    // "Sarah J." Pill Filter on right of search
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AuraCard)
                            .border(1.dp, AuraBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setAccountDialogVisible(true) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (authUserState.displayName.isNotBlank()) authUserState.displayName else "Sarah J.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Section Title: Aura Inbox & Date
            Text(
                text = "Aura Inbox",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AuraDark,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Today, October 28, 2024",
                fontSize = 13.sp,
                color = AuraMuted,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Email Table / Grouped Card Container
            Surface(
                color = AuraCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AuraBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)) {
                    // Column Header Row: Checkbox, "From", "Date"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .border(1.5.dp, AuraMutedLight, RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "From",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Date",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraDark
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Email Rows
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(emails.take(5), key = { it.id }) { email ->
                            val isSelected = email.id == selectedEmailId

                            if (isSelected) {
                                // Selected Email Card (Highlighted in gold/amber as in photo)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AuraCardSelected)
                                        .border(1.5.dp, AuraBorderSelected, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedEmailId = email.id
                                            isCopilotExpanded = true
                                        }
                                        .padding(12.dp)
                                        .testTag("aura_email_item_selected")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Checked Checkbox
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 2.dp)
                                                .size(18.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AuraAccent)
                                                .clickable {
                                                    selectedEmailId = if (isSelected) null else email.id
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }

                                        // Circle Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(AuraBorderSelected),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = email.senderName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = AuraDark
                                            )
                                        }

                                        // Email Info
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = email.senderName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AuraDark
                                                )
                                                Text(
                                                    text = "11:04 AM",
                                                    fontSize = 12.sp,
                                                    color = AuraDarkSubtle,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Text(
                                                text = email.subject,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AuraDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = "AI Focus: High Impact",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AuraAccent
                                            )

                                            Text(
                                                text = if (email.snippet.startsWith("Summary:")) email.snippet else "Summary: ${email.snippet}",
                                                fontSize = 12.sp,
                                                color = AuraDarkSubtle,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Regular Unselected Email Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedEmailId = email.id
                                            isCopilotExpanded = true
                                        }
                                        .padding(horizontal = 6.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Unchecked Checkbox
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(18.dp)
                                            .border(1.5.dp, AuraMutedLight, RoundedCornerShape(4.dp))
                                    )

                                    // Circle Initial Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8A8275)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = email.senderName.firstOrNull()?.uppercase() ?: "J",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                    }

                                    // Content
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${email.senderName} - ${email.subject}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AuraDark,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "11:04 AM",
                                                fontSize = 12.sp,
                                                color = AuraDarkSubtle
                                            )
                                        }

                                        Text(
                                            text = if (email.snippet.startsWith("Summary:")) email.snippet else "Summary: ${email.snippet}",
                                            fontSize = 12.sp,
                                            color = AuraMuted,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // 5. Slide-up AI Email Copilot Bottom Sheet (matching exact photo layout)
        if (isCopilotExpanded && activeEmail != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        ambientColor = ShadowAmbient,
                        spotColor = ShadowSpot
                    ),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = AuraCard,
                border = BorderStroke(1.dp, AuraBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Drag Handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AuraMutedLight)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Copilot Header with Title & Close 'X'
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Email Copilot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = AuraDark
                        )

                        IconButton(
                            onClick = { isCopilotExpanded = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Copilot",
                                tint = AuraDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Contact Header Pill Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AuraBg,
                        border = BorderStroke(1.dp, AuraBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AuraBorderSelected),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeEmail.senderName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AuraDark
                                    )
                                }

                                Column {
                                    Text(
                                        text = activeEmail.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AuraDark
                                    )
                                    Text(
                                        text = activeEmail.senderName,
                                        fontSize = 12.sp,
                                        color = AuraMuted
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.selectEmail(activeEmail) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Options",
                                    tint = AuraDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Key Summary Section
                    Text(
                        text = "Key Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AuraDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeEmail.aiSummary ?: "Eleanor reviewed the proposal, highlighted budget alignment by Friday. Approved the design direction.",
                        fontSize = 13.sp,
                        color = AuraDark,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // AI Suggested Actions Section
                    Text(
                        text = "AI Suggested Actions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AuraDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Action 1: Aura Reply: Confirm Timelines
                        AuraActionButton(
                            icon = Icons.AutoMirrored.Filled.Reply,
                            label = "Aura Reply: Confirm Timelines",
                            onClick = {
                                viewModel.startCompose(
                                    to = activeEmail.senderEmail,
                                    subject = "Re: ${activeEmail.subject}",
                                    body = "Hi ${activeEmail.senderName},\n\nI've reviewed the timelines and confirmed that our milestones for next sprint are aligned. I will follow up with details tomorrow.\n\nBest,\nSarah J."
                                )
                            }
                        )

                        // Action 2: Reply: Address Budget
                        AuraActionButton(
                            icon = Icons.AutoMirrored.Filled.Reply,
                            label = "Reply: Address Budget",
                            onClick = {
                                viewModel.startCompose(
                                    to = activeEmail.senderEmail,
                                    subject = "Re: ${activeEmail.subject} - Budget Alignment",
                                    body = "Hi ${activeEmail.senderName},\n\nRegarding the budget questions highlighted, I have allocated the necessary resources for Q4 approval before Friday.\n\nBest,\nSarah J."
                                )
                            }
                        )

                        // Action 3: Create Task
                        AuraActionButton(
                            icon = Icons.Outlined.CheckCircleOutline,
                            label = "Create Task",
                            onClick = {
                                viewModel.executeQuickAction("Created Task: Review Q4 Budget & Timelines with ${activeEmail.senderName}")
                            }
                        )

                        // Action 4: Aura Draft: Timelines Clarification
                        AuraActionButton(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "Aura Draft: Timelines Clarification",
                            onClick = {
                                viewModel.startCompose(
                                    to = activeEmail.senderEmail,
                                    subject = "Timeline Clarification: ${activeEmail.subject}",
                                    body = "Hi ${activeEmail.senderName},\n\nHere is a detailed clarification on our deliverable schedule and Q4 dependencies.\n\nBest,\nSarah J."
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AuraActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = AuraCard,
        border = BorderStroke(1.dp, AuraBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraDark,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AuraDark
            )
        }
    }
}
