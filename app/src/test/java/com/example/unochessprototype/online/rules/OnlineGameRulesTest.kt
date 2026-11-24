package com.example.unochessprototype.online.rules

import com.example.unochessprototype.game.*
import com.example.unochessprototype.online.model.*
import kotlin.random.Random
import org.junit.Assert.*
import org.junit.Test

class OnlineGameRulesTest {
    @Test fun localAndOnlineRemainIdenticalAcrossCompleteGames() {
        var exchanges = 0
        var moves = 0
        for (seed in 0 until 30) {
            val localRandom = Random(seed)
            val onlineRandom = Random(seed)
            val local = UnoChessGame(localRandom)
            var online = OnlineGameRules.createInitial("owner", "guest", onlineRandom)
            assertEquals(local.snapshot(), OnlineGameRules.engine(online).snapshot())
            repeat(200) {
                if (local.result == null) {
                    val uid = online.currentUid
                    assertEquals(local.playableCards().map { it.id }, OnlineGameRules.playableCards(online, uid))
                    assertEquals(local.canExchangeTwoCards(), OnlineGameRules.canExchange(online, uid))
                    if (local.canExchangeTwoCards()) {
                        val ids = local.hand().take(2).map { it.id }.toSet()
                        local.exchangeTwoCards(ids)
                        online = OnlineGameRules.exchangeTwoCards(online, uid, ids, onlineRandom)
                        exchanges++
                    } else {
                        val card = local.playableCards().first()
                        val file = if (local.actsAsJoker(card)) local.playableFiles(card).first() else null
                        val legal = local.legalMovesForCard(card.id, file)
                        assertEquals(legal, OnlineGameRules.legalMovesForCard(online, uid, card.id, file))
                        local.playCardAndMove(card.id, legal.first(), file)
                        online = OnlineGameRules.playMove(online, uid, card.id, legal.first(), file, onlineRandom)
                        moves++
                    }
                    assertEquals("seed=$seed turn=${local.completedTurns}", local.snapshot(), OnlineGameRules.engine(online).snapshot())
                    val snapshot = local.snapshot()
                    val cards = snapshot.whiteHand + snapshot.blackHand + snapshot.drawPile + snapshot.discardPile
                    assertEquals(40, cards.size)
                    assertEquals(40, cards.map { it.id }.toSet().size)
                }
            }
        }
        assertTrue(exchanges > 0)
        assertTrue(moves > 0)
    }

    @Test fun mismatchedColorAndNumberCannotBePlayedEvenIfPieceCanMove() {
        val state = (0..100).map { OnlineGameRules.createInitial("owner", "guest", Random(it)) }.first { s ->
            val engine = OnlineGameRules.engine(s)
            engine.hand().any { !engine.matchesTopDiscard(it) && it.number!! - 1 in engine.board.movableFiles(engine.currentPlayer) }
        }
        val engine = OnlineGameRules.engine(state)
        val card = engine.hand().first { !engine.matchesTopDiscard(it) && it.number!! - 1 in engine.board.movableFiles(engine.currentPlayer) }
        assertTrue(OnlineGameRules.playableFiles(state, state.currentUid, card.id).isEmpty())
        val move = (0..7).flatMap { engine.board.legalMovesFrom(Square(it, card.number!! - 1)) }
            .first { engine.board.pieceAt(it.from)?.player == engine.currentPlayer }
        try {
            OnlineGameRules.playMove(state, state.currentUid, card.id, move)
            fail("A mismatched card must be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun restoreDoesNotRepeatDealOrTurnDraw() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(7))
        assertEquals(6, state.handFor(state.currentUid).size)
        assertEquals(1, state.discardPile.size)
        val first = OnlineGameRules.engine(state).snapshot()
        assertEquals(first, UnoChessGame(initialState = first).snapshot())
    }

    @Test(expected = IllegalArgumentException::class)
    fun opponentCannotPlayOutOfTurn() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(17))
        val opponent = if (state.currentUid == "owner") "guest" else "owner"
        OnlineGameRules.playMove(state, opponent, state.handFor(opponent).first(), Move(Square(6, 0), Square(5, 0)))
    }

    @Test fun endgameNumberCardActsAsJokerAndCapturesKingOnAnotherFile() {
        val board = ChessBoard.empty().apply {
            placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.ROOK))
            placeForTest(Square(7, 7), Piece(Player.WHITE, PieceType.KING))
            placeForTest(Square(0, 0), Piece(Player.BLACK, PieceType.KING))
        }
        val state = OnlineGameState(version = 2, ownerUid = "owner", guestUid = "guest",
            whiteUid = "owner", blackUid = "guest", currentUid = "owner", board = board.toCodes(),
            ownerHand = listOf(1, 2, 3, 4, 5, 6), guestHand = listOf(8, 9, 10, 11, 12),
            discardPile = listOf(23), lastDiscardId = 23,
            drawPile = (0..39).filterNot { it in listOf(1,2,3,4,5,6,8,9,10,11,12,23) })
        assertTrue(OnlineGameRules.actsAsJoker(state, "owner", 1))
        val local = OnlineGameRules.engine(state)
        val move = Move(Square(7, 0), Square(0, 0))
        local.playCardAndMove(1, move, 0)
        val next = OnlineGameRules.playMove(state, "owner", 1, move, 0)
        assertEquals(local.snapshot(), OnlineGameRules.engine(next).snapshot())
        assertEquals("owner", next.winnerUid)
        assertEquals(5, next.ownerHand.size)
    }

    @Test fun timeoutUsesLocalResultAndLog() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(3))
        val local = OnlineGameRules.engine(state)
        local.timeOut(Player.WHITE)
        assertEquals(local.snapshot(), OnlineGameRules.engine(OnlineGameRules.timeOut(state, Player.WHITE)).snapshot())
    }

    @Test(expected = IllegalArgumentException::class)
    fun legacyGameCannotSilentlyUseNewRules() {
        OnlineGameRules.engine(OnlineGameState())
    }
}
