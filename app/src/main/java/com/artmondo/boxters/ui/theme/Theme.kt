package com.artmondo.boxters.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = GameColors.uiAccent,
    secondary = GameColors.boardStroke,
    background = GameColors.backgroundDark,
    surface = GameColors.backgroundMid,
    onPrimary = GameColors.backgroundDark,
    onSecondary = GameColors.backgroundDark,
    onBackground = GameColors.uiText,
    onSurface = GameColors.uiText,
    error = GameColors.uiError,
)

@Composable
fun BoxtersTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
