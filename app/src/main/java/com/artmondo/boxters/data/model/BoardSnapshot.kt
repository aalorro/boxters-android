package com.artmondo.boxters.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BoardSnapshot(
    val levelIndex: Int,
    val cells: List<CellSnapshot>,
    val moveHistory: List<MoveSnapshot>,
    val movesRemaining: Int,
    val score: Int,
    val wordsUsed: List<String>,
    val objectivesProgress: List<ObjectiveSnapshot>,
    val comboCount: Int,
    val lastWordCellKeys: List<String>,
    val lives: Int
)

@Serializable
data class CellSnapshot(
    val q: Int,
    val r: Int,
    val letter: Char?,
    val cellType: String,
    val isActive: Boolean,
    val isCleared: Boolean,
    val isIlluminated: Boolean,
    val usedInMove: Boolean
)

@Serializable
data class MoveSnapshot(
    val word: String,
    val pathKeys: List<String>
)

@Serializable
data class ObjectiveSnapshot(
    val type: String,
    val progress: Int,
    val completed: Boolean
)
