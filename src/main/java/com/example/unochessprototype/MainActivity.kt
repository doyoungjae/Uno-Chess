package com.example.unochessprototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unochess.core.ActiveUnoCard
import com.unochess.core.ChessMove
import com.unochess.core.Piece
import com.unochess.core.PieceType
import com.unochess.core.PlayerSide
import com.unochess.core.Square
import com.unochess.core.UnoCard
import com.unochess.core.UnoChessGame

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    UnoChessPrototypeScreen()
                }
            }
        }
    }
}

@Composable
fun UnoChessPrototypeScreen() {
    var game by remember { mutableStateOf(UnoChessGame()) }
    var revision by remember { mutableIntStateOf(0) }

    var selectedHandIndex by remember { mutableStateOf<Int?>(null) }
    var penaltySelected by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedFrom by remember { mutableStateOf<Square?>(null) }
    var legalMoves by remember { mutableStateOf<List<ChessMove>>(emptyList()) }
    var jokerFile by remember { mutableStateOf<Int?>(null) }
    var showLogs by remember { mutableStateOf(false) }
    var message by remember {
        mutableStateOf("손패는 항상 5장입니다. 사용할 카드 1장을 선택해 기물을 이동하세요.")
    }

    // 순수 Kotlin 게임 객체 내부 변경을 Compose가 다시 읽도록 하기 위한 revision.
    @Suppress("UNUSED_VARIABLE") val redraw = revision

    fun touch() { revision++ }

    fun clearNormalSelection() {
        selectedHandIndex = null
        selectedFrom = null
        legalMoves = emptyList()
        jokerFile = null
    }

    fun clearAllSelection() {
        clearNormalSelection()
        penaltySelected = emptyList()
    }

    fun activeFile(): Int? {
        val idx = selectedHandIndex ?: return null
        val card = game.handOf(game.currentPlayer).getOrNull(idx) ?: return null
        return when (card) {
            is UnoCard.NumberCard -> card.number
            UnoCard.Joker -> jokerFile
        }
    }

    fun selectPiece(square: Square) {
        val idx = selectedHandIndex ?: return
        val file = activeFile()
        if (file == null) {
            message = "조커를 사용하려면 먼저 A~H 중 사용할 열을 선택하세요."
            return
        }

        val piece = game.board.get(square)
        if (piece?.side != game.currentPlayer) {
            message = "현재 플레이어의 기물을 선택하세요."
            return
        }
        if (square.fileNumber != file) {
            message = "${fileLetter(file)}열의 기물만 움직일 수 있습니다."
            return
        }

        val moves = try {
            game.legalMovesForCard(idx, jokerFile).filter { it.from == square }
        } catch (e: Exception) {
            message = e.message ?: "이동 계산 오류"
            emptyList()
        }

        selectedFrom = square
        legalMoves = moves
        message = if (moves.isEmpty()) {
            "${pieceName(piece.type)} ${square.algebraic()}는 현재 이동할 수 없습니다."
        } else {
            "${pieceName(piece.type)} ${square.algebraic()} 선택 · 초록=이동 / 빨강=포획"
        }
    }

    fun attemptMove(to: Square) {
        val idx = selectedHandIndex ?: return
        val from = selectedFrom ?: return
        val move = legalMoves.firstOrNull { it.from == from && it.to == to } ?: return

        try {
            val result = game.playCardAndMove(
                handIndex = idx,
                move = move,
                jokerFile = jokerFile
            )

            message = buildString {
                append("${sideName(result.player)}: ${cardLabel(result.playedCard)} 사용 · ")
                append("${move.from.algebraic()}→${move.to.algebraic()}")
                if (result.capturedPiece != null) append(" · ${pieceName(result.capturedPiece.type)} 포획")
                if (result.promotedToQueen) append(" · 폰→퀸 자동 승급")
                append(" · ${cardLabel(result.drawnCard)} 보충")
                if (result.winner != null) append(" · ${sideName(result.winner)} 승리!")
            }

            clearAllSelection()
            touch()
        } catch (e: Exception) {
            message = e.message ?: "이동 처리 중 오류가 발생했습니다."
        }
    }

    val playableIndices = game.playableCardIndices().toSet()
    val noPlayableCard = !game.isGameOver && playableIndices.isEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("UNO Chess · Local Prototype", fontWeight = FontWeight.Bold, fontSize = 21.sp)
                    Text(
                        "현재 턴: ${sideName(game.currentPlayer)} · 손패 ${game.handOf(game.currentPlayer).size}장 · 덱 ${game.deckRemaining()}장",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { showLogs = true }) { Text("로그") }
                    OutlinedButton(onClick = {
                        game = UnoChessGame()
                        clearAllSelection()
                        message = "새 게임입니다. ${sideName(game.currentPlayer)}부터 시작합니다."
                        touch()
                    }) { Text("새 게임") }
                }
            }

            StatusCard(
                top = game.topCard(),
                message = message,
                winner = game.winner,
                noPlayable = noPlayableCard
            )

            ChessBoardView(
                game = game,
                activeFile = activeFile(),
                selectedFrom = selectedFrom,
                legalMoves = legalMoves,
                onSquareClick = { square ->
                    if (game.isGameOver) return@ChessBoardView
                    if (noPlayableCard) {
                        message = "움직일 수 있는 카드가 없습니다. 손패에서 버릴 카드 2장을 선택하세요."
                        return@ChessBoardView
                    }
                    if (selectedHandIndex == null) {
                        message = "먼저 손패에서 사용할 카드를 선택하세요."
                        return@ChessBoardView
                    }
                    if (selectedFrom != null && legalMoves.any { it.to == square }) {
                        attemptMove(square)
                    } else {
                        selectPiece(square)
                    }
                }
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("${sideName(game.currentPlayer)} 손패", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (noPlayableCard) "이동 가능한 카드 없음 · 2장을 선택하세요 (${penaltySelected.size}/2)"
                                else "같은 색/숫자 또는 조커 · 이동 가능한 카드만 활성화",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (noPlayableCard) {
                            Button(
                                enabled = penaltySelected.size == 2 && !game.isGameOver,
                                onClick = {
                                    try {
                                        val result = game.applyNoMovePenalty(penaltySelected)
                                        message = "패널티: ${result.discardedCards.joinToString { cardLabel(it) }} 버림 · " +
                                            "${result.drawnCards.joinToString { cardLabel(it) }} 드로우 · 손패 5장 유지"
                                        clearAllSelection()
                                        touch()
                                    } catch (e: Exception) {
                                        message = e.message ?: "패널티 처리 오류"
                                    }
                                }
                            ) { Text("2장 버리고 2장 뽑기") }
                        }
                    }

                    HandView(
                        cards = game.handOf(game.currentPlayer),
                        playable = playableIndices,
                        normalSelectedIndex = selectedHandIndex,
                        penaltySelected = penaltySelected.toSet(),
                        penaltyMode = noPlayableCard,
                        onSelect = { index ->
                            if (game.isGameOver) return@HandView

                            if (noPlayableCard) {
                                penaltySelected = if (index in penaltySelected) {
                                    penaltySelected - index
                                } else if (penaltySelected.size < 2) {
                                    penaltySelected + index
                                } else {
                                    penaltySelected
                                }
                                message = "패널티로 버릴 카드 ${penaltySelected.size}/2장 선택"
                                return@HandView
                            }

                            if (index !in playableIndices) {
                                message = "이 카드는 현재 바닥 카드 조건 또는 체스 이동 조건을 만족하지 않습니다."
                                return@HandView
                            }

                            selectedHandIndex = index
                            selectedFrom = null
                            legalMoves = emptyList()
                            jokerFile = null

                            val card = game.handOf(game.currentPlayer)[index]
                            message = when (card) {
                                UnoCard.Joker -> "JOKER 선택 · 색상은 무시합니다. 움직일 열만 고르세요."
                                is UnoCard.NumberCard -> "${cardLabel(card)} 선택 · ${fileLetter(card.number)}열 활성화"
                            }
                        }
                    )
                }
            }

            val selectedCard = selectedHandIndex?.let { game.handOf(game.currentPlayer).getOrNull(it) }
            if (selectedCard == UnoCard.Joker && !noPlayableCard && !game.isGameOver) {
                JokerFileOptions(
                    legalFiles = game.legalJokerFiles(),
                    selectedFile = jokerFile,
                    onFile = {
                        jokerFile = it
                        selectedFrom = null
                        legalMoves = emptyList()
                        message = "JOKER · ${fileLetter(it)}열 선택 · 색상 조건 없음"
                    }
                )
            }

            Text(
                "규칙: 손패 5장 고정 · 정상 이동 후 1장 보충 · 이동 가능 카드가 없으면 2장 버리고 2장 보충 · 캐슬링/앙파상 없음 · 폰은 끝에서 퀸 자동 승급",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (showLogs) {
            LogDialog(
                logs = game.logs(),
                onDismiss = { showLogs = false }
            )
        }
    }
}

