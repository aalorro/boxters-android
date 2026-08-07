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
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
                        onPlay = viewModel::onPlayClicked
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
                    onLogout = viewModel::onLogout,
                    onShare = viewModel::onShareBoard,
                    onNavigateLevel = viewModel::onNavigateLevel,
                    onSolutionWordSelected = viewModel::onSolutionWordSelected,
                    onFrame = viewModel::onFrame
                )
            }
        }
    }
}
