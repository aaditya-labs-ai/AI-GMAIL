package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantScreen
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun MainAppScreen(
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authUserState by viewModel.authUserState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.aiStatusMessage) {
        uiState.aiStatusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(AuraBg),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = AuraDark,
                    contentColor = Color.White,
                    actionColor = AuraAccent,
                    shape = RoundedCornerShape(16.dp),
                    snackbarData = data
                )
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        ambientColor = ShadowAmbient,
                        spotColor = ShadowSpot
                    ),
                color = AuraBg,
                border = BorderStroke(1.dp, AuraBorder)
            ) {
                NavigationBar(
                    containerColor = AuraBg,
                    contentColor = AuraDark,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Inbox
                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.Inbox,
                        onClick = { viewModel.setScreen(AssistantScreen.Inbox) },
                        icon = {
                            Icon(
                                if (uiState.currentScreen is AssistantScreen.Inbox) Icons.Filled.Inbox else Icons.Outlined.Inbox,
                                contentDescription = "Inbox",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraDark,
                            selectedTextColor = AuraDark,
                            indicatorColor = AuraCardSelected,
                            unselectedIconColor = AuraMuted,
                            unselectedTextColor = AuraMuted
                        ),
                        modifier = Modifier.testTag("nav_inbox")
                    )

                    // 2. Drafts
                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.ColdMailStudio,
                        onClick = { viewModel.setScreen(AssistantScreen.ColdMailStudio) },
                        icon = {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = "Drafts",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraDark,
                            selectedTextColor = AuraDark,
                            indicatorColor = AuraCardSelected,
                            unselectedIconColor = AuraMuted,
                            unselectedTextColor = AuraMuted
                        ),
                        modifier = Modifier.testTag("nav_drafts")
                    )

                    // 3. Sent
                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.SocialHub,
                        onClick = { viewModel.setScreen(AssistantScreen.SocialHub) },
                        icon = {
                            Icon(
                                Icons.Outlined.Send,
                                contentDescription = "Sent",
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraDark,
                            selectedTextColor = AuraDark,
                            indicatorColor = AuraCardSelected,
                            unselectedIconColor = AuraMuted,
                            unselectedTextColor = AuraMuted
                        ),
                        modifier = Modifier.testTag("nav_sent")
                    )

                    // 4. Center AI Copilot
                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.AICopilot,
                        onClick = { viewModel.setScreen(AssistantScreen.AICopilot) },
                        icon = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (uiState.currentScreen is AssistantScreen.AICopilot) AuraAccent else AuraCardSelected)
                                    .border(1.dp, AuraBorder, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (uiState.currentScreen is AssistantScreen.AICopilot) Color.White else AuraAccent
                                )
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraAccent,
                            selectedTextColor = AuraAccent,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = AuraMuted,
                            unselectedTextColor = AuraMuted
                        ),
                        modifier = Modifier.testTag("nav_copilot")
                    )

                    // 5. Account / Profile
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.setAccountDialogVisible(true) },
                        icon = {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AuraDark,
                            selectedTextColor = AuraDark,
                            indicatorColor = AuraCardSelected,
                            unselectedIconColor = AuraMuted,
                            unselectedTextColor = AuraMuted
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        },
        floatingActionButton = {}
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Light3dBackground)
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                is AssistantScreen.Inbox -> {
                    InboxScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                is AssistantScreen.ColdMailStudio -> {
                    ColdMailScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                is AssistantScreen.AutomationStudio -> {
                    AutomationScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                is AssistantScreen.SocialHub -> {
                    SocialHubScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                is AssistantScreen.AICopilot -> {
                    AICopilotScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
                else -> {
                    InboxScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }

    // Detail Bottom Sheet
    uiState.selectedEmail?.let { email ->
        EmailDetailSheet(
            email = email,
            viewModel = viewModel,
            isAnalyzing = uiState.isAnalyzing,
            onDismiss = { viewModel.selectEmail(null) }
        )
    }

    // Compose Bottom Sheet
    if (uiState.isComposeOpen) {
        ComposeEmailSheet(
            viewModel = viewModel,
            userEmail = authUserState.email,
            initialTo = uiState.composeInitialTo,
            initialSubject = uiState.composeInitialSubject,
            initialBody = uiState.composeInitialBody,
            onDismiss = { viewModel.setComposeOpen(false) }
        )
    }

    // Google & Firebase Auth Dialog
    if (uiState.showAccountDialog) {
        AccountAuthDialog(
            authUserState = authUserState,
            isAuthenticating = uiState.isAuthenticating,
            onSignInWithGoogle = { viewModel.signInWithGoogle() },
            onSignOut = { viewModel.signOutUser() },
            onDismiss = { viewModel.setAccountDialogVisible(false) }
        )
    }

    // Notification Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            viewModel = viewModel,
            preferences = uiState.notificationPreferences,
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}
