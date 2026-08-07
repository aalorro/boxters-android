package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artmondo.boxters.ui.theme.*

@Composable
fun RegisterScreen(onRegister: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Boxters",
                style = GameTypography.title.copy(fontSize = 48.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find words. Unlock modes. Master the board.",
                style = GameTypography.body.copy(
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Light,
                    color = GameColors.uiTextDim,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 20) name = it },
                placeholder = { Text("Stargazer", color = GameColors.uiTextDim) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (name.isNotBlank()) onRegister(name) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = GameColors.uiText,
                    unfocusedTextColor = GameColors.uiText,
                    focusedBorderColor = GameColors.uiAccent,
                    unfocusedBorderColor = GameColors.uiPanelBorder,
                    cursorColor = GameColors.uiAccent
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (name.isNotBlank()) onRegister(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.uiAccent,
                    contentColor = GameColors.backgroundDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Begin Journey",
                    style = GameTypography.button.copy(fontSize = 18.sp)
                )
            }
        }
    }
}
