package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artmondo.boxters.data.repository.LeaderboardEntry
import com.artmondo.boxters.ui.theme.*

@Composable
fun LeaderboardDialog(
    entries: List<LeaderboardEntry>,
    playerRank: Int,
    playerId: String,
    isLoading: Boolean,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .background(GameColors.backgroundDark, RoundedCornerShape(16.dp))
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leaderboard",
                    style = GameTypography.levelName.copy(fontSize = 24.sp)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2715",
                        color = GameColors.uiText,
                        fontSize = 20.sp
                    )
                }
            }

            HorizontalDivider(
                color = GameColors.uiPanelBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GameColors.uiAccent,
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else if (entries.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No leaderboard data available.\nCheck your connection and try again.",
                        style = GameTypography.body.copy(
                            fontSize = 14.sp,
                            color = GameColors.uiTextDim
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Column headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "#",
                        style = GameTypography.modeBadge.copy(fontSize = 11.sp, color = GameColors.uiTextDim),
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = "Player",
                        style = GameTypography.modeBadge.copy(fontSize = 11.sp, color = GameColors.uiTextDim),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Score",
                        style = GameTypography.modeBadge.copy(fontSize = 11.sp, color = GameColors.uiTextDim),
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Lvls",
                        style = GameTypography.modeBadge.copy(fontSize = 11.sp, color = GameColors.uiTextDim),
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Mode",
                        style = GameTypography.modeBadge.copy(fontSize = 11.sp, color = GameColors.uiTextDim),
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }

                HorizontalDivider(color = GameColors.uiPanelBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                // Scrollable entries
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    entries.forEachIndexed { index, entry ->
                        val rank = index + 1
                        val isCurrentPlayer = entry.id == playerId
                        LeaderboardRow(
                            rank = rank,
                            entry = entry,
                            isCurrentPlayer = isCurrentPlayer
                        )
                        if (index < entries.lastIndex) {
                            HorizontalDivider(
                                color = GameColors.uiPanelBorder.copy(alpha = 0.3f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                // Player rank footer
                if (playerRank > LEADERBOARD_SIZE || playerRank == -1) {
                    HorizontalDivider(color = GameColors.uiPanelBorder, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (playerRank > 0) "Your rank: #$playerRank" else "Not ranked yet",
                            style = GameTypography.body.copy(
                                fontSize = 13.sp,
                                color = GameColors.uiTextDim
                            )
                        )
                    }
                }
            }
        }
    }
}

private const val LEADERBOARD_SIZE = 30

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentPlayer: Boolean
) {
    val rankText = when (rank) {
        1 -> "\uD83E\uDD47"  // gold medal
        2 -> "\uD83E\uDD48"  // silver medal
        3 -> "\uD83E\uDD49"  // bronze medal
        else -> "$rank"
    }

    val bgColor = if (isCurrentPlayer) {
        GameColors.uiAccent.copy(alpha = 0.12f)
    } else {
        GameColors.backgroundDark.copy(alpha = 0f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rankText,
            style = GameTypography.body.copy(
                fontSize = if (rank <= 3) 16.sp else 13.sp,
                fontWeight = if (rank <= 3) FontWeight.Bold else FontWeight.Normal,
                color = GameColors.uiText
            ),
            modifier = Modifier.width(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.name,
                    style = GameTypography.body.copy(
                        fontSize = 13.sp,
                        fontWeight = if (isCurrentPlayer) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrentPlayer) GameColors.uiAccent else GameColors.uiText
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isCurrentPlayer) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOU",
                        style = GameTypography.modeBadge.copy(
                            fontSize = 9.sp,
                            color = GameColors.backgroundDark,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .background(GameColors.uiAccent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Text(
            text = entry.totalScore.toString(),
            style = GameTypography.body.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.uiText
            ),
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )

        Text(
            text = entry.levelsCompleted.toString(),
            style = GameTypography.body.copy(fontSize = 12.sp, color = GameColors.uiTextDim),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )

        Text(
            text = modeEmoji(entry.highestMode),
            fontSize = 14.sp,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.Center
        )
    }
}

private fun modeEmoji(mode: String): String = when (mode) {
    "simple" -> "\uD83C\uDF31"
    "clear" -> "\uD83E\uDDF9"
    "chain" -> "\uD83D\uDD17"
    "illuminate" -> "\uD83D\uDCA1"
    "apex" -> "\uD83D\uDC51"
    else -> "\uD83C\uDF31"
}
