package com.artmondo.boxters.data.repository

import android.content.Context
import com.artmondo.boxters.data.model.BoardSnapshot
import com.artmondo.boxters.data.model.PlayerProfile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PlayerRepository(context: Context) {
    private val prefs = context.getSharedPreferences("boxters_player", Context.MODE_PRIVATE)
    private val boardPrefs = context.getSharedPreferences("boxters_boards", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun loadProfile(): PlayerProfile? {
        val data = prefs.getString("profile", null) ?: return null
        return try {
            json.decodeFromString<PlayerProfile>(data)
        } catch (e: Exception) {
            null
        }
    }

    fun saveProfile(profile: PlayerProfile) {
        prefs.edit().putString("profile", json.encodeToString(profile)).apply()
    }

    fun saveBoardSnapshot(mode: String, snapshot: BoardSnapshot) {
        boardPrefs.edit().putString("board_$mode", json.encodeToString(snapshot)).apply()
    }

    fun loadBoardSnapshot(mode: String): BoardSnapshot? {
        val data = boardPrefs.getString("board_$mode", null) ?: return null
        return try {
            json.decodeFromString<BoardSnapshot>(data)
        } catch (e: Exception) {
            null
        }
    }

    fun clearBoardSnapshot(mode: String) {
        boardPrefs.edit().remove("board_$mode").apply()
    }

    fun loadDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", true)
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }
}
