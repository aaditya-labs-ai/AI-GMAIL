package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiApiClient
import com.example.ui.theme.*
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    var to by remember { mutableStateOf(initialTo) }
    var subject by remember { mutableStateOf(initialSubject) }
    var body by remember { mutableStateOf(initialBody) }
    var isPolishing by remember { mutableStateOf(false) }
    var showAiGenerator by remember { mutableStateOf(false) }
    var aiContextKeywords by remember { mutableStateOf("") }
    var selectedFramework by remember { mutableStateOf("AIDA (Attention, Interest, Desire, Action)") }
    var selectedTone by remember { mutableStateOf("Executive & Direct") }
    var isGeneratingWithAi by remember { mutableStateOf(false) }
    var suggestedSubjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var isListeningSpeech by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Speech Recognizer Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningSpeech = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                body = if (body.isBlank()) spokenText else "$body $spokenText"
                Toast.makeText(context, "Dictation added to email body!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission Launcher for Microphone
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your email body message...")
                }
                isListeningSpeech = true
                speechLauncher.launch(intent)
            } catch (e: Exception) {
                isListeningSpeech = false
                Toast.makeText(context, "Speech recognition not available on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Microphone permission required for speech-to-text dictation", Toast.LENGTH_SHORT).show()
        }
    }

    fun startSpeechDictation() {
        recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
    }

    // Sync initial values if changed externally
    LaunchedEffect(initialTo, initialSubject, initialBody) {
        if (initialTo.isNotBlank()) to = initialTo
        if (initialSubject.isNotBlank()) subject = initialSubject
        if (initialBody.isNotBlank()) body = initialBody
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Light3dBorder) },
        modifier = Modifier.fillMaxHeight(0.95f)
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

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Schedule Send Button
                    OutlinedButton(
                        onClick = { showScheduleDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricBlue),
                        border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(36.dp).testTag("btn_schedule_send")
                    ) {
                        Icon(Icons.Outlined.ScheduleSend, contentDescription = "Schedule Send", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Schedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

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
                        Text("Draft", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sender Card
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

            // 3D "Generate with AI" Toggle Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (showAiGenerator) 4.dp else 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = ShadowAmbient,
                        spotColor = Purple3d
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (showAiGenerator) Purple3dLight else Color.White)
                    .border(
                        1.dp,
                        if (showAiGenerator) Purple3d.copy(alpha = 0.4f) else Light3dBorder,
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { showAiGenerator = !showAiGenerator }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .shadow(2.dp, RoundedCornerShape(8.dp), spotColor = Purple3d)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Gradient3dPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Generate with AI (Cold Email & Templates)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (showAiGenerator) Purple3dDark else Text3dPrimary
                            )
                            Text(
                                text = "Powered by Gemini 3.5 Flash • Context & Keywords",
                                fontSize = 11.sp,
                                color = Text3dSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = if (showAiGenerator) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Purple3d
                    )
                }
            }

            // Expandable AI Generator Form
            AnimatedVisibility(
                visible = showAiGenerator,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Purple3d.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CONTEXT OR KEYWORDS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple3d,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = aiContextKeywords,
                        onValueChange = { aiContextKeywords = it },
                        placeholder = { Text("e.g. Follow-up after meeting, propose 15min partnership demo, offering 30% discount", fontSize = 12.sp, color = Text3dMuted) },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Light3dCardSubtle,
                            unfocusedContainerColor = Light3dCardSubtle,
                            focusedBorderColor = Purple3d,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_ai_context_keywords")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SELECT FRAMEWORK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Text3dSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val frameworks = listOf(
                            "AIDA (Attention, Interest, Desire, Action)",
                            "PAS (Problem, Agitate, Solve)",
                            "BAB (Before, After, Bridge)",
                            "🚀 Seed / Investor Pitch",
                            "💼 Quick Intro"
                        )
                        items(frameworks) { fw ->
                            val isSel = selectedFramework == fw
                            AssistChip(
                                onClick = { selectedFramework = fw },
                                label = { Text(fw.substringBefore("(").trim(), fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSel) Purple3dLight else Light3dCardSubtle,
                                    labelColor = if (isSel) Purple3dDark else Text3dPrimary
                                ),
                                border = BorderStroke(1.dp, if (isSel) Purple3d else Light3dBorder),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3D "Generate with AI" Execution Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = Purple3d
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gradient3dPurple)
                            .clickable(enabled = !isGeneratingWithAi) {
                                val kw = aiContextKeywords.ifBlank { "Executive outreach and partnership opportunity for Aditya Rai" }
                                isGeneratingWithAi = true
                                viewModel.generateColdEmailFromKeywords(
                                    contextKeywords = kw,
                                    framework = selectedFramework,
                                    tone = selectedTone
                                ) { genSubject, genBody ->
                                    subject = genSubject
                                    body = genBody
                                    isGeneratingWithAi = false
                                    showAiGenerator = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isGeneratingWithAi) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini is Crafting Template...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate Cold Email with AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
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
                trailingIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                isPolishing = true
                                val prompt = "Generate 3 high open-rate cold email subject line variants for an email from Aditya Rai about:\n${if (subject.isNotBlank()) subject else body.take(120)}\nFormat output as 3 separate lines with no numbering."
                                try {
                                    val res = GeminiApiClient.callGemini(prompt)
                                    suggestedSubjects = res.lines().filter { it.isNotBlank() }.take(3)
                                } catch (_: Exception) {}
                                isPolishing = false
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = "Suggest Subject Lines",
                            tint = Purple3d,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_compose_subject")
            )

            // Subject suggestions if available
            if (suggestedSubjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Purple3dLight)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "AI Suggested Subject Lines (Tap to use):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple3dDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    suggestedSubjects.forEach { altSub ->
                        Text(
                            text = "• $altSub",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Text3dPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    subject = altSub.replace("•", "").trim()
                                }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }

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
                            text = "AI WRITING ENHANCER & POLISHER",
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
                                if (body.isNotBlank()) {
                                    coroutineScope.launch {
                                        isPolishing = true
                                        val prompt = "Elevate this email body for Aditya Rai, adding a compelling value hook and clear call-to-action:\n$body"
                                        val result = GeminiApiClient.callGemini(prompt)
                                        body = result
                                        isPolishing = false
                                    }
                                }
                            },
                            label = { Text("Add Clear CTA", fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body field with Dictation Action
            Box(modifier = Modifier.fillMaxWidth()) {
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

                // Voice Dictation Button inside body card
                IconButton(
                    onClick = { startSpeechDictation() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = if (isListeningSpeech) GmailCoral else ElectricBlue)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isListeningSpeech) GmailCoral else ElectricBlueLight)
                        .testTag("btn_voice_dictation")
                ) {
                    Icon(
                        imageVector = if (isListeningSpeech) Icons.Default.Mic else Icons.Outlined.Mic,
                        contentDescription = "Voice Dictate Email Body",
                        tint = if (isListeningSpeech) Color.White else ElectricBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (isListeningSpeech) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GmailCoralLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(12.dp), color = GmailCoral, strokeWidth = 2.dp)
                    Text("Listening to voice input... speak clearly into microphone", fontSize = 11.sp, color = GmailCoral, fontWeight = FontWeight.SemiBold)
                }
            }

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

        // Schedule Send Dialog
        if (showScheduleDialog) {
            ScheduleSendDialog(
                recipientTo = to,
                subject = subject,
                onDismiss = { showScheduleDialog = false },
                onScheduleConfirm = { scheduledEpoch, formattedString ->
                    showScheduleDialog = false
                    if (to.isNotBlank() && subject.isNotBlank()) {
                        viewModel.scheduleSendEmail(
                            to = to,
                            subject = subject,
                            body = body,
                            scheduledTimeEpoch = scheduledEpoch,
                            scheduledTimeFormatted = formattedString
                        )
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Please enter recipient and subject before scheduling", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
fun ScheduleSendDialog(
    recipientTo: String,
    subject: String,
    onDismiss: () -> Unit,
    onScheduleConfirm: (scheduledEpoch: Long, formattedString: String) -> Unit
) {
    val cal = Calendar.getInstance()
    
    // Preset 1: Tomorrow morning 8:00 AM
    val tomorrowMorning = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 8)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    
    // Preset 2: Tomorrow afternoon 2:00 PM
    val tomorrowAfternoon = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 14)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    // Preset 3: Monday morning 9:00 AM
    val mondayMorning = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, if (get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) 2 else if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) 1 else (Calendar.MONDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7)
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    // Preset 4: Quick Test (in 30 seconds)
    val testQuick = Calendar.getInstance().apply {
        add(Calendar.SECOND, 30)
    }

    val dateFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = ElectricBlue)
                Text("Schedule Send", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Text3dPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Pick a date and time to automatically send this email in background via Firestore sync & worker trigger:",
                    fontSize = 12.sp,
                    color = Text3dSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Option 1
                Surface(
                    onClick = {
                        onScheduleConfirm(tomorrowMorning.timeInMillis, dateFormat.format(tomorrowMorning.time))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Light3dCardSubtle,
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.fillMaxWidth().testTag("opt_tomorrow_morning")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tomorrow Morning", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Text3dPrimary)
                            Text(dateFormat.format(tomorrowMorning.time), fontSize = 11.sp, color = Text3dMuted)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Text3dMuted, modifier = Modifier.size(12.dp))
                    }
                }

                // Option 2
                Surface(
                    onClick = {
                        onScheduleConfirm(tomorrowAfternoon.timeInMillis, dateFormat.format(tomorrowAfternoon.time))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Light3dCardSubtle,
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.fillMaxWidth().testTag("opt_tomorrow_afternoon")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tomorrow Afternoon", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Text3dPrimary)
                            Text(dateFormat.format(tomorrowAfternoon.time), fontSize = 11.sp, color = Text3dMuted)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Text3dMuted, modifier = Modifier.size(12.dp))
                    }
                }

                // Option 3
                Surface(
                    onClick = {
                        onScheduleConfirm(mondayMorning.timeInMillis, dateFormat.format(mondayMorning.time))
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = Light3dCardSubtle,
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.fillMaxWidth().testTag("opt_monday_morning")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monday Morning", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Text3dPrimary)
                            Text(dateFormat.format(mondayMorning.time), fontSize = 11.sp, color = Text3dMuted)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Text3dMuted, modifier = Modifier.size(12.dp))
                    }
                }

                // Option 4: Quick Demo (30s dispatch)
                Surface(
                    onClick = {
                        onScheduleConfirm(testQuick.timeInMillis, "In 30 seconds (${dateFormat.format(testQuick.time)})")
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricBlueLight,
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("opt_quick_dispatch")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("⚡ Quick Background Demo (in 30s)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ElectricBlue)
                            Text("Triggers worker dispatch in 30 seconds", fontSize = 11.sp, color = ElectricBlue)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(12.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Text3dSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

