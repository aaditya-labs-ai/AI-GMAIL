package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantScreen
import com.example.ui.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    val emails by viewModel.emails.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val authUserState by viewModel.authUserState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Light3dBackground)
    ) {
        // 3D Elevated Top Bar
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    ambientColor = ShadowAmbient,
                    spotColor = ShadowSpot
                ),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .clickable { viewModel.setAccountDialogVisible(true) }
                            .testTag("btn_profile_auth_header")
                    ) {
                        // 3D Avatar Container with Google status ring
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = CircleShape,
                                    ambientColor = ShadowBlueGlow,
                                    spotColor = ElectricBlue
                                )
                                .clip(CircleShape)
                                .background(Gradient3dPrimary)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (authUserState.displayName.isNotBlank()) {
                                    authUserState.displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                                } else "AR",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gmail Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Text3dPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Emerald3dLight)
                                        .border(1.dp, Emerald3d.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Google Auth",
                                        color = Color(0xFF065F46),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "${authUserState.email} • $unreadCount unread",
                                fontSize = 12.sp,
                                color = Text3dSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 3D Settings Button (Notification Preferences)
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape,
                                    ambientColor = ShadowAmbient,
                                    spotColor = ShadowSpot
                                )
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Light3dBorder, CircleShape)
                        ) {
                            IconButton(
                                onClick = { viewModel.setSettingsOpen(true) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("btn_settings_open")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Notification Settings",
                                    tint = Text3dSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // 3D Google Account / Security Button
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 4.dp,
                                    shape = CircleShape,
                                    ambientColor = ShadowAmbient,
                                    spotColor = ShadowSpot
                                )
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Light3dBorder, CircleShape)
                        ) {
                            IconButton(
                                onClick = { viewModel.setAccountDialogVisible(true) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("btn_auth_dialog_open")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Google Account Auth",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Tactile Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = ShadowAmbient,
                            spotColor = ShadowSpot
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF8FAFD))
                        .border(1.dp, Light3dBorder, RoundedCornerShape(18.dp))
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search emails, senders, action items...", fontSize = 13.sp, color = Text3dMuted) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = ElectricBlue
                            )
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Text3dMuted)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Text3dPrimary,
                            unfocusedTextColor = Text3dPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("input_search_emails")
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 3D Google Auth Status Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = ShadowAmbient,
                            spotColor = ShadowSpot
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(20.dp))
                        .clickable { viewModel.setAccountDialogVisible(true) }
                        .padding(14.dp)
                ) {
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
                                    .size(36.dp)
                                    .shadow(3.dp, CircleShape, spotColor = Emerald3d)
                                    .clip(CircleShape)
                                    .background(Emerald3dLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Emerald3d,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Firebase & Google Auth",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Text3dPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald3d
                                    )
                                }
                                Text(
                                    text = "Signed in as ${authUserState.email}",
                                    fontSize = 11.sp,
                                    color = Text3dSecondary
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Manage Auth",
                            tint = Text3dMuted
                        )
                    }
                }
            }

            // D3-based Category Breakdown Bar Chart Stats Card
            item {
                InboxCategoryStatsCard(
                    emails = emails,
                    selectedFilter = uiState.selectedTagFilter,
                    onCategorySelected = { category ->
                        viewModel.setSelectedTagFilter(category)
                    }
                )
            }

            // 3D Hero Insight Card (Claymorphic / Dimensional)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = ShadowBlueGlow,
                            spotColor = ElectricBlue
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Gradient3dHero)
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(listOf(Color.White, Color(0xFFD8E6F8))),
                            RoundedCornerShape(26.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Emerald3d)
                                )
                                Text(
                                    text = "AI EXECUTIVE COPILOT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    letterSpacing = 1.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Purple3d)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Purple3dLight)
                                    .border(1.dp, Purple3d.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "⚡ Real-time Triage",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple3dDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (unreadCount > 0) "You have $unreadCount high-priority threads ready for review." else "All high-priority threads resolved.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Text3dPrimary,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "AI has prioritized inbound opportunities and primed cold outreach sequences across connected accounts.",
                            fontSize = 13.sp,
                            color = Text3dSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3D Raised Action Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = ShadowBlueGlow,
                                    spotColor = ElectricBlue
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(Gradient3dPrimary)
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.triggerAutomationEngine() },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Triage & Execute All",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3D Quick-Action Grid Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Write Cold Mail 3D Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(135.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = ShadowAmbient,
                                spotColor = ShadowSpot
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(22.dp))
                            .clickable { viewModel.setScreen(AssistantScreen.ColdMailStudio) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = Purple3d)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gradient3dPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.EditNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Cold Mail Studio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Text3dPrimary
                                )
                                Text(
                                    text = "Targeted Outbound",
                                    fontSize = 11.sp,
                                    color = Text3dSecondary
                                )
                            }
                        }
                    }

                    // Social Sync 3D Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(135.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = ShadowAmbient,
                                spotColor = ShadowSpot
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(22.dp))
                            .clickable { viewModel.setScreen(AssistantScreen.SocialHub) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .shadow(4.dp, RoundedCornerShape(12.dp), spotColor = ElectricBlue)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Gradient3dPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Hub,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Social Hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Text3dPrimary
                                )
                                Text(
                                    text = "LinkedIn & X Synced",
                                    fontSize = 11.sp,
                                    color = Text3dSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3D Visual Tag & Folder Filter Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Tag Category Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "CATEGORIES & TAGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Text3dMuted,
                            letterSpacing = 0.5.sp
                        )
                        if (uiState.selectedTagFilter != null) {
                            Text(
                                text = "Clear Tag Filter",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue,
                                modifier = Modifier
                                    .clickable { viewModel.setSelectedTagFilter(null) }
                                    .padding(4.dp)
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip3D(
                                label = "🏷️ All Mails",
                                isSelected = uiState.selectedTagFilter == null,
                                onClick = { viewModel.setSelectedTagFilter(null) }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "💼 Work",
                                isSelected = uiState.selectedTagFilter == "Work",
                                onClick = {
                                    if (uiState.selectedTagFilter == "Work") viewModel.setSelectedTagFilter(null)
                                    else viewModel.setSelectedTagFilter("Work")
                                }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "👤 Personal",
                                isSelected = uiState.selectedTagFilter == "Personal",
                                onClick = {
                                    if (uiState.selectedTagFilter == "Personal") viewModel.setSelectedTagFilter(null)
                                    else viewModel.setSelectedTagFilter("Personal")
                                }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "🚨 Urgent",
                                isSelected = uiState.selectedTagFilter == "Urgent",
                                onClick = {
                                    if (uiState.selectedTagFilter == "Urgent") viewModel.setSelectedTagFilter(null)
                                    else viewModel.setSelectedTagFilter("Urgent")
                                }
                            )
                        }
                    }

                    // Folder Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip3D(
                                label = "Inbox ($unreadCount)",
                                isSelected = uiState.currentFolder == EmailFolder.INBOX && uiState.selectedTagFilter == null,
                                onClick = {
                                    viewModel.setSelectedTagFilter(null)
                                    viewModel.setFolder(EmailFolder.INBOX)
                                }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "⭐ Starred",
                                isSelected = uiState.currentFolder == EmailFolder.STARRED && uiState.selectedTagFilter == null,
                                onClick = {
                                    viewModel.setSelectedTagFilter(null)
                                    viewModel.setFolder(EmailFolder.STARRED)
                                }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "📤 Sent",
                                isSelected = uiState.currentFolder == EmailFolder.SENT && uiState.selectedTagFilter == null,
                                onClick = {
                                    viewModel.setSelectedTagFilter(null)
                                    viewModel.setFolder(EmailFolder.SENT)
                                }
                            )
                        }
                        item {
                            FilterChip3D(
                                label = "📝 Drafts",
                                isSelected = uiState.currentFolder == EmailFolder.DRAFTS && uiState.selectedTagFilter == null,
                                onClick = {
                                    viewModel.setSelectedTagFilter(null)
                                    viewModel.setFolder(EmailFolder.DRAFTS)
                                }
                            )
                        }
                    }
                }
            }

            // 3D Room Database & Gmail Network Sync Progress Header Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = ShadowAmbient,
                            spotColor = ShadowSpot
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(1.dp, Light3dBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
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
                                        .shadow(2.dp, CircleShape, spotColor = if (uiState.isSyncing) ElectricBlue else Emerald3d)
                                        .clip(CircleShape)
                                        .background(if (uiState.isSyncing) ElectricBlueLight else Emerald3dLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = ElectricBlue,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.CloudDone,
                                            contentDescription = "Synced",
                                            tint = Emerald3d,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (uiState.isSyncing) "Syncing with Room..." else "Room Database Cache",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Text3dPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.isSyncing) "• Background" else "• Offline Ready",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (uiState.isSyncing) ElectricBlue else Emerald3d
                                        )
                                    }
                                    Text(
                                        text = uiState.syncProgressMessage,
                                        fontSize = 11.sp,
                                        color = Text3dSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 3D Sync Now Button
                            Button(
                                onClick = { viewModel.triggerSync() },
                                enabled = !uiState.isSyncing,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Light3dCardSubtle,
                                    contentColor = ElectricBlue,
                                    disabledContainerColor = Light3dCardSubtle.copy(alpha = 0.5f)
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("btn_sync_room_db")
                            ) {
                                Icon(
                                    Icons.Default.Sync,
                                    contentDescription = "Sync",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (uiState.isSyncing) "Syncing" else "Sync Now",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (uiState.isSyncing) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { uiState.syncProgressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = ElectricBlue,
                                trackColor = ElectricBlueLight,
                            )
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRIORITY INBOX & THREADS (SWIPE TO ARCHIVE/DELETE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${emails.size} items",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricBlue
                    )
                }
            }

            // 3D Email Cards
            if (emails.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(8.dp, CircleShape, spotColor = Emerald3d)
                                    .clip(CircleShape)
                                    .background(Emerald3dLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.MarkEmailRead,
                                    contentDescription = null,
                                    tint = Emerald3d,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Inbox Zero Achieved",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Text3dPrimary
                            )
                            Text(
                                text = "All threads in this category have been handled.",
                                fontSize = 13.sp,
                                color = Text3dSecondary
                            )
                        }
                    }
                }
            } else {
                items(emails, key = { it.id }) { email ->
                    SwipeableEmail3DItem(
                        email = email,
                        onSelect = { viewModel.selectEmail(email) },
                        onToggleStar = { viewModel.toggleStarred(email) },
                        onArchive = { viewModel.moveEmail(email.id, EmailFolder.ARCHIVE) },
                        onDelete = { viewModel.deleteEmail(email.id) }
                    )
                }
            }
        }
    }
}
