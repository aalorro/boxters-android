package com.artmondo.boxters.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.artmondo.boxters.data.model.PlayerProfile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val LEADERBOARD_COLLECTION = "leaderboard"
private const val LEADERBOARD_SIZE = 30
private const val SUBMIT_COOLDOWN_MS = 5000L
private const val MAX_LEVEL_SCORE = 2500
private const val CACHE_KEY = "boxters_leaderboard_cache"
private val MODE_ORDER = listOf("simple", "clear", "chain", "illuminate", "apex")

@Serializable
data class LeaderboardEntry(
    val id: String,
    val name: String,
    val totalScore: Int,
    val levelsCompleted: Int,
    val highestMode: String
)

@Serializable
private data class CachedLeaderboard(
    val entries: List<LeaderboardEntry>,
    val cachedAt: Long
)

class FirebaseRepository(context: Context) {

    private val db = Firebase.firestore
    private val prefs: SharedPreferences =
        context.getSharedPreferences("boxters_firebase", Context.MODE_PRIVATE)
    private var lastSubmitTime = 0L

    suspend fun submitScore(player: PlayerProfile) {
        val now = System.currentTimeMillis()
        if (now - lastSubmitTime < SUBMIT_COOLDOWN_MS) return
        lastSubmitTime = now

        val playerId = player.playerId
        if (playerId.isEmpty()) return

        var highestMode = "simple"
        for (m in MODE_ORDER) {
            if (m in player.unlockedModes) highestMode = m
        }

        val name = (player.name).take(20)
        val localLevelScores = player.levelScores
        var remoteLevelScores: Map<String, Long> = emptyMap()
        var remoteBaseScore = 0L
        var remoteLevelsCompleted = 0L

        try {
            val doc = db.collection(LEADERBOARD_COLLECTION).document(playerId).get().await()
            if (doc.exists()) {
                val remote = doc.data ?: emptyMap()
                @Suppress("UNCHECKED_CAST")
                remoteLevelScores = (remote["levelScores"] as? Map<String, Long>) ?: emptyMap()
                remoteBaseScore = (remote["baseScore"] as? Long) ?: 0L
                remoteLevelsCompleted = (remote["levelsCompleted"] as? Long) ?: 0L

                // Migration: if remote has totalScore but no levelScores, preserve as baseScore
                if (remoteLevelScores.isEmpty() && ((remote["totalScore"] as? Long) ?: 0L) > 0L) {
                    remoteBaseScore = (remote["totalScore"] as? Long) ?: 0L
                }
            }
        } catch (_: Exception) { /* proceed with local values */ }

        // Merge levelScores: take max per level, cap at MAX_LEVEL_SCORE
        val mergedLevelScores = remoteLevelScores.mapValues { it.value.toInt() }.toMutableMap()
        for ((levelId, score) in localLevelScores) {
            val capped = minOf(score, MAX_LEVEL_SCORE)
            mergedLevelScores[levelId] = maxOf(mergedLevelScores[levelId] ?: 0, capped)
        }

        // Derive totalScore and bestScore from merged levelScores
        val levelScoreValues = mergedLevelScores.values
        val levelScoreSum = levelScoreValues.sum()
        val baseScore = maxOf(player.baseScore, remoteBaseScore.toInt())
        val totalScore = baseScore + levelScoreSum
        val bestScore = levelScoreValues.maxOrNull() ?: 0

        val data = hashMapOf<String, Any>(
            "name" to name,
            "nameLower" to name.lowercase(),
            "levelScores" to mergedLevelScores,
            "baseScore" to baseScore,
            "totalScore" to totalScore,
            "bestScore" to bestScore,
            "levelsCompleted" to maxOf(player.levelsCompleted, remoteLevelsCompleted.toInt()),
            "highestMode" to highestMode,
            "gender" to player.gender,
            "ageGroup" to player.ageGroup,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            db.collection(LEADERBOARD_COLLECTION).document(playerId)
                .set(data, SetOptions.merge()).await()
        } catch (_: Exception) { /* submit failed silently */ }
    }

