package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SocialAccount
import com.example.data.model.SocialOutreachItem
import com.example.data.model.SocialPlatform
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialHubScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.socialAccounts.collectAsStateWithLifecycle(emptyList())
    val outreachList by viewModel.socialOutreach.collectAsStateWithLifecycle(emptyList())
    val clipboardManager = LocalClipboardManager.current

    var selectedPlatform by remember { mutableStateOf(SocialPlatform.LINKEDIN) }
    var targetRecipient by remember { mutableStateOf("") }
    var pitchIdea by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Light3dBackground)
    ) {
        // 3D Header
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
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
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = ElectricBlue)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Gradient3dPrimary)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Hub,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Social Hub",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Text3dPrimary
                        )
                        Text(
                            text = "${accounts.count { it.isConnected }} connected channels",
                            fontSize = 12.sp,
                            color = Text3dSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = ElectricBlue)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricBlueLight)
                        .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Live Sync",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueDark
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
            // Synced Social Platforms
            item {
                Text(
                    text = "SYNCED PLATFORMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text3dSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    accounts.forEach { account ->
                        SocialAccount3DCard(
                            account = account,
                            onToggle = { viewModel.toggleSocialConnection(account.platform, !account.isConnected) }
                        )
                    }
                }
            }

            // Cross-Platform Outbound Composer 3D Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = ShadowBlueGlow,
                            spotColor = ElectricBlue
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .border(1.5.dp, Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))), RoundedCornerShape(26.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "CROSS-CHANNEL OUTBOUND COMPOSER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "Adapt email pitches into native InMail or high-converting direct messages.",
                            fontSize = 12.sp,
                            color = Text3dSecondary
                        )

                        // Channel selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(SocialPlatform.LINKEDIN, SocialPlatform.TWITTER_X, SocialPlatform.GITHUB).forEach { platform ->
                                val isSel = selectedPlatform == platform
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(
                                            elevation = if (isSel) 4.dp else 1.dp,
                                            shape = RoundedCornerShape(14.dp),
                                            spotColor = ElectricBlue
                                        )
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSel) Gradient3dPrimary else Brush.verticalGradient(listOf(Color.White, Light3dCardSubtle)))
                                        .border(1.dp, if (isSel) Color.Transparent else Light3dBorder, RoundedCornerShape(14.dp))
                                        .clickable { selectedPlatform = platform }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = platform.name.replace("_", "/"),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Text3dSecondary
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = targetRecipient,
                            onValueChange = { targetRecipient = it },
                            label = { Text("Target Person / Handle", fontSize = 12.sp, color = Text3dSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Light3dCardSubtle,
                                unfocusedContainerColor = Light3dCardSubtle,
                                focusedTextColor = Text3dPrimary,
                                unfocusedTextColor = Text3dPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_social_target")
                        )

                        OutlinedTextField(
                            value = pitchIdea,
                            onValueChange = { pitchIdea = it },
                            label = { Text("Pitch Concept to Adapt", fontSize = 12.sp, color = Text3dSecondary) },
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Light3dCardSubtle,
                                unfocusedContainerColor = Light3dCardSubtle,
                                focusedTextColor = Text3dPrimary,
                                unfocusedTextColor = Text3dPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_social_pitch")
                        )

                        // 3D Generate Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 6.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = ShadowBlueGlow,
                                    spotColor = ElectricBlue
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(Gradient3dPrimary)
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .clickable(enabled = targetRecipient.isNotBlank() && pitchIdea.isNotBlank()) {
                                    viewModel.createSocialOutreach(
                                        platform = selectedPlatform,
                                        target = targetRecipient,
                                        pitchBody = pitchIdea,
                                        outreachType = when (selectedPlatform) {
                                            SocialPlatform.LINKEDIN -> "LinkedIn InMail"
                                            SocialPlatform.TWITTER_X -> "Twitter / X DM"
                                            SocialPlatform.GITHUB -> "GitHub Note"
                                            else -> "Direct Message"
                                        }
                                    )
                                    targetRecipient = ""
                                    pitchIdea = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate & Queue Outreach", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Queued Outreaches
            item {
                Text(
                    text = "OUTREACH QUEUE & DISPATCHES (${outreachList.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Text3dSecondary,
                    letterSpacing = 1.sp
                )
            }

            if (outreachList.isEmpty()) {
                item {
                    Text(
                        text = "No queued messages. Generate tailored outreach above.",
                        fontSize = 12.sp,
                        color = Text3dSecondary
                    )
                }
            } else {
                items(outreachList, key = { it.id }) { item ->
                    SocialOutreach3DCard(
                        item = item,
                        onCopy = { clipboardManager.setText(AnnotatedString(item.messageText)) },
                        onSend = { viewModel.sendSocialOutreach(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SocialAccount3DCard(
    account: SocialAccount,
    onToggle: () -> Unit
) {
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
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = ElectricBlue)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            when (account.platform) {
                                SocialPlatform.LINKEDIN -> Gradient3dPrimary
                                SocialPlatform.TWITTER_X -> Brush.horizontalGradient(listOf(Color(0xFF0F172A), Color(0xFF334155)))
                                SocialPlatform.GITHUB -> Gradient3dPurple
                                else -> Gradient3dPrimary
                            }
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (account.platform) {
                            SocialPlatform.LINKEDIN -> Icons.Default.Work
                            SocialPlatform.TWITTER_X -> Icons.Default.Tag
                            SocialPlatform.GITHUB -> Icons.Default.Code
                            SocialPlatform.SUBSTACK -> Icons.AutoMirrored.Filled.Article
                            SocialPlatform.INSTAGRAM -> Icons.Default.PhotoCamera
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = account.displayName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Text3dPrimary
                    )
                    Text(
                        text = "${account.username} • ${account.platform}",
                        fontSize = 11.sp,
                        color = Text3dSecondary
                    )
                }
            }

            Switch(
                checked = account.isConnected,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ElectricBlue,
                    uncheckedThumbColor = Text3dSecondary,
                    uncheckedTrackColor = Light3dCardSubtle
                )
            )
        }
    }
}

@Composable
fun SocialOutreach3DCard(
    item: SocialOutreachItem,
    onCopy: () -> Unit,
    onSend: () -> Unit
) {
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
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricBlueLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${item.platform} • ${item.outreachType}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlueDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isSent) Emerald3dLight else Purple3dLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (item.isSent) "SENT" else "QUEUED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isSent) Color(0xFF065F46) else Purple3dDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Target: ${item.targetPersonOrChannel}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Text3dPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Light3dCardSubtle,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Light3dBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.messageText,
                    fontSize = 12.sp,
                    color = Text3dSecondary,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Text3dSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", fontSize = 11.sp, color = Text3dSecondary)
                }
                if (!item.isSent) {
                    Button(
                        onClick = onSend,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mark Sent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
