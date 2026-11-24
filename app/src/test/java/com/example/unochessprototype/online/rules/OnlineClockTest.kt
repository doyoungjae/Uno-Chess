package com.example.unochessprototype.online.rules

import com.example.unochessprototype.online.model.OnlineGameState
import com.example.unochessprototype.online.model.OnlineResultType
import org.junit.Assert.*
import org.junit.Test

class OnlineClockTest {
    private val game = OnlineGameState(whiteUid = "white", blackUid = "black", currentUid = "white", turnStartedAt = 10000L)

    @Test fun onlyActivePlayerLosesTime() {
        assertEquals(598000L, OnlineClock.remaining(game, true, 12000L))
        assertEquals(600000L, OnlineClock.remaining(game, false, 12000L))
    }

    @Test fun introAndTransitionDoNotConsumeTime() {
        assertEquals(600000L, OnlineClock.remaining(game, true, 9000L))
        assertEquals(10000L, OnlineClock.settle(game, 9000L).turnStartedAt)
    }

    @Test fun persistedSnapshotKeepsElapsedTimeAcrossReconnectAndTurnChange() {
        val saved = OnlineClock.settle(game, 12000L)
        assertEquals(595000L, OnlineClock.remaining(saved, true, 15000L))
        val next = saved.copy(currentUid = "black", turnStartedAt = 13500L)
        assertEquals(598000L, OnlineClock.remaining(next, true, 15500L))
        assertEquals(598000L, OnlineClock.remaining(next, false, 15500L))
    }

    @Test fun timeoutStopsClockAndAwardsOpponentExactlyOnce() {
        val finished = OnlineClock.settle(game.copy(whiteRemainingMs = 1000L), 11000L)
        assertEquals(OnlineResultType.WIN, finished.resultType)
        assertEquals("black", finished.winnerUid)
        assertEquals(0L, finished.whiteRemainingMs)
        assertEquals(finished, OnlineClock.settle(finished, 90000L))
        assertEquals(600000L, OnlineClock.remaining(finished, false, 90000L))
    }

    @Test fun blackTimeoutAwardsWhite() {
        val finished = OnlineClock.settle(game.copy(currentUid = "black", blackRemainingMs = 10L), 10010L)
        assertEquals("white", finished.winnerUid)
    }

    @Test fun legacyRoomGetsAnInitialClockWithoutImmediateTimeout() {
        val initialized = OnlineClock.settle(game.copy(turnStartedAt = 0L), 1000000L)
        assertFalse(initialized.hasResult())
        assertEquals(1000000L + OnlineClock.INTRO_MS, initialized.turnStartedAt)
        assertEquals(600000L, initialized.whiteRemainingMs)
    }
}
