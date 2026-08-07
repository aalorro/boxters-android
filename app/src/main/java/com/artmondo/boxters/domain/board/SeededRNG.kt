package com.artmondo.boxters.domain.board

/**
 * Mulberry32 PRNG — must match the JS implementation exactly for board sharing compatibility.
 * JS version uses 32-bit integer math with unsigned operations.
 */
class SeededRNG(seed: Int) {
    private var state: Long = seed.toLong() and 0xFFFFFFFFL

    fun next(): Float {
        state = (state + 0x6D2B79F5L) and 0xFFFFFFFFL
        var t = state
        t = ((t xor (t shr 15)) * (t or 1L)) and 0xFFFFFFFFL
        t = (t + ((t xor (t shr 7)) * (t or 61L)) and 0xFFFFFFFFL) xor t
        t = t and 0xFFFFFFFFL
        val result = (t xor (t shr 14)) and 0xFFFFFFFFL
        return result.toFloat() / 4294967296f
    }

    fun nextInt(bound: Int): Int = (next() * bound).toInt()

    fun nextFloat(): Float = next()

    companion object {
        fun seedForLevel(sessionSeed: Int, levelIndex: Int, boardAttempt: Int = 0): Int {
            return sessionSeed xor (levelIndex * 2654435761.toInt()) xor (boardAttempt * 1103515245)
        }
    }
}

fun <T> shuffleWith(list: List<T>, rng: SeededRNG): List<T> {
    val result = list.toMutableList()
    for (i in result.size - 1 downTo 1) {
        val j = (rng.next() * (i + 1)).toInt()
        val tmp = result[i]
        result[i] = result[j]
        result[j] = tmp
    }
    return result
}
