package com.artmondo.boxters.domain.board

import com.artmondo.boxters.util.Gameplay
import com.artmondo.boxters.util.LetterFrequency

object LetterGenerator {
    private val letters: List<Char>
    private val cumulativeWeights: List<Float>
    private val safeLetters: List<Char>
    private val safeCumulativeWeights: List<Float>

    init {
        val sorted = LetterFrequency.WEIGHTS.entries.sortedByDescending { it.value }
        letters = sorted.map { it.key }
        val total = sorted.sumOf { it.value.toDouble() }.toFloat()
        var cumulative = 0f
        cumulativeWeights = sorted.map { entry ->
            cumulative += entry.value / total
            cumulative
        }

        val safeEntries = sorted.filter { it.key in Gameplay.SAFE_LETTERS }
        safeLetters = safeEntries.map { it.key }
        val safeTotal = safeEntries.sumOf { it.value.toDouble() }.toFloat()
        var safeCum = 0f
        safeCumulativeWeights = safeEntries.map { entry ->
            safeCum += entry.value / safeTotal
            safeCum
        }
    }

    fun randomLetter(rng: SeededRNG? = null): Char {
        val r = rng?.next() ?: Math.random().toFloat()
        for (i in letters.indices) {
            if (r <= cumulativeWeights[i]) return letters[i]
        }
        return letters.last()
    }

    fun safeLetter(rng: SeededRNG? = null): Char {
        val r = rng?.next() ?: Math.random().toFloat()
        for (i in safeLetters.indices) {
            if (r <= safeCumulativeWeights[i]) return safeLetters[i]
        }
        return safeLetters.last()
    }

    fun generateLetters(
        count: Int,
        rng: SeededRNG,
        useSafeLetters: Boolean = false,
        smallBoard: Boolean = false
    ): List<Char> {
        val result = mutableListOf<Char>()
        for (i in 0 until count) {
            var letter: Char
            if (useSafeLetters) {
                letter = safeLetter(rng)
            } else {
                letter = randomLetter(rng)
                // Suppress rare letters on small boards
                if (smallBoard && letter in Gameplay.RARE_LETTERS) {
                    if (rng.next() < Gameplay.RARE_LETTER_REJECT_PROB) {
                        letter = randomLetter(rng)
                    }
                }
            }
            result.add(letter)
        }

        // Vowel guarantee
        val vowelCount = result.count { it in Gameplay.VOWELS }
        val minVowels = (count * Gameplay.VOWEL_GUARANTEE_PERCENT).toInt()
        if (vowelCount < minVowels) {
            val vowelList = listOf('A', 'E', 'I', 'O', 'U')
            val consonantIndices = result.indices.filter { result[it] !in Gameplay.VOWELS }.shuffled()
            var needed = minVowels - vowelCount
            for (idx in consonantIndices) {
                if (needed <= 0) break
                result[idx] = vowelList[rng.nextInt(vowelList.size)]
                needed--
            }
        }

        return result
    }
}
