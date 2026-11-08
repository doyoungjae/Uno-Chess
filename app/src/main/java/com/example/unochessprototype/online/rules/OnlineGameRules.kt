package com.example.unochessprototype.online.rules

import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.ChessBoard
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.online.model.OnlineGameState
import com.example.unochessprototype.online.model.OnlineResultType
import kotlin.random.Random

object OnlineGameRules {
    const val MAX_TURNS = 200
    const val HAND_SIZE = 5
    const val TOTAL_CARD_COUNT = 40

    fun createInitial(
        ownerUid: String,
        guestUid: String,
        random: Random = Random.Default,
    ): OnlineGameState {
        require(ownerUid.isNotBlank() && guestUid.isNotBlank() && ownerUid != guestUid)

        var ownerCard: Int
        var guestCard: Int
        do {
            ownerCard = random.nextInt(1, 9)
            guestCard = random.nextInt(1, 9)
        } while (ownerCard == guestCard)

        val whiteUid = if (ownerCard > guestCard) ownerUid else guestUid
        val blackUid = if (whiteUid == ownerUid) guestUid else ownerUid
        val shuffled = (0 until TOTAL_CARD_COUNT).shuffled(random)

        return OnlineGameState(
            ownerUid = ownerUid,
            guestUid = guestUid,
            whiteUid = whiteUid,
            blackUid = blackUid,
            currentUid = whiteUid,
            ownerFirstCard = ownerCard,
            guestFirstCard = guestCard,
            completedTurns = 0,
            board = ChessBoard.standard().toCodes(),
            ownerHand = shuffled.take(HAND_SIZE),
            guestHand = shuffled.drop(HAND_SIZE).take(HAND_SIZE),
            drawPile = shuffled.drop(HAND_SIZE * 2),
            logs = listOf(
                "온라인 게임 시작",
                "선공 추첨 · HOST $ownerCard / GUEST $guestCard",
                "WHITE · ${shortUid(whiteUid)} 선공",
            ),
        )
    }

    fun card(cardId: Int): UnoCard {
        require(cardId in 0 until TOTAL_CARD_COUNT) { "Invalid card id: $cardId" }
        if (cardId >= 32) return UnoCard(id = cardId, color = null, number = null)
        val color = CardColor.entries[cardId / 8]
        val number = (cardId % 8) + 1
        return UnoCard(id = cardId, color = color, number = number)
    }

    fun boardFromState(state: OnlineGameState): ChessBoard = ChessBoard.fromCodes(state.board)

    fun sideFor(state: OnlineGameState, uid: String): Player = when (uid) {
        state.whiteUid -> Player.WHITE
        state.blackUid -> Player.BLACK
        else -> error("User is not a player in this game.")
    }

    fun playableFiles(state: OnlineGameState, uid: String, cardId: Int): Set<Int> {
        if (state.hasResult() || uid != state.currentUid) return emptySet()
        if (cardId !in state.handFor(uid)) return emptySet()

        val board = boardFromState(state)
        val movable = board.movableFiles(sideFor(state, uid))
        val selected = card(cardId)
        return if (selected.isJoker) {
            movable
        } else {
            val file = selected.number!! - 1
            if (file in movable) setOf(file) else emptySet()
        }
    }

    fun playableCards(state: OnlineGameState, uid: String): List<Int> =
        state.handFor(uid).filter { playableFiles(state, uid, it).isNotEmpty() }

    fun canExchange(state: OnlineGameState, uid: String): Boolean {
        if (state.hasResult() || uid != state.currentUid) return false
        val board = boardFromState(state)
        val side = sideFor(state, uid)
        return board.hasAnyLegalMove(side) && playableCards(state, uid).isEmpty()
    }

    fun legalMovesForCard(
        state: OnlineGameState,
        uid: String,
        cardId: Int,
        chosenFileForJoker: Int? = null,
    ): List<Move> {
        if (state.hasResult() || uid != state.currentUid) return emptyList()
        if (cardId !in state.handFor(uid)) return emptyList()

        val selected = card(cardId)
        val files = playableFiles(state, uid, cardId)
        val file = if (selected.isJoker) {
            chosenFileForJoker ?: return emptyList()
        } else {
            selected.number!! - 1
        }
        if (file !in files) return emptyList()

        val board = boardFromState(state)
        val side = sideFor(state, uid)
        return buildList {
            for (row in 0..7) {
                val from = Square(row, file)
                if (board.pieceAt(from)?.player == side) {
                    addAll(board.legalMovesFrom(from))
                }
            }
        }
    }

