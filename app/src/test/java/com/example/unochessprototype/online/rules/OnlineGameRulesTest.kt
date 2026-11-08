package com.example.unochessprototype.online.rules

import com.example.unochessprototype.game.ChessBoard
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.online.model.OnlineGameState
import com.example.unochessprototype.online.model.OnlineResultType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OnlineGameRulesTest {
    @Test
    fun initialStateHasValidUniqueCardDistributionAndFirstPlayer() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(7))
        assertEquals(5, state.ownerHand.size)
        assertEquals(5, state.guestHand.size)
        assertEquals(30, state.drawPile.size)
        assertEquals(40, (state.ownerHand + state.guestHand + state.drawPile).toSet().size)
        assertNotEquals(state.ownerFirstCard, state.guestFirstCard)
        assertEquals(state.whiteUid, state.currentUid)
        assertEquals(64, state.board.size)
    }

    @Test
    fun normalMoveConsumesCardDrawsReplacementAndChangesTurn() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(11))
        val uid = state.currentUid
        val cardId = OnlineGameRules.playableCards(state, uid).first()
        val card = OnlineGameRules.card(cardId)
        val jokerFile = if (card.isJoker) OnlineGameRules.playableFiles(state, uid, cardId).first() else null
        val move = OnlineGameRules.legalMovesForCard(state, uid, cardId, jokerFile).first()

        val next = OnlineGameRules.playMove(state, uid, cardId, move, jokerFile, Random(13))
        assertEquals(1, next.completedTurns)
        assertFalse(next.hasResult())
        assertNotEquals(uid, next.currentUid)
        assertEquals(5, next.handFor(uid).size)
        assertFalse(cardId in next.handFor(uid))
    }

    @Test(expected = IllegalArgumentException::class)
    fun opponentCannotPlayOutOfTurn() {
        val state = OnlineGameRules.createInitial("owner", "guest", Random(17))
        val wrongUid = if (state.currentUid == "owner") "guest" else "owner"
        OnlineGameRules.playMove(
            state = state,
            actorUid = wrongUid,
            cardId = state.handFor(wrongUid).first(),
            move = Move(Square(6, 0), Square(5, 0)),
        )
    }

    @Test
    fun exchangeWorksOnlyWhenNoHandCardCanActivateMovableFile() {
        val board = ChessBoard.empty().apply {
            placeForTest(Square(7, 4), Piece(Player.WHITE, PieceType.KING))
            placeForTest(Square(0, 4), Piece(Player.BLACK, PieceType.KING))
        }
        val ownerHand = listOf(0, 1, 2, 5, 6)
        val guestHand = listOf(8, 9, 10, 13, 14)
        val state = OnlineGameState(
            ownerUid = "owner",
            guestUid = "guest",
            whiteUid = "owner",
            blackUid = "guest",
            currentUid = "owner",
            ownerFirstCard = 8,
            guestFirstCard = 2,
            board = board.toCodes(),
            ownerHand = ownerHand,
            guestHand = guestHand,
            drawPile = (0 until 40).filterNot { it in (ownerHand + guestHand).toSet() },
        )

        assertTrue(OnlineGameRules.canExchange(state, "owner"))
        val next = OnlineGameRules.exchangeTwoCards(state, "owner", setOf(0, 1), Random(19))
        assertEquals(5, next.ownerHand.size)
        assertEquals("guest", next.currentUid)
        assertEquals(1, next.completedTurns)
    }

    @Test
    fun kingCaptureEndsGameImmediately() {
        val board = ChessBoard.empty().apply {
            placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.ROOK))
            placeForTest(Square(7, 7), Piece(Player.WHITE, PieceType.KING))
            placeForTest(Square(0, 0), Piece(Player.BLACK, PieceType.KING))
        }
        val ownerHand = listOf(0, 1, 2, 3, 4)
        val guestHand = listOf(8, 9, 10, 11, 12)
        val state = OnlineGameState(
            ownerUid = "owner",
            guestUid = "guest",
            whiteUid = "owner",
            blackUid = "guest",
            currentUid = "owner",
            ownerFirstCard = 8,
            guestFirstCard = 1,
            board = board.toCodes(),
            ownerHand = ownerHand,
            guestHand = guestHand,
            drawPile = (0 until 40).filterNot { it in (ownerHand + guestHand).toSet() },
        )

        val next = OnlineGameRules.playMove(
            state = state,
            actorUid = "owner",
            cardId = 0,
            move = Move(Square(7, 0), Square(0, 0)),
        )
        assertEquals(OnlineResultType.WIN, next.resultType)
        assertEquals("owner", next.winnerUid)
        assertEquals(4, next.ownerHand.size)
        assertEquals(0, next.completedTurns)
    }
}
