package com.example.unochessprototype.game

enum class Player {
    WHITE,
    BLACK;

    fun opposite(): Player = if (this == WHITE) BLACK else WHITE

    fun displayName(): String = if (this == WHITE) "PLAYER 1 (WHITE)" else "PLAYER 2 (BLACK)"
}

enum class PieceType {
    KING,
    QUEEN,
    ROOK,
    BISHOP,
    KNIGHT,
    PAWN
}

data class Piece(
    val player: Player,
    val type: PieceType,
)

data class Square(
    val row: Int,
    val col: Int,
) {
    init {
        require(row in 0..7 && col in 0..7) { "Square must be inside the chess board." }
    }

    fun algebraic(): String {
        val file = ('a'.code + col).toChar()
        val rank = 8 - row
        return "$file$rank"
    }
}

data class Move(
    val from: Square,
    val to: Square,
)

data class MoveOutcome(
    val movedPieceBeforePromotion: Piece,
    val movedPieceAfterPromotion: Piece,
    val capturedPiece: Piece?,
    val promoted: Boolean,
)

enum class CardColor(val shortName: String, val displayName: String) {
    RED("R", "RED"),
    BLUE("B", "BLUE"),
    GREEN("G", "GREEN"),
    YELLOW("Y", "YELLOW"),
}

data class UnoCard(
    val id: Int,
    val color: CardColor?,
    val number: Int?,
) {
    val isJoker: Boolean
        get() = number == null

    init {
        if (isJoker) {
            require(color == null) { "Joker must not have a color." }
        } else {
            require(color != null) { "Number card must have a color." }
            require(number in 1..8) { "Number card must be between 1 and 8." }
        }
    }

    fun displayName(): String = if (isJoker) {
        "JOKER"
    } else {
        "${color!!.displayName}-$number"
    }
}

sealed class GameResult {
    data class Win(val winner: Player) : GameResult()
    data class Draw(val reason: String) : GameResult()
}

data class PlayResult(
    val moveOutcome: MoveOutcome,
    val usedCard: UnoCard,
    val replacementCard: UnoCard?,
    val gameResult: GameResult?,
)

data class ExchangeResult(
    val discardedCards: List<UnoCard>,
    val drawnCards: List<UnoCard>,
    val gameResult: GameResult?,
)
