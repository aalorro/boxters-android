package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artmondo.boxters.data.model.GameMode
import com.artmondo.boxters.ui.game.GameUiState
import com.artmondo.boxters.ui.theme.*

@Composable
fun WelcomeScreen(
    uiState: GameUiState,
    onModeSelected: (GameMode) -> Unit,
    onPlay: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleTheme: () -> Unit,
    onInfoClicked: () -> Unit,
    onRandom: () -> Unit = {},
    onSettingsClicked: () -> Unit = {},
    onLeaderboardClicked: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(GameColors.backgroundMid, GameColors.backgroundDark),
                    radius = 800f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Top-right buttons: Info + Sound toggle
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Info button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(22.dp))
                    .border(1.dp, GameColors.uiAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .clickable { onInfoClicked() },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    // "i" dot
                    drawCircle(GameColors.uiAccent, radius = 2.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx, cy - 7.dp.toPx()))
                    // "i" body
                    drawRoundRect(
                        color = GameColors.uiAccent,
                        topLeft = androidx.compose.ui.geometry.Offset(cx - 2.dp.toPx(), cy - 2.dp.toPx()),
                        size = Size(4.dp.toPx(), 12.dp.toPx()),
                        cornerRadius = CornerRadius(1.5.dp.toPx())
                    )
                }
            }
            // Sound toggle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(22.dp))
                    .clickable { onToggleSound() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.audioEnabled) "\uD83D\uDD0A" else "\uD83D\uDD07",
                    fontSize = 20.sp
                )
            }
            // Theme toggle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(22.dp))
                    .clickable { onToggleTheme() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isDarkMode) "\u2600\uFE0F" else "\uD83C\uDF19",
                    fontSize = 20.sp
                )
            }
            // Settings gear button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(22.dp))
                    .border(1.dp, GameColors.uiAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .clickable { onSettingsClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u2699\uFE0F",
                    fontSize = 20.sp
                )
            }
            // Trophy button — leaderboard
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(GameColors.uiPanel, RoundedCornerShape(22.dp))
                    .border(1.dp, GameColors.uiAccent.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .clickable { onLeaderboardClicked() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\uD83C\uDFC6",
                    fontSize = 20.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Boxters",
                style = GameTypography.title.copy(fontSize = 42.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val welcomeFontSize = if (LocalConfiguration.current.screenWidthDp >= 600) 24.sp else 16.sp
            Text(
                text = "Welcome back, ",
                style = GameTypography.body.copy(fontSize = welcomeFontSize),
                textAlign = TextAlign.Center
            )
            Text(
                text = uiState.playerName,
                style = GameTypography.body.copy(
                    fontSize = welcomeFontSize,
                    color = GameColors.uiAccent,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Stats row
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("Total\nScore", uiState.totalScore.toString())
                StatItem("Games\nPlayed", uiState.playerStats?.gamesPlayed?.toString() ?: "0")
                StatItem("Levels\nCompleted", uiState.playerStats?.levelsCompleted?.toString() ?: "0")
                StatItem("Best\nScore", uiState.playerStats?.bestScore?.toString() ?: "0")
            }
            Spacer(modifier = Modifier.height(36.dp))

            // Mode selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (mode in GameMode.entries) {
                    val isUnlocked = mode in uiState.unlockedModes
                    val isSelected = mode == uiState.selectedMode

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .then(
                                if (isSelected) Modifier.background(
                                    Brush.horizontalGradient(
                                        listOf(GameColors.uiAccent, GameColors.uiAccent.copy(alpha = 0.8f))
                                    ),
                                    RoundedCornerShape(8.dp)
                                ) else Modifier.border(
                                    1.dp,
                                    if (isUnlocked) GameColors.uiPanelBorder else GameColors.uiPanelBorder.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                            )
                            .alpha(if (isUnlocked) 1f else 0.5f)
                            .clickable(enabled = isUnlocked) { onModeSelected(mode) }
                    ) {
                        Text(
                            text = if (isUnlocked) "${modeEmoji(mode)} ${mode.displayName}" else "\uD83D\uDD12",
                            style = GameTypography.modeBadge.copy(
                                fontSize = 11.sp,
                                color = if (isSelected) GameColors.backgroundDark else GameColors.uiText
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))

            // Play button
            Button(
                onClick = onPlay,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.uiAccent,
                    contentColor = GameColors.backgroundDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(56.dp)
            ) {
                Text(
                    text = "Play",
                    style = GameTypography.button.copy(fontSize = 22.sp)
                )
            }

            // Random button — only visible after completing Apex mode
            if (uiState.hasCompletedApex) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRandom,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GameColors.uiAccent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, GameColors.uiAccent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Random",
                        style = GameTypography.button.copy(fontSize = 18.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

private fun modeEmoji(mode: GameMode): String = when (mode) {
    GameMode.SIMPLE -> "\uD83C\uDF31"
    GameMode.CLEAR -> "\uD83E\uDDF9"
    GameMode.CHAIN -> "\uD83D\uDD17"
    GameMode.ILLUMINATE -> "\uD83D\uDCA1"
    GameMode.APEX -> "\uD83D\uDC51"
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = GameTypography.statValue
        )
        Text(
            text = label,
            style = GameTypography.statLabel,
            textAlign = TextAlign.Center
        )
    }
}
