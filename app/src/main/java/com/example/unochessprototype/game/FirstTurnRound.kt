package com.example.unochessprototype.game

enum class LocalPlayerSlot {
    PLAYER1,
    PLAYER2,
}

/**
 * 선공 결정 한 판. 높은 숫자가 이기고 JOKER는 숫자 카드에 진다.
 * 둘 다 JOKER이거나 같은 숫자면 winner == null (다시 뽑기).
 */
data class FirstTurnRound(
    val player1Card: UnoCard,
    val player2Card: UnoCard,
) {
    val winner: LocalPlayerSlot?
        get() {
            val p1 = player1Card.number ?: 0
            val p2 = player2Card.number ?: 0
            return when {
                p1 > p2 -> LocalPlayerSlot.PLAYER1
                p2 > p1 -> LocalPlayerSlot.PLAYER2
                else -> null
            }
        }

    val winnerCard: UnoCard?
        get() = when (winner) {
            LocalPlayerSlot.PLAYER1 -> player1Card
            LocalPlayerSlot.PLAYER2 -> player2Card
            null -> null
        }

    val loserCard: UnoCard?
        get() = when (winner) {
            LocalPlayerSlot.PLAYER1 -> player2Card
            LocalPlayerSlot.PLAYER2 -> player1Card
            null -> null
        }
}
