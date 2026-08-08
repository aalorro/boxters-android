package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artmondo.boxters.ui.theme.*

private enum class InfoTab(val label: String) {
    ABOUT("About"),
    GAMEPLAY("Game Play"),
    PRIVACY("Privacy"),
    CONTACT("Contact")
}

@Composable
fun InfoDialog(onDismiss: () -> Unit) {
    var selectedTab by remember { mutableStateOf(InfoTab.ABOUT) }

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
            // Header with close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Boxters",
                    style = GameTypography.levelName.copy(fontSize = 24.sp)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✕",
                        color = GameColors.uiText,
                        fontSize = 20.sp
                    )
                }
            }

            // Tab row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (tab in InfoTab.entries) {
                    val isSelected = tab == selectedTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (isSelected) GameColors.uiAccent else GameColors.uiPanel,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = GameTypography.modeBadge.copy(
                                fontSize = 12.sp,
                                color = if (isSelected) GameColors.backgroundDark else GameColors.uiTextDim
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = GameColors.uiPanelBorder, thickness = 1.dp)

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    InfoTab.ABOUT -> AboutContent()
                    InfoTab.GAMEPLAY -> GamePlayContent()
                    InfoTab.PRIVACY -> PrivacyContent()
                    InfoTab.CONTACT -> ContactContent()
                }
            }
        }
    }
}

@Composable
private fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoText("Boxters is a word puzzle game where you trace paths across a hexagonal board to form words. Sharpen your vocabulary, test your spatial reasoning, and discover words you never knew existed.")

        InfoText("Challenge yourself across four unique game modes \u2014 Simple, Clear, Chain, and Illuminate \u2014 each with its own twist on word-finding strategy. Complete each mode to unlock the next!")

        Spacer(modifier = Modifier.height(4.dp))
        InfoHeader("Quick Tips")

        BulletPoint("Longer words score more points \u2014 a 6-letter word is worth far more than two 3-letter words.")
        BulletPoint("Scan the entire board before tracing. The best path isn't always the most obvious one.")
        BulletPoint("Common prefixes and suffixes (-ing, -tion, re-, un-) can help you spot longer words.")
        BulletPoint("Edge tiles are harder to connect later \u2014 use them early when you can.")
        BulletPoint("Don't waste moves on guesses. Invalid words (not in the dictionary) still cost a move!")

        Spacer(modifier = Modifier.height(4.dp))
        InfoText("80,000+ word dictionary. No internet required.")

        InfoText(
            "Developed by ArtMondo",
            color = GameColors.uiTextDim
        )
        InfoText(
            "Version 1.0.0",
            color = GameColors.uiTextDim
        )
    }
}

@Composable
private fun GamePlayContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoHeader("Basics")
        BulletPoint("Trace a path through adjacent hexagonal tiles to spell a word (minimum 3 letters).")
        BulletPoint("Each tile can only be used once per word.")
        BulletPoint("Words must be in the dictionary to count.")
        BulletPoint("Invalid words (3+ letters, not in dictionary) cost a move.")

        Spacer(modifier = Modifier.height(4.dp))
        InfoHeader("Modes")

        ModeDescription(
            "Simple",
            "Form the required words to complete each level. Tiles stay on the board and can be reused across words.",
            "Focus on longer words for higher scores. Plan your path before tracing."
        )
        ModeDescription(
            "Clear",
            "Sweep the board clean. Each word you form removes those tiles from the board.",
            "If only 1\u20132 tiles remain in a cluster with no valid word, the game auto-clears them for you."
        )
        ModeDescription(
            "Chain",
            "Form words one at a time. After each word, the tiles you used get new random letters. To build combos, make your next word pass through the same tile positions (shown with a blue pulse).",
            "If the blue tiles don't form a usable word, play elsewhere \u2014 you'll lose the combo but can start a new chain."
        )
        ModeDescription(
            "Illuminate",
            "Light up the board. Each word permanently illuminates the tiles it passes through. Cover every tile to win.",
            "Prioritize unlit tiles at the edges \u2014 they're harder to reach later."
        )

        Spacer(modifier = Modifier.height(4.dp))
        InfoHeader("Anchors")
        InfoText("Some levels have Anchor cells (red rim). Every word you form must pass through an Anchor tile.")

        InfoHeader("Scoring")
        BulletPoint("Points are based on letter values and word length.")
        BulletPoint("Harder modes have higher score multipliers.")
        BulletPoint("Earn 1\u20133 stars per level based on your score.")

        InfoHeader("Sharing")
        InfoText("Tap the share button (bottom-right of the board) to copy a link to your current board. Send it to a friend \u2014 they'll get the exact same board layout so you can compare scores!")

        InfoHeader("Dictionary")
        InfoText("Boxters uses a curated dictionary of 80,000+ English words.")
        BulletPoint("Word length: 3 to 8 letters only.")
        BulletPoint("Base source: ENABLE1 (Enhanced North American Benchmark Lexicon) \u2014 the standard word list used in competitive English word games.")
        BulletPoint("Modern additions: Common slang, widely-used acronyms, and words from major dictionaries.")
        BulletPoint("Not included: Proper nouns, hyphenated words, words longer than 8 letters, or obscure abbreviations.")

        InfoHeader("Lives")
        InfoText("You start with 3 lives. Failing a level costs a life. Lose all 3 lives and you'll need to wait 5 minutes before retrying with fresh lives.")
    }
}

