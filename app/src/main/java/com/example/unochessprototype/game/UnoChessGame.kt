package com.example.unochessprototype.game

import kotlin.random.Random

class UnoChessGame(
    private val random: Random = Random.Default,
    initialState: GameSnapshot? = null,
    /** 온라인처럼 BLACK 화면이 180도 뒤집혀 보이면 true. 카드 숫자 열을 BLACK 시점으로 센다. */
    private val blackViewFlipped: Boolean = false,
) {
    companion object {
        const val MAX_TURNS = 200
        const val HAND_SIZE = 5
        const val TOTAL_CARD_COUNT = 40
        const val ENDGAME_PIECE_LIMIT = 4
    }

    var board: ChessBoard = ChessBoard.standard()
        private set

    private val allCards: List<UnoCard> = createAllCards()
    private val hands: MutableMap<Player, MutableList<UnoCard>> = mutableMapOf(
        Player.WHITE to mutableListOf(),
        Player.BLACK to mutableListOf(),
    )
    private var drawPile: MutableList<UnoCard> = mutableListOf()
    private val discardPile: MutableList<UnoCard> = mutableListOf()

    var currentPlayer: Player = Player.WHITE
        private set

    var completedTurns: Int = 0
        private set

    var result: GameResult? = null
        private set

    private val _logs = mutableListOf<String>()
    val logs: List<String>
        get() = _logs.toList()

    // init { reset() } 보다 먼저 선언해야 한다. 뒤에 두면 초기값(emptyList)이 reset() 결과를 덮어쓴다.
    /** 선공 뽑기 기록. 마지막 판이 승부가 난 판이며, 이긴 쪽(PLAYER1/2)이 WHITE·선공이 된다. */
    var firstTurnRounds: List<FirstTurnRound> = emptyList()
        private set

    val player1Starts: Boolean
        get() = firstTurnRounds.lastOrNull()?.winner == LocalPlayerSlot.PLAYER1

    init {
        if (initialState == null) reset() else restore(initialState)
    }

    /** 온라인 수신 상태를 복원할 때 reset/선공 추첨/턴 드로우를 다시 실행하지 않는다. */
    private fun restore(state: GameSnapshot) {
        board = ChessBoard.fromCodes(state.board)
        currentPlayer = state.currentPlayer
        completedTurns = state.completedTurns
        result = state.result
        hands.getValue(Player.WHITE).addAll(state.whiteHand)
        hands.getValue(Player.BLACK).addAll(state.blackHand)
        drawPile.addAll(state.drawPile)
        discardPile.addAll(state.discardPile)
        firstTurnRounds = state.firstTurnRounds
        _logs.addAll(state.logs)
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        board.toCodes(), currentPlayer, completedTurns, result,
        hand(Player.WHITE), hand(Player.BLACK), drawPile.toList(), discardPile.toList(),
        firstTurnRounds.toList(), logs,
    )

    fun reset() {
        board = ChessBoard.standard()
        currentPlayer = Player.WHITE
        completedTurns = 0
        result = null
        _logs.clear()

        val deck = allCards.shuffled(random).toMutableList()
        discardPile.clear()
        firstTurnRounds = decideFirstTurn(deck)

        hands.getValue(Player.WHITE).apply {
            clear()
            addAll(deck.take(HAND_SIZE))
        }
        hands.getValue(Player.BLACK).apply {
            clear()
            addAll(deck.drop(HAND_SIZE).take(HAND_SIZE))
        }
        drawPile = deck.drop(HAND_SIZE * 2).toMutableList()

        log("게임 시작 · 각 플레이어 손패 5장 · 드로우 덱 ${drawPile.size}장")
        evaluateStartOfTurn()
    }

    // 덱 맨 위에서 1장씩 뽑아 비교. 동점이면 두 장을 덱에 섞어 다시 뽑고,
    // 승부가 나면 진 카드는 첫 버림 카드가 되고 이긴 카드는 덱에 섞는다.
    private fun decideFirstTurn(deck: MutableList<UnoCard>): List<FirstTurnRound> {
        val rounds = mutableListOf<FirstTurnRound>()
        while (true) {
            val round = FirstTurnRound(deck.removeAt(deck.lastIndex), deck.removeAt(deck.lastIndex))
            rounds += round
            log("선공 뽑기 · PLAYER 1 ${round.player1Card.displayName()} vs PLAYER 2 ${round.player2Card.displayName()}")
            val winnerCard = round.winnerCard
            val loserCard = round.loserCard
            if (winnerCard == null || loserCard == null) {
                deck += listOf(round.player1Card, round.player2Card)
                deck.shuffle(random)
                log("선공 뽑기 · 동점 · 다시 뽑기")
                continue
            }
            discardPile += loserCard
            deck += winnerCard
            deck.shuffle(random)
            log("선공 결정 · ${round.winner} 선공(WHITE) · ${loserCard.displayName()} 첫 버림 카드")
            return rounds
        }
    }

    fun hand(player: Player = currentPlayer): List<UnoCard> = hands.getValue(player).toList()

    fun drawPileCount(): Int = drawPile.size

    fun topDiscard(): UnoCard? = discardPile.lastOrNull()

    fun allCardsSnapshot(): List<UnoCard> = allCards.toList()

    // 종반전: 자기 기물이 4개 이하면 손패 전체가 JOKER로 취급된다.
    fun isEndgame(player: Player = currentPlayer): Boolean =
        board.pieceCount(player) <= ENDGAME_PIECE_LIMIT

    fun actsAsJoker(card: UnoCard, player: Player = currentPlayer): Boolean =
        card.isJoker || isEndgame(player)

    /**
     * UNO 매칭: 버림 덱 맨 위 카드와 색 또는 숫자가 같아야 낼 수 있다.
     * JOKER(종반전 포함)는 언제나 낼 수 있고, 맨 위가 JOKER면 다음 카드는 아무거나 낼 수 있다.
     */
    fun matchesTopDiscard(card: UnoCard, player: Player = currentPlayer): Boolean {
        val top = topDiscard() ?: return true
        if (actsAsJoker(card, player) || top.isJoker) return true
        return card.color == top.color || card.number == top.number
    }

    /** 카드 숫자 N → 카드를 내는 플레이어 화면 왼쪽에서 N번째 열의 보드 인덱스. */
    fun fileForNumber(number: Int, player: Player = currentPlayer): Int =
        if (player == Player.BLACK && blackViewFlipped) 8 - number else number - 1

    fun playableFiles(card: UnoCard, player: Player = currentPlayer): Set<Int> {
        if (result != null || !matchesTopDiscard(card, player)) return emptySet()
        val movable = board.movableFiles(player)
        return if (actsAsJoker(card, player)) {
            movable
        } else {
            val file = fileForNumber(card.number!!, player)
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
        val file = if (actsAsJoker(card)) {
            chosenFileForJoker ?: return emptyList()
        } else {
            fileForNumber(card.number!!)
        }

        if (file !in files) return emptyList()

        return buildList {
            for (row in 0..7) {
                val from = Square(row, file)
                val piece = board.pieceAt(from)
                if (piece?.player == currentPlayer) addAll(board.legalMovesFrom(from))
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
        val file = if (actsAsJoker(card, player)) {
            requireNotNull(chosenFileForJoker) { "Joker requires a file selection." }
        } else {
            fileForNumber(card.number!!, player)
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
            log("${player.displayName()} · ${outcome.capturedPiece.player} ${outcome.capturedPiece.type} 포획")
            if (board.pieceCount(player.opposite()) == ENDGAME_PIECE_LIMIT) {
                log("종반전 · ${player.opposite().displayName()} 기물 ${ENDGAME_PIECE_LIMIT}개 이하 · 모든 손패 JOKER 취급")
            }
        }

        if (outcome.promoted) {
            log("${player.displayName()} · PAWN ${move.to.algebraic()} 도달 → QUEEN 자동 승급")
        }

        playerHand.removeAll { it.id == card.id }
        discardPile += card

        if (outcome.capturedPiece?.type == PieceType.KING) {
            result = GameResult.Win(player)
            log("게임 종료 · ${player.displayName()} 승리 · KING 직접 포획")
            return PlayResult(outcome, card, result)
        }

        check(playerHand.size == HAND_SIZE) { "Hand-size invariant broken after normal turn." }

        finishTurn()

        return PlayResult(outcome, card, result)
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

        val discardedIds = discarded.map { it.id }.toSet()
        playerHand.removeAll { it.id in discardedIds }
        discardPile += discarded
        log(
            "${player.displayName()} · 사용 가능한 카드 없음 · " +
                "${discarded.joinToString { it.displayName() }} 2장 버림"
        )

        // 턴은 넘어가지 않는다. 2장을 뽑아 6장을 유지하고, 낼 카드가 생길 때까지 교환을 반복할 수 있다.
        val drawn = listOf(drawCard(player), drawCard(player))
        check(playerHand.size == HAND_SIZE + 1) { "Hand-size invariant broken after two-card exchange." }
        log("${player.displayName()} · ${drawn.joinToString { it.displayName() }} 2장 드로우 · 턴 유지")

        return ExchangeResult(discarded, drawn, result)
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

    fun timeOut(player: Player) {
        if (result != null) return
        result = GameResult.Win(player.opposite(), "상대의 시간이 모두 소진되었습니다.")
        log("게임 종료 · ${player.displayName()} 시간 초과 · ${player.opposite().displayName()} 승리")
    }

    private fun evaluateStartOfTurn() {
        if (result != null) return

        // 턴 시작 드로우: 5장 -> 6장
        val drawn = drawCard(currentPlayer)
        log("${currentPlayer.displayName()} · ${drawn.displayName()} 1장 드로우 · 손패 ${HAND_SIZE + 1}장")

        if (!board.hasAnyLegalMove(currentPlayer)) {
            result = GameResult.Draw("${currentPlayer.displayName()}에게 이동 가능한 체스 기물이 없습니다.")
            log("게임 종료 · 무승부 · ${currentPlayer.displayName()} 이동 가능한 기물 없음")
        }
    }

    private fun drawCard(player: Player): UnoCard {
        if (drawPile.isEmpty()) rebuildDrawPile()
        val card = drawPile.removeAt(drawPile.lastIndex)
        hands.getValue(player) += card
        return card
    }

    private fun rebuildDrawPile() {
        // 맨 위 버림 카드는 매칭 기준이라 남겨 두고 나머지만 섞는다.
        val top = topDiscard()
        val keepIds = hands.values.flatten().map { it.id }.toSet() + listOfNotNull(top?.id)
        drawPile = allCards
            .filterNot { it.id in keepIds }
            .shuffled(random)
            .toMutableList()
        discardPile.clear()
        top?.let { discardPile += it }
        check(drawPile.isNotEmpty()) { "No cards left to rebuild the draw pile." }
        log("덱 재구성 · 손패를 제외한 ${drawPile.size}장 셔플")
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

    internal fun boardForTest(): ChessBoard = board

    internal fun forceHandsForTest(white: List<UnoCard>, black: List<UnoCard>) {
        hands.getValue(Player.WHITE).apply { clear(); addAll(white) }
        hands.getValue(Player.BLACK).apply { clear(); addAll(black) }
        val handIds = (white + black).map { it.id }.toSet()
        drawPile = allCards.filterNot { it.id in handIds }.shuffled(random).toMutableList()
        discardPile.clear()
    }

    internal fun setTopDiscardForTest(card: UnoCard) {
        drawPile.removeAll { it.id == card.id }
        discardPile += card
    }
}
