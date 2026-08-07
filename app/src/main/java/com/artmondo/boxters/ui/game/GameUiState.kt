package com.artmondo.boxters.ui.game

import androidx.compose.ui.geometry.Offset
import com.artmondo.boxters.data.model.*

enum class TraceValidation { NONE, PREFIX, VALID, INVALID }

data class HexCellState(
    val q: Int,
    val r: Int,
    val key: String,
    val letter: Char?,
    val cellType: CellType,
    val isActive: Boolean,
    val isCleared: Boolean,
    val isIlluminated: Boolean,
    val isAnchor: Boolean,
    val glowIntensity: Float,
    val usedInMove: Boolean
)

data class ObjectiveState(
    val type: String,
    val description: String,
    val target: Int,
    val progress: Int,
    val completed: Boolean,
    val isPrimary: Boolean
)

data class PlayerStats(
    val gamesPlayed: Int,
    val levelsCompleted: Int,
    val bestScore: Int
)

data class SolutionWord(
    val word: String,
    val path: List<HexCoord>
)

data class GameUiState(
    val gameState: GameState = GameState.LOADING,
    val cells: List<HexCellState> = emptyList(),
    val tracePath: List<HexCoord> = emptyList(),
    val traceWord: String = "",
    val traceValidation: TraceValidation = TraceValidation.NONE,
    val hexSize: Float = 47f,
    val boardCenter: Offset = Offset.Zero,
    val mode: GameMode = GameMode.SIMPLE,
    val levelData: LevelData? = null,
    val modeLevelNum: Int = 1,
    val movesRemaining: Int = 0,
    val maxMoves: Int = 0,
    val score: Int = 0,
    val totalScore: Int = 0,
    val lives: Int = 3,
    val stars: Int = 0,
    val objectives: List<ObjectiveState> = emptyList(),
    val wordsFormed: List<String> = emptyList(),
    val feedbackMessage: String? = null,
    val feedbackTimer: Float = 0f,
    val tutorialMessage: String? = null,
    val comboCount: Int = 0,
    val comboMultiplier: Float = 1.0f,
    val percentLit: Int = 0,
    val cellsRemaining: Int = 0,
    val lastWordCellKeys: Set<String> = emptySet(),
    // Overlays
    val solutionWords: List<SolutionWord> = emptyList(),
    val highlightedSolutionPath: List<HexCoord>? = null,
    val cooldownUntil: Long = 0,
    val cooldownRemaining: String = "",
    val isUltimateVictory: Boolean = false,
    val victoryTitle: String = "",
    val victorySubtitle: String = "",
    // Audio
    val audioEnabled: Boolean = true,
    // Navigation
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val hasMovesInProgress: Boolean = false,
    // Menu
    val playerName: String = "",
    val isRegistered: Boolean = false,
    val playerStats: PlayerStats? = null,
    val unlockedModes: List<GameMode> = listOf(GameMode.SIMPLE),
    val selectedMode: GameMode = GameMode.SIMPLE,
    // Loading
    val dictionaryLoaded: Boolean = false
)
