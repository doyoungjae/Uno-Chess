package com.example.unochessprototype.game

import kotlin.random.Random

enum class LocalPlayerSlot {
    PLAYER1,
    PLAYER2,
}

data class FirstTurnDrawResult(
    val player1Card: Int,
    val player2Card: Int,
) {
    init {
        require(player1Card in 1..8)
        require(player2Card in 1..8)
    }

    val isTie: Boolean
        get() = player1Card == player2Card

    val winner: LocalPlayerSlot?
        get() = when {
            player1Card > player2Card -> LocalPlayerSlot.PLAYER1
            player2Card > player1Card -> LocalPlayerSlot.PLAYER2
            else -> null
        }
}

class FirstTurnDecider(
    private val random: Random = Random.Default,
) {
    fun drawCard(): Int = random.nextInt(from = 1, until = 9)

    fun decide(player1Card: Int, player2Card: Int): FirstTurnDrawResult =
        FirstTurnDrawResult(player1Card, player2Card)
}
