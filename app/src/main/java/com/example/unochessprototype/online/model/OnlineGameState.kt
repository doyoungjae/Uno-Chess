package com.example.unochessprototype.online.model

data class OnlineGameState(
    val version: Int = 1,
    val discardPile: List<Int> = emptyList(),
    val firstTurnRounds: List<OnlineFirstTurnRound> = emptyList(),
    val whiteRemainingMs: Long = 600_000L,
    val blackRemainingMs: Long = 600_000L,
    val turnStartedAt: Long = 0L,
    val lastDiscardId: Int = -1,
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

data class OnlineFirstTurnRound(val ownerCardId: Int = 0, val guestCardId: Int = 0)
