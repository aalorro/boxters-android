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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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
                            .defaultMinSize(minHeight = 36.dp)
                            .background(
                                if (isSelected) GameColors.uiAccent else GameColors.uiPanel,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = tab }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.label,
                            style = GameTypography.modeBadge.copy(
                                fontSize = 11.sp,
                                color = if (isSelected) GameColors.backgroundDark else GameColors.uiTextDim
                            ),
                            textAlign = TextAlign.Center
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
        BulletPoint("Tap the share button to copy your board link. Send it to a friend to challenge them on the same board!")
        BulletPoint("To load a shared board, copy the link, then tap the load button (left of the share button).")

        Spacer(modifier = Modifier.height(4.dp))
        InfoText("80,000+ word dictionary. No internet required.")

        InfoText(
            "Developed by ArtMondo",
            color = GameColors.uiTextDim
        )
        InfoText(
            "Version 1.1.1",
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

        InfoHeader("Sharing & Loading")
        InfoText("Tap the share button (bottom-right of the board) to copy a link to your current board. Send it to a friend \u2014 they'll get the exact same board layout so you can compare scores!")
        InfoText("To load a shared board, copy the link to your clipboard, then tap the load button (to the left of the share button). The board will load even if you haven't unlocked that level yet.")

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
        InfoText("Effective Date: August 6, 2026", color = GameColors.uiTextDim)

        InfoText("Boxters is committed to protecting your privacy. This policy explains what data is stored, how it is used, and your rights regarding that data.")

        InfoHeader("Data Collection")
        InfoText("Boxters does not collect, transmit, or share any personal information.")
        BulletPoint("No personal data is collected, processed, or sent to any server.")
        BulletPoint("No advertising SDKs are integrated.")
        BulletPoint("No account creation or internet connection is required to play.")
        BulletPoint("No analytics or tracking tools are used in this app.")

        InfoHeader("Contact Form")
        InfoText("If you choose to use the Contact form, the name, email, and message you enter are sent to Formspree for delivery. This data is not stored by Boxters and is subject to Formspree's privacy policy. Submitting the form is entirely optional.")

        InfoHeader("Local Storage")
        InfoText("Your game progress (player name, scores, level progress, and settings) is stored locally on your device using SharedPreferences. This data:")
        BulletPoint("Never leaves your device.")
        BulletPoint("Is accessible only to you within this app.")
        BulletPoint("Is not shared across devices or apps.")
        BulletPoint("Can be cleared at any time (see below).")

        InfoHeader("Your Rights")
        InfoText("You have full control over your data:")
        BulletPoint("Access: Your data is stored on your device and visible to you at any time.")
        BulletPoint("Deletion: Clear all saved data by going to Android Settings > Apps > Boxters > Storage > Clear Data.")
        BulletPoint("Portability: No cloud data exists to export \u2014 all data is local.")

        InfoHeader("Children's Privacy")
        InfoText("Boxters does not collect any personal data from any users, including children.")

        InfoHeader("Changes to This Policy")
        InfoText("Any updates to this privacy policy will be reflected here with a revised effective date.")

        InfoText(
            "Last updated: August 9, 2026",
            color = GameColors.uiTextDim
        )
    }
}

private enum class FormStatus { IDLE, SENDING, SUCCESS, ERROR }

@Composable
private fun ContactContent() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(FormStatus.IDLE) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedTextColor = GameColors.uiText,
        focusedTextColor = GameColors.uiText,
        cursorColor = GameColors.uiAccent,
        unfocusedBorderColor = GameColors.uiPanelBorder,
        focusedBorderColor = GameColors.uiAccent,
        unfocusedLabelColor = GameColors.uiTextDim,
        focusedLabelColor = GameColors.uiAccent,
        unfocusedContainerColor = GameColors.backgroundMid,
        focusedContainerColor = GameColors.backgroundMid
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoHeader("Contact Us")
        InfoText("Report a bug, suggest a word for the dictionary, share feature ideas, or send us feedback.")

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            enabled = status != FormStatus.SENDING,
            colors = fieldColors,
            textStyle = GameTypography.body.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            enabled = status != FormStatus.SENDING,
            colors = fieldColors,
            textStyle = GameTypography.body.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            minLines = 4,
            maxLines = 6,
            enabled = status != FormStatus.SENDING,
            colors = fieldColors,
            textStyle = GameTypography.body.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                status = FormStatus.SENDING
                scope.launch {
                    try {
                        val success = submitContactForm(name, email, message)
                        if (success) {
                            status = FormStatus.SUCCESS
                            name = ""
                            email = ""
                            message = ""
                        } else {
                            status = FormStatus.ERROR
                            errorMessage = "Failed to send. Please try again."
                        }
                    } catch (e: Exception) {
                        status = FormStatus.ERROR
                        errorMessage = "Network error. Check your connection."
                    }
                }
            },
            enabled = status != FormStatus.SENDING && message.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = GameColors.uiAccent,
                contentColor = GameColors.backgroundDark,
                disabledContainerColor = GameColors.uiPanel,
                disabledContentColor = GameColors.uiTextDim
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (status == FormStatus.SENDING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = GameColors.backgroundDark,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Send", style = GameTypography.modeBadge)
            }
        }

        when (status) {
            FormStatus.SUCCESS -> InfoText("Message sent! We'll get back to you soon.", color = GameColors.uiSuccess)
            FormStatus.ERROR -> InfoText(errorMessage, color = GameColors.uiError)
            else -> {}
        }
    }
}

private suspend fun submitContactForm(name: String, email: String, message: String): Boolean {
    return withContext(Dispatchers.IO) {
        val url = URL("https://formspree.io/f/mgawvrkq")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true

            val jsonBody = buildString {
                append("{")
                append("\"name\":\"${name.replace("\"", "\\\"")}\"")
                append(",\"email\":\"${email.replace("\"", "\\\"")}\"")
                append(",\"message\":\"${message.replace("\"", "\\\"").replace("\n", "\\n")}\"")
                append("}")
            }

            conn.outputStream.use { it.write(jsonBody.toByteArray()) }
            conn.responseCode in 200..299
        } finally {
            conn.disconnect()
        }
    }
}

// --- Reusable building blocks ---

@Composable
private fun InfoHeader(text: String) {
    Text(
        text = text,
        style = GameTypography.levelName.copy(fontSize = 16.sp),
        modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
    )
}

@Composable
private fun InfoText(
    text: String,
    color: androidx.compose.ui.graphics.Color = GameColors.uiText
) {
    Text(
        text = text,
        style = GameTypography.body.copy(fontSize = 13.sp, color = color),
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(modifier = Modifier.padding(start = 8.dp).fillMaxWidth()) {
        Text(
            text = "\u2022  ",
            style = GameTypography.body.copy(fontSize = 13.sp),
        )
        Text(
            text = text,
            style = GameTypography.body.copy(fontSize = 13.sp),
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ModeDescription(name: String, description: String, tip: String) {
    Column(modifier = Modifier.padding(start = 8.dp).fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GameColors.uiAccent)) {
                    append(name)
                }
                append(" \u2014 $description")
            },
            style = GameTypography.body.copy(fontSize = 13.sp),
            lineHeight = 18.sp
        )
        Text(
            text = "Tip: $tip",
            style = GameTypography.body.copy(
                fontSize = 12.sp,
                color = GameColors.uiTextDim,
                fontWeight = FontWeight.Light
            ),
            modifier = Modifier.padding(start = 8.dp, top = 4.dp),
            lineHeight = 16.sp
        )
    }
}
