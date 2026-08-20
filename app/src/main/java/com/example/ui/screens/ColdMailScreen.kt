package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColdMailScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    val campaigns by viewModel.coldCampaigns.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) }
    var bannerPromptInput by remember { mutableStateOf("Futuristic clean gradient header for AI enterprise email campaign") }

    val frameworks = listOf(
        "PAS (Problem, Agitate, Solve)",
        "AIDA (Attention, Interest, Desire, Action)",
        "BAB (Before, After, Bridge)",
        "🚀 YC / Seed Pitch",
        "💼 Enterprise SaaS",
        "🎙️ Collab Pitch"
    )

    val tones = listOf("Executive & Direct", "Confident & Punchy", "Friendly & Warm", "Ultra-Concise (50 words)")
    val imageSizes = listOf("1K", "2K", "4K")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Light3dBackground)
    ) {
        // 3D Elevated Header
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
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Purple3d)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Gradient3dPurple)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.EditNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Cold Mail Studio",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Text3dPrimary
                            )
                            Text(
                                text = "Gemini 3.1 Pro • 3.5 Flash • 3.1 Flash-Lite",
                                fontSize = 11.sp,
                                color = Purple3d,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Emerald3d)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Emerald3dLight)
                            .border(1.dp, Emerald3d.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "~42% Reply Rate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Light3dCardSubtle,
                    contentColor = Purple3d,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Purple3d
                        )
                    },
                    divider = { },
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Light3dBorder, RoundedCornerShape(16.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "AI Generator",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) Purple3d else Text3dSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Visuals & Media",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) Purple3d else Text3dSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Saved Vault (${campaigns.size})",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (selectedTab == 2) Purple3d else Text3dSecondary
                            )
                        }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AI Model Modes Bar (Thinking / Flash / Flash Lite)
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, Light3dBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = Purple3d)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "INTELLIGENCE & REASONING MODES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple3d,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // High Thinking Switch
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (uiState.enableHighThinking) Gradient3dPurple else Brush.verticalGradient(listOf(Light3dCardSubtle, Color.White)))
                                        .border(1.dp, if (uiState.enableHighThinking) Purple3d else Light3dBorder, RoundedCornerShape(14.dp))
                                        .clickable { viewModel.toggleHighThinking(!uiState.enableHighThinking) }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Psychology,
                                                contentDescription = null,
                                                tint = if (uiState.enableHighThinking) Color.White else Purple3d,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Deep Thinking",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (uiState.enableHighThinking) Color.White else Text3dPrimary
                                            )
                                        }
                                        Text(
                                            "Gemini 3.1 Pro (High)",
                                            fontSize = 9.sp,
                                            color = if (uiState.enableHighThinking) Color.White.copy(alpha = 0.8f) else Text3dSecondary
                                        )
                                    }
                                }

                                // Low Latency Switch
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (uiState.isFastLiteMode) Gradient3dPrimary else Brush.verticalGradient(listOf(Light3dCardSubtle, Color.White)))
                                        .border(1.dp, if (uiState.isFastLiteMode) ElectricBlue else Light3dBorder, RoundedCornerShape(14.dp))
                                        .clickable { viewModel.toggleFastLiteMode(!uiState.isFastLiteMode) }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.Bolt,
                                                contentDescription = null,
                                                tint = if (uiState.isFastLiteMode) Color.White else ElectricBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Low Latency",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (uiState.isFastLiteMode) Color.White else Text3dPrimary
                                            )
                                        }
                                        Text(
                                            "Gemini 3.1 Flash-Lite",
                                            fontSize = 9.sp,
                                            color = if (uiState.isFastLiteMode) Color.White.copy(alpha = 0.8f) else Text3dSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Framework Selection
                item {
                    Text(
                        text = "1. PSYCHOLOGICAL FRAMEWORK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(frameworks) { fw ->
                            FilterChip3D(
                                label = fw,
                                isSelected = uiState.coldFramework == fw,
                                onClick = { viewModel.updateColdForm(framework = fw) }
                            )
                        }
                    }
                }

                // 3D Target Persona Input Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = ShadowAmbient,
                                spotColor = ShadowSpot
                            )
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "2. TARGET PERSONA & VALUE OFFER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Purple3d,
                                letterSpacing = 1.sp
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = uiState.coldTargetName,
                                    onValueChange = { viewModel.updateColdForm(targetName = it) },
                                    label = { Text("Target Name", fontSize = 12.sp, color = Text3dSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Light3dCardSubtle,
                                        unfocusedContainerColor = Light3dCardSubtle,
                                        focusedTextColor = Text3dPrimary,
                                        unfocusedTextColor = Text3dPrimary,
                                        focusedBorderColor = Purple3d,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_cold_name")
                                )
                                OutlinedTextField(
                                    value = uiState.coldTargetCompany,
                                    onValueChange = { viewModel.updateColdForm(targetCompany = it) },
                                    label = { Text("Company", fontSize = 12.sp, color = Text3dSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Light3dCardSubtle,
                                        unfocusedContainerColor = Light3dCardSubtle,
                                        focusedTextColor = Text3dPrimary,
                                        unfocusedTextColor = Text3dPrimary,
                                        focusedBorderColor = Purple3d,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_cold_company")
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = uiState.coldTargetRole,
                                    onValueChange = { viewModel.updateColdForm(targetRole = it) },
                                    label = { Text("Role (e.g. CEO, VP)", fontSize = 12.sp, color = Text3dSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Light3dCardSubtle,
                                        unfocusedContainerColor = Light3dCardSubtle,
                                        focusedTextColor = Text3dPrimary,
                                        unfocusedTextColor = Text3dPrimary,
                                        focusedBorderColor = Purple3d,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = uiState.coldTargetLocation,
                                    onValueChange = { viewModel.updateColdForm(targetLocation = it) },
                                    label = { Text("Location (e.g. SF, NY)", fontSize = 12.sp, color = Text3dSecondary) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Light3dCardSubtle,
                                        unfocusedContainerColor = Light3dCardSubtle,
                                        focusedTextColor = Text3dPrimary,
                                        unfocusedTextColor = Text3dPrimary,
                                        focusedBorderColor = Purple3d,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Google Maps Grounding Lead Location button
                            FilledTonalButton(
                                onClick = { viewModel.searchTargetLocationMaps() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = Emerald3dLight,
                                    contentColor = Color(0xFF065F46)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isSearchingMaps) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Querying Google Maps Grounding...", fontSize = 11.sp)
                                } else {
                                    Icon(Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ground with Google Maps (${uiState.coldTargetCompany})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (uiState.mapsGroundingResult != null) {
                                Surface(
                                    color = Light3dCardSubtle,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Light3dBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.mapsGroundingResult,
                                        fontSize = 11.sp,
                                        color = Text3dPrimary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = uiState.coldValueProp,
                                onValueChange = { viewModel.updateColdForm(valueProp = it) },
                                label = { Text("Core Value Proposition / Solution", fontSize = 12.sp, color = Text3dSecondary) },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Light3dCardSubtle,
                                    unfocusedContainerColor = Light3dCardSubtle,
                                    focusedTextColor = Text3dPrimary,
                                    unfocusedTextColor = Text3dPrimary,
                                    focusedBorderColor = Purple3d,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = uiState.coldCta,
                                onValueChange = { viewModel.updateColdForm(cta = it) },
                                label = { Text("Call to Action (e.g. 10-min demo this Thursday)", fontSize = 12.sp, color = Text3dSecondary) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Light3dCardSubtle,
                                    unfocusedContainerColor = Light3dCardSubtle,
                                    focusedTextColor = Text3dPrimary,
                                    unfocusedTextColor = Text3dPrimary,
                                    focusedBorderColor = Purple3d,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "Tone:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Text3dSecondary
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(tones) { tone ->
                                    FilterChip3D(
                                        label = tone,
                                        isSelected = uiState.coldTone == tone,
                                        onClick = { viewModel.updateColdForm(tone = tone) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 3D Generate Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(18.dp),
                                ambientColor = ShadowPurpleGlow,
                                spotColor = Purple3d
                            )
                            .clip(RoundedCornerShape(18.dp))
                            .background(Gradient3dPurple)
                            .border(
                                1.dp,
                                Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable(enabled = !uiState.isGeneratingColdMail) { viewModel.generateColdMail() }
                            .testTag("btn_generate_cold_mail"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isGeneratingColdMail) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.enableHighThinking) "Deep Strategic Thinking (Gemini 3.1 Pro)..."
                                    else if (uiState.isFastLiteMode) "Generating (Gemini 3.1 Flash-Lite)..."
                                    else "Generating Sequence (Gemini 3.5 Flash)...",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (uiState.enableHighThinking) "Generate with Deep Thinking (3.1 Pro)"
                                    else if (uiState.isFastLiteMode) "Generate Rapid Pitch (3.1 Flash-Lite)"
                                    else "Generate World-Class Cold Pitch",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // 3D Output Result Card
                item {
                    AnimatedVisibility(visible = uiState.generatedColdMail.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(26.dp),
                                    ambientColor = ShadowPurpleGlow,
                                    spotColor = Purple3d
                                )
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color.White)
                                .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Purple3d.copy(alpha = 0.3f))), RoundedCornerShape(26.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "GENERATED OUTBOUND SEQUENCE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Purple3d,
                                        letterSpacing = 1.sp
                                    )
                                    Box(
                                        modifier = Modifier
                                            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Purple3d)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Purple3dLight)
                                            .border(1.dp, Purple3d.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Score: ${uiState.coldDeliverabilityScore}/100",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Purple3dDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    color = Light3dCardSubtle,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Light3dBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.generatedColdMail,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = Text3dPrimary,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(uiState.generatedColdMail))
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Light3dCardSubtle,
                                            contentColor = Text3dPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    FilledTonalButton(
                                        onClick = { viewModel.saveGeneratedColdCampaign() },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Light3dCardSubtle,
                                            contentColor = Text3dPrimary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val lines = uiState.generatedColdMail.lines()
                                            val sub = lines.firstOrNull { it.contains("Subject:", ignoreCase = true) }
                                                ?.substringAfter("Subject:", "")?.trim() ?: "Opportunity for ${uiState.coldTargetCompany}"
                                            viewModel.openComposeWithContent(
                                                to = "${uiState.coldTargetName.lowercase().replace(" ", "")}@${uiState.coldTargetCompany.lowercase().replace(" ", "")}.com",
                                                subject = sub,
                                                body = uiState.generatedColdMail
                                            )
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Purple3d,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.weight(1.3f)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Compose", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // Visuals & Multimodal AI Studio Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // High Quality Image Generator with 1K, 2K, 4K Affordance
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Light3dBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Purple3d)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Purple3d, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HIGH-QUALITY IMAGE GENERATION",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Purple3d,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Model: gemini-3-pro-image-preview for campaign headers and visual assets.",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )

                            // Resolution Affordance (1K, 2K, 4K)
                            Text("Select Resolution:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Text3dSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                imageSizes.forEach { size ->
                                    FilterChip3D(
                                        label = size,
                                        isSelected = uiState.selectedImageSize == size,
                                        onClick = { viewModel.setImageSize(size) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = bannerPromptInput,
                                onValueChange = { bannerPromptInput = it },
                                label = { Text("Visual Prompt", fontSize = 12.sp) },
                                minLines = 2,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Light3dCardSubtle,
                                    unfocusedContainerColor = Light3dCardSubtle,
                                    focusedTextColor = Text3dPrimary,
                                    unfocusedTextColor = Text3dPrimary,
                                    focusedBorderColor = Purple3d,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { viewModel.generateCampaignBanner(bannerPromptInput) },
                                enabled = !uiState.isGeneratingBanner,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Purple3d, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isGeneratingBanner) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating ${uiState.selectedImageSize} Asset...")
                                } else {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate ${uiState.selectedImageSize} Header Visual", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (uiState.generatedBannerUrl != null) {
                                AsyncImage(
                                    model = uiState.generatedBannerUrl,
                                    contentDescription = "Generated Campaign Header",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(1.dp, Light3dBorder, RoundedCornerShape(16.dp))
                                )
                            }
                        }
                    }
                }

                // Analyze Pitch Deck / Document with Gemini 3.1 Pro Vision
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Light3dBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = ElectricBlue)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ANALYZE IMAGES & DOCUMENTS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Model: gemini-3.1-pro-preview for scanning business cards, pitch decks, and email screenshots.",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )

                            Button(
                                onClick = {
                                    // Create a sample pitch card bitmap to analyze
                                    val testBitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(testBitmap)
                                    val paint = Paint().apply { color = android.graphics.Color.WHITE }
                                    canvas.drawRect(0f, 0f, 400f, 200f, paint)
                                    paint.color = android.graphics.Color.DKGRAY
                                    paint.textSize = 20f
                                    canvas.drawText("Sarah Jenkins - VP Growth @ Vertex Cloud", 20f, 60f, paint)
                                    canvas.drawText("Focus: Multi-Region Kubernetes Scaling", 20f, 110f, paint)
                                    viewModel.analyzeImageDocument(testBitmap)
                                },
                                enabled = !uiState.isAnalyzingVision,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isAnalyzingVision) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyzing with Gemini 3.1 Pro Vision...")
                                } else {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Upload & Analyze Pitch Card", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (uiState.visionAnalysisResult != null) {
                                Surface(
                                    color = Light3dCardSubtle,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Light3dBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.visionAnalysisResult,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Text3dPrimary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Audio Voice Memo Transcription using Gemini 3.5 Flash
                item {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, Light3dBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Emerald3d)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Emerald3d, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TRANSCRIBE AUDIO & VOICE MEMOS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald3d,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Model: gemini-3.5-flash for real-time microphone dictation to email drafts.",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )

                            Button(
                                onClick = { viewModel.transcribeVoiceMemo() },
                                enabled = !uiState.isRecordingVoice,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald3d, contentColor = Color.White),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.isRecordingVoice) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Transcribing Audio with Gemini 3.5 Flash...")
                                } else {
                                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Record & Transcribe Voice Memo", fontWeight = FontWeight.Bold)
                                }
                            }

                            if (uiState.transcribedVoiceText != null) {
                                Surface(
                                    color = Light3dCardSubtle,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Light3dBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = uiState.transcribedVoiceText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        color = Text3dPrimary,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Vault Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(campaigns, key = { it.id }) { campaign ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = ShadowAmbient,
                                spotColor = ShadowSpot
                            )
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = campaign.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Text3dPrimary
                                    )
                                    Text(
                                        text = "${campaign.targetName} • ${campaign.targetCompany}",
                                        fontSize = 12.sp,
                                        color = Text3dSecondary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Purple3dLight)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${campaign.deliverabilityScore}% Score",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Purple3dDark
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = campaign.body,
                                fontSize = 12.sp,
                                color = Text3dSecondary,
                                maxLines = 4,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(campaign.body))
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Text3dSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp, color = Text3dSecondary)
                                }
                                Button(
                                    onClick = {
                                        viewModel.sendEmail(
                                            to = "${campaign.targetName.lowercase().replace(" ", "")}@${campaign.targetCompany.lowercase().replace(" ", "")}.com",
                                            subject = campaign.subjectVariantA,
                                            body = campaign.body,
                                            isDraft = true
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Purple3d, contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Use in Drafts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
