package com.example.unochessprototype.online.rules

import com.example.unochessprototype.online.model.OnlineGameState
import com.example.unochessprototype.online.model.OnlineResultType

/** Persisted clock snapshots; all callers use Firebase-adjusted epoch milliseconds. */
object OnlineClock {
    const val INTRO_MS = 5_000L
    const val TRANSITION_MS = 1_500L
    fun remaining(state: OnlineGameState, white: Boolean, now: Long): Long {
        val saved = if (white) state.whiteRemainingMs else state.blackRemainingMs
        val active = state.currentUid == if (white) state.whiteUid else state.blackUid
        val elapsed = if (active && !state.hasResult() && state.turnStartedAt > 0) (now - state.turnStartedAt).coerceAtLeast(0) else 0
        return (saved - elapsed).coerceAtLeast(0)
    }
    fun settle(state: OnlineGameState, now: Long): OnlineGameState {
        if (state.hasResult()) return state
        if (state.turnStartedAt == 0L) return state.copy(turnStartedAt = now + INTRO_MS)
        val white = remaining(state, true, now)
        val black = remaining(state, false, now)
        val updated = state.copy(whiteRemainingMs = white, blackRemainingMs = black, turnStartedAt = maxOf(now, state.turnStartedAt))
        if (white > 0 && black > 0) return updated
        return if (state.version == OnlineGameRules.VERSION) {
            OnlineGameRules.timeOut(updated, if (white == 0L) com.example.unochessprototype.game.Player.WHITE else com.example.unochessprototype.game.Player.BLACK)
        } else updated.copy(resultType = OnlineResultType.WIN,
            winnerUid = if (white == 0L) state.blackUid else state.whiteUid,
            resultReason = "상대의 시간이 모두 소진되었습니다.",
            logs = state.logs + "게임 종료 · 시간 초과")
    }
}
