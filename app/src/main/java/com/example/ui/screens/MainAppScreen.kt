package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantScreen
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun MainAppScreen(
    viewModel: AssistantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val authUserState by viewModel.authUserState.collectAsState()
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
            .background(Light3dBackground),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = Text3dPrimary,
                    contentColor = Color.White,
                    actionColor = SkyBlue,
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
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        ambientColor = ShadowAmbient,
                        spotColor = ShadowSpot
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    contentColor = Text3dSecondary,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.Inbox,
                        onClick = { viewModel.setScreen(AssistantScreen.Inbox) },
                        icon = {
                            BadgedBox(badge = {
                                if (uiState.unreadCount > 0) {
                                    Badge(
                                        containerColor = GmailCoral,
                                        contentColor = Color.White
                                    ) {
                                        Text("${uiState.unreadCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }) {
                                Icon(
                                    if (uiState.currentScreen is AssistantScreen.Inbox) Icons.Filled.Inbox else Icons.Outlined.Inbox,
                                    contentDescription = "Inbox"
                                )
                            }
                        },
                        label = { Text("Inbox", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            selectedTextColor = ElectricBlue,
                            indicatorColor = ElectricBlueLight,
                            unselectedIconColor = Text3dMuted,
                            unselectedTextColor = Text3dMuted
                        ),
                        modifier = Modifier.testTag("nav_inbox")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.ColdMailStudio,
                        onClick = { viewModel.setScreen(AssistantScreen.ColdMailStudio) },
                        icon = {
                            Icon(
                                if (uiState.currentScreen is AssistantScreen.ColdMailStudio) Icons.Filled.EditNote else Icons.Outlined.EditNote,
                                contentDescription = "Cold Mail"
                            )
                        },
                        label = { Text("Cold Mail", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple3d,
                            selectedTextColor = Purple3d,
                            indicatorColor = Purple3dLight,
                            unselectedIconColor = Text3dMuted,
                            unselectedTextColor = Text3dMuted
                        ),
                        modifier = Modifier.testTag("nav_cold_mail")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.AutomationStudio,
                        onClick = { viewModel.setScreen(AssistantScreen.AutomationStudio) },
                        icon = {
                            Icon(
                                if (uiState.currentScreen is AssistantScreen.AutomationStudio) Icons.Filled.AutoMode else Icons.Outlined.AutoMode,
                                contentDescription = "Automations"
                            )
                        },
                        label = { Text("Automate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald3d,
                            selectedTextColor = Emerald3d,
                            indicatorColor = Emerald3dLight,
                            unselectedIconColor = Text3dMuted,
                            unselectedTextColor = Text3dMuted
                        ),
                        modifier = Modifier.testTag("nav_automations")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.SocialHub,
                        onClick = { viewModel.setScreen(AssistantScreen.SocialHub) },
                        icon = {
                            Icon(
                                if (uiState.currentScreen is AssistantScreen.SocialHub) Icons.Filled.Hub else Icons.Outlined.Hub,
                                contentDescription = "Social Hub"
                            )
                        },
                        label = { Text("Socials", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricBlue,
                            selectedTextColor = ElectricBlue,
                            indicatorColor = ElectricBlueLight,
                            unselectedIconColor = Text3dMuted,
                            unselectedTextColor = Text3dMuted
                        ),
                        modifier = Modifier.testTag("nav_social_hub")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen is AssistantScreen.AICopilot,
                        onClick = { viewModel.setScreen(AssistantScreen.AICopilot) },
                        icon = {
                            Icon(
                                if (uiState.currentScreen is AssistantScreen.AICopilot) Icons.Filled.Forum else Icons.Outlined.Forum,
                                contentDescription = "Copilot"
                            )
                        },
                        label = { Text("Copilot", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Purple3d,
                            selectedTextColor = Purple3d,
                            indicatorColor = Purple3dLight,
                            unselectedIconColor = Text3dMuted,
                            unselectedTextColor = Text3dMuted
                        ),
                        modifier = Modifier.testTag("nav_copilot")
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.currentScreen is AssistantScreen.Inbox) {
                // 3D Elevated Pill FAB with Gradient & Raised Shadow
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = ShadowBlueGlow,
                            spotColor = ElectricBlue
                        )
                        .clip(RoundedCornerShape(24.dp))
                        .background(Gradient3dPrimary)
                        .border(
                            1.dp,
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.5f), Color.Transparent)),
                            RoundedCornerShape(24.dp)
                        )
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.setComposeOpen(true) },
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Compose Email", tint = Color.White) },
                        text = { Text("Compose Email", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp) },
                        modifier = Modifier.testTag("fab_compose_email")
                    )
                }
            }
        }
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
            onLinkCustomEmail = { email -> viewModel.linkCustomGoogleEmail(email) },
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
