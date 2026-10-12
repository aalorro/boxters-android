package com.artmondo.boxters.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.artmondo.boxters.ui.theme.*

@Composable
fun SettingsDialog(
    playerName: String,
    isDarkMode: Boolean,
    gender: String,
    ageGroup: String,
    onToggleTheme: () -> Unit,
    onGenderChanged: (String) -> Unit,
    onAgeGroupChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onExportProfile: () -> Unit,
    onImportProfile: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(GameColors.backgroundDark, RoundedCornerShape(16.dp))
                .padding(bottom = 20.dp)
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
                    text = "Settings",
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

            // Scrollable content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Player Name ---
                SettingsSection("Player Name") {
                    var editedName by remember { mutableStateOf(playerName) }
                    val nameChanged = editedName.trim() != playerName
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { if (it.length <= 20) editedName = it },
                        singleLine = true,
                        textStyle = GameTypography.body.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = GameColors.uiText,
                            focusedTextColor = GameColors.uiText,
                            unfocusedBorderColor = GameColors.uiPanelBorder,
                            focusedBorderColor = GameColors.uiAccent,
                            unfocusedContainerColor = GameColors.backgroundMid,
                            focusedContainerColor = GameColors.backgroundMid,
                            cursorColor = GameColors.uiAccent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (nameChanged && editedName.trim().length in 1..3) {
                        Text(
                            text = "Name must be at least 4 characters",
                            style = GameTypography.body.copy(
                                fontSize = 12.sp,
                                color = GameColors.uiTextDim
                            )
                        )
                    }
                    if (nameChanged && editedName.trim().length >= 4) {
                        Button(
                            onClick = {
                                onNameChanged(editedName)
                                editedName = editedName.trim()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameColors.uiAccent,
                                contentColor = GameColors.backgroundDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Save",
                                style = GameTypography.modeBadge.copy(fontSize = 13.sp)
                            )
                        }
                    }
                }

                // --- Theme Toggle ---
                SettingsSection("Theme") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isDarkMode) "Dark Mode" else "Light Mode",
                            style = GameTypography.body.copy(fontSize = 14.sp)
                        )
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onToggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GameColors.uiAccent,
                                checkedTrackColor = GameColors.uiAccent.copy(alpha = 0.3f),
                                uncheckedThumbColor = GameColors.uiTextDim,
                                uncheckedTrackColor = GameColors.uiPanel,
                                uncheckedBorderColor = GameColors.uiPanelBorder
                            )
                        )
                    }
                }

                // --- Gender Selector ---
                SettingsSection("Gender") {
                    SettingsDropdown(
                        selectedValue = gender,
                        options = listOf("", "Male", "Female", "Other", "Prefer not to say"),
                        displayLabels = listOf("Not set", "Male", "Female", "Other", "Prefer not to say"),
                        onSelected = onGenderChanged
                    )
                }

                // --- Age Group Selector ---
                SettingsSection("Age Group") {
                    SettingsDropdown(
                        selectedValue = ageGroup,
                        options = listOf("", "Under 18", "18-24", "25-34", "35-44", "45-54", "55+"),
                        displayLabels = listOf("Not set", "Under 18", "18-24", "25-34", "35-44", "45-54", "55+"),
                        onSelected = onAgeGroupChanged
                    )
                }

                // --- Export / Import Profile ---
                SettingsSection("Profile Data") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onExportProfile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GameColors.uiAccent,
                                contentColor = GameColors.backgroundDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Export",
                                style = GameTypography.modeBadge.copy(fontSize = 14.sp)
                            )
                        }
                        OutlinedButton(
                            onClick = onImportProfile,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GameColors.uiAccent
                            ),
                            border = BorderStroke(1.dp, GameColors.uiAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Import",
                                style = GameTypography.modeBadge.copy(fontSize = 14.sp)
                            )
                        }
                    }
                }

                // --- Close Button ---
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.uiPanel,
                        contentColor = GameColors.uiText
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text(
                        text = "Close",
                        style = GameTypography.modeBadge.copy(fontSize = 14.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = GameTypography.levelName.copy(fontSize = 14.sp),
            modifier = Modifier.fillMaxWidth()
        )
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdown(
    selectedValue: String,
    options: List<String>,
    displayLabels: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIndex = options.indexOf(selectedValue).coerceAtLeast(0)

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayLabels[selectedIndex],
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = GameTypography.body.copy(fontSize = 14.sp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = GameColors.uiText,
                focusedTextColor = GameColors.uiText,
                unfocusedBorderColor = GameColors.uiPanelBorder,
                focusedBorderColor = GameColors.uiAccent,
                unfocusedContainerColor = GameColors.backgroundMid,
                focusedContainerColor = GameColors.backgroundMid,
                unfocusedTrailingIconColor = GameColors.uiTextDim,
                focusedTrailingIconColor = GameColors.uiAccent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(GameColors.backgroundMid)
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayLabels[index],
                            style = GameTypography.body.copy(
                                fontSize = 14.sp,
                                color = if (option == selectedValue) GameColors.uiAccent else GameColors.uiText
                            )
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