    fun playMove(
        state: OnlineGameState,
        actorUid: String,
        cardId: Int,
        move: Move,
        chosenFileForJoker: Int? = null,
        random: Random = Random.Default,
    ): OnlineGameState {
        require(!state.hasResult()) { "The game is already over." }
        require(actorUid == state.currentUid) { "It is not this user's turn." }

        val hand = state.handFor(actorUid)
        require(cardId in hand) { "Selected card is not in the current user's hand." }

        val selected = card(cardId)
        val allowedMoves = legalMovesForCard(state, actorUid, cardId, chosenFileForJoker)
        require(move in allowedMoves) { "Illegal card or chess move." }

        val board = boardFromState(state)
        val side = sideFor(state, actorUid)
        val outcome = board.move(side, move)

        var ownerHand = state.ownerHand.toMutableList()
        var guestHand = state.guestHand.toMutableList()
        var drawPile = state.drawPile.toMutableList()
        val activeHand = if (actorUid == state.ownerUid) ownerHand else guestHand

        val actionLogs = mutableListOf<String>()
        actionLogs += "${side.displayName()} · ${selected.displayName()} · ${move.from.algebraic()}→${move.to.algebraic()}"
        if (outcome.capturedPiece != null) {
            actionLogs += "${side.displayName()} · ${outcome.capturedPiece.type} 포획"
        }
        if (outcome.promoted) {
            actionLogs += "${side.displayName()} · PAWN → QUEEN 승급"
        }

        if (outcome.capturedPiece?.type == PieceType.KING) {
            activeHand.remove(cardId)
            return state.copy(
                board = board.toCodes(),
                ownerHand = ownerHand,
                guestHand = guestHand,
                resultType = OnlineResultType.WIN,
                winnerUid = actorUid,
                resultReason = "KING 직접 포획",
                logs = appendLogs(state.logs, actionLogs + "게임 종료 · KING 직접 포획"),
            )
        }

        if (drawPile.isEmpty()) {
            drawPile = rebuildDeck(ownerHand, guestHand, random)
            actionLogs += "드로우 덱 재구성 · 양쪽 손패 제외 30장"
        }
        val replacement = drawPile.removeAt(drawPile.lastIndex)
        activeHand += replacement
        activeHand.remove(cardId)
        check(activeHand.size == HAND_SIZE)

        return finishTurn(
            state = state.copy(
                board = board.toCodes(),
                ownerHand = ownerHand,
                guestHand = guestHand,
                drawPile = drawPile,
                logs = appendLogs(state.logs, actionLogs + "카드 1장 보충"),
            ),
            actorUid = actorUid,
        )
    }

    fun exchangeTwoCards(
        state: OnlineGameState,
        actorUid: String,
        cardIds: Set<Int>,
        random: Random = Random.Default,
    ): OnlineGameState {
        require(!state.hasResult()) { "The game is already over." }
        require(actorUid == state.currentUid) { "It is not this user's turn." }
        require(canExchange(state, actorUid)) { "Card exchange is not available now." }
        require(cardIds.size == 2) { "Exactly two cards must be selected." }

        var ownerHand = state.ownerHand.toMutableList()
        var guestHand = state.guestHand.toMutableList()
        var drawPile = state.drawPile.toMutableList()
        val activeHand = if (actorUid == state.ownerUid) ownerHand else guestHand
        require(cardIds.all { it in activeHand }) { "Exchange card is not in hand." }

        if (drawPile.size < 2) {
            drawPile = rebuildDeck(ownerHand, guestHand, random)
        }

        val drawn = listOf(
            drawPile.removeAt(drawPile.lastIndex),
            drawPile.removeAt(drawPile.lastIndex),
        )
        activeHand += drawn
        activeHand.removeAll { it in cardIds }
        check(activeHand.size == HAND_SIZE)

        val log = "${sideFor(state, actorUid).displayName()} · 카드 2장 교환"
        return finishTurn(
            state = state.copy(
                ownerHand = ownerHand,
                guestHand = guestHand,
                drawPile = drawPile,
                logs = appendLogs(state.logs, listOf(log)),
            ),
            actorUid = actorUid,
        )
    }

    fun forfeit(state: OnlineGameState, actorUid: String): OnlineGameState {
        require(!state.hasResult()) { "The game is already over." }
        require(actorUid == state.ownerUid || actorUid == state.guestUid) { "Not a room participant." }
        val winner = if (actorUid == state.ownerUid) state.guestUid else state.ownerUid
        return state.copy(
            resultType = OnlineResultType.WIN,
            winnerUid = winner,
            resultReason = "상대가 경기에서 나갔습니다.",
            logs = appendLogs(state.logs, listOf("게임 종료 · ${shortUid(actorUid)} 기권")),
        )
    }

    private fun finishTurn(state: OnlineGameState, actorUid: String): OnlineGameState {
        val nextTurns = state.completedTurns + 1
        if (nextTurns >= MAX_TURNS) {
            return state.copy(
                completedTurns = nextTurns,
                resultType = OnlineResultType.DRAW,
                resultReason = "${MAX_TURNS}턴 동안 KING이 포획되지 않았습니다.",
                logs = appendLogs(state.logs, listOf("게임 종료 · 최대 ${MAX_TURNS}턴 무승부")),
            )
        }

        val nextUid = if (actorUid == state.ownerUid) state.guestUid else state.ownerUid
        val nextSide = sideFor(state, nextUid)
        val board = boardFromState(state)
        if (!board.hasAnyLegalMove(nextSide)) {
            return state.copy(
                completedTurns = nextTurns,
                currentUid = nextUid,
                resultType = OnlineResultType.DRAW,
                resultReason = "${nextSide.displayName()}에게 이동 가능한 기물이 없습니다.",
                logs = appendLogs(state.logs, listOf("게임 종료 · 이동 가능한 기물 없음")),
            )
        }

        return state.copy(
            completedTurns = nextTurns,
            currentUid = nextUid,
            logs = appendLogs(state.logs, listOf("턴 변경 · ${nextSide.displayName()}")),
        )
    }

    private fun rebuildDeck(
        ownerHand: List<Int>,
        guestHand: List<Int>,
        random: Random,
    ): MutableList<Int> {
        val handIds = (ownerHand + guestHand).toSet()
        require(handIds.size == HAND_SIZE * 2) { "Hands must contain 10 unique cards." }
        return (0 until TOTAL_CARD_COUNT)
            .filterNot { it in handIds }
            .shuffled(random)
            .toMutableList()
    }

    private fun appendLogs(existing: List<String>, additions: List<String>): List<String> =
        (existing + additions).takeLast(40)

    private fun shortUid(uid: String): String = uid.takeLast(6).uppercase()
}
