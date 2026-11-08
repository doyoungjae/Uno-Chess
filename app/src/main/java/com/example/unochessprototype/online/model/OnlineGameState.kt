package com.example.unochessprototype.online.model

data class OnlineGameState(
    val version: Int = 1,
    val ownerUid: String = "",
    val guestUid: String = "",
    val whiteUid: String = "",
    val blackUid: String = "",
    val currentUid: String = "",
    val ownerFirstCard: Int = 0,
    val guestFirstCard: Int = 0,
    val completedTurns: Int = 0,
    val board: List<String> = emptyList(),
    val ownerHand: List<Int> = emptyList(),
    val guestHand: List<Int> = emptyList(),
    val drawPile: List<Int> = emptyList(),
    val resultType: String = OnlineResultType.NONE,
    val winnerUid: String = "",
    val resultReason: String = "",
    val logs: List<String> = emptyList(),
) {
    fun hasResult(): Boolean = resultType != OnlineResultType.NONE

    fun handFor(uid: String): List<Int> = when (uid) {
        ownerUid -> ownerHand
        guestUid -> guestHand
        else -> emptyList()
    }
}

object OnlineResultType {
    const val NONE = "NONE"
    const val WIN = "WIN"
    const val DRAW = "DRAW"
}
