package com.unochess.core

import kotlin.random.Random

/**
 * UNO Chess의 UI/Firebase와 분리된 순수 게임 로직.
 *
 * 현재 로컬 프로토타입 규칙
 * 1) 각 플레이어 손패는 항상 5장 유지
 * 2) 턴 시작 드로우 없음
 * 3) 카드 1장 선택 + 기물 이동 성공 -> 사용 카드 버림 -> 카드 1장 보충 -> 상대 턴
 * 4) 실제로 움직일 수 있는 카드가 하나도 없으면 카드 2장 선택 -> 2장 버림 -> 2장 보충 -> 상대 턴
 * 5) 숫자 1..8은 A..H 파일을 활성화
 * 6) Joker는 색상과 무관하게 항상 매칭 가능하며, 원하는 파일을 선택
 * 7) 캐슬링/앙파상 없음, 폰은 끝 랭크에서 Queen 자동 승급
 * 8) 체크/체크메이트 없음, 상대 King 직접 포획 시 승리
 * 9) 덱에 필요한 카드 수가 부족하면 두 플레이어의 손패 10장을 제외한 나머지 30장을 섞어 덱 재구성
 */
class UnoChessGame(
    private val random: Random = Random.Default
) {
    val board = ChessBoard()

    private val deck = UnoDeck(random)
    private val hands = mutableMapOf(
        PlayerSide.WHITE to mutableListOf<UnoCard>(),
        PlayerSide.BLACK to mutableListOf<UnoCard>()
    )

    /** 물리적으로 현재 두 손패/드로우 덱에 없는 카드들. */
    private val discardPool = mutableListOf<UnoCard>()

    /** 바닥 카드의 매칭 조건은 덱 재구성 후에도 유지하기 위해 별도 상태로 보관한다. */
    private lateinit var top: ActiveUnoCard

    private val gameLogs = mutableListOf<String>()

    var currentPlayer: PlayerSide = PlayerSide.WHITE
        private set

    var winner: PlayerSide? = null
        private set

    val isGameOver: Boolean get() = winner != null

    init { setupGame() }

    fun handOf(side: PlayerSide): List<UnoCard> = hands.getValue(side).toList()
    fun topCard(): ActiveUnoCard = top
    fun logs(): List<String> = gameLogs.toList()
    fun deckRemaining(): Int = deck.remaining()
    fun discardedCount(): Int = discardPool.size

    private fun setupGame() {
        repeat(UnoDeck.HAND_SIZE) {
            hands.getValue(PlayerSide.WHITE) += deck.draw()
            hands.getValue(PlayerSide.BLACK) += deck.draw()
        }
        determineFirstPlayerAndStartCard()
        log("게임 시작 · ${sideLabel(currentPlayer)} 선공 · 시작 카드 ${cardLabel(top.card)}")
        log("초기 손패: P1 5장 / P2 5장 · 덱 ${deck.remaining()}장")
    }

    /**
     * 숫자 카드 한 장씩 뽑아 높은 숫자가 선공.
     * 동률이면 다시 뽑는다. 낮은 숫자를 시작 바닥 카드로 두고 나머지는 덱에 반환한다.
     */
    private fun determineFirstPlayerAndStartCard() {
        while (true) {
            val held = mutableListOf<UnoCard>()
            val white = drawNumberCard(held)
            val black = drawNumberCard(held)

            if (white.number == black.number) {
                deck.returnAndShuffle(held)
                continue
            }

            val whiteWins = white.number > black.number
            currentPlayer = if (whiteWins) PlayerSide.WHITE else PlayerSide.BLACK
            val lower = if (whiteWins) black else white

            top = ActiveUnoCard(lower, lower.number)
            discardPool += lower

            val returnCards = held.toMutableList()
            returnCards.remove(lower)
            deck.returnAndShuffle(returnCards)
            return
        }
    }

    private fun drawNumberCard(held: MutableList<UnoCard>): UnoCard.NumberCard {
        while (true) {
            val card = deck.draw()
            held += card
            if (card is UnoCard.NumberCard) return card
        }
    }

    /**
     * 바닥 카드와의 매칭 규칙.
     * - Joker는 색/숫자와 무관하게 항상 매칭 가능
     * - 바닥이 Joker이면 다음 플레이어는 어떤 카드든 사용 가능
     * - 숫자 카드는 같은 색 또는 같은 숫자일 때 매칭
     */
    fun canMatchTop(card: UnoCard): Boolean = when {
        card == UnoCard.Joker -> true
        top.card == UnoCard.Joker -> true
        card is UnoCard.NumberCard && top.card is UnoCard.NumberCard -> {
            val topNumber = top.card as UnoCard.NumberCard
            card.color == topNumber.color || card.number == topNumber.number
        }
        else -> false
    }

    /** 카드가 매칭될 뿐 아니라 실제로 해당 카드로 움직일 체스 기물이 있어야 한다. */
    fun canActuallyMove(card: UnoCard, side: PlayerSide = currentPlayer): Boolean {
        if (!canMatchTop(card)) return false
        val moves = board.legalMovesFor(side)
        return when (card) {
            is UnoCard.NumberCard -> moves.any { it.from.fileNumber == card.number }
            UnoCard.Joker -> moves.isNotEmpty()
        }
    }

    /** 현재 턴에서 실제로 사용해 기물을 움직일 수 있는 손패 인덱스. */
    fun playableCardIndices(side: PlayerSide = currentPlayer): List<Int> {
        val hand = hands.getValue(side)
        return hand.indices.filter { canActuallyMove(hand[it], side) }
    }

    fun hasPlayableCard(side: PlayerSide = currentPlayer): Boolean = playableCardIndices(side).isNotEmpty()

    /** Joker로 선택 가능한 파일. 실제 합법 이동이 하나 이상 존재하는 파일만 반환한다. */
    fun legalJokerFiles(side: PlayerSide = currentPlayer): Set<Int> =
        board.legalMovesFor(side).map { it.from.fileNumber }.toSet()

    /**
     * 특정 카드로 가능한 체스 이동 목록.
     * 숫자 N -> N번째 파일(A=1..H=8), Joker -> 선택한 jokerFile.
     */
    fun legalMovesForCard(
        handIndex: Int,
        jokerFile: Int? = null
    ): List<ChessMove> {
        check(!isGameOver) { "Game is over" }
        val card = hands.getValue(currentPlayer).getOrNull(handIndex)
            ?: error("Invalid hand index")
        require(canMatchTop(card)) { "Card $card cannot be matched with ${top.card}" }

        val file = fileFor(card, jokerFile)
        return board.legalMovesFor(currentPlayer)
            .filter { it.from.fileNumber == file }
    }

    /**
     * 카드 1장 사용 + 체스 기물 이동 + 카드 1장 보충.
     * 정상 처리 후 손패는 다시 5장이 되고 상대 턴으로 넘어간다.
     */
    fun playCardAndMove(
        handIndex: Int,
        move: ChessMove,
        jokerFile: Int? = null
    ): TurnResult {
        check(!isGameOver) { "Game is over" }
        check(handOf(currentPlayer).size == UnoDeck.HAND_SIZE) { "Hand must start turn with 5 cards" }

        // 카드를 사용하기 전 두 손패가 5장일 때 덱을 재구성해야 정확히 30장을 만들 수 있다.
        ensureDrawCapacity(1)

        val hand = hands.getValue(currentPlayer)
        val card = hand.getOrNull(handIndex) ?: error("Invalid hand index")
        require(canActuallyMove(card)) { "This card cannot produce a legal move now" }

        val file = fileFor(card, jokerFile)
        require(move.from.fileNumber == file) {
            "Selected card activates file $file, but move starts from file ${move.from.fileNumber}"
        }

        val allowedMoves = legalMovesForCard(handIndex, jokerFile)
        require(move in allowedMoves) { "Move $move is not allowed by chess rules/card restriction" }

        val mover = currentPlayer
        val movingPiece = board.get(move.from) ?: error("No piece at ${move.from.algebraic()}")
        val willPromote = movingPiece.type == PieceType.PAWN && (move.to.row == 0 || move.to.row == 7)

        // 카드 사용
        hand.removeAt(handIndex)
        discardPool += card
        top = ActiveUnoCard(card, file)

        // 체스 이동
        val captured = board.move(move)
        val kingCaptured = captured?.type == PieceType.KING
        if (kingCaptured) winner = mover

        log(
            buildString {
                append("${sideLabel(mover)} · ${cardLabel(card)} 사용 · ")
                append("${pieceLabel(movingPiece.type)} ${move.from.algebraic()}→${move.to.algebraic()}")
                if (captured != null) append(" · ${pieceLabel(captured.type)} 포획")
                if (willPromote) append(" · PAWN→QUEEN 자동 승급")
            }
        )

        // 사용한 1장을 즉시 보충하여 손패 5장 유지
        val drawn = deck.draw()
        hand += drawn
        log("${sideLabel(mover)} 보충 드로우 · ${cardLabel(drawn)} · 손패 ${hand.size}장")

        if (kingCaptured) {
            log("게임 종료 · ${sideLabel(mover)} 승리 · 상대 KING 포획")
        } else {
            currentPlayer = currentPlayer.opposite()
            log("턴 변경 · ${sideLabel(currentPlayer)} 차례")
        }

        return TurnResult(
            player = mover,
            playedCard = card,
            drawnCard = drawn,
            move = move,
            capturedPiece = captured,
            promotedToQueen = willPromote,
            winner = winner
        )
    }

    /**
     * 손패 5장 중 실제로 움직일 수 있는 카드가 하나도 없을 때:
     * 정확히 2장을 선택해 버리고 2장을 새로 뽑아 손패를 5장으로 복구한 뒤 턴 종료.
     * 전달한 인덱스의 순서대로 버리며 두 번째 카드가 새 바닥 카드가 된다.
     */
    fun applyNoMovePenalty(discardHandIndices: List<Int>): PenaltyResult {
        check(!isGameOver) { "Game is over" }
        check(!hasPlayableCard()) { "Penalty is allowed only when no card can make a legal move" }
        require(discardHandIndices.size == 2 && discardHandIndices.distinct().size == 2) {
            "Select exactly two different cards"
        }

        val hand = hands.getValue(currentPlayer)
        require(hand.size == UnoDeck.HAND_SIZE) { "Hand must contain 5 cards before penalty" }
        require(discardHandIndices.all { it in hand.indices }) { "Invalid hand index" }

        // 손패를 건드리기 전에 덱 + 버림 카드 = 정확히 30장이 되도록 재구성한다.
        ensureDrawCapacity(2)

        val player = currentPlayer
        val discardedInOrder = discardHandIndices.map { hand[it] }

        // 인덱스 밀림을 막기 위해 실제 제거는 내림차순.
        discardHandIndices.sortedDescending().forEach { hand.removeAt(it) }

        for (card in discardedInOrder) {
            discardPool += card
            top = ActiveUnoCard(card, (card as? UnoCard.NumberCard)?.number)
        }

        val drawn = listOf(deck.draw(), deck.draw())
        hand += drawn

        log(
            "${sideLabel(player)} 패널티 · ${discardedInOrder.joinToString { cardLabel(it) }} 2장 버림 · " +
                "${drawn.joinToString { cardLabel(it) }} 2장 드로우 · 손패 ${hand.size}장"
        )

        currentPlayer = currentPlayer.opposite()
        log("턴 변경 · ${sideLabel(currentPlayer)} 차례")

        return PenaltyResult(
            player = player,
            discardedCards = discardedInOrder,
            drawnCards = drawn
        )
    }

    /**
     * 필요한 카드 수보다 덱이 적으면, 두 플레이어의 5장+5장 손패를 제외한 모든 카드 30장을
     * 다시 모아 섞는다. 바닥 카드의 매칭 조건(top)은 상태 정보로 유지한다.
     */
    private fun ensureDrawCapacity(required: Int) {
        if (deck.remaining() >= required) return

        val recyclable = deck.drainAll() + discardPool.toList()
        val handCount = hands.values.sumOf { it.size }
        val expected = UnoDeck.TOTAL_CARDS - handCount
        check(recyclable.size == expected) {
            "Card invariant broken: recyclable=${recyclable.size}, expected=$expected, handCount=$handCount"
        }

        discardPool.clear()
        deck.rebuild(recyclable)
        log("덱 재구성 · 두 플레이어 손패 ${handCount}장 제외 · ${recyclable.size}장 셔플")
    }

    private fun fileFor(card: UnoCard, jokerFile: Int?): Int = when (card) {
        is UnoCard.NumberCard -> card.number
        UnoCard.Joker -> requireNotNull(jokerFile) { "Joker requires a file number (1..8)" }
            .also { require(it in 1..8) }
    }

    private fun log(message: String) {
        gameLogs += "${gameLogs.size + 1}. $message"
    }

    private fun sideLabel(side: PlayerSide): String = if (side == PlayerSide.WHITE) "PLAYER 1(WHITE)" else "PLAYER 2(BLACK)"

    private fun pieceLabel(type: PieceType): String = when (type) {
        PieceType.KING -> "KING"
        PieceType.QUEEN -> "QUEEN"
        PieceType.ROOK -> "ROOK"
        PieceType.BISHOP -> "BISHOP"
        PieceType.KNIGHT -> "KNIGHT"
        PieceType.PAWN -> "PAWN"
    }

    private fun cardLabel(card: UnoCard): String = when (card) {
        UnoCard.Joker -> "JOKER"
        is UnoCard.NumberCard -> "${card.color.name}-${card.number}"
    }
}

data class TurnResult(
    val player: PlayerSide,
    val playedCard: UnoCard,
    val drawnCard: UnoCard,
    val move: ChessMove,
    val capturedPiece: Piece?,
    val promotedToQueen: Boolean,
    val winner: PlayerSide?
)

data class PenaltyResult(
    val player: PlayerSide,
    val discardedCards: List<UnoCard>,
    val drawnCards: List<UnoCard>
)
