package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = KosBluePrimary,
    onPrimary = KosWhite,
    primaryContainer = KosBlueContainer,
    onPrimaryContainer = KosBlueDark,
    secondary = KosCyanAccent,
    onSecondary = KosWhite,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = KosTeal,
    onTertiary = KosWhite,
    background = KosBackground,
    onBackground = KosTextPrimary,
    surface = KosSurface,
    onSurface = KosTextPrimary,
    surfaceVariant = KosSurfaceVariant,
    onSurfaceVariant = KosTextSecondary,
    outline = KosOutlineVariant,
    error = KosError,
    onError = KosWhite,
    errorContainer = KosErrorContainer,
    onErrorContainer = Color(0xFF991B1B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFE3F2FD),
    secondary = Color(0xFF81D4FA),
    onSecondary = Color(0xFF01579B),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

@Composable
fun KosConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Keep distinctive brand blue identity consistent
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