    suspend fun fetchLeaderboard(): List<LeaderboardEntry> {
        return try {
            val snapshot = db.collection(LEADERBOARD_COLLECTION)
                .orderBy("totalScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val entries = snapshot.documents.map { doc ->
                val d = doc.data ?: emptyMap()
                LeaderboardEntry(
                    id = doc.id,
                    name = (d["name"] as? String) ?: "Anonymous",
                    totalScore = (d["totalScore"] as? Long)?.toInt() ?: 0,
                    levelsCompleted = (d["levelsCompleted"] as? Long)?.toInt() ?: 0,
                    highestMode = (d["highestMode"] as? String) ?: "simple"
                )
            }

            cacheLeaderboard(entries)
            entries
        } catch (_: Exception) {
            getCachedLeaderboard()
        }
    }

    suspend fun syncProfile(player: PlayerProfile): Boolean {
        val playerId = player.playerId
        if (playerId.isEmpty()) return false

        return try {
            val doc = db.collection(LEADERBOARD_COLLECTION).document(playerId).get().await()
            if (!doc.exists()) return false

            val remote = doc.data ?: return false
            var changed = false

            // Merge levelScores from remote (max per level)
            @Suppress("UNCHECKED_CAST")
            val remoteLevelScores = (remote["levelScores"] as? Map<String, Long>) ?: emptyMap()
            for ((levelId, score) in remoteLevelScores) {
                val localScore = player.levelScores[levelId] ?: 0
                if (score.toInt() > localScore) {
                    player.levelScores[levelId] = score.toInt()
                    changed = true
                }
            }

            // Migration: if remote has totalScore but no levelScores, preserve as baseScore
            val remoteBaseScore = if (remoteLevelScores.isNotEmpty()) {
                (remote["baseScore"] as? Long)?.toInt() ?: 0
            } else {
                (remote["totalScore"] as? Long)?.toInt() ?: 0
            }
            if (remoteBaseScore > player.baseScore) {
                player.baseScore = remoteBaseScore
                changed = true
            }

            // Recompute totalScore from levelScores + baseScore
            val recomputed = player.baseScore + player.levelScores.values.sum()
            if (recomputed != player.totalScore) {
                player.totalScore = recomputed
                changed = true
            }

            // Recompute bestScore
            if (player.levelScores.isNotEmpty()) {
                val best = player.levelScores.values.max()
                if (best != player.bestScore) {
                    player.bestScore = best
                    changed = true
                }
            }

            val remoteLevelsCompleted = (remote["levelsCompleted"] as? Long)?.toInt() ?: 0
            if (remoteLevelsCompleted > player.levelsCompleted) {
                player.levelsCompleted = remoteLevelsCompleted
                changed = true
            }

            changed
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkNameAvailable(name: String): Boolean {
        return try {
            val lower = name.lowercase()
            // Check nameLower field first (new documents)
            val snapshot = db.collection(LEADERBOARD_COLLECTION)
                .whereEqualTo("nameLower", lower)
                .limit(1)
                .get()
                .await()
            if (!snapshot.isEmpty) return false

            // Fallback: check all documents for legacy entries without nameLower
            val allDocs = db.collection(LEADERBOARD_COLLECTION).get().await()
            val taken = allDocs.documents.any { doc ->
                val d = doc.data ?: return@any false
                d["nameLower"] == null && (d["name"] as? String)?.lowercase() == lower
            }
            !taken
        } catch (_: Exception) {
            true // Allow on error
        }
    }

    fun findPlayerRank(entries: List<LeaderboardEntry>, playerId: String): Int {
        val idx = entries.indexOfFirst { it.id == playerId }
        return if (idx >= 0) idx + 1 else -1
    }

    private fun cacheLeaderboard(entries: List<LeaderboardEntry>) {
        try {
            val cached = CachedLeaderboard(entries, System.currentTimeMillis())
            prefs.edit().putString(CACHE_KEY, Json.encodeToString(cached)).apply()
        } catch (_: Exception) { /* ignore */ }
    }

    fun getCachedLeaderboard(): List<LeaderboardEntry> {
        return try {
            val raw = prefs.getString(CACHE_KEY, null) ?: return emptyList()
            Json.decodeFromString<CachedLeaderboard>(raw).entries
        } catch (_: Exception) {
            emptyList()
        }
    }
}
