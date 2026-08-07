package com.artmondo.boxters.domain.tracer

import com.artmondo.boxters.data.model.HexCell
import com.artmondo.boxters.data.model.HexCoord
import com.artmondo.boxters.data.model.Field
import com.artmondo.boxters.domain.dictionary.Dictionary
import com.artmondo.boxters.domain.hex.HexMath

enum class TraceResult { NONE, EXTENDED, BACKTRACK }

data class TraceEndResult(
    val path: List<HexCell>,
    val word: String,
    val isValid: Boolean,
    val isPrefix: Boolean
)

class WordTracer(
    private var field: Field,
    private val dictionary: Dictionary
) {
    val path = mutableListOf<HexCell>()
    var isActive = false
        private set

    fun setField(newField: Field) {
        field = newField
    }

    fun start(coord: HexCoord): Boolean {
        val cell = field.getCell(coord.q, coord.r)
        if (cell == null || cell.letter == null || !cell.isActive) return false

        path.clear()
        path.add(cell)
        isActive = true
        return true
    }

    fun extend(coord: HexCoord): TraceResult {
        if (!isActive || path.isEmpty()) return TraceResult.NONE

        val cell = field.getCell(coord.q, coord.r)
        if (cell == null || cell.letter == null || !cell.isActive) return TraceResult.NONE

        val lastCell = path.last()

        // Same cell as last — ignore
        if (cell.key == lastCell.key) return TraceResult.NONE

        // Backtrack: if this is the second-to-last cell, pop the last
        if (path.size >= 2) {
            val prevCell = path[path.size - 2]
            if (cell.key == prevCell.key) {
                path.removeAt(path.lastIndex)
                return TraceResult.BACKTRACK
            }
        }

        // Check adjacency
        if (!HexMath.areAdjacent(lastCell.coord, cell.coord)) return TraceResult.NONE

        // Check not already in path
        if (path.any { it.key == cell.key }) return TraceResult.NONE

        // Valid extension
        path.add(cell)
        return TraceResult.EXTENDED
    }

    fun end(): TraceEndResult {
        isActive = false
        val result = TraceEndResult(
            path = path.toList(),
            word = currentWord,
            isValid = isValidWord,
            isPrefix = isValidPrefix
        )
        path.clear()
        return result
    }

    fun cancel() {
        isActive = false
        path.clear()
    }

    val currentWord: String
        get() = path.mapNotNull { it.letter }.joinToString("")

    val isValidWord: Boolean
        get() {
            val word = currentWord
            return word.length >= 3 && dictionary.isWord(word)
        }

    val isValidPrefix: Boolean
        get() {
            val word = currentWord
            return word.isNotEmpty() && dictionary.isPrefix(word)
        }
}
