package com.artmondo.boxters.data.model

import com.artmondo.boxters.domain.dictionary.Dictionary
import com.artmondo.boxters.domain.hex.HexMath

class Field {
    val cells: MutableMap<String, HexCell> = mutableMapOf()
    val wordsFormed: MutableList<String> = mutableListOf()

    fun addCell(cell: HexCell) {
        cells[cell.key] = cell
    }

    fun getCell(q: Int, r: Int): HexCell? = cells["$q,$r"]

    fun getCellByKey(key: String): HexCell? = cells[key]

    fun hasCell(q: Int, r: Int): Boolean = cells.containsKey("$q,$r")

    fun getNeighborCells(q: Int, r: Int): List<HexCell> {
        return HexMath.getNeighbors(q, r).mapNotNull { cells[it.key] }
    }

    fun getAllCells(): List<HexCell> = cells.values.toList()

    fun getActiveCells(): List<HexCell> = cells.values.filter { it.isActive && !it.isCleared }

    fun findWordsWithPaths(dictionary: Dictionary, maxResults: Int = 8): List<WordWithPath> {
        val results = mutableListOf<WordWithPath>()
        val seen = mutableSetOf<String>()
        val allCells = cells.values.filter { it.letter != null && it.isActive }

        for (startCell in allCells) {
            traceWordsWithPaths(startCell, mutableListOf(startCell), dictionary, results, seen)
        }

        results.sortByDescending { it.word.length }
        return results.take(maxResults)
    }

    fun allCellsCoverable(dictionary: Dictionary): Boolean {
        val allCells = cells.values.filter { it.letter != null && it.isActive }
        val covered = mutableSetOf<String>()
        val total = allCells.size
        val seen = mutableSetOf<String>()

        for (startCell in allCells) {
            if (covered.size >= total) return true
            traceForCoverage(startCell, mutableListOf(startCell), dictionary, covered, seen, total)
        }

        return covered.size >= total
    }

    private fun traceForCoverage(
        current: HexCell,
        path: MutableList<HexCell>,
        dictionary: Dictionary,
        covered: MutableSet<String>,
        seen: MutableSet<String>,
        total: Int
    ) {
        if (covered.size >= total) return
        val word = path.map { it.letter }.joinToString("")
        if (word.length >= 3 && dictionary.isWord(word) && word !in seen) {
            seen.add(word)
            for (c in path) covered.add(c.key)
            if (covered.size >= total) return
        }
        if (word.length >= 8 || !dictionary.isPrefix(word)) return

        val neighbors = getNeighborCells(current.q, current.r)
        for (next in neighbors) {
            if (next.letter != null && next.isActive && next !in path) {
                path.add(next)
                traceForCoverage(next, path, dictionary, covered, seen, total)
                path.removeAt(path.lastIndex)
                if (covered.size >= total) return
            }
        }
    }

    private fun traceWordsWithPaths(
        current: HexCell,
        path: MutableList<HexCell>,
        dictionary: Dictionary,
        found: MutableList<WordWithPath>,
        seen: MutableSet<String>
    ) {
        val word = path.map { it.letter }.joinToString("")
        if (word.length >= 3 && dictionary.isWord(word) && word !in seen) {
            seen.add(word)
            found.add(WordWithPath(word, path.map { HexCoord(it.q, it.r) }))
        }
        if (word.length >= 8 || !dictionary.isPrefix(word)) return

        val neighbors = getNeighborCells(current.q, current.r)
        for (next in neighbors) {
            if (next.letter != null && next.isActive && next !in path) {
                path.add(next)
                traceWordsWithPaths(next, path, dictionary, found, seen)
                path.removeAt(path.lastIndex)
            }
        }
    }
}

data class WordWithPath(val word: String, val path: List<HexCoord>)
