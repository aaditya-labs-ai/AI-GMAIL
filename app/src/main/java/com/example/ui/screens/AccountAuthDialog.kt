package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.auth.AuthUserState
import com.example.ui.theme.*

@Composable
fun AccountAuthDialog(
    authUserState: AuthUserState,
    isAuthenticating: Boolean,
    onSignInWithGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onLinkCustomEmail: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customEmailInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = ShadowBlueGlow,
                    spotColor = ElectricBlue
                )
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(
                    1.5.dp,
                    Brush.verticalGradient(listOf(Color.White, Color(0xFFE2E8F0))),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
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
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "GOOGLE AUTHENTICATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Text3dSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3D Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(8.dp, CircleShape, spotColor = ElectricBlue)
                        .clip(CircleShape)
                        .background(Gradient3dPrimary)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (authUserState.displayName.isNotBlank()) {
                            authUserState.displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                        } else "AR",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = authUserState.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Text3dPrimary
                )

                Text(
                    text = authUserState.email,
                    fontSize = 13.sp,
                    color = Text3dSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Badge
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .shadow(2.dp, RoundedCornerShape(10.dp), spotColor = Emerald3d)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald3dLight)
                            .border(1.dp, Emerald3d.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Emerald3d))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Firebase Google Auth Linked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Security & Integration Specs
                Surface(
                    color = Light3dCardSubtle,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Light3dBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Provider:", fontSize = 12.sp, color = Text3dSecondary)
                            Text("Google Identity / Firebase", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Text3dPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Credential Manager:", fontSize = 12.sp, color = Text3dSecondary)
                            Text("Active & Encrypted", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald3d)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gmail Read/Write Scope:", fontSize = 12.sp, color = Text3dSecondary)
                            Text("Authorized", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Google Sign In Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = ShadowBlueGlow,
                            spotColor = ElectricBlue
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .background(Gradient3dPrimary)
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable(enabled = !isAuthenticating) { onSignInWithGoogle() }
                        .testTag("btn_google_signin_action"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAuthenticating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting Google Account...", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign in with Google",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Email Link option
                if (!showCustomInput) {
                    TextButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Link a different Gmail account",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ElectricBlue
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customEmailInput,
                            onValueChange = { customEmailInput = it },
                            placeholder = { Text("e.g. name@gmail.com", fontSize = 12.sp, color = Text3dMuted) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Light3dCardSubtle,
                                unfocusedContainerColor = Light3dCardSubtle,
                                focusedTextColor = Text3dPrimary,
                                unfocusedTextColor = Text3dPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { showCustomInput = false },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    if (customEmailInput.isNotBlank()) {
                                        onLinkCustomEmail(customEmailInput)
                                        showCustomInput = false
                                    }
                                },
                                enabled = customEmailInput.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue, contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Link Gmail", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Sign out button
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sign Out",
                        color = GmailCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
