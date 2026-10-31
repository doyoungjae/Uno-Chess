package com.example.unochessprototype.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnoChessGameTest {

    @Test
    fun deck_has_40_cards_and_each_hand_starts_with_5() {
        val game = UnoChessGame(Random(1))
        val cards = game.allCardsSnapshot()

        assertEquals(40, cards.size)
        assertEquals(32, cards.count { !it.isJoker })
        assertEquals(8, cards.count { it.isJoker })
        assertEquals(5, game.hand(Player.WHITE).size)
        assertEquals(5, game.hand(Player.BLACK).size)
        assertEquals(30, game.drawPileCount())
    }

    @Test
    fun colors_do_not_change_number_card_file() {
        val game = UnoChessGame(Random(2))
        val all = game.allCardsSnapshot()

        val red2 = all.first { it.color == CardColor.RED && it.number == 2 }
        val blue2 = all.first { it.color == CardColor.BLUE && it.number == 2 }

        // Initial B-file white pawn can move, so both colors activate the same file.
        assertEquals(setOf(1), game.playableFiles(red2, Player.WHITE))
        assertEquals(setOf(1), game.playableFiles(blue2, Player.WHITE))
    }

    @Test
    fun pawn_promotes_to_queen_at_opposite_end() {
        val board = ChessBoard.empty()
        board.placeForTest(Square(1, 0), Piece(Player.WHITE, PieceType.PAWN))
        board.placeForTest(Square(7, 7), Piece(Player.WHITE, PieceType.KING))
        board.placeForTest(Square(0, 7), Piece(Player.BLACK, PieceType.KING))

        val outcome = board.move(
            Player.WHITE,
            Move(Square(1, 0), Square(0, 0)),
        )

        assertTrue(outcome.promoted)
        assertEquals(PieceType.QUEEN, board.pieceAt(Square(0, 0))!!.type)
    }

    @Test
    fun castling_and_en_passant_are_not_generated() {
        val board = ChessBoard.standard()

        val whiteKingMoves = board.legalMovesFrom(Square(7, 4))
        assertFalse(whiteKingMoves.any { kotlin.math.abs(it.to.col - 4) == 2 })

        // No special en-passant state exists in the board model at all.
        val whitePawnMoves = board.legalMovesFrom(Square(6, 4))
        assertTrue(whitePawnMoves.all { it.to.col == 4 })
    }

    @Test
    fun direct_king_capture_is_a_normal_capture_on_board() {
        val board = ChessBoard.empty()
        board.placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.ROOK))
        board.placeForTest(Square(0, 0), Piece(Player.BLACK, PieceType.KING))
        board.placeForTest(Square(7, 7), Piece(Player.WHITE, PieceType.KING))

        val outcome = board.move(
            Player.WHITE,
            Move(Square(7, 0), Square(0, 0)),
        )

        assertEquals(PieceType.KING, outcome.capturedPiece!!.type)
    }
}
