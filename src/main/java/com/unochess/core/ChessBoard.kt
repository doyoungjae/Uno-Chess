package com.unochess.core

import kotlin.math.abs
import kotlin.math.sign

/**
 * UNO Chess용 체스 보드.
 *
 * 프로젝트 규칙상 체크/체크메이트가 없고 상대 KING을 직접 잡으면 승리하므로,
 * 표준 체스의 '킹을 체크 상태에 둘 수 없음' 검사는 의도적으로 적용하지 않는다.
 * 기물의 기본 이동 방식과 경로 차단 규칙은 체스 규칙을 따른다.
 *
 * V1: 캐슬링/앙파상은 제외. 폰 승급은 마지막 랭크 도달 시 기본 QUEEN 승급.
 */
class ChessBoard private constructor(
    private val cells: Array<Array<Piece?>>
) {
    constructor() : this(Array(8) { arrayOfNulls<Piece>(8) }) {
        setupStandardPosition()
    }

    fun copy(): ChessBoard {
        val copied = Array(8) { r -> Array<Piece?>(8) { c -> cells[r][c] } }
        return ChessBoard(copied)
    }

    fun get(square: Square): Piece? = cells[square.row][square.col]

    fun allPieces(): List<Pair<Square, Piece>> = buildList {
        for (r in 0..7) for (c in 0..7) {
            val p = cells[r][c] ?: continue
            add(Square(r, c) to p)
        }
    }

    fun legalMovesFor(side: PlayerSide): List<ChessMove> = buildList {
        for ((square, piece) in allPieces()) {
            if (piece.side == side) addAll(legalMovesFrom(square))
        }
    }

    fun legalMovesFrom(from: Square): List<ChessMove> {
        val piece = get(from) ?: return emptyList()
        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(from, piece)
            PieceType.KNIGHT -> jumpMoves(from, piece, KNIGHT_OFFSETS)
            PieceType.KING -> jumpMoves(from, piece, KING_OFFSETS)
            PieceType.ROOK -> slidingMoves(from, piece, ROOK_DIRECTIONS)
            PieceType.BISHOP -> slidingMoves(from, piece, BISHOP_DIRECTIONS)
            PieceType.QUEEN -> slidingMoves(from, piece, QUEEN_DIRECTIONS)
        }
    }

    /**
     * 이동을 적용하고 잡힌 기물을 반환한다.
     */
    fun move(move: ChessMove): Piece? {
        val piece = get(move.from) ?: error("No piece at ${move.from.algebraic()}")
        require(legalMovesFrom(move.from).any { it.from == move.from && it.to == move.to }) {
            "Illegal chess move: $move"
        }

        val captured = get(move.to)
        cells[move.to.row][move.to.col] = piece
        cells[move.from.row][move.from.col] = null

        if (piece.type == PieceType.PAWN && (move.to.row == 0 || move.to.row == 7)) {
            val promotion = move.promotion
                ?.takeIf { it in setOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT) }
                ?: PieceType.QUEEN
            cells[move.to.row][move.to.col] = Piece(piece.side, promotion)
        }
        return captured
    }

    /** Test/debug helper. */
    fun clear() {
        for (r in 0..7) for (c in 0..7) cells[r][c] = null
    }

    /** Test/debug helper. */
    fun put(square: Square, piece: Piece?) {
        cells[square.row][square.col] = piece
    }

    private fun setupStandardPosition() {
        clear()
        val back = listOf(
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        )
        for (c in 0..7) {
            cells[0][c] = Piece(PlayerSide.BLACK, back[c])
            cells[1][c] = Piece(PlayerSide.BLACK, PieceType.PAWN)
            cells[6][c] = Piece(PlayerSide.WHITE, PieceType.PAWN)
            cells[7][c] = Piece(PlayerSide.WHITE, back[c])
        }
    }

    private fun pawnMoves(from: Square, piece: Piece): List<ChessMove> = buildList {
        val dir = if (piece.side == PlayerSide.WHITE) -1 else 1
        val startRow = if (piece.side == PlayerSide.WHITE) 6 else 1

        val oneRow = from.row + dir
        if (oneRow in 0..7) {
            val one = Square(oneRow, from.col)
            if (get(one) == null) {
                add(ChessMove(from, one))
                val twoRow = from.row + 2 * dir
                if (from.row == startRow && twoRow in 0..7) {
                    val two = Square(twoRow, from.col)
                    if (get(two) == null) add(ChessMove(from, two))
                }
            }

            for (dc in listOf(-1, 1)) {
                val c = from.col + dc
                if (c !in 0..7) continue
                val target = Square(oneRow, c)
                val targetPiece = get(target)
                if (targetPiece != null && targetPiece.side != piece.side) {
                    add(ChessMove(from, target))
                }
            }
        }
    }

    private fun jumpMoves(
        from: Square,
        piece: Piece,
        offsets: List<Pair<Int, Int>>
    ): List<ChessMove> = buildList {
        for ((dr, dc) in offsets) {
            val r = from.row + dr
            val c = from.col + dc
            if (r !in 0..7 || c !in 0..7) continue
            val to = Square(r, c)
            val target = get(to)
            if (target == null || target.side != piece.side) add(ChessMove(from, to))
        }
    }

    private fun slidingMoves(
        from: Square,
        piece: Piece,
        directions: List<Pair<Int, Int>>
    ): List<ChessMove> = buildList {
        for ((dr, dc) in directions) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..7 && c in 0..7) {
                val to = Square(r, c)
                val target = get(to)
                if (target == null) {
                    add(ChessMove(from, to))
                } else {
                    if (target.side != piece.side) add(ChessMove(from, to))
                    break
                }
                r += dr
                c += dc
            }
        }
    }

    fun ascii(): String {
        val sb = StringBuilder()
        for (r in 0..7) {
            sb.append(8 - r).append(' ')
            for (c in 0..7) {
                val p = cells[r][c]
                sb.append(symbolOf(p)).append(' ')
            }
            sb.appendLine()
        }
        sb.appendLine("  a b c d e f g h")
        return sb.toString()
    }

    private fun symbolOf(piece: Piece?): Char {
        if (piece == null) return '.'
        val base = when (piece.type) {
            PieceType.KING -> 'k'
            PieceType.QUEEN -> 'q'
            PieceType.ROOK -> 'r'
            PieceType.BISHOP -> 'b'
            PieceType.KNIGHT -> 'n'
            PieceType.PAWN -> 'p'
        }
        return if (piece.side == PlayerSide.WHITE) base.uppercaseChar() else base
    }

    companion object {
        private val ROOK_DIRECTIONS = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
        private val BISHOP_DIRECTIONS = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1)
        private val QUEEN_DIRECTIONS = ROOK_DIRECTIONS + BISHOP_DIRECTIONS
        private val KNIGHT_OFFSETS = listOf(
            -2 to -1, -2 to 1, -1 to -2, -1 to 2,
            1 to -2, 1 to 2, 2 to -1, 2 to 1
        )
        private val KING_OFFSETS = buildList {
            for (dr in -1..1) for (dc in -1..1) {
                if (dr != 0 || dc != 0) add(dr to dc)
            }
        }
    }
}
