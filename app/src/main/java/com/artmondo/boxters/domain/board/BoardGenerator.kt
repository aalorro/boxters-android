package com.artmondo.boxters.domain.board

import com.artmondo.boxters.data.model.*
import com.artmondo.boxters.domain.dictionary.Dictionary
import com.artmondo.boxters.domain.hex.HexMath
import com.artmondo.boxters.util.Gameplay

object BoardGenerator {

    fun generateBoard(
        levelData: LevelData,
        dictionary: Dictionary,
        seed: Int
    ): Board {
        val board = Board()
        board.mode = levelData.mode
        board.maxMoves = levelData.maxMoves
        board.movesRemaining = levelData.maxMoves

        val radius = HexMath.shapeToRadius(levelData.layout.shape)
        val cells = HexMath.hexSpiral(HexCoord(0, 0), radius)

        // Create hex cells
        for (coord in cells) {
            board.field.addCell(HexCell(q = coord.q, r = coord.r))
        }

        val rng = SeededRNG(seed)

        // Place anchors
        if (levelData.layout.anchors > 0) {
            placeAnchors(board, levelData.layout.anchors, radius, rng)
        }

        // Generate letters based on mode
        when (levelData.mode) {
            "simple" -> generateSimpleBoard(board, levelData, dictionary, rng)
            "clear" -> generateClearBoard(board, levelData, dictionary, rng)
            "chain" -> generateChainBoard(board, levelData, dictionary, rng)
            "illuminate" -> generateIlluminateBoard(board, levelData, dictionary, rng)
        }

        return board
    }

    fun generateBoardFromLetters(
        levelData: LevelData,
        letters: String,
        anchorIndices: List<Int>
    ): Board {
        val board = Board()
        board.mode = levelData.mode
        board.maxMoves = levelData.maxMoves
        board.movesRemaining = levelData.maxMoves

        val radius = HexMath.shapeToRadius(levelData.layout.shape)
        val coords = HexMath.hexSpiral(HexCoord(0, 0), radius)

        for ((i, coord) in coords.withIndex()) {
            val cell = HexCell(q = coord.q, r = coord.r)
            if (i < letters.length) {
                cell.letter = letters[i]
            }
            if (i in anchorIndices) {
                cell.cellType = CellType.ANCHOR
            }
            board.field.addCell(cell)
        }

        return board
    }

    private fun placeAnchors(board: Board, count: Int, radius: Int, rng: SeededRNG) {
        val candidates = board.field.getAllCells().filter {
            HexMath.hexDistance(HexCoord(0, 0), HexCoord(it.q, it.r)) < radius
        }
        val shuffled = shuffleWith(candidates, rng)
        for (i in 0 until minOf(count, shuffled.size)) {
            shuffled[i].cellType = CellType.ANCHOR
        }
    }

    private fun generateSimpleBoard(
        board: Board,
        levelData: LevelData,
        dictionary: Dictionary,
        rng: SeededRNG
    ) {
        val wordCount = levelData.layout.wordCount
        val minLen = levelData.layout.minWordLen
        val maxLen = levelData.layout.maxWordLen

        // Pick candidate words from dictionary
        val candidates = dictionary.getWordList().filter {
            it.length in minLen..maxLen
        }
        val shuffled = shuffleWith(candidates, rng)

        var placed = 0
        for (word in shuffled) {
            if (placed >= wordCount) break
            if (placeWordOnGrid(board, word, rng)) {
                placed++
            }
        }

        // Ensure anchors are covered
        val anchors = board.field.getAllCells().filter { it.isAnchor && it.letter == null }
        for (anchor in anchors) {
            val anchorWords = shuffleWith(candidates.filter { it.length in 3..6 }, rng)
            for (word in anchorWords.take(20)) {
                if (placeWordThroughCell(board, word, anchor, rng)) break
            }
        }

        // Fill remaining empty cells
        fillEmptyCells(board, rng, useSafe = false)
    }

