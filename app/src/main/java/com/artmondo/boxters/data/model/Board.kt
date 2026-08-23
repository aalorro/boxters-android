package com.artmondo.boxters.data.model

import com.artmondo.boxters.domain.board.LetterGenerator
import com.artmondo.boxters.util.Combo
import kotlin.random.Random

data class Move(val word: String, val path: List<HexCoord>)

data class MoveResult(
    val cleared: List<HexCoord> = emptyList(),
    val replaced: List<Pair<HexCoord, Char>> = emptyList(),
    val illuminated: List<HexCoord> = emptyList()
)

class Board {
    val field: Field = Field()
    val moveHistory: MutableList<Move> = mutableListOf()
    var movesRemaining: Int = 10
    var maxMoves: Int = 10
    var mode: String = "simple"
    var comboCount: Int = 0
    val lastWordCellKeys: MutableSet<String> = mutableSetOf()
    var apexWeights: ApexWeights? = null
    var lastApexEffect: String? = null

    fun executeMove(word: String, path: List<HexCell>): MoveResult {
        moveHistory.add(Move(word, path.map { HexCoord(it.q, it.r) }))
        movesRemaining--

        for (cell in path) {
            cell.usedInMove = true
        }

        val cleared = mutableListOf<HexCoord>()
        val replaced = mutableListOf<Pair<HexCoord, Char>>()
        val illuminated = mutableListOf<HexCoord>()

        when (mode) {
            "simple" -> {
                for (cell in path) {
                    cell.isIlluminated = true
                    cell.glowIntensity = 1.0f
                    illuminated.add(HexCoord(cell.q, cell.r))
                }
            }
            "clear" -> {
                for (cell in path) {
                    cleared.add(HexCoord(cell.q, cell.r))
                    cell.letter = null
                    cell.isCleared = true
                    cell.isActive = false
                }
            }
            "chain" -> {
                val currentKeys = path.map { it.key }.toSet()
                val hasOverlap = currentKeys.any { it in lastWordCellKeys }

                if (hasOverlap && lastWordCellKeys.isNotEmpty()) {
                    comboCount = minOf(comboCount + 1, Combo.MAX_COMBO)
                } else {
                    comboCount = 0
                }
                lastWordCellKeys.clear()
                lastWordCellKeys.addAll(currentKeys)

                for (cell in path) {
                    val newLetter = LetterGenerator.randomLetter()
                    cell.letter = newLetter
                    cell.glowIntensity = 1.0f
                    replaced.add(Pair(HexCoord(cell.q, cell.r), newLetter))
                }
            }
            "illuminate" -> {
                for (cell in path) {
                    cell.isIlluminated = true
                    cell.glowIntensity = 1.0f
                    illuminated.add(HexCoord(cell.q, cell.r))
                }
            }
            "apex" -> {
                // Pick random effect based on weights or uniform 1/3
                val chosenEffect = apexWeights?.let { w ->
                    val r = Random.nextFloat()
                    when {
                        r < w.clear -> "clear"
                        r < w.clear + w.chain -> "chain"
                        else -> "illuminate"
                    }
                } ?: listOf("clear", "chain", "illuminate").random()
                lastApexEffect = chosenEffect

                // All tiles are always illuminated first (counts toward % lit)
                for (cell in path) {
                    cell.isIlluminated = true
                    cell.glowIntensity = 1.0f
                }

                // Then apply the random effect on top
                when (chosenEffect) {
                    "clear" -> {
                        for (cell in path) {
                            if (cell.isAnchor) {
                                // Anchor cells can't be cleared — stay illuminated
                                illuminated.add(HexCoord(cell.q, cell.r))
                            } else {
                                cleared.add(HexCoord(cell.q, cell.r))
                                cell.letter = null
                                cell.isCleared = true
                                cell.isActive = false
                            }
                        }
                    }
                    "chain" -> {
                        val currentKeys = path.map { it.key }.toSet()
                        val hasOverlap = currentKeys.any { it in lastWordCellKeys }
                        if (hasOverlap && lastWordCellKeys.isNotEmpty()) {
                            comboCount = minOf(comboCount + 1, Combo.MAX_COMBO)
                        } else {
                            comboCount = 0
                        }
                        lastWordCellKeys.clear()
                        lastWordCellKeys.addAll(currentKeys)
                        for (cell in path) {
                            val newLetter = LetterGenerator.randomLetter()
                            cell.letter = newLetter
                            replaced.add(Pair(HexCoord(cell.q, cell.r), newLetter))
                        }
                    }
                    "illuminate" -> {
                        for (cell in path) {
                            illuminated.add(HexCoord(cell.q, cell.r))
                        }
                    }
                }
            }
        }

        return MoveResult(cleared, replaced, illuminated)
    }

    fun reset() {
        moveHistory.clear()
        movesRemaining = maxMoves
        comboCount = 0
        lastWordCellKeys.clear()
        lastApexEffect = null
    }
}
