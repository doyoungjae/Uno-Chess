package com.example.unochessprototype.game

import kotlin.random.Random

class UnoChessGame(
    private val random: Random = Random.Default,
) {
    companion object {
        const val MAX_TURNS = 200
        const val HAND_SIZE = 5
        const val TOTAL_CARD_COUNT = 40
    }

    var board: ChessBoard = ChessBoard.standard()
        private set

    private val allCards: List<UnoCard> = createAllCards()
    private val hands: MutableMap<Player, MutableList<UnoCard>> = mutableMapOf(
        Player.WHITE to mutableListOf(),
        Player.BLACK to mutableListOf(),
    )
    private var drawPile: MutableList<UnoCard> = mutableListOf()

    var currentPlayer: Player = Player.WHITE
        private set

    var completedTurns: Int = 0
        private set

    var result: GameResult? = null
        private set

    private val _logs = mutableListOf<String>()
    val logs: List<String>
        get() = _logs.toList()

    init {
        reset()
    }

    fun reset() {
        board = ChessBoard.standard()
        currentPlayer = Player.WHITE
        completedTurns = 0
        result = null
        _logs.clear()

        val shuffled = allCards.shuffled(random)
        hands.getValue(Player.WHITE).apply {
            clear()
            addAll(shuffled.take(HAND_SIZE))
        }
        hands.getValue(Player.BLACK).apply {
            clear()
            addAll(shuffled.drop(HAND_SIZE).take(HAND_SIZE))
        }
        drawPile = shuffled.drop(HAND_SIZE * 2).toMutableList()

        log("게임 시작 · 각 플레이어 손패 5장 · 드로우 덱 ${drawPile.size}장")
        evaluateStartOfTurn()
    }

    fun hand(player: Player = currentPlayer): List<UnoCard> = hands.getValue(player).toList()

    fun drawPileCount(): Int = drawPile.size

    fun allCardsSnapshot(): List<UnoCard> = allCards.toList()

    fun playableFiles(card: UnoCard, player: Player = currentPlayer): Set<Int> {
        if (result != null) return emptySet()
        val movable = board.movableFiles(player)
        return if (card.isJoker) {
            movable
        } else {
            val file = card.number!! - 1
            if (file in movable) setOf(file) else emptySet()
        }
    }

    fun playableCards(player: Player = currentPlayer): List<UnoCard> =
        hand(player).filter { playableFiles(it, player).isNotEmpty() }

    fun canExchangeTwoCards(): Boolean =
        result == null &&
            board.hasAnyLegalMove(currentPlayer) &&
            playableCards(currentPlayer).isEmpty()

    fun legalMovesForCard(cardId: Int, chosenFileForJoker: Int? = null): List<Move> {
        val card = hand(currentPlayer).firstOrNull { it.id == cardId } ?: return emptyList()
        val files = playableFiles(card)
        val file = if (card.isJoker) {
            chosenFileForJoker ?: return emptyList()
        } else {
            card.number!! - 1
        }

        if (file !in files) return emptyList()

        return buildList {
            for (row in 0..7) {
                val from = Square(row, file)
                val piece = board.pieceAt(from)
                if (piece?.player == currentPlayer) {
                    addAll(board.legalMovesFrom(from))
                }
            }
        }
    }

    fun playCardAndMove(
        cardId: Int,
        move: Move,
        chosenFileForJoker: Int? = null,
    ): PlayResult {
        check(result == null) { "The game is already over." }

        val player = currentPlayer
        val playerHand = hands.getValue(player)
        val card = playerHand.firstOrNull { it.id == cardId }
            ?: error("Selected card is not in the current player's hand.")

        val allowedFiles = playableFiles(card, player)
        val file = if (card.isJoker) {
            requireNotNull(chosenFileForJoker) { "Joker requires a file selection." }
        } else {
            card.number!! - 1
        }

        require(file in allowedFiles) { "This card cannot activate a movable file right now." }
        require(move.from.col == file) { "Selected piece is not on the activated file." }
        require(move in board.legalMovesFrom(move.from)) { "Illegal chess move." }

        val movingPiece = board.pieceAt(move.from) ?: error("No piece selected.")
        require(movingPiece.player == player) { "Cannot move the opponent's piece." }

        val outcome = board.move(player, move)

        log(
            "${player.displayName()} · ${card.displayName()} 사용 · " +
                "${outcome.movedPieceBeforePromotion.type} ${move.from.algebraic()}→${move.to.algebraic()}"
        )

        if (outcome.capturedPiece != null) {
            log(
                "${player.displayName()} · ${outcome.capturedPiece.player} " +
                    "${outcome.capturedPiece.type} 포획"
            )
        }

        if (outcome.promoted) {
            log("${player.displayName()} · PAWN ${move.to.algebraic()} 도달 → QUEEN 자동 승급")
        }

        // King capture ends the game immediately. No replacement card is drawn.
        if (outcome.capturedPiece?.type == PieceType.KING) {
            playerHand.removeAll { it.id == card.id }
            result = GameResult.Win(player)
            log("게임 종료 · ${player.displayName()} 승리 · KING 직접 포획")
            return PlayResult(
                moveOutcome = outcome,
                usedCard = card,
                replacementCard = null,
                gameResult = result,
            )
        }

        // Keep the used card in hand until a replacement has been secured.
        // This preserves exactly 10 cards across both hands when the 30-card pool is rebuilt.
        ensureDrawCapacity(1)
        val replacement = drawPile.removeLast()
        playerHand += replacement
        playerHand.removeAll { it.id == card.id }

        check(playerHand.size == HAND_SIZE) { "Hand-size invariant broken after normal turn." }
        log("${player.displayName()} · ${replacement.displayName()} 1장 보충 · 손패 5장 유지")

        finishTurn()

        return PlayResult(
            moveOutcome = outcome,
            usedCard = card,
            replacementCard = replacement,
            gameResult = result,
        )
    }

    fun exchangeTwoCards(cardIds: Set<Int>): ExchangeResult {
        check(result == null) { "The game is already over." }
        require(canExchangeTwoCards()) { "Two-card exchange is only allowed when no hand card can move a piece." }
        require(cardIds.size == 2) { "Exactly two cards must be selected." }

        val player = currentPlayer
        val playerHand = hands.getValue(player)
        val discarded = cardIds.map { id ->
            playerHand.firstOrNull { it.id == id }
                ?: error("Selected exchange card is not in the current player's hand.")
        }

        // If fewer than 2 cards remain, rebuild before drawing so both hands still contain 10 cards.
        ensureDrawCapacity(2)
        val drawn = listOf(drawPile.removeLast(), drawPile.removeLast())

        // Add replacements first, then remove the selected cards: 5 -> 7 -> 5.
        playerHand += drawn
        val discardedIds = discarded.map { it.id }.toSet()
        playerHand.removeAll { it.id in discardedIds }

        check(playerHand.size == HAND_SIZE) { "Hand-size invariant broken after two-card exchange." }

        log(
            "${player.displayName()} · 이동 가능한 카드 없음 · " +
                "${discarded.joinToString { it.displayName() }} 2장 교환"
        )
        log("${player.displayName()} · ${drawn.joinToString { it.displayName() }} 2장 드로우 · 손패 5장 유지")

        finishTurn()

        return ExchangeResult(
            discardedCards = discarded,
            drawnCards = drawn,
            gameResult = result,
        )
    }

    private fun finishTurn() {
        completedTurns += 1

        if (completedTurns >= MAX_TURNS) {
            result = GameResult.Draw("${MAX_TURNS}턴 동안 KING이 포획되지 않았습니다.")
            log("게임 종료 · 무승부 · 최대 ${MAX_TURNS}턴 도달")
            return
        }

        currentPlayer = currentPlayer.opposite()
        log("턴 변경 · ${currentPlayer.displayName()} 차례")
        evaluateStartOfTurn()
    }

    private fun evaluateStartOfTurn() {
        if (result != null) return

        if (!board.hasAnyLegalMove(currentPlayer)) {
            result = GameResult.Draw("${currentPlayer.displayName()}에게 이동 가능한 체스 기물이 없습니다.")
            log("게임 종료 · 무승부 · ${currentPlayer.displayName()} 이동 가능한 기물 없음")
        }
    }

    private fun ensureDrawCapacity(required: Int) {
        if (drawPile.size >= required) return
        rebuildThirtyCardPool()
        check(drawPile.size >= required) { "Not enough cards after rebuilding the draw pile." }
    }

    /**
     * Both hands are kept at five cards when this function is called.
     * Therefore exactly 10 cards are excluded and the other 30 cards are shuffled.
     *
     * Number-card colors remain only visual identities; there is no floor-card or color-matching rule.
     */
    private fun rebuildThirtyCardPool() {
        val handIds = hands.values.flatten().map { it.id }.toSet()
        check(handIds.size == HAND_SIZE * 2) { "Deck rebuild requires exactly 10 unique cards in hands." }

        drawPile = allCards
            .filterNot { it.id in handIds }
            .shuffled(random)
            .toMutableList()

        check(drawPile.size == 30) { "Rebuilt deck must contain exactly 30 cards." }
        log("덱 재구성 · 양쪽 손패 10장 제외 · 나머지 30장 셔플")
    }

    private fun log(message: String) {
        _logs += "${_logs.size + 1}. $message"
    }

    private fun createAllCards(): List<UnoCard> {
        val cards = mutableListOf<UnoCard>()
        var id = 0

        CardColor.entries.forEach { color ->
            for (number in 1..8) {
                cards += UnoCard(id = id++, color = color, number = number)
            }
        }

        repeat(8) {
            cards += UnoCard(id = id++, color = null, number = null)
        }

        check(cards.size == TOTAL_CARD_COUNT)
        return cards
    }

    // Test-only helpers. They are internal so normal UI code does not depend on them.
    internal fun boardForTest(): ChessBoard = board

    internal fun forceHandsForTest(white: List<UnoCard>, black: List<UnoCard>) {
        require(white.size == HAND_SIZE && black.size == HAND_SIZE)
        hands.getValue(Player.WHITE).apply { clear(); addAll(white) }
        hands.getValue(Player.BLACK).apply { clear(); addAll(black) }
        val handIds = (white + black).map { it.id }.toSet()
        drawPile = allCards.filterNot { it.id in handIds }.shuffled(random).toMutableList()
    }
}