    private fun generateClearBoard(
        board: Board,
        levelData: LevelData,
        dictionary: Dictionary,
        rng: SeededRNG
    ) {
        val allCells = board.field.getAllCells()
        val candidates = dictionary.getWordList().filter { it.length in 3..8 }
        val shuffled = shuffleWith(candidates, rng)

        val uncovered = allCells.map { it.key }.toMutableSet()

        // Try to tile the board with non-overlapping words
        for (attempt in 0 until 100) {
            if (uncovered.isEmpty()) break
            val wordLen = maxOf(3, minOf(8, 8 - attempt / 15))
            val wordCandidates = shuffled.filter { it.length <= wordLen }

            for (word in wordCandidates.take(50)) {
                if (uncovered.isEmpty()) break
                val startCells = board.field.getAllCells()
                    .filter { it.key in uncovered && it.letter == null }
                val shuffledStarts = shuffleWith(startCells, rng)

                for (start in shuffledStarts.take(5)) {
                    val path = findPathForWord(board, word, start, uncoveredOnly = true)
                    if (path != null) {
                        for ((i, cell) in path.withIndex()) {
                            cell.letter = word[i]
                            uncovered.remove(cell.key)
                        }
                        break
                    }
                }
            }
        }

        // Fill any remaining cells
        fillEmptyCells(board, rng, useSafe = false)
    }

    private fun generateChainBoard(
        board: Board,
        levelData: LevelData,
        dictionary: Dictionary,
        rng: SeededRNG
    ) {
        // Chain mode: random letters with solvability check
        val smallBoard = board.field.getAllCells().size < Gameplay.SMALL_BOARD_THRESHOLD
        var bestBoard: Board? = null
        var bestScore = 0

        for (attempt in 0 until 200) {
            val attemptRng = SeededRNG(SeededRNG.seedForLevel(
                rng.nextInt(Int.MAX_VALUE), 0, attempt
            ))

            // Fill with random letters
            val cells = board.field.getAllCells()
            val letters = LetterGenerator.generateLetters(
                cells.size, attemptRng, useSafeLetters = false, smallBoard = smallBoard
            )
            for ((i, cell) in cells.withIndex()) {
                cell.letter = letters[i]
            }

            // Check solvability
            val score = countQualifyingWords(board, dictionary, levelData)
            if (score > bestScore) {
                bestScore = score
                bestBoard = null // We'll just keep using the current board state
            }
            if (score >= 10) return // Good enough
        }

        // If we didn't find a great board, the last attempt is still in place
    }

    private fun generateIlluminateBoard(
        board: Board,
        levelData: LevelData,
        dictionary: Dictionary,
        rng: SeededRNG
    ) {
        val candidates = dictionary.getWordList().filter { it.length in 3..7 }
        val shuffled = shuffleWith(candidates, rng)

        val allCellKeys = board.field.getAllCells().map { it.key }.toMutableSet()
        val covered = mutableSetOf<String>()

        // Place overlapping words to cover all cells
        for (attempt in 0 until 200) {
            if (covered.containsAll(allCellKeys)) break

            val uncoveredCells = board.field.getAllCells().filter { it.key !in covered }
            val targetCell = if (uncoveredCells.isNotEmpty() && attempt > 50) {
                uncoveredCells[rng.nextInt(uncoveredCells.size)]
            } else null

            for (word in shuffled.take(30)) {
                val startCells = if (targetCell != null) listOf(targetCell)
                else shuffleWith(board.field.getAllCells(), rng).take(5)

                for (start in startCells) {
                    val path = findPathForWord(board, word, start, uncoveredOnly = false)
                    if (path != null) {
                        for ((i, cell) in path.withIndex()) {
                            cell.letter = word[i]
                            covered.add(cell.key)
                        }
                        break
                    }
                }
                if (covered.containsAll(allCellKeys)) break
            }
        }

        // Fill remaining with safe letters
        fillEmptyCells(board, rng, useSafe = true)

        // Fix uncoverable cells
        fixUncoverableCells(board, dictionary, rng)
    }

    private fun placeWordOnGrid(board: Board, word: String, rng: SeededRNG): Boolean {
        val allCells = shuffleWith(board.field.getAllCells(), rng)

        // Prefer starting from anchor cells
        val starts = allCells.sortedByDescending { if (it.isAnchor) 1 else 0 }

        for (start in starts.take(15)) {
            val path = findPathForWord(board, word, start, uncoveredOnly = false)
            if (path != null) {
                for ((i, cell) in path.withIndex()) {
                    cell.letter = word[i]
                }
                return true
            }
        }
        return false
    }