@Composable
private fun PrivacyContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoHeader("Privacy Policy")

        InfoText("Boxters is committed to protecting your privacy. This policy explains what data is stored, how it is used, and your rights regarding that data.")

        InfoHeader("Data Collection")
        InfoText("Boxters does not collect, transmit, or share any personal information.")
        BulletPoint("No personal data is collected, processed, or sent to any server.")
        BulletPoint("No advertising SDKs are integrated.")
        BulletPoint("No account creation or internet connection is required to play.")

        InfoHeader("Local Storage")
        InfoText("Your game progress (player name, scores, level progress, and settings) is stored locally on your device using SharedPreferences. This data:")
        BulletPoint("Never leaves your device.")
        BulletPoint("Is accessible only to you.")
        BulletPoint("Is not shared across devices.")
        BulletPoint("Can be cleared at any time by clearing the app's data in Android Settings.")

        InfoHeader("Your Rights")
        InfoText("You have full control over your data:")
        BulletPoint("Access: Your data is stored on your device and visible only to you.")
        BulletPoint("Deletion: Clear all saved data by going to Android Settings > Apps > Boxters > Clear Data.")
        BulletPoint("Portability: No cloud data exists to export \u2014 all data is local.")

        InfoHeader("Children's Privacy")
        InfoText("Boxters does not collect any personal data from any users, including children.")

        InfoHeader("Changes to This Policy")
        InfoText("Any updates to this privacy policy will be reflected here with a revised effective date.")

        InfoText(
            "Last updated: August 2026",
            color = GameColors.uiTextDim
        )
    }
}

@Composable
private fun ContactContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoHeader("Contact Us")

        InfoText("Report a bug, suggest a word for the dictionary, share feature ideas, or send us feedback.")

        InfoText("Email us at:")
        Text(
            text = "hello@artmondo.com",
            style = GameTypography.body.copy(
                color = GameColors.uiAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
        InfoText("Think a word should be in the dictionary? Let us know!")
    }
}

// --- Reusable building blocks ---

@Composable
private fun InfoHeader(text: String) {
    Text(
        text = text,
        style = GameTypography.levelName.copy(fontSize = 18.sp),
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun InfoText(
    text: String,
    color: androidx.compose.ui.graphics.Color = GameColors.uiText
) {
    Text(
        text = text,
        style = GameTypography.body.copy(fontSize = 14.sp, color = color),
        lineHeight = 20.sp
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = "\u2022  ",
            style = GameTypography.body.copy(fontSize = 14.sp),
        )
        Text(
            text = text,
            style = GameTypography.body.copy(fontSize = 14.sp),
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ModeDescription(name: String, description: String, tip: String) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GameColors.uiAccent)) {
                    append(name)
                }
                append(" \u2014 $description")
            },
            style = GameTypography.body.copy(fontSize = 14.sp),
            lineHeight = 20.sp
        )
        Text(
            text = "Tip: $tip",
            style = GameTypography.body.copy(
                fontSize = 13.sp,
                color = GameColors.uiTextDim,
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            lineHeight = 18.sp
        )
    }
}