@Composable
private fun StatusCard(
    top: ActiveUnoCard,
    message: String,
    winner: PlayerSide?,
    noPlayable: Boolean
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("바닥 카드: ", fontWeight = FontWeight.SemiBold)
                MiniUnoCard(top.card)
                Spacer(Modifier.width(8.dp))
                Text(
                    when (top.card) {
                        UnoCard.Joker -> "JOKER · 색상 무시"
                        is UnoCard.NumberCard -> "${top.card.color.name} / 숫자 ${top.card.number}"
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(message)
            if (noPlayable && winner == null) {
                Spacer(Modifier.height(4.dp))
                Text("현재 손패로 실제 기물 이동 불가 → 카드 2장 교체", fontWeight = FontWeight.Bold)
            }
            if (winner != null) {
                Spacer(Modifier.height(4.dp))
                Text("게임 종료 · ${sideName(winner)} 승리", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChessBoardView(
    game: UnoChessGame,
    activeFile: Int?,
    selectedFrom: Square?,
    legalMoves: List<ChessMove>,
    onSquareClick: (Square) -> Unit
) {
    // 기존보다 명암 대비를 크게 해 기물과 하이라이트가 잘 보이도록 조정.
    val light = Color(0xFFF3E6C8)
    val dark = Color(0xFF4A6A73)
    val active = Color(0xFFFFC107)
    val moveColor = Color(0xFF39B86B)
    val captureColor = Color(0xFFE53935)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, Color(0xFF263238), RoundedCornerShape(4.dp))
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val square = Square(row, col)
                    val piece = game.board.get(square)
                    val targetMove = legalMoves.firstOrNull { it.to == square }
                    val selected = selectedFrom == square
                    val squareColor = if ((row + col) % 2 == 0) light else dark

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(squareColor)
                            .then(if (activeFile == col + 1) Modifier.border(3.dp, active) else Modifier)
                            .then(if (selected) Modifier.border(4.dp, Color(0xFF7C3AED)) else Modifier)
                            .clickable { onSquareClick(square) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (targetMove != null) {
                            val capture = piece != null
                            Box(
                                modifier = Modifier
                                    .size(if (capture) 36.dp else 17.dp)
                                    .then(
                                        if (capture) Modifier.border(4.dp, captureColor, CircleShape)
                                        else Modifier.background(moveColor, CircleShape)
                                    )
                            )
                        }

                        if (piece != null) {
                            PieceView(piece)
                        }

                        Text(
                            square.algebraic(),
                            modifier = Modifier.align(Alignment.BottomStart).padding(2.dp),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if ((row + col) % 2 == 0) Color(0xFF3D5A63) else Color(0xFFFFF3D6)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceView(piece: Piece) {
    val whiteSide = piece.side == PlayerSide.WHITE
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(if (whiteSide) Color(0xFF263238) else Color(0xFFF7F3E8), CircleShape)
            .border(1.dp, if (whiteSide) Color.White else Color(0xFF111827), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            pieceSymbol(piece),
            fontSize = 29.sp,
            lineHeight = 29.sp,
            color = if (whiteSide) Color.White else Color(0xFF111111),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HandView(
    cards: List<UnoCard>,
    playable: Set<Int>,
    normalSelectedIndex: Int?,
    penaltySelected: Set<Int>,
    penaltyMode: Boolean,
    onSelect: (Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        itemsIndexed(cards) { index, card ->
            val enabled = penaltyMode || index in playable
            val selected = if (penaltyMode) index in penaltySelected else normalSelectedIndex == index
            UnoCardView(
                card = card,
                selected = selected,
                enabled = enabled,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun UnoCardView(card: UnoCard, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg = unoColor(card)
    Card(
        modifier = Modifier
            .size(width = 62.dp, height = 88.dp)
            .then(if (selected) Modifier.border(4.dp, Color(0xFFFFC107), RoundedCornerShape(12.dp)) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (enabled) bg else bg.copy(alpha = 0.30f)),
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                if (card == UnoCard.Joker) "J" else (card as UnoCard.NumberCard).number.toString(),
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun MiniUnoCard(card: UnoCard) {
    Box(
        modifier = Modifier
            .size(width = 36.dp, height = 50.dp)
            .background(unoColor(card), RoundedCornerShape(7.dp))
            .border(1.dp, Color.White, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (card == UnoCard.Joker) "J" else (card as UnoCard.NumberCard).number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun JokerFileOptions(
    legalFiles: Set<Int>,
    selectedFile: Int?,
    onFile: (Int) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text("JOKER · 이동할 열 선택", fontWeight = FontWeight.Bold)
            Text("조커는 색상을 신경쓰지 않습니다. 실제 이동 가능한 기물이 있는 열만 선택할 수 있습니다.", fontSize = 12.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items((1..8).toList()) { file ->
                    OutlinedButton(
                        enabled = file in legalFiles,
                        onClick = { onFile(file) },
                        border = BorderStroke(
                            if (selectedFile == file) 3.dp else 1.dp,
                            if (selectedFile == file) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                        )
                    ) { Text(fileLetter(file)) }
                }
            }
        }
    }
}

@Composable
private fun LogDialog(logs: List<String>, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 620.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xE6232937))
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("게임 로그", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = onDismiss) { Text("닫기", color = Color.White) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        logs.asReversed().forEach { entry ->
                            Text(entry, color = Color(0xFFF3F4F6), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun pieceSymbol(piece: Piece): String = when (piece.side to piece.type) {
    PlayerSide.WHITE to PieceType.KING -> "♔"
    PlayerSide.WHITE to PieceType.QUEEN -> "♕"
    PlayerSide.WHITE to PieceType.ROOK -> "♖"
    PlayerSide.WHITE to PieceType.BISHOP -> "♗"
    PlayerSide.WHITE to PieceType.KNIGHT -> "♘"
    PlayerSide.WHITE to PieceType.PAWN -> "♙"
    PlayerSide.BLACK to PieceType.KING -> "♚"
    PlayerSide.BLACK to PieceType.QUEEN -> "♛"
    PlayerSide.BLACK to PieceType.ROOK -> "♜"
    PlayerSide.BLACK to PieceType.BISHOP -> "♝"
    PlayerSide.BLACK to PieceType.KNIGHT -> "♞"
    PlayerSide.BLACK to PieceType.PAWN -> "♟"
    else -> "기물 타입 오류"
}

private fun unoColor(card: UnoCard): Color = when (card) {
    UnoCard.Joker -> Color(0xFF202124)
    is UnoCard.NumberCard -> when (card.color) {
        com.unochess.core.UnoColor.RED -> Color(0xFFC62828)
        com.unochess.core.UnoColor.YELLOW -> Color(0xFFF9A825)
        com.unochess.core.UnoColor.GREEN -> Color(0xFF2E7D32)
        com.unochess.core.UnoColor.BLUE -> Color(0xFF1565C0)
    }
}

private fun sideName(side: PlayerSide?): String = when (side) {
    PlayerSide.WHITE -> "PLAYER 1(White)"
    PlayerSide.BLACK -> "PLAYER 2(Black)"
    null -> "-"
}

private fun cardLabel(card: UnoCard): String = when (card) {
    UnoCard.Joker -> "JOKER"
    is UnoCard.NumberCard -> "${card.color.name}-${card.number}"
}

private fun pieceName(type: PieceType): String = when (type) {
    PieceType.KING -> "KING"
    PieceType.QUEEN -> "QUEEN"
    PieceType.ROOK -> "ROOK"
    PieceType.BISHOP -> "BISHOP"
    PieceType.KNIGHT -> "KNIGHT"
    PieceType.PAWN -> "PAWN"
}

private fun fileLetter(file: Int?): String =
    if (file == null || file !in 1..8) "-" else ('A'.code + file - 1).toChar().toString()
