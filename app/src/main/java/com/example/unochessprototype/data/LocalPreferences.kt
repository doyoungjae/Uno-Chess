package com.example.unochessprototype.data

import android.content.Context

private const val PREFS_NAME = "uno_chess_local"

data class LocalProfile(
    val nickname: String = "PLAYER 1",
    val avatar: String = "♞",
    val games: Int = 0,
    val wins: Int = 0,
    val draws: Int = 0,
)

data class LocalSettings(
    val bgmEnabled: Boolean = true,
    val effectsEnabled: Boolean = true,
    val bgmVolume: Float = 0.7f,
    val effectsVolume: Float = 0.8f,
    val moveHints: Boolean = true,
    val showCoordinates: Boolean = true,
    val boardTheme: Int = 0,
)

class LocalPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadProfile(): LocalProfile = LocalProfile(
        nickname = prefs.getString("nickname", "PLAYER 1") ?: "PLAYER 1",
        avatar = prefs.getString("avatar", "♞") ?: "♞",
        games = prefs.getInt("games", 0),
        wins = prefs.getInt("wins", 0),
        draws = prefs.getInt("draws", 0),
    )

    fun saveProfile(profile: LocalProfile) {
        prefs.edit()
            .putString("nickname", profile.nickname)
            .putString("avatar", profile.avatar)
            .putInt("games", profile.games)
            .putInt("wins", profile.wins)
            .putInt("draws", profile.draws)
            .apply()
    }

    fun loadSettings(): LocalSettings = LocalSettings(
        bgmEnabled = prefs.getBoolean("bgmEnabled", true),
        effectsEnabled = prefs.getBoolean("effectsEnabled", true),
        bgmVolume = prefs.getFloat("bgmVolume", 0.7f),
        effectsVolume = prefs.getFloat("effectsVolume", 0.8f),
        moveHints = prefs.getBoolean("moveHints", true),
        showCoordinates = prefs.getBoolean("showCoordinates", true),
        boardTheme = prefs.getInt("boardTheme", 0),
    )

    fun saveSettings(settings: LocalSettings) {
        prefs.edit()
            .putBoolean("bgmEnabled", settings.bgmEnabled)
            .putBoolean("effectsEnabled", settings.effectsEnabled)
            .putFloat("bgmVolume", settings.bgmVolume)
            .putFloat("effectsVolume", settings.effectsVolume)
            .putBoolean("moveHints", settings.moveHints)
            .putBoolean("showCoordinates", settings.showCoordinates)
            .putInt("boardTheme", settings.boardTheme)
            .apply()
    }
}
