package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.data.api.GeminiApiClient
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeEmailSheet(
    viewModel: AssistantViewModel,
    userEmail: String,
    initialTo: String = "",
    initialSubject: String = "",
    initialBody: String = "",
    onDismiss: () -> Unit
) {
    var to by remember { mutableStateOf(initialTo) }
    var subject by remember { mutableStateOf(initialSubject) }
    var body by remember { mutableStateOf(initialBody) }
    var isPolishing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Light3dBorder) },
        modifier = Modifier.fillMaxHeight(0.94f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compose Email",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Text3dPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.sendEmail(to, subject, body, isDraft = true)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Light3dCardSubtle,
                            contentColor = Text3dPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Draft", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (to.isNotBlank() && subject.isNotBlank()) 4.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = ElectricBlue
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (to.isNotBlank() && subject.isNotBlank()) Gradient3dPrimary else Brush.verticalGradient(listOf(Light3dCardSubtle, Light3dBorder)))
                    ) {
                        Button(
                            onClick = {
                                if (to.isNotBlank() && subject.isNotBlank()) {
                                    viewModel.sendEmail(to, subject, body, isDraft = false)
                                    onDismiss()
                                }
                            },
                            enabled = to.isNotBlank() && subject.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_send_email")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sender
            Surface(
                color = Light3dCardSubtle,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Light3dBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "From: ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Text3dSecondary
                    )
                    Text(
                        text = "Aditya Rai <$userEmail>",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Text3dPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // To field
            OutlinedTextField(
                value = to,
                onValueChange = { to = it },
                label = { Text("To (Recipient Email)", color = Text3dSecondary) },
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
                    .testTag("input_compose_to")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subject field
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject", color = Text3dSecondary) },
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
                    .testTag("input_compose_subject")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3D AI Polish Toolbar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, RoundedCornerShape(18.dp), spotColor = Purple3d)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White)
                    .border(1.5.dp, Purple3d.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "AI WRITING ENHANCER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Purple3d,
                            letterSpacing = 1.sp
                        )

                        if (isPolishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Purple3d,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                if (body.isNotBlank()) {
                                    coroutineScope.launch {
                                        isPolishing = true
                                        val prompt = "Rewrite this email body for Aditya Rai to be ultra-concise, punchy, and under 80 words:\n$body"
                                        val result = GeminiApiClient.callGemini(prompt)
                                        body = result
                                        isPolishing = false
                                    }
                                }
                            },
                            label = { Text("Make Concise", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        AssistChip(
                            onClick = {
                                if (body.isNotBlank()) {
                                    coroutineScope.launch {
                                        isPolishing = true
                                        val prompt = "Rewrite this email body for Aditya Rai in a confident, high-caliber executive tone for founders/investors:\n$body"
                                        val result = GeminiApiClient.callGemini(prompt)
                                        body = result
                                        isPolishing = false
                                    }
                                }
                            },
                            label = { Text("Executive Tone", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        AssistChip(
                            onClick = {
                                if (subject.isNotBlank()) {
                                    coroutineScope.launch {
                                        isPolishing = true
                                        val prompt = "Generate 1 high-open rate irresistible subject line for this email:\nSubject: $subject\nBody: $body"
                                        val result = GeminiApiClient.callGemini(prompt)
                                        subject = result.lines().firstOrNull()?.replace("Subject:", "")?.trim() ?: subject
                                        isPolishing = false
                                    }
                                }
                            },
                            label = { Text("Catchy Subject", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body field
            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Compose email body...", color = Text3dSecondary) },
                minLines = 8,
                maxLines = 16,
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
                    .testTag("input_compose_body")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Signature
            Surface(
                color = Light3dCardSubtle,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Light3dBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Signature:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Best regards,\nAditya Rai\nkumaradityarai0005@gmail.com",
                        fontSize = 12.sp,
                        color = Text3dSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
