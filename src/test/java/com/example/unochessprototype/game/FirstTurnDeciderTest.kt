package com.example.unochessprototype.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirstTurnDeciderTest {
    private val decider = FirstTurnDecider()

    @Test
    fun player1WinsWithHigherCard() {
        val result = decider.decide(player1Card = 8, player2Card = 3)
        assertEquals(LocalPlayerSlot.PLAYER1, result.winner)
    }

    @Test
    fun player2WinsWithHigherCard() {
        val result = decider.decide(player1Card = 2, player2Card = 7)
        assertEquals(LocalPlayerSlot.PLAYER2, result.winner)
    }

    @Test
    fun tieRequiresRedraw() {
        val result = decider.decide(player1Card = 5, player2Card = 5)
        assertEquals(true, result.isTie)
        assertNull(result.winner)
    }
}
