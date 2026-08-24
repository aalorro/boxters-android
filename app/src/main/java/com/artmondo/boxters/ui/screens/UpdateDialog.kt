package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artmondo.boxters.ui.theme.GameColors
import com.artmondo.boxters.ui.theme.GameTypography

@Composable
fun UpdateDialog(
    onUpdate: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(GameColors.backgroundDark, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Update Available",
                style = GameTypography.levelName.copy(fontSize = 22.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "A new version of Boxters is available. Update now for the latest features and improvements.",
                style = GameTypography.body.copy(fontSize = 15.sp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Update button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GameColors.uiAccent)
                    .clickable { onUpdate() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Update",
                    style = GameTypography.button.copy(
                        fontSize = 17.sp,
                        color = GameColors.backgroundDark
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Later button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GameColors.uiPanel)
                    .clickable { onDismiss() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Later",
                    style = GameTypography.button.copy(
                        fontSize = 17.sp,
                        color = GameColors.uiText
                    )
                )
            }
        }
    }
}
