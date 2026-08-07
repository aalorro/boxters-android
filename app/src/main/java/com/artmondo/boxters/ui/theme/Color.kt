package com.artmondo.boxters.ui.theme

import androidx.compose.ui.graphics.Color

object GameColors {
    // Backgrounds
    val backgroundDark = Color(0xFF050520)
    val backgroundMid = Color(0xFF0A0A2E)
    val backgroundLight = Color(0xFF0F0F3D)

    // Board
    val boardFill = Color(0x0FFDF4E3) // 6% alpha
    val boardStroke = Color(0xFFFFD700)
    val boardGlow = Color(0x99FFD700)
    val boardText = Color(0xFFFDF4E3)
    val boardHighlight = Color(0xFFFF6B6B)
    val boardDim = Color(0x4DFDF4E3)

    // Trace
    val traceValid = Color(0xFF4ADE80)
    val tracePrefix = Color(0xFFFFD700)
    val traceInvalid = Color(0xFFFF4444)

    // UI
    val uiText = Color(0xFFE0E0E8)
    val uiTextDim = Color(0x99E0E0E8)
    val uiAccent = Color(0xFFFFD700)
    val uiSuccess = Color(0xFF4ADE80)
    val uiWarning = Color(0xFFF97316)
    val uiError = Color(0xFFEF4444)
    val uiPanel = Color(0xD90A0A2E)
    val uiPanelBorder = Color(0x33FFD700)

    // Stars
    val starColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFD700),
        Color(0xFF87CEEB),
        Color(0xFFDDA0DD),
        Color(0xFF98FB98)
    )

    // Cell types
    val anchorRim = Color(0xFFFF6B6B)
    val anchorFill = Color(0xFF2A1A1A)

    // Mode-specific
    val simpleAccent = Color(0xFFFFD700)
    val clearAccent = Color(0xFFEF4444)
    val chainAccent = Color(0xFF4ADE80)
    val chainCombo = Color(0xFF22C55E)
    val illuminateAccent = Color(0xFFF59E0B)
    val illuminateLit = Color(0x40FFD700)
}
