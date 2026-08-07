package com.artmondo.boxters.domain.scoring

import com.artmondo.boxters.data.model.Board
import com.artmondo.boxters.data.model.LevelData
import com.artmondo.boxters.domain.objectives.ObjectiveTracker
import com.artmondo.boxters.util.Combo
import com.artmondo.boxters.util.LetterValues
import com.artmondo.boxters.util.Scoring
import kotlin.math.floor
import kotlin.math.min

object ScoringEngine {

    fun calculateMoveScore(word: String, tier: String, comboCount: Int = 0): Int {
        var letterScore = 0
        for (ch in word.uppercase()) {
            letterScore += LetterValues.valueOf(ch)
        }
        if (tier == "chain" && comboCount > 0) {
            val comboMult = Combo.MULTIPLIERS[min(comboCount, Combo.MAX_COMBO)]
            letterScore = floor(letterScore * comboMult).toInt()
        }
        return letterScore
    }

    fun calculateLevelScore(
        board: Board,
        tier: String,
        objectivesTracker: ObjectiveTracker,
        levelData: LevelData
    ): Int {
        val base = Scoring.BASE_TIER[tier] ?: 50
        val multiplier = Scoring.MULTIPLIER_TIER[tier] ?: 1.0f
        val levelMult = levelData.scoreMult

        var letterTotal = 0
        for (move in board.moveHistory) {
            for (ch in move.word.uppercase()) {
                letterTotal += LetterValues.valueOf(ch)
            }
        }

        val efficiency = board.movesRemaining * Scoring.EFFICIENCY_PER_MOVE
        val secondaryBonus = if (objectivesTracker.allSecondaryComplete)
            Scoring.SECONDARY_COMPLETION_BONUS else 0

        val rawScore = (base + letterTotal + efficiency + secondaryBonus) * multiplier * levelMult
        return floor(rawScore).toInt()
    }

    fun getStars(score: Int, level: LevelData): Int {
        return when {
            score >= level.threeStarTarget -> 3
            score >= level.twoStarTarget -> 2
            else -> 1
        }
    }
}
