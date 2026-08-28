package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NushuColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryBamboo,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryAmber,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BambooBackground,
    onBackground = BambooTextPrimary,
    surface = BambooDarkSurface,
    onSurface = BambooTextPrimary,
    surfaceVariant = BambooCard,
    onSurfaceVariant = BambooTextSecondary,
    outline = BambooBorder,
    outlineVariant = Color(0xFF1E261D)
)

@Composable
fun NushuDetectorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NushuColorScheme,
        typography = Typography,
        content = content
    )
}
