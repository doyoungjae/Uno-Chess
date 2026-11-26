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
            val local = UnoChessGame(localRandom, blackViewFlipped = true)
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

    @Test fun blackCardNumberCountsFromBlacksOwnLeft() {
        // BLACK 차례, 바닥은 JOKER라 RED-2(id 1)를 낼 수 있다. BLACK 화면 왼쪽 2번째 = 보드 인덱스 6(g열).
        val state = OnlineGameState(version = 2, ownerUid = "owner", guestUid = "guest",
            whiteUid = "owner", blackUid = "guest", currentUid = "guest", board = ChessBoard.standard().toCodes(),
            ownerHand = listOf(8, 9, 10, 11, 12), guestHand = listOf(1, 2, 3, 4, 5, 6),
            discardPile = listOf(32), drawPile = (0..39).filterNot { it in listOf(1,2,3,4,5,6,8,9,10,11,12,32) })
        assertEquals(6, OnlineGameRules.fileForNumber(state, "guest", 2))
        assertEquals(1, OnlineGameRules.fileForNumber(state, "owner", 2))
        assertEquals(setOf(6), OnlineGameRules.playableFiles(state, "guest", 1))
        val moves = OnlineGameRules.legalMovesForCard(state, "guest", 1)
        assertTrue(moves.isNotEmpty() && moves.all { it.from.col == 6 })
        val next = OnlineGameRules.playMove(state, "guest", 1, Move(Square(1, 6), Square(2, 6)))
        assertEquals("owner", next.currentUid)
        try {
            OnlineGameRules.playMove(state, "guest", 1, Move(Square(1, 1), Square(2, 1)))
            fail("WHITE 기준 열(b)은 거부되어야 한다")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun exchangeKeepsTurnAndRepeatsUntilPlayable() {
        val board = ChessBoard.empty().apply {
            placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.KING))
            for (row in 3..6) placeForTest(Square(row, 0), Piece(Player.WHITE, PieceType.PAWN))
            placeForTest(Square(0, 7), Piece(Player.BLACK, PieceType.KING))
        }
        // WHITE 손패는 G/H열 카드뿐이라 낼 수 없다. 드로우 순서: 30, 22(G열, 여전히 불가) → 33, 32(JOKER).
        val fixed = listOf(7, 15, 23, 31, 6, 14, 0, 1, 2, 3, 4, 21, 32, 33, 22, 30)
        var state = OnlineGameState(version = 2, ownerUid = "owner", guestUid = "guest",
            whiteUid = "owner", blackUid = "guest", currentUid = "owner", board = board.toCodes(),
            ownerHand = listOf(7, 15, 23, 31, 6, 14), guestHand = listOf(0, 1, 2, 3, 4), discardPile = listOf(21),
            drawPile = (0..39).filterNot { it in fixed } + listOf(32, 33, 22, 30))
        assertTrue(OnlineGameRules.canExchange(state, "owner"))

        state = OnlineGameRules.exchangeTwoCards(state, "owner", setOf(7, 15))
        assertEquals("owner", state.currentUid)
        assertEquals(0, state.completedTurns)
        assertEquals(6, state.ownerHand.size)
        assertTrue(OnlineGameRules.canExchange(state, "owner"))

        state = OnlineGameRules.exchangeTwoCards(state, "owner", setOf(30, 22))
        assertEquals("owner", state.currentUid)
        assertFalse(OnlineGameRules.canExchange(state, "owner"))
        assertTrue(32 in OnlineGameRules.playableCards(state, "owner"))

        val move = Move(Square(3, 0), Square(2, 0))
        state = OnlineGameRules.playMove(state, "owner", 32, move, 0)
        assertEquals("guest", state.currentUid)
        assertEquals(1, state.completedTurns)
        assertEquals(5, state.ownerHand.size)
        assertEquals(6, state.guestHand.size)
        assertEquals(40, (state.ownerHand + state.guestHand + state.drawPile + state.discardPile).toSet().size)
    }

=======
>>>>>>> origin/feature/sound
    @Test(expected = IllegalArgumentException::class)
    fun legacyGameCannotSilentlyUseNewRules() {
        OnlineGameRules.engine(OnlineGameState())
    }
}
