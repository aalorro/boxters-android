package com.artmondo.boxters.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfile(
    var name: String,
    var playerId: String = "",
    var gamesPlayed: Int = 0,
    var levelsCompleted: Int = 0,
    var totalScore: Int = 0,
    var bestScore: Int = 0,
    var baseScore: Int = 0,
    val levelScores: MutableMap<String, Int> = mutableMapOf(),
    val currentLevels: MutableMap<String, Int> = mutableMapOf(),
    val highestLevels: MutableMap<String, Int> = mutableMapOf(),
    val unlockedModes: MutableList<String> = mutableListOf("simple"),
    val celebratedModes: MutableList<String> = mutableListOf(),
    var lastMode: String = "simple",
    var cooldownUntil: Long = 0,
    var lives: Int = 3,
    var livesLostAtLevel: Int = -1,
    var gender: String = "",
    var ageGroup: String = "",
    var isInGauntlet: Boolean = false
)
