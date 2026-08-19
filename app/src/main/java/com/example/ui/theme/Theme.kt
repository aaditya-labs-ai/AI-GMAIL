package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light3dColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = ElectricBlueLight,
    onPrimaryContainer = ElectricBlueDark,
    secondary = Purple3d,
    onSecondary = Color.White,
    secondaryContainer = Purple3dLight,
    onSecondaryContainer = Purple3dDark,
    tertiary = Emerald3d,
    onTertiary = Color.White,
    tertiaryContainer = Emerald3dLight,
    onTertiaryContainer = Color(0xFF065F46),
    background = Light3dBackground,
    onBackground = Text3dPrimary,
    surface = Light3dSurface,
    onSurface = Text3dPrimary,
    surfaceVariant = Light3dCardSubtle,
    onSurfaceVariant = Text3dSecondary,
    surfaceContainer = Light3dSurfaceElevated,
    surfaceContainerHigh = Color(0xFFFFFFFF),
    outline = Light3dBorder,
    outlineVariant = Light3dBorder.copy(alpha = 0.5f)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set to Light theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Light3dColorScheme,
        typography = Typography,
        content = content
    )
}
