package com.example.unochessprototype.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class UnoChessGameEndgameTest {
    // Same id layout as UnoChessGame.createAllCards(): 8 numbers per color, jokers from 32.
    private fun card(id: Int) = UnoCard(id, CardColor.entries[id / 8], id % 8 + 1)

    // Six cards on files G/H only (numbers 7 and 8).
    private val ghHand = listOf(7, 15, 23, 31, 6, 14).map(::card)
    private val blackHand = (0..4).map(::card)

    @Test
    fun turnStartsWithSixCardsAndPlayingKeepsFive() {
        val game = UnoChessGame(Random(1))
        assertEquals(6, game.hand(Player.WHITE).size)
        assertEquals(5, game.hand(Player.BLACK).size)
        // 버림 덱을 비워 랜덤 첫 버림 카드와 무관하게 아무 카드나 낼 수 있게 한다.
        game.forceHandsForTest(game.hand(Player.WHITE), game.hand(Player.BLACK))

        val cardId = game.playableCards().first().id
        val jokerFile = game.playableFiles(game.hand().first { it.id == cardId }).first()
        val move = game.legalMovesForCard(cardId, jokerFile).first()
        game.playCardAndMove(cardId, move, jokerFile)

        assertEquals(5, game.hand(Player.WHITE).size)
        assertEquals(6, game.hand(Player.BLACK).size)
        assertEquals(cardId, game.topDiscard()?.id)
    }

    @Test
    fun noPlayableCardDiscardsTwoAndDrawsOne() {
        val game = UnoChessGame(Random(1))
        game.forceHandsForTest(ghHand, blackHand)
        val board = game.boardForTest()
        board.clearForTest()
        // 5 white pieces, all on file A -> no endgame, no G/H move possible.
        board.placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.KING))
        for (row in 3..6) board.placeForTest(Square(row, 0), Piece(Player.WHITE, PieceType.PAWN))
        board.placeForTest(Square(0, 7), Piece(Player.BLACK, PieceType.KING))

        assertFalse(game.isEndgame())
        assertTrue(game.canExchangeTwoCards())

        val result = game.exchangeTwoCards(setOf(7, 15))
        assertEquals(1, result.drawnCards.size)
        assertEquals(5, game.hand(Player.WHITE).size)
        assertEquals(6, game.hand(Player.BLACK).size)
        assertEquals(Player.BLACK, game.currentPlayer)
    }

    @Test
    fun numberCardsActAsJokerWhenFourOrFewerPiecesRemain() {
        val game = UnoChessGame(Random(1))
        game.forceHandsForTest(ghHand, blackHand)
        val board = game.boardForTest()
        board.clearForTest()
        board.placeForTest(Square(7, 4), Piece(Player.WHITE, PieceType.KING))
        board.placeForTest(Square(7, 0), Piece(Player.WHITE, PieceType.ROOK))
        board.placeForTest(Square(0, 4), Piece(Player.BLACK, PieceType.KING))

        assertTrue(game.isEndgame())
        assertTrue(game.actsAsJoker(ghHand[0]))
        val files = game.playableFiles(ghHand[0])
        assertTrue(0 in files && 4 in files)
        assertFalse(game.canExchangeTwoCards())

        val move = game.legalMovesForCard(ghHand[0].id, chosenFileForJoker = 0).first()
        game.playCardAndMove(ghHand[0].id, move, chosenFileForJoker = 0)
        assertEquals(Player.BLACK, game.currentPlayer)
        assertEquals(5, game.hand(Player.WHITE).size)
    }

    @Test
    fun firstTurnJokerLosesAndTiesRedraw() {
        val joker = UnoCard(32, null, null)
        val joker2 = UnoCard(33, null, null)
        assertEquals(LocalPlayerSlot.PLAYER2, FirstTurnRound(joker, card(0)).winner)
        assertEquals(LocalPlayerSlot.PLAYER1, FirstTurnRound(card(7), card(14)).winner)
        assertEquals(null, FirstTurnRound(joker, joker2).winner)
        assertEquals(null, FirstTurnRound(card(2), card(10)).winner) // RED-3 vs BLUE-3
    }

    @Test
    fun firstTurnLoserBecomesFirstDiscardAndNoCardIsLost() {
        repeat(30) { seed ->
            val game = UnoChessGame(Random(seed))
            val decisive = game.firstTurnRounds.last()
            assertTrue(game.firstTurnRounds.dropLast(1).all { it.winner == null })
            assertEquals(decisive.loserCard, game.topDiscard())
            val total = game.hand(Player.WHITE).size + game.hand(Player.BLACK).size + game.drawPileCount() + 1
            assertEquals(UnoChessGame.TOTAL_CARD_COUNT, total)
        }
    }

    @Test
    fun deckHasEightOfEachColorAndEightJokers() {
        val cards = UnoChessGame(Random(1)).allCardsSnapshot()
        assertEquals(40, cards.size)
        CardColor.entries.forEach { color ->
            assertEquals((1..8).toList(), cards.filter { it.color == color }.map { it.number })
        }
        assertEquals(8, cards.count { it.isJoker })
    }

    @Test
    fun firstTurnRoundsSurviveConstruction() {
        // 회귀: firstTurnRounds가 init 뒤에 선언되면 비어 있어 player1Starts가 크래시했다.
        val game = UnoChessGame(Random(3))
        assertTrue(game.firstTurnRounds.isNotEmpty())
        game.player1Starts
    }

    @Test
    fun onlyCardsMatchingTopDiscardColorOrNumberArePlayable() {
        val game = UnoChessGame(Random(1))
        // RED-2, BLUE-5, GREEN-3, YELLOW-7, JOKER, GREEN-8
        val white = listOf(card(1), card(12), card(18), card(30), UnoCard(32, null, null), card(23))
        game.forceHandsForTest(white, blackHand.map { card(it.id + 5) })
        game.setTopDiscardForTest(card(4)) // RED-5

        assertEquals(setOf(1, 12, 32), game.playableCards().map { it.id }.toSet())
        assertTrue(game.playableFiles(card(18)).isEmpty())
        assertFalse(game.canExchangeTwoCards())

        // 낸 카드가 새 기준이 된다: BLUE-5 를 내면 다음은 BLUE 또는 5.
        val move = game.legalMovesForCard(12).first()
        game.playCardAndMove(12, move)
        assertEquals(12, game.topDiscard()?.id)
    }

    @Test
    fun nothingMatchesTopDiscardAllowsExchange() {
        val game = UnoChessGame(Random(1))
        // GREEN/YELLOW 2~4만, 맨 위는 BLUE-1 -> 매칭 없음(기물은 모든 열에서 이동 가능).
        val white = listOf(17, 18, 19, 25, 26, 27).map(::card)
        game.forceHandsForTest(white, blackHand)
        game.setTopDiscardForTest(card(8))

        assertTrue(game.playableCards().isEmpty())
        assertTrue(game.canExchangeTwoCards())
        game.exchangeTwoCards(setOf(17, 18))
        assertEquals(5, game.hand(Player.WHITE).size)
        assertEquals(18, game.topDiscard()?.id)
    }

    @Test
    fun jokerOnTopAllowsAnyCard() {
        val game = UnoChessGame(Random(1))
        val white = listOf(17, 18, 19, 25, 26, 27).map(::card)
        game.forceHandsForTest(white, blackHand)
        game.setTopDiscardForTest(UnoCard(39, null, null))
        assertEquals(6, game.playableCards().size)
    }

    @Test
    fun timeOutGivesWinToOpponent() {
        val game = UnoChessGame(Random(1))
        game.timeOut(Player.WHITE)
        assertEquals(Player.BLACK, (game.result as GameResult.Win).winner)
    }
}
