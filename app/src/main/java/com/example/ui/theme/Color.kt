package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// 3D Light Theme Canvas & Surfaces
val Light3dBackground = Color(0xFFF4F7FC)
val Light3dSurface = Color(0xFFFFFFFF)
val Light3dSurfaceElevated = Color(0xFFFAFCFF)
val Light3dCard = Color(0xFFFFFFFF)
val Light3dCardSubtle = Color(0xFFF0F4FA)
val Light3dBorder = Color(0xFFE2E8F0)
val Light3dBorderHighlight = Color(0xFFF8FAFC)

// 3D Dimensional Gradients & Accents
val ElectricBlue = Color(0xFF1A73E8)
val ElectricBlueDark = Color(0xFF0D53B8)
val ElectricBlueLight = Color(0xFFE8F0FE)
val RoyalBlue = Color(0xFF2563EB)
val SkyBlue = Color(0xFF38BDF8)

val Purple3d = Color(0xFF7C3AED)
val Purple3dLight = Color(0xFFF3E8FF)
val Purple3dDark = Color(0xFF5B21B6)

val GmailCoral = Color(0xFFEA4335)
val GmailCoralLight = Color(0xFFFEECEB)

val Emerald3d = Color(0xFF10B981)
val Emerald3dLight = Color(0xFFECFDF5)

val Amber3d = Color(0xFFF59E0B)
val Amber3dLight = Color(0xFFFEF3C7)

// Text Colors (High Contrast Light Mode)
val Text3dPrimary = Color(0xFF0F172A)
val Text3dSecondary = Color(0xFF475569)
val Text3dMuted = Color(0xFF94A3B8)

// 3D Drop Shadows
val ShadowAmbient = Color(0x1A0F172A)
val ShadowSpot = Color(0x2E1E293B)
val ShadowBlueGlow = Color(0x331A73E8)
val ShadowPurpleGlow = Color(0x337C3AED)

// Gradients
val Gradient3dHero = Brush.linearGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFEEF4FD))
)
val Gradient3dPrimary = Brush.horizontalGradient(
    listOf(Color(0xFF1A73E8), Color(0xFF3B82F6))
)
val Gradient3dPurple = Brush.horizontalGradient(
    listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
)
val Gradient3dCoral = Brush.horizontalGradient(
    listOf(Color(0xFFEA4335), Color(0xFFFF6B6B))
)
val Gradient3dCardHighlight = Brush.verticalGradient(
    listOf(Color(0xFFFFFFFF), Color(0xFFF6F9FD))
)
