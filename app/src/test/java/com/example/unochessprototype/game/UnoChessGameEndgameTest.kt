package com.example.unochessprototype.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class UnoChessGameEndgameTest {
    private fun card(id: Int, number: Int) = UnoCard(id, CardColor.RED, number)

    @Test
    fun numberCardsActAsJokerWhenFourOrFewerPiecesRemain() {
        val game = UnoChessGame(Random(1))
        val hand = (1..5).map { card(it - 1, 8) } // all H-file cards
        game.forceHandsForTest(hand, (5..9).map { card(it, 8) })

        val board = game.boardForTest()
        board.clearForTest()
        board.placeForTest(Square(7, 4), Piece(Player.WHITE, PieceType.KING))
        board.placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.ROOK))
        board.placeForTest(Square(0, 4), Piece(Player.BLACK, PieceType.KING))

        assertTrue(game.isEndgame())
        val files = game.playableFiles(hand[0])
        assertTrue(0 in files && 4 in files)
        assertFalse(game.canExchangeTwoCards())

        val move = game.legalMovesForCard(hand[0].id, chosenFileForJoker = 0).first()
        game.playCardAndMove(hand[0].id, move, chosenFileForJoker = 0)
        assertEquals(Player.BLACK, game.currentPlayer)
    }

    @Test
    fun standardBoardIsNotEndgame() {
        assertFalse(UnoChessGame(Random(1)).isEndgame())
    }
}
