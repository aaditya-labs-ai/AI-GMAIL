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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AutomationLog
import com.example.data.model.AutomationRule
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomationScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    val rules by viewModel.automationRules.collectAsStateWithLifecycle()
    val logs by viewModel.automationLogs.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    val activeCount = remember(rules) { rules.count { it.isEnabled } }

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
                                .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Emerald3d)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(Emerald3d, Color(0xFF34D399))))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.AutoMode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Inbox Automations",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Text3dPrimary
                            )
                            Text(
                                text = "$activeCount active background rules",
                                fontSize = 12.sp,
                                color = Text3dSecondary
                            )
                        }
                    }

                    // 3D Run All Button
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Emerald3d)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(Emerald3d, Color(0xFF059669))))
                    ) {
                        Button(
                            onClick = { viewModel.triggerAutomationEngine() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("btn_run_engine_now")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Light3dCardSubtle,
                    contentColor = Emerald3d,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Emerald3d
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
                        text = { Text("Rules ($activeCount)", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp, color = if (selectedTab == 0) Emerald3d else Text3dSecondary) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Audit Logs (${logs.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp, color = if (selectedTab == 1) Emerald3d else Text3dSecondary) }
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3D Add Rule Button
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = ShadowAmbient)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(1.dp, Emerald3d.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                            .clickable { showAddRuleDialog = true }
                            .testTag("card_add_custom_rule")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Emerald3d, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Autonomous Rule",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Emerald3d
                            )
                        }
                    }
                }

                items(rules, key = { it.id }) { rule ->
                    Automation3DCard(
                        rule = rule,
                        onToggle = { viewModel.toggleAutomationRule(rule) }
                    )
                }
            }
        } else {
            // Logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (logs.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No execution logs yet. Tap 'Run All' above.", color = Text3dSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        Log3DItem(log = log)
                    }
                }
            }
        }
    }

    if (showAddRuleDialog) {
        AddAutomationRuleDialog3D(
            onDismiss = { showAddRuleDialog = false },
            onAdd = { name, desc, tType, tCond, aType, aParam ->
                viewModel.addCustomAutomationRule(name, desc, tType, tCond, aType, aParam)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun Automation3DCard(
    rule: AutomationRule,
    onToggle: () -> Unit
) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(3.dp, RoundedCornerShape(14.dp), spotColor = Emerald3d)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (rule.isEnabled) Emerald3dLight else Light3dCardSubtle)
                    .border(1.dp, if (rule.isEnabled) Emerald3d.copy(alpha = 0.3f) else Light3dBorder, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (rule.triggerType) {
                        "DOMAIN" -> Icons.Default.Business
                        "KEYWORD" -> Icons.Default.Search
                        "NO_REPLY" -> Icons.Default.HourglassEmpty
                        else -> Icons.Default.Mail
                    },
                    contentDescription = null,
                    tint = if (rule.isEnabled) Emerald3d else Text3dMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Text3dPrimary
                )
                Text(
                    text = rule.description,
                    fontSize = 12.sp,
                    color = Text3dSecondary,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Triggered: ${rule.executionCount} times",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Text3dMuted
                )
            }

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Emerald3d,
                    uncheckedThumbColor = Text3dSecondary,
                    uncheckedTrackColor = Light3dCardSubtle
                )
            )
        }
    }
}

@Composable
fun Log3DItem(log: AutomationLog) {
    val dateFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }
    val timeFormatted = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp), ambientColor = ShadowAmbient)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Light3dBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Emerald3d,
                modifier = Modifier.size(18.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.ruleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Text3dPrimary
                    )
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = Text3dMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.details,
                    fontSize = 12.sp,
                    color = Text3dSecondary
                )
            }
        }
    }
}

@Composable
fun AddAutomationRuleDialog3D(
    onDismiss: () -> Unit,
    onAdd: (name: String, desc: String, triggerType: String, triggerCond: String, actionType: String, actionParam: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var triggerCond by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                text = "New Autonomous Rule",
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = Text3dPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name", color = Text3dSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Light3dCardSubtle,
                        unfocusedContainerColor = Light3dCardSubtle,
                        focusedTextColor = Text3dPrimary,
                        unfocusedTextColor = Text3dPrimary,
                        focusedBorderColor = Emerald3d,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = triggerCond,
                    onValueChange = { triggerCond = it },
                    label = { Text("Keywords / Domain Condition", color = Text3dSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Light3dCardSubtle,
                        unfocusedContainerColor = Light3dCardSubtle,
                        focusedTextColor = Text3dPrimary,
                        unfocusedTextColor = Text3dPrimary,
                        focusedBorderColor = Emerald3d,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Action Goal", color = Text3dSecondary) },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Light3dCardSubtle,
                        unfocusedContainerColor = Light3dCardSubtle,
                        focusedTextColor = Text3dPrimary,
                        unfocusedTextColor = Text3dPrimary,
                        focusedBorderColor = Emerald3d,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && triggerCond.isNotBlank()) {
                        onAdd(name, desc, "KEYWORD", triggerCond, "AUTO_LABEL", "High Priority")
                    }
                },
                enabled = name.isNotBlank() && triggerCond.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald3d, contentColor = Color.White)
            ) {
                Text("Activate Rule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Text3dSecondary)
            }
        }
    )
}
