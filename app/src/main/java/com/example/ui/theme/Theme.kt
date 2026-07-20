package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreen,
    secondary = WhatsAppDarkGreen,
    tertiary = WhatsAppDarkTeal,
    background = TextDark,
    surface = TextDark,
    onPrimary = CardWhite,
    onSecondary = CardWhite,
    onTertiary = CardWhite,
    onBackground = CardWhite,
    onSurface = CardWhite
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreen,
    secondary = WhatsAppDarkGreen,
    tertiary = WhatsAppDarkTeal,
    background = ChatBackground,
    surface = CardWhite,
    onPrimary = CardWhite,
    onSecondary = CardWhite,
    onTertiary = CardWhite,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
