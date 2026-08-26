package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Aura Design Tokens (Warm Editorial Aesthetic from Design Mockup)
val AuraBg = Color(0xFFFAF7F0)             // Warm alabaster canvas
val AuraCard = Color(0xFFFDFBF7)           // Clean parchment card background
val AuraCardSelected = Color(0xFFF4ECE1)   // Highlighted active / selected card
val AuraBorder = Color(0xFFE8E2D5)         // Subtle soft paper border
val AuraBorderSelected = Color(0xFFC8A882) // Selected card border outline
val AuraBorderSubtle = Color(0xFFEFE9DD)   // Ultra subtle container border
val AuraDark = Color(0xFF2B2824)           // Deep warm espresso text
val AuraDarkSubtle = Color(0xFF4A453E)     // Medium espresso text
val AuraMuted = Color(0xFF8C8375)          // Secondary labels and timestamps
val AuraMutedLight = Color(0xFFB5AC9E)     // Muted icons and disabled states
val AuraAccent = Color(0xFF935F28)         // Warm amber / gold AI accent
val AuraAccentLight = Color(0xFFE6C387)    // Sparkle luminous highlight
val AuraAccentBg = Color(0xFFFDF7E7)       // Subtle amber tinted background
val AuraTagGreen = Color(0xFF44653A)       // Olive / sage green badge
val AuraTagGreenBg = Color(0xFFEBF1E8)     // Soft green badge background
val AuraTagGreenBorder = Color(0xFFD6E3D1) // Soft green border
val AuraTagBlue = Color(0xFF3D4F7C)        // Soft slate blue badge
val AuraTagBlueBg = Color(0xFFECEEF5)      // Soft slate blue badge background
val AuraTagBlueBorder = Color(0xFFD7DBE8)  // Soft slate blue border
val AuraSearchBg = Color(0xFFEFE9DD)       // Warm search bar background

// 3D Light Theme Canvas & Surfaces (Maintained for backward compatibility)
val Light3dBackground = AuraBg
val Light3dSurface = AuraCard
val Light3dSurfaceElevated = AuraCard
val Light3dCard = AuraCard
val Light3dCardSubtle = AuraCardSelected
val Light3dBorder = AuraBorder
val Light3dBorderHighlight = Color(0xFFF8FAFC)

// 3D Dimensional Gradients & Accents
val ElectricBlue = Color(0xFF935F28)
val ElectricBlueDark = Color(0xFF6E451C)
val ElectricBlueLight = Color(0xFFF4ECE1)
val RoyalBlue = Color(0xFF935F28)
val SkyBlue = Color(0xFFD4A373)

val Purple3d = Color(0xFF935F28)
val Purple3dLight = Color(0xFFF4ECE1)
val Purple3dDark = Color(0xFF6E451C)

val GmailCoral = Color(0xFFC04B3E)
val GmailCoralLight = Color(0xFFFDECEB)

val Emerald3d = Color(0xFF44653A)
val Emerald3dLight = Color(0xFFEBF1E8)
val Emerald3dDark = Color(0xFF2D4427)

val Amber3d = Color(0xFF935F28)
val Amber3dLight = Color(0xFFFEF3C7)

// Text Colors (High Contrast Light Mode)
val Text3dPrimary = AuraDark
val Text3dSecondary = AuraDarkSubtle
val Text3dMuted = AuraMuted

// 3D Drop Shadows
val ShadowAmbient = Color(0x142B2824)
val ShadowSpot = Color(0x1A2B2824)
val ShadowBlueGlow = Color(0x20935F28)
val ShadowPurpleGlow = Color(0x20935F28)

// Gradients
val Gradient3dHero = Brush.linearGradient(
    listOf(Color(0xFFFDFBF7), Color(0xFFF4ECE1))
)
val Gradient3dPrimary = Brush.horizontalGradient(
    listOf(Color(0xFF322E2B), Color(0xFF22201E))
)
val Gradient3dDark = Brush.horizontalGradient(
    listOf(Color(0xFF322E2B), Color(0xFF22201E))
)
val Gradient3dPurple = Brush.horizontalGradient(
    listOf(Color(0xFF935F28), Color(0xFFB47B3E))
)
val Gradient3dCoral = Brush.horizontalGradient(
    listOf(Color(0xFFC04B3E), Color(0xFFD97768))
)
val Gradient3dEmerald = Brush.horizontalGradient(
    listOf(Color(0xFF44653A), Color(0xFF628955))
)
val Gradient3dCardHighlight = Brush.verticalGradient(
    listOf(Color(0xFFFDFBF7), Color(0xFFF4ECE1))
)

