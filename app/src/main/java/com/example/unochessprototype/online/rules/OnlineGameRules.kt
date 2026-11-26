package com.example.unochessprototype.online.rules

import com.example.unochessprototype.game.*
import com.example.unochessprototype.online.model.*
import kotlin.random.Random

/** UID/전송 상태 변환과 차례 검증만 담당한다. 게임 규칙은 UnoChessGame에서 실행한다. */
object OnlineGameRules {
    const val VERSION = 2
    const val MAX_TURNS = UnoChessGame.MAX_TURNS
    const val HAND_SIZE = UnoChessGame.HAND_SIZE
    const val TOTAL_CARD_COUNT = UnoChessGame.TOTAL_CARD_COUNT

    fun createInitial(ownerUid: String, guestUid: String, random: Random = Random.Default): OnlineGameState {
        require(ownerUid.isNotBlank() && guestUid.isNotBlank() && ownerUid != guestUid)
        val engine = UnoChessGame(random, blackViewFlipped = true)
        val white = if (engine.player1Starts) ownerUid else guestUid
        return encode(OnlineGameState(version = VERSION, ownerUid = ownerUid, guestUid = guestUid,
            whiteUid = white, blackUid = if (white == ownerUid) guestUid else ownerUid), engine)
    }

    fun card(cardId: Int): UnoCard {
        require(cardId in 0 until TOTAL_CARD_COUNT)
        return if (cardId >= 32) UnoCard(cardId, null, null)
        else UnoCard(cardId, CardColor.entries[cardId / 8], cardId % 8 + 1)
    }

    fun boardFromState(state: OnlineGameState): ChessBoard = ChessBoard.fromCodes(state.board)

    fun sideFor(state: OnlineGameState, uid: String): Player = when (uid) {
        state.whiteUid -> Player.WHITE
        state.blackUid -> Player.BLACK
        else -> error("Not a game participant.")
    }

    fun engine(state: OnlineGameState, random: Random = Random.Default): UnoChessGame {
        require(state.version == VERSION) { "이전 규칙의 경기입니다. 새 방을 만들어 주세요." }
        val result = when (state.resultType) {
            OnlineResultType.WIN -> GameResult.Win(sideFor(state, state.winnerUid), state.resultReason)
            OnlineResultType.DRAW -> GameResult.Draw(state.resultReason)
            else -> null
        }
        return UnoChessGame(random, GameSnapshot(
            state.board, sideFor(state, state.currentUid), state.completedTurns, result,
            state.handFor(state.whiteUid).map(::card), state.handFor(state.blackUid).map(::card),
            state.drawPile.map(::card), state.discardPile.map(::card),
            state.firstTurnRounds.map { FirstTurnRound(card(it.ownerCardId), card(it.guestCardId)) }, state.logs,
        ), blackViewFlipped = true)
    }

    private fun encode(state: OnlineGameState, engine: UnoChessGame): OnlineGameState {
        val snapshot = engine.snapshot()
        fun uid(side: Player) = if (side == Player.WHITE) state.whiteUid else state.blackUid
        val result = snapshot.result
        val lastRound = snapshot.firstTurnRounds.lastOrNull()
        return state.copy(
            version = VERSION, board = snapshot.board, currentUid = uid(snapshot.currentPlayer),
            completedTurns = snapshot.completedTurns,
            ownerHand = engine.hand(sideFor(state, state.ownerUid)).map { it.id },
            guestHand = engine.hand(sideFor(state, state.guestUid)).map { it.id },
            drawPile = snapshot.drawPile.map { it.id }, discardPile = snapshot.discardPile.map { it.id },
            lastDiscardId = snapshot.discardPile.lastOrNull()?.id ?: -1,
            firstTurnRounds = snapshot.firstTurnRounds.map { OnlineFirstTurnRound(it.player1Card.id, it.player2Card.id) },
            ownerFirstCard = lastRound?.player1Card?.number ?: 0,
            guestFirstCard = lastRound?.player2Card?.number ?: 0,
            resultType = when (result) { is GameResult.Win -> OnlineResultType.WIN; is GameResult.Draw -> OnlineResultType.DRAW; null -> OnlineResultType.NONE },
            winnerUid = if (result is GameResult.Win) uid(result.winner) else "",
            resultReason = when (result) { is GameResult.Win -> result.reason; is GameResult.Draw -> result.reason; null -> "" },
            logs = snapshot.logs,
        )
    }

    fun fileForNumber(state: OnlineGameState, uid: String, number: Int): Int =
        engine(state).fileForNumber(number, sideFor(state, uid))

    fun actsAsJoker(state: OnlineGameState, uid: String, cardId: Int): Boolean =
        engine(state).actsAsJoker(card(cardId), sideFor(state, uid))

    fun playableFiles(state: OnlineGameState, uid: String, cardId: Int): Set<Int> {
        if (state.hasResult() || uid != state.currentUid || cardId !in state.handFor(uid)) return emptySet()
        return engine(state).playableFiles(card(cardId))
    }

    fun playableCards(state: OnlineGameState, uid: String): List<Int> =
        state.handFor(uid).filter { playableFiles(state, uid, it).isNotEmpty() }

    fun canExchange(state: OnlineGameState, uid: String): Boolean =
        !state.hasResult() && uid == state.currentUid && engine(state).canExchangeTwoCards()

    fun legalMovesForCard(state: OnlineGameState, uid: String, cardId: Int, chosenFileForJoker: Int? = null): List<Move> {
        if (state.hasResult() || uid != state.currentUid) return emptyList()
        return engine(state).legalMovesForCard(cardId, chosenFileForJoker)
    }

    fun playMove(state: OnlineGameState, actorUid: String, cardId: Int, move: Move,
                 chosenFileForJoker: Int? = null, random: Random = Random.Default): OnlineGameState {
        require(actorUid == state.currentUid) { "It is not this user's turn." }
        val engine = engine(state, random)
        engine.playCardAndMove(cardId, move, chosenFileForJoker)
        return encode(state, engine)
    }

    fun exchangeTwoCards(state: OnlineGameState, actorUid: String, cardIds: Set<Int>, random: Random = Random.Default): OnlineGameState {
        require(actorUid == state.currentUid) { "It is not this user's turn." }
        val engine = engine(state, random)
        engine.exchangeTwoCards(cardIds)
        return encode(state, engine)
    }

    fun timeOut(state: OnlineGameState, player: Player): OnlineGameState {
        val engine = engine(state)
        engine.timeOut(player)
        return encode(state, engine)
    }

    fun forfeit(state: OnlineGameState, actorUid: String): OnlineGameState {
        require(actorUid == state.ownerUid || actorUid == state.guestUid)
        check(!state.hasResult())
        return state.copy(resultType = OnlineResultType.WIN,
            winnerUid = if (actorUid == state.ownerUid) state.guestUid else state.ownerUid,
            resultReason = "상대가 경기에서 나갔습니다.", logs = state.logs + "게임 종료 · 기권")
    }
}
