package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SageColorScheme = darkColorScheme(
    primary = SagePrimary,
    onPrimary = Color.White,
    primaryContainer = SagePrimaryStart,
    onPrimaryContainer = SagePrimaryLight,
    secondary = SageAccent,
    onSecondary = Color.White,
    secondaryContainer = SageAccentDark,
    onSecondaryContainer = Color.White,
    tertiary = SageGold,
    onTertiary = Color.Black,
    background = SageBackground,
    onBackground = SageTextPrimary,
    surface = SageSurface,
    onSurface = SageTextPrimary,
    surfaceVariant = SageRaisedSurface,
    onSurfaceVariant = SageTextSecondary,
    outline = SageCardBorder,
    error = SageError,
    onError = Color.White
)

@Composable
fun SageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SageColorScheme,
        typography = Typography,
        content = content
    )
}

