package com.artmondo.boxters.ui.theme

import androidx.compose.ui.graphics.Color

object GameColors {
    var isDark = true

    // Backgrounds
    val backgroundDark get() = if (isDark) Color(0xFF050520) else Color(0xFFF5E6D3)
    val backgroundMid get() = if (isDark) Color(0xFF0A0A2E) else Color(0xFFFDF0E0)
    val backgroundLight get() = if (isDark) Color(0xFF0F0F3D) else Color(0xFFFFF8F0)

    // Board
    val boardFill get() = if (isDark) Color(0x0FFDF4E3) else Color(0x186B4226)
    val boardStroke get() = if (isDark) Color(0xFFFFD700) else Color(0xFF6B4226)
    val boardGlow get() = if (isDark) Color(0x99FFD700) else Color(0x60FF6B35)
    val boardText get() = if (isDark) Color(0xFFFDF4E3) else Color(0xFF3D2B1F)
    val boardHighlight get() = if (isDark) Color(0xFFFF6B6B) else Color(0xFFFF4D6D)
    val boardDim get() = if (isDark) Color(0x4DFDF4E3) else Color(0x4D3D2B1F)

    // Trace
    val traceValid get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF10B981)
    val tracePrefix get() = if (isDark) Color(0xFFFFD700) else Color(0xFFFF6B35)
    val traceInvalid get() = if (isDark) Color(0xFFFF4444) else Color(0xFFEF4444)

    // UI
    val uiText get() = if (isDark) Color(0xFFE0E0E8) else Color(0xFF2D2D2D)
    val uiTextDim get() = if (isDark) Color(0x99E0E0E8) else Color(0x992D2D2D)
    val uiAccent get() = if (isDark) Color(0xFFFFD700) else Color(0xFFFF6B35)
    val uiSuccess get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF10B981)
    val uiWarning get() = if (isDark) Color(0xFFF97316) else Color(0xFFE85D04)
    val uiError get() = if (isDark) Color(0xFFEF4444) else Color(0xFFDC2626)
    val uiPanel get() = if (isDark) Color(0xD90A0A2E) else Color(0xE6FFFFFF)
    val uiPanelBorder get() = if (isDark) Color(0x33FFD700) else Color(0x33FF6B35)

    // Stars
    val starColors get() = if (isDark) listOf(
        Color(0xFFFFFFFF),
        Color(0xFFFFD700),
        Color(0xFF87CEEB),
        Color(0xFFDDA0DD),
        Color(0xFF98FB98)
    ) else listOf(
        Color(0xFFFF6B9D),
        Color(0xFFFF6B35),
        Color(0xFF00D2FF),
        Color(0xFF7CFF6B),
        Color(0xFFCB6CE6)
    )

    // Cell types
    val anchorRim get() = if (isDark) Color(0xFFFF6B6B) else Color(0xFFFF4D6D)
    val anchorFill get() = if (isDark) Color(0xFF2A1A1A) else Color(0xFFFFE0E6)

    // Mode-specific
    val simpleAccent get() = if (isDark) Color(0xFFFFD700) else Color(0xFFFF6B35)
    val clearAccent get() = if (isDark) Color(0xFFEF4444) else Color(0xFFFF4D6D)
    val chainAccent get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF10B981)
    val chainCombo get() = if (isDark) Color(0xFF22C55E) else Color(0xFF059669)
    val illuminateAccent get() = if (isDark) Color(0xFFF59E0B) else Color(0xFFE85D04)
    val illuminateLit get() = if (isDark) Color(0x40FFD700) else Color(0x40FF6B35)
}