    private fun placeWordThroughCell(
        board: Board,
        word: String,
        targetCell: HexCell,
        rng: SeededRNG
    ): Boolean {
        // Try to place a word that passes through the target cell
        for (startPos in 0 until word.length) {
            val path = findPathForWordThroughCell(board, word, targetCell, startPos)
            if (path != null) {
                for ((i, cell) in path.withIndex()) {
                    cell.letter = word[i]
                }
                return true
            }
        }
        return false
    }

    private fun findPathForWord(
        board: Board,
        word: String,
        startCell: HexCell,
        uncoveredOnly: Boolean
    ): List<HexCell>? {
        // Check if start cell is compatible
        if (startCell.letter != null && startCell.letter != word[0]) return null

        val path = mutableListOf(startCell)
        if (dfsPath(board, word, 1, path, uncoveredOnly)) {
            return path
        }
        return null
    }

    private fun dfsPath(
        board: Board,
        word: String,
        index: Int,
        path: MutableList<HexCell>,
        uncoveredOnly: Boolean
    ): Boolean {
        if (index >= word.length) return true

        val current = path.last()
        val neighbors = board.field.getNeighborCells(current.q, current.r)

        for (neighbor in neighbors) {
            if (neighbor in path) continue
            if (uncoveredOnly && neighbor.letter != null) continue
            if (neighbor.letter != null && neighbor.letter != word[index]) continue

            path.add(neighbor)
            if (dfsPath(board, word, index + 1, path, uncoveredOnly)) return true
            path.removeAt(path.lastIndex)
        }
        return false
    }

    private fun findPathForWordThroughCell(
        board: Board,
        word: String,
        targetCell: HexCell,
        targetIndex: Int
    ): List<HexCell>? {
        if (targetCell.letter != null && targetCell.letter != word[targetIndex]) return null

        // Build path before target
        val beforePath = mutableListOf<HexCell>()
        val afterPath = mutableListOf<HexCell>()

        // Simple approach: build outward from target
        val fullPath = MutableList<HexCell?>(word.length) { null }
        fullPath[targetIndex] = targetCell

        // Try to fill positions before target
        for (i in targetIndex - 1 downTo 0) {
            val refCell = fullPath[i + 1] ?: return null
            val neighbors = board.field.getNeighborCells(refCell.q, refCell.r)
            val valid = neighbors.filter { n ->
                (n.letter == null || n.letter == word[i]) && fullPath.none { it?.key == n.key }
            }
            if (valid.isEmpty()) return null
            fullPath[i] = valid.first()
        }

        // Try to fill positions after target
        for (i in targetIndex + 1 until word.length) {
            val refCell = fullPath[i - 1] ?: return null
            val neighbors = board.field.getNeighborCells(refCell.q, refCell.r)
            val valid = neighbors.filter { n ->
                (n.letter == null || n.letter == word[i]) && fullPath.none { it?.key == n.key }
            }
            if (valid.isEmpty()) return null
            fullPath[i] = valid.first()
        }

        return if (fullPath.all { it != null }) fullPath.filterNotNull() else null
    }

    private fun fillEmptyCells(board: Board, rng: SeededRNG, useSafe: Boolean) {
        val emptyCells = board.field.getAllCells().filter { it.letter == null }
        val smallBoard = board.field.getAllCells().size < Gameplay.SMALL_BOARD_THRESHOLD
        val letters = LetterGenerator.generateLetters(
            emptyCells.size, rng, useSafeLetters = useSafe, smallBoard = smallBoard
        )
        for ((i, cell) in emptyCells.withIndex()) {
            cell.letter = letters[i]
        }
    }

    private fun fixUncoverableCells(board: Board, dictionary: Dictionary, rng: SeededRNG) {
        val commonLetters = listOf('E', 'A', 'R', 'S', 'T', 'O', 'N', 'I')
        for (pass in 0 until 5) {
            if (board.field.allCellsCoverable(dictionary)) return
            val uncoverable = findUncoverableCells(board, dictionary)
            for (cell in uncoverable) {
                cell.letter = commonLetters[rng.nextInt(commonLetters.size)]
            }
        }
    }

