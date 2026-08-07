package com.artmondo.boxters.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerProfile(
    val name: String,
    var gamesPlayed: Int = 0,
    var levelsCompleted: Int = 0,
    var totalScore: Int = 0,
    var bestScore: Int = 0,
    val currentLevels: MutableMap<String, Int> = mutableMapOf(),
    val highestLevels: MutableMap<String, Int> = mutableMapOf(),
    val unlockedModes: MutableList<String> = mutableListOf("simple"),
    val celebratedModes: MutableList<String> = mutableListOf(),
    var lastMode: String = "simple",
    var cooldownUntil: Long = 0,
    var lives: Int = 3
)
