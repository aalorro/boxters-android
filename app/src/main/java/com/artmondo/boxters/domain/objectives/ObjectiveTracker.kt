package com.artmondo.boxters.domain.objectives

import com.artmondo.boxters.data.model.Board
import com.artmondo.boxters.data.model.Move
import com.artmondo.boxters.data.model.ObjectiveDef
import kotlin.math.floor
import kotlin.math.max

class Objective(
    val type: String,
    val description: String,
    var target: Int,
    val params: Map<String, Int>,
    val isPrimary: Boolean,
    var progress: Int = 0,
    var completed: Boolean = false
) {
    fun check(board: Board, lastMove: Move?) {
        when (type) {
            "formWord" -> checkFormWord(lastMove)
            "formWordLength" -> checkFormWordLength(lastMove)
            "illuminateAnchors" -> checkAnchors(board)
            "useAllCells" -> checkAllCells(board)
            "totalWords" -> checkTotalWords(board)
            "clearAllCells" -> checkClearAllCells(board)
            "achieveCombo" -> checkAchieveCombo(board)
            "illuminatePercent" -> checkIlluminatePercent(board)
        }
    }

    private fun checkFormWord(lastMove: Move?) {
        val minLen = params["minLength"] ?: 3
        if (lastMove != null && lastMove.word.length >= minLen) {
            progress++
            if (progress >= target) completed = true
        }
    }

    private fun checkFormWordLength(lastMove: Move?) {
        val minLen = params["minLength"] ?: 3
        if (lastMove != null && lastMove.word.length >= minLen) {
            progress++
            if (progress >= target) completed = true
        }
    }

    private fun checkAnchors(board: Board) {
        val anchors = board.field.getAllCells().filter { it.isAnchor }
        val illuminated = anchors.filter { it.isIlluminated || it.usedInMove }
        progress = illuminated.size
        target = anchors.size
        completed = illuminated.size >= anchors.size
    }

    private fun checkAllCells(board: Board) {
        val total = board.field.getAllCells().size
        val illuminated = board.field.getAllCells().count { it.isIlluminated }
        progress = illuminated
        target = total
        completed = illuminated >= total
    }

    private fun checkTotalWords(board: Board) {
        progress = board.moveHistory.size
        completed = progress >= target
    }

    private fun checkClearAllCells(board: Board) {
        val total = board.field.getAllCells().size
        val cleared = board.field.getAllCells().count { it.isCleared }
        progress = cleared
        target = total
        completed = cleared >= total
    }

    private fun checkAchieveCombo(board: Board) {
        progress = max(progress, board.comboCount)
        completed = progress >= target
    }

    private fun checkIlluminatePercent(board: Board) {
        val allCells = board.field.getAllCells()
        val illuminated = allCells.count { it.isIlluminated }
        val pct = if (allCells.isNotEmpty()) floor(illuminated.toFloat() / allCells.size * 100).toInt() else 0
        progress = pct
        completed = pct >= target
    }
}

class ObjectiveTracker(objectiveDefs: List<ObjectiveDef>) {
    val primary: List<Objective> = objectiveDefs.filter { it.isPrimary }.map {
        Objective(it.type, it.description, it.target, it.params, true)
    }
    val secondary: List<Objective> = objectiveDefs.filter { !it.isPrimary }.map {
        Objective(it.type, it.description, it.target, it.params, false)
    }

    fun checkAll(board: Board, lastMove: Move?) {
        val allObjs = primary + secondary
        val formWordObjs = allObjs.filter {
            (it.type == "formWord" || it.type == "formWordLength") && !it.completed
        }

        var wordClaimed = false
        if (lastMove != null && formWordObjs.size > 1) {
            val sorted = formWordObjs.sortedByDescending { it.params["minLength"] ?: 3 }
            for (obj in sorted) {
                val minLen = obj.params["minLength"] ?: 3
                if (lastMove.word.length >= minLen && !wordClaimed) {
                    obj.check(board, lastMove)
                    wordClaimed = true
                }
            }
        }

        for (obj in primary) {
            if (wordClaimed && (obj.type == "formWord" || obj.type == "formWordLength")) continue
            obj.check(board, lastMove)
        }
        for (obj in secondary) {
            if (wordClaimed && (obj.type == "formWord" || obj.type == "formWordLength")) continue
            obj.check(board, lastMove)
        }
    }

    val allPrimaryComplete: Boolean
        get() = primary.all { it.completed }

    val allSecondaryComplete: Boolean
        get() = secondary.isEmpty() || secondary.all { it.completed }

    val allObjectives: List<Objective>
        get() = primary + secondary
}