    private fun findUncoverableCells(board: Board, dictionary: Dictionary): List<HexCell> {
        val allCells = board.field.getAllCells().filter { it.letter != null && it.isActive }
        val covered = mutableSetOf<String>()
        val seen = mutableSetOf<String>()

        for (startCell in allCells) {
            traceForCoverage(board, startCell, mutableListOf(startCell), dictionary, covered, seen)
        }

        return allCells.filter { it.key !in covered }
    }

    private fun traceForCoverage(
        board: Board,
        current: HexCell,
        path: MutableList<HexCell>,
        dictionary: Dictionary,
        covered: MutableSet<String>,
        seen: MutableSet<String>
    ) {
        val word = path.mapNotNull { it.letter }.joinToString("")
        if (word.length >= 3 && dictionary.isWord(word) && word !in seen) {
            seen.add(word)
            for (c in path) covered.add(c.key)
        }
        if (word.length >= 8 || !dictionary.isPrefix(word)) return

        val neighbors = board.field.getNeighborCells(current.q, current.r)
        for (next in neighbors) {
            if (next.letter != null && next.isActive && next !in path) {
                path.add(next)
                traceForCoverage(board, next, path, dictionary, covered, seen)
                path.removeAt(path.lastIndex)
            }
        }
    }

    private fun countQualifyingWords(
        board: Board,
        dictionary: Dictionary,
        levelData: LevelData
    ): Int {
        val minLen = levelData.objectives.mapNotNull { it.params["minLength"] }.minOrNull() ?: 3
        val hasAnchors = board.field.getAllCells().any { it.isAnchor }
        val anchors = if (hasAnchors) board.field.getAllCells().filter { it.isAnchor }.map { it.key }.toSet() else null

        val words = mutableSetOf<String>()
        val allCells = board.field.getAllCells().filter { it.letter != null && it.isActive }

        for (startCell in allCells) {
            countWords(board, startCell, mutableListOf(startCell), dictionary, words, minLen, anchors)
        }

        return words.size
    }

    private fun countWords(
        board: Board,
        current: HexCell,
        path: MutableList<HexCell>,
        dictionary: Dictionary,
        found: MutableSet<String>,
        minLen: Int,
        anchors: Set<String>?
    ) {
        val word = path.mapNotNull { it.letter }.joinToString("")
        if (word.length >= minLen && dictionary.isWord(word)) {
            if (anchors == null || path.any { it.key in anchors }) {
                found.add(word)
            }
        }
        if (word.length >= 8 || !dictionary.isPrefix(word)) return

        val neighbors = board.field.getNeighborCells(current.q, current.r)
        for (next in neighbors) {
            if (next.letter != null && next.isActive && next !in path) {
                path.add(next)
                countWords(board, next, path, dictionary, found, minLen, anchors)
                path.removeAt(path.lastIndex)
            }
        }
    }

    fun autoClearIsolated(board: Board): List<HexCoord> {
        val activeCells = board.field.getActiveCells()
        val visited = mutableSetOf<String>()
        val clusters = mutableListOf<List<HexCell>>()

        for (cell in activeCells) {
            if (cell.key in visited) continue
            val cluster = mutableListOf<HexCell>()
            val queue = ArrayDeque<HexCell>()
            queue.add(cell)
            visited.add(cell.key)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                cluster.add(current)
                for (neighbor in board.field.getNeighborCells(current.q, current.r)) {
                    if (neighbor.isActive && !neighbor.isCleared && neighbor.key !in visited) {
                        visited.add(neighbor.key)
                        queue.add(neighbor)
                    }
                }
            }
            clusters.add(cluster)
        }

        val cleared = mutableListOf<HexCoord>()
        for (cluster in clusters) {
            if (cluster.size < Gameplay.AUTO_CLEAR_MIN_CLUSTER) {
                for (cell in cluster) {
                    cell.letter = null
                    cell.isCleared = true
                    cell.isActive = false
                    cleared.add(HexCoord(cell.q, cell.r))
                }
            }
        }
        return cleared
    }
}
