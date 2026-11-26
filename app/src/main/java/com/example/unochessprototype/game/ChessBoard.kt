package com.example.unochessprototype.game

class ChessBoard private constructor(
    private val cells: Array<Array<Piece?>>,
) {
    companion object {
        fun standard(): ChessBoard {
            val cells = Array(8) { arrayOfNulls<Piece>(8) }
            val backRank = listOf(
                PieceType.ROOK,
                PieceType.KNIGHT,
                PieceType.BISHOP,
                PieceType.QUEEN,
                PieceType.KING,
                PieceType.BISHOP,
                PieceType.KNIGHT,
                PieceType.ROOK,
            )

            for (col in 0..7) {
                cells[0][col] = Piece(Player.BLACK, backRank[col])
                cells[1][col] = Piece(Player.BLACK, PieceType.PAWN)
                cells[6][col] = Piece(Player.WHITE, PieceType.PAWN)
                cells[7][col] = Piece(Player.WHITE, backRank[col])
            }
            return ChessBoard(cells)
        }

        fun empty(): ChessBoard = ChessBoard(Array(8) { arrayOfNulls<Piece>(8) })

        fun fromCodes(codes: List<String>): ChessBoard {
            require(codes.size == 64) { "Board encoding must contain exactly 64 cells." }
            val cells = Array(8) { arrayOfNulls<Piece>(8) }
            codes.forEachIndexed { index, code ->
                if (code.isBlank()) return@forEachIndexed
                require(code.length == 2) { "Invalid board code: $code" }
                val player = when (code[0]) {
                    'W' -> Player.WHITE
                    'B' -> Player.BLACK
                    else -> error("Invalid player code: $code")
                }
                val type = when (code[1]) {
                    'K' -> PieceType.KING
                    'Q' -> PieceType.QUEEN
                    'R' -> PieceType.ROOK
                    'B' -> PieceType.BISHOP
                    'N' -> PieceType.KNIGHT
                    'P' -> PieceType.PAWN
                    else -> error("Invalid piece code: $code")
                }
                cells[index / 8][index % 8] = Piece(player, type)
            }
            return ChessBoard(cells)
        }
    }

    fun pieceAt(square: Square): Piece? = cells[square.row][square.col]

    fun pieceCount(player: Player): Int = cells.sumOf { row -> row.count { it?.player == player } }

    fun snapshot(): List<List<Piece?>> = cells.map { row -> row.toList() }

    fun toCodes(): List<String> = buildList(64) {
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = cells[row][col]
                add(
                    if (piece == null) {
                        ""
                    } else {
                        val side = if (piece.player == Player.WHITE) 'W' else 'B'
                        val kind = when (piece.type) {
                            PieceType.KING -> 'K'
                            PieceType.QUEEN -> 'Q'
                            PieceType.ROOK -> 'R'
                            PieceType.BISHOP -> 'B'
                            PieceType.KNIGHT -> 'N'
                            PieceType.PAWN -> 'P'
                        }
                        "$side$kind"
                    }
                )
            }
        }
    }

    fun legalMovesFrom(from: Square): List<Move> {
        val piece = pieceAt(from) ?: return emptyList()
        val result = mutableListOf<Move>()

        fun addIfValid(row: Int, col: Int) {
            if (row !in 0..7 || col !in 0..7) return
            val target = cells[row][col]
            if (target == null || target.player != piece.player) {
                result += Move(from, Square(row, col))
            }
        }

        fun ray(deltaRow: Int, deltaCol: Int) {
            var row = from.row + deltaRow
            var col = from.col + deltaCol
            while (row in 0..7 && col in 0..7) {
                val target = cells[row][col]
                if (target == null) {
                    result += Move(from, Square(row, col))
                } else {
                    if (target.player != piece.player) {
                        result += Move(from, Square(row, col))
                    }
                    break
                }
                row += deltaRow
                col += deltaCol
            }
        }

        when (piece.type) {
            PieceType.ROOK -> {
                ray(1, 0)
                ray(-1, 0)
                ray(0, 1)
                ray(0, -1)
            }
            PieceType.BISHOP -> {
                ray(1, 1)
                ray(1, -1)
                ray(-1, 1)
                ray(-1, -1)
            }
            PieceType.QUEEN -> {
                ray(1, 0)
                ray(-1, 0)
                ray(0, 1)
                ray(0, -1)
                ray(1, 1)
                ray(1, -1)
                ray(-1, 1)
                ray(-1, -1)
            }
            PieceType.KNIGHT -> {
                listOf(
                    2 to 1,
                    2 to -1,
                    -2 to 1,
                    -2 to -1,
                    1 to 2,
                    1 to -2,
                    -1 to 2,
                    -1 to -2,
                ).forEach { (dr, dc) -> addIfValid(from.row + dr, from.col + dc) }
            }
            PieceType.KING -> {
                for (dr in -1..1) {
                    for (dc in -1..1) {
                        if (dr != 0 || dc != 0) addIfValid(from.row + dr, from.col + dc)
                    }
                }
            }
            PieceType.PAWN -> {
                val direction = if (piece.player == Player.WHITE) -1 else 1
                val startRow = if (piece.player == Player.WHITE) 6 else 1
                val oneStepRow = from.row + direction

                if (oneStepRow in 0..7 && cells[oneStepRow][from.col] == null) {
                    result += Move(from, Square(oneStepRow, from.col))
                    val twoStepRow = from.row + 2 * direction
                    if (
                        from.row == startRow &&
                        twoStepRow in 0..7 &&
                        cells[twoStepRow][from.col] == null
                    ) {
                        result += Move(from, Square(twoStepRow, from.col))
                    }
                }

                for (dc in listOf(-1, 1)) {
                    val captureCol = from.col + dc
                    if (oneStepRow !in 0..7 || captureCol !in 0..7) continue
                    val target = cells[oneStepRow][captureCol]
                    if (target != null && target.player != piece.player) {
                        result += Move(from, Square(oneStepRow, captureCol))
                    }
                }
            }
        }

        return result
    }

    fun allLegalMoves(player: Player): List<Move> {
        val moves = mutableListOf<Move>()
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = cells[row][col]
                if (piece?.player == player) {
                    moves += legalMovesFrom(Square(row, col))
                }
            }
        }
        return moves
    }

    /** 상대 기물이 현재 플레이어의 KING을 공격하고 있는지 확인한다. */
    fun isKingInCheck(player: Player): Boolean {
        var kingSquare: Square? = null
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = cells[row][col]
                if (piece?.player == player && piece.type == PieceType.KING) {
                    kingSquare = Square(row, col)
                    break
                }
            }
            if (kingSquare != null) break
        }

        val target = kingSquare ?: return false
        for (row in 0..7) {
            for (col in 0..7) {
                val piece = cells[row][col]
                if (piece?.player == player.opposite()) {
                    if (legalMovesFrom(Square(row, col)).any { it.to == target }) return true
                }
            }
        }
        return false
    }

    fun hasAnyLegalMove(player: Player): Boolean = allLegalMoves(player).isNotEmpty()

    fun hasLegalMoveFromFile(player: Player, file: Int): Boolean {
        if (file !in 0..7) return false
        for (row in 0..7) {
            val piece = cells[row][file]
            if (piece?.player == player && legalMovesFrom(Square(row, file)).isNotEmpty()) return true
        }
        return false
    }

    fun movableFiles(player: Player): Set<Int> = buildSet {
        for (file in 0..7) {
            if (hasLegalMoveFromFile(player, file)) add(file)
        }
    }

    fun move(player: Player, move: Move): MoveOutcome {
        val piece = pieceAt(move.from) ?: error("No piece at ${move.from.algebraic()}.")
        require(piece.player == player) { "That piece does not belong to the current player." }
        require(move in legalMovesFrom(move.from)) { "Illegal chess move: $move" }

        val captured = pieceAt(move.to)
        cells[move.from.row][move.from.col] = null

        val promoted = piece.type == PieceType.PAWN &&
            ((piece.player == Player.WHITE && move.to.row == 0) ||
                (piece.player == Player.BLACK && move.to.row == 7))

        val finalPiece = if (promoted) Piece(piece.player, PieceType.QUEEN) else piece
        cells[move.to.row][move.to.col] = finalPiece

        return MoveOutcome(
            movedPieceBeforePromotion = piece,
            movedPieceAfterPromotion = finalPiece,
            capturedPiece = captured,
            promoted = promoted,
        )
    }

    internal fun clearForTest() {
        for (row in 0..7) for (col in 0..7) cells[row][col] = null
    }

    internal fun placeForTest(square: Square, piece: Piece) {
        cells[square.row][square.col] = piece
    }
}
