package com.artmondo.boxters.util

object Timing {
    const val TRACE_ANIM_DURATION_MS = 200
    const val SUBMIT_ANIM_DURATION_MS = 600
    const val VICTORY_ANIM_DURATION_MS = 1500
    const val PARTICLE_FADE_TIME_MS = 2000
    const val VICTORY_TRANSITION_DELAY_MS = 1200
    const val DEFEAT_TRANSITION_DELAY_MS = 2000
    const val COOLDOWN_DURATION_MS = 300_000L
    const val FEEDBACK_DISPLAY_MS = 2000
    const val LOADING_MIN_DISPLAY_MS = 600
    const val CONFETTI_INTERVAL_MS = 2000
}

object Gameplay {
    const val MAX_LIVES = 3
    const val MIN_WORD_LENGTH = 3
    const val MAX_WORD_LENGTH = 8
    const val MAX_TRACE_DEPTH = 8
    const val VOWEL_GUARANTEE_PERCENT = 0.35f
    const val RARE_LETTER_REJECT_PROB = 0.85f
    const val SMALL_BOARD_THRESHOLD = 30
    const val AUTO_CLEAR_MIN_CLUSTER = 3
    const val MAX_SOLUTION_WORDS = 8
    const val LEVELS_PER_MODE = 14
    const val TOTAL_LEVELS = 56
    val VOWELS = setOf('A', 'E', 'I', 'O', 'U')
    val SAFE_LETTERS = ('A'..'Z').toSet() - setOf('J', 'Q', 'V', 'X', 'Z')
    val RARE_LETTERS = setOf('J', 'Q', 'X', 'Z')
}

object HexGeometry {
    const val DEFAULT_SIZE_DP = 61f
    const val MIN_SIZE_DP = 39f
    const val MAX_SIZE_DP = 81f
    const val PADDING_DP = 4f
    const val LETTER_SIZE_RATIO = 0.675f
    const val GLOW_RADIUS_DP = 12f
    const val RIM_WIDTH_DP = 2.5f
}

object Combo {
    val MULTIPLIERS = floatArrayOf(1.0f, 1.5f, 2.0f, 2.5f, 3.0f)
    const val MAX_COMBO = 4
}

object Scoring {
    val BASE_TIER = mapOf("simple" to 50, "clear" to 60, "chain" to 70, "illuminate" to 80)
    val MULTIPLIER_TIER = mapOf("simple" to 1.0f, "clear" to 1.2f, "chain" to 1.4f, "illuminate" to 1.6f)
    const val EFFICIENCY_PER_MOVE = 15
    const val SECONDARY_COMPLETION_BONUS = 30
}

object LetterValues {
    val VALUES = mapOf(
        'A' to 5, 'B' to 15, 'C' to 10, 'D' to 10, 'E' to 5,
        'F' to 15, 'G' to 10, 'H' to 12, 'I' to 5, 'J' to 25,
        'K' to 20, 'L' to 8, 'M' to 10, 'N' to 8, 'O' to 5,
        'P' to 10, 'Q' to 30, 'R' to 8, 'S' to 5, 'T' to 5,
        'U' to 8, 'V' to 18, 'W' to 15, 'X' to 25, 'Y' to 12,
        'Z' to 30
    )

    fun valueOf(letter: Char): Int = VALUES[letter.uppercaseChar()] ?: 0
}

object LetterFrequency {
    val WEIGHTS = mapOf(
        'E' to 12.7f, 'T' to 9.1f, 'A' to 8.2f, 'O' to 7.5f,
        'I' to 7.0f, 'N' to 6.7f, 'S' to 6.3f, 'H' to 6.1f,
        'R' to 6.0f, 'D' to 4.3f, 'L' to 4.0f, 'C' to 2.8f,
        'U' to 2.8f, 'M' to 2.4f, 'W' to 2.4f, 'F' to 2.2f,
        'G' to 2.0f, 'Y' to 2.0f, 'P' to 1.9f, 'B' to 1.5f,
        'V' to 1.0f, 'K' to 0.8f, 'J' to 0.2f, 'X' to 0.2f,
        'Q' to 0.1f, 'Z' to 0.1f
    )
}

val MODE_ORDER = listOf("simple", "clear", "chain", "illuminate")
