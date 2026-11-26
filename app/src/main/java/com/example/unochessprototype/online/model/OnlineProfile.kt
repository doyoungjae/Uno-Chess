package com.example.unochessprototype.online.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class OnlineProfile(
    val uid: String = "",
    val nickname: String = "PLAYER",
    val avatar: String = "♞",
    val onlineGames: Int = 0,
    val onlineWins: Int = 0,
    val onlineLosses: Int = 0,
    val onlineDraws: Int = 0,
    /** users/{uid}/friends/{friendUid} = true */
    val friends: Map<String, Boolean> = emptyMap(),
) {
    @get:Exclude
    val winRate: Int
        get() = if (onlineGames == 0) 0 else onlineWins * 100 / onlineGames
}
