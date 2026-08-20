package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
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
import com.example.data.model.AiChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun AICopilotScreen(
    viewModel: AssistantViewModel,
    uiState: com.example.ui.viewmodel.AssistantUiState,
    modifier: Modifier = Modifier
) {
    var promptInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "Summarize top priority emails",
        "Draft cold pitch for YC founders",
        "Propose Thursday 2 PM meeting to Marc",
        "Repurpose pitch for LinkedIn"
    )

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

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
                            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Purple3d)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Gradient3dPurple)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Forum,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Executive Copilot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Text3dPrimary
                        )
                        Text(
                            text = "AI reasoning powered by Gemini",
                            fontSize = 12.sp,
                            color = Text3dSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Purple3d)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Purple3dLight)
                        .border(1.dp, Purple3d.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Gemini Live",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple3dDark
                    )
                }
            }
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(uiState.chatMessages, key = { it.id }) { msg ->
                ChatBubble3D(message = msg)
            }

            if (uiState.isCopilotThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Purple3d,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Copilot is analyzing inbox & generating response...",
                            fontSize = 12.sp,
                            color = Text3dSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestions
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = ShadowAmbient)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Light3dBorder, RoundedCornerShape(14.dp))
                        .clickable { viewModel.sendCopilotMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Text3dSecondary
                    )
                }
            }
        }

        // 3D Input Toolbar
        Surface(
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, ambientColor = ShadowAmbient, spotColor = ShadowSpot)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask your Gmail assistant...", fontSize = 13.sp, color = Text3dMuted) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Light3dCardSubtle,
                        unfocusedContainerColor = Light3dCardSubtle,
                        focusedBorderColor = Purple3d,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Text3dPrimary,
                        unfocusedTextColor = Text3dPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_copilot_chat")
                )

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(
                            elevation = if (promptInput.isNotBlank()) 6.dp else 0.dp,
                            shape = CircleShape,
                            ambientColor = ShadowPurpleGlow,
                            spotColor = Purple3d
                        )
                        .clip(CircleShape)
                        .background(if (promptInput.isNotBlank()) Gradient3dPurple else Brush.verticalGradient(listOf(Light3dCardSubtle, Light3dBorder)))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        .testTag("btn_send_copilot")
                ) {
                    IconButton(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                viewModel.sendCopilotMessage(promptInput)
                                promptInput = ""
                            }
                        },
                        enabled = promptInput.isNotBlank() && !uiState.isCopilotThinking,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (promptInput.isNotBlank()) Color.White else Text3dMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble3D(message: AiChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .shadow(3.dp, RoundedCornerShape(10.dp), spotColor = Purple3d)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Gradient3dPurple)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Forum, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    ),
                    ambientColor = if (isUser) ShadowPurpleGlow else ShadowAmbient,
                    spotColor = if (isUser) Purple3d else ShadowSpot
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(if (isUser) Gradient3dPurple else Brush.verticalGradient(listOf(Color.White, Color(0xFFF9FAFC))))
                .border(
                    1.dp,
                    if (isUser) Color.White.copy(alpha = 0.4f) else Light3dBorder,
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
        ) {
            Text(
                text = message.message,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = if (isUser) Color.White else Text3dPrimary,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}
