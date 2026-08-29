package com.artmondo.boxters.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.artmondo.boxters.data.model.GameState
import com.artmondo.boxters.ui.canvas.GameCanvas
import com.artmondo.boxters.ui.game.GameEvent
import com.artmondo.boxters.ui.game.GameViewModel

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    deepLinkUri: String? = null,
    updateAvailable: Boolean = false,
    onUpdateClicked: () -> Unit = {},
    onUpdateDismissed: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showLeaderboard by remember { mutableStateOf(false) }
    var updateDismissed by remember { mutableStateOf(false) }

    // Save board state when app goes to background
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.saveBoardOnPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Handle deep link
    LaunchedEffect(deepLinkUri) {
        if (deepLinkUri != null && uiState.dictionaryLoaded) {
            viewModel.handleDeepLink(deepLinkUri)
        }
    }

    // Handle back button - return to menu from game
    BackHandler(
        enabled = uiState.gameState != GameState.LOADING && uiState.gameState != GameState.MENU
    ) {
        viewModel.onLogout()
    }

    // Handle one-shot events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEvent.PlayTone -> viewModel.audioManager.playTone(event.letter)
                is GameEvent.PlayWordChord -> viewModel.audioManager.playWordChord(event.word)
                is GameEvent.PlayError -> viewModel.audioManager.playError()
                is GameEvent.PlayVictoryFanfare -> viewModel.audioManager.playVictory()
                is GameEvent.PlayUltimateVictory -> viewModel.audioManager.playUltimateVictory()
                is GameEvent.PlayFail -> viewModel.audioManager.playFail()
                is GameEvent.PlayPuzzleLoop -> viewModel.audioManager.playPuzzleLoop()
                is GameEvent.StopPuzzleLoop -> viewModel.audioManager.stopPuzzleLoop()
                is GameEvent.ShowFeedback -> {} // Handled via uiState
                is GameEvent.CopyToClipboard -> {
                    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(
                        android.content.ClipData.newPlainText("Boxters", event.text)
                    )
                }
            }
        }
    }

    Box(modifier = modifier.systemBarsPadding()) {
        when (uiState.gameState) {
            GameState.LOADING -> LoadingScreen()
            GameState.MENU -> {
                if (!uiState.isRegistered) {
                    RegisterScreen(onRegister = viewModel::onRegister)
                } else {
                    WelcomeScreen(
                        uiState = uiState,
                        onModeSelected = viewModel::onModeSelected,
                        onPlay = viewModel::onPlayClicked,
                        onRandom = viewModel::onRandomClicked,
                        onToggleSound = viewModel::onToggleSound,
                        onToggleTheme = viewModel::onToggleTheme,
                        onInfoClicked = { showInfoDialog = true },
                        onSettingsClicked = { showSettingsDialog = true },
                        onLeaderboardClicked = {
                            showLeaderboard = true
                            viewModel.fetchLeaderboard()
                        }
                    )
                }
            }
            else -> {
                GameCanvas(
                    uiState = uiState,
                    particleSystem = viewModel.particleSystem,
                    onTraceStart = viewModel::onTraceStart,
                    onTraceMove = viewModel::onTraceMove,
                    onTraceEnd = viewModel::onTraceEnd,
                    onVictoryTapped = viewModel::onVictoryTapped,
                    onDefeatContinue = viewModel::onDefeatContinue,
                    onCooldownTapped = viewModel::onCooldownTapped,
                    onToggleSound = viewModel::onToggleSound,
                    onInfoClicked = { showInfoDialog = true },
                    onLogout = viewModel::onLogout,
                    onShare = viewModel::onShareBoard,
                    onLoad = {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        viewModel.onLoadBoard(text)
                    },
                    onNavigateLevel = viewModel::onNavigateLevel,
                    onSolutionWordSelected = viewModel::onSolutionWordSelected,
                    onFrame = viewModel::onFrame,
                    onGameCompleteAction = viewModel::onGameCompleteAction
                )
            }
        }

        if (showInfoDialog) {
            InfoDialog(onDismiss = { showInfoDialog = false })
        }

        if (showSettingsDialog) {
            SettingsDialog(
                playerName = uiState.playerName,
                isDarkMode = uiState.isDarkMode,
                gender = uiState.gender,
                ageGroup = uiState.ageGroup,
                onToggleTheme = viewModel::onToggleTheme,
                onNameChanged = viewModel::onNameChanged,
                onGenderChanged = viewModel::onGenderChanged,
                onAgeGroupChanged = viewModel::onAgeGroupChanged,
                onExportProfile = {
                    val encoded = viewModel.onExportProfile()
                    if (encoded != null) {
                        val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                        clipboard?.setPrimaryClip(
                            android.content.ClipData.newPlainText("Boxters Profile", encoded)
                        )
                    }
                },
                onImportProfile = {
                    val clipboard = context.getSystemService(android.content.ClipboardManager::class.java)
                    val text = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    if (text != null) {
                        viewModel.onImportProfile(text)
                    }
                },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showLeaderboard) {
            LeaderboardDialog(
                entries = uiState.leaderboard,
                playerRank = uiState.playerRank,
                playerId = uiState.playerId,
                playerEntry = uiState.playerLeaderboardEntry,
                isLoading = uiState.leaderboardLoading,
                onDismiss = { showLeaderboard = false }
            )
        }

        if (updateAvailable && !updateDismissed && uiState.gameState == GameState.MENU) {
            UpdateDialog(
                onUpdate = onUpdateClicked,
                onDismiss = {
                    updateDismissed = true
                    onUpdateDismissed()
                }
            )
        }
    }
}
