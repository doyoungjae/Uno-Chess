package com.unochess.core

enum class PlayerSide {
    WHITE, BLACK;
    fun opposite(): PlayerSide = if (this == WHITE) BLACK else WHITE
}

enum class PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

data class Piece(val side: PlayerSide, val type: PieceType)

data class Square(val row: Int, val col: Int) {
    init {
        require(row in 0..7 && col in 0..7) { "Square out of range: row=$row col=$col" }
    }

    /** 1..8, where 1=A-file and 8=H-file. */
    val fileNumber: Int get() = col + 1

    /** Standard chess rank 1..8. */
    val rankNumber: Int get() = 8 - row

    fun algebraic(): String = "${('a'.code + col).toChar()}$rankNumber"

    companion object {
        fun fromAlgebraic(value: String): Square {
            require(value.length == 2) { "Use algebraic square such as e2" }
            val file = value[0].lowercaseChar()
            val rank = value[1].digitToInt()
            require(file in 'a'..'h' && rank in 1..8)
            return Square(row = 8 - rank, col = file - 'a')
        }
    }
}

data class ChessMove(
    val from: Square,
    val to: Square,
    val promotion: PieceType? = null
) {
    override fun toString(): String = from.algebraic() + to.algebraic()
}
