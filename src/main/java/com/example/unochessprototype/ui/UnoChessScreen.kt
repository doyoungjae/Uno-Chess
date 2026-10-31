package com.example.unochessprototype.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.game.UnoChessGame

private val LightSquare = Color(0xFFF1D9B5)
private val DarkSquare = Color(0xFF4F6B66)
private val ActiveFile = Color(0xFFFFC107)
private val LegalMove = Color(0xFF2E7D32)
private val CaptureMove = Color(0xFFC62828)
private val SelectedPiece = Color(0xFF1565C0)

@Composable
fun UnoChessScreen() {
    val game = remember { UnoChessGame() }
    var revision by remember { mutableIntStateOf(0) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var activeFile by remember { mutableStateOf<Int?>(null) }
    var selectedFrom by remember { mutableStateOf<Square?>(null) }
    var selectedExchangeIds by remember { mutableStateOf(setOf<Int>()) }
    var showLogs by remember { mutableStateOf(false) }

    @Suppress("UNUSED_VARIABLE")
    val forceRecompose = revision

    fun clearSelection() {
        selectedCardId = null
        activeFile = null
        selectedFrom = null
        selectedExchangeIds = emptySet()
    }

    fun refresh() {
        revision += 1
    }

    val currentHand = game.hand()
    val currentResult = game.result
    val canExchange = game.canExchangeTwoCards()
    val selectedCard = currentHand.firstOrNull { it.id == selectedCardId }
    val legalMoves = if (selectedCardId != null && activeFile != null && selectedFrom != null) {
        game.legalMovesForCard(selectedCardId!!, activeFile)
            .filter { it.from == selectedFrom }
    } else {
        emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (currentResult) {
                        is GameResult.Win -> "${currentResult.winner.displayName()} 승리"
                        is GameResult.Draw -> "무승부"
                        null -> "${game.currentPlayer.displayName()} 차례"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "완료 턴 ${game.completedTurns}/${UnoChessGame.MAX_TURNS} · 덱 ${game.drawPileCount()}장 · 손패 ${currentHand.size}장",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            OutlinedButton(onClick = { showLogs = true }) {
                Text("로그")
            }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(onClick = {
                game.reset()
                clearSelection()
                refresh()
            }) {
                Text("새 게임")
            }
        }

        GameStatusCard(
            game = game,
            canExchange = canExchange,
            selectedCard = selectedCard,
            activeFile = activeFile,
            result = currentResult,
        )

        ChessBoardView(
            game = game,
            activeFile = activeFile,
            selectedFrom = selectedFrom,
            legalMoves = legalMoves,
            enabled = currentResult == null && selectedCardId != null && activeFile != null,
            onSquareClick = { square ->
                if (currentResult != null || selectedCardId == null || activeFile == null) return@ChessBoardView

                val destination = legalMoves.firstOrNull { it.to == square }
                if (destination != null && selectedFrom != null) {
                    game.playCardAndMove(
                        cardId = selectedCardId!!,
                        move = destination,
                        chosenFileForJoker = if (selectedCard?.isJoker == true) activeFile else null,
                    )
                    clearSelection()
                    refresh()
                    return@ChessBoardView
                }

                val piece = game.board.pieceAt(square)
                if (
                    square.col == activeFile &&
                    piece?.player == game.currentPlayer &&
                    game.board.legalMovesFrom(square).isNotEmpty()
                ) {
                    selectedFrom = square
                }
            },
        )

        if (selectedCard?.isJoker == true && activeFile == null && currentResult == null) {
            JokerFilePicker(
                files = game.playableFiles(selectedCard),
                onSelect = { file ->
                    activeFile = file
                    selectedFrom = null
                },
            )
        }

        Text(
            text = if (canExchange) {
                "이동 가능한 카드가 없습니다. 버릴 카드 2장을 선택하세요."
            } else {
                "손패 5장 · 카드 색상은 게임 규칙에 영향이 없고 숫자만 A~H 열을 결정합니다."
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (canExchange) FontWeight.Bold else FontWeight.Normal,
        )

        CardHand(
            cards = currentHand,
            game = game,
            selectedCardId = selectedCardId,
            selectedExchangeIds = selectedExchangeIds,
            exchangeMode = canExchange,
            gameOver = currentResult != null,
            onCardClick = { card ->
                if (currentResult != null) return@CardHand

                if (canExchange) {
                    selectedExchangeIds = if (card.id in selectedExchangeIds) {
                        selectedExchangeIds - card.id
                    } else if (selectedExchangeIds.size < 2) {
                        selectedExchangeIds + card.id
                    } else {
                        selectedExchangeIds
                    }
                    return@CardHand
                }

                if (game.playableFiles(card).isEmpty()) return@CardHand
                selectedCardId = card.id
                selectedFrom = null
                activeFile = if (card.isJoker) null else card.number!! - 1
            },
        )

        if (canExchange && currentResult == null) {
            Button(
                onClick = {
                    game.exchangeTwoCards(selectedExchangeIds)
                    clearSelection()
                    refresh()
                },
                enabled = selectedExchangeIds.size == 2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("선택한 2장 버리고 2장 뽑기")
            }
        }
    }

    if (showLogs) {
        LogOverlay(
            logs = game.logs,
            onClose = { showLogs = false },
        )
    }
}

@Composable
private fun GameStatusCard(
    game: UnoChessGame,
    canExchange: Boolean,
    selectedCard: UnoCard?,
    activeFile: Int?,
    result: GameResult?,
) {
    val text = when (result) {
        is GameResult.Win -> "상대 KING을 직접 포획하여 게임이 즉시 종료되었습니다."
        is GameResult.Draw -> result.reason
        null -> when {
            canExchange -> "현재 손패로 실제 이동 가능한 열이 없습니다. 2장을 교환하면 턴이 종료됩니다."
            selectedCard == null -> "사용할 카드를 선택하세요. 숫자 1~8은 각각 A~H 열에 대응합니다."
            selectedCard.isJoker && activeFile == null -> "JOKER 선택: 움직일 열을 자유롭게 고르세요."
            activeFile != null -> "${('A'.code + activeFile).toChar()}열 활성화: 해당 열의 내 기물을 선택하세요."
            else -> "카드를 선택하세요."
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ChessBoardView(
    game: UnoChessGame,
    activeFile: Int?,
    selectedFrom: Square?,
    legalMoves: List<Move>,
    enabled: Boolean,
    onSquareClick: (Square) -> Unit,
) {
    val board = game.board.snapshot()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, Color(0xFF263238), RoundedCornerShape(4.dp)),
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val square = Square(row, col)
                    val piece = board[row][col]
                    val destination = legalMoves.firstOrNull { it.to == square }
                    val isCapture = destination != null && piece != null
                    val isLegal = destination != null
                    val isSelected = selectedFrom == square
                    val isActiveFile = activeFile == col

                    val base = if ((row + col) % 2 == 0) LightSquare else DarkSquare

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(base)
                            .then(
                                if (isActiveFile) Modifier.border(2.dp, ActiveFile)
                                else Modifier
                            )
                            .then(
                                if (isSelected) Modifier.border(3.dp, SelectedPiece)
                                else Modifier
                            )
                            .clickable(enabled = enabled) { onSquareClick(square) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLegal && !isCapture) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(LegalMove.copy(alpha = 0.9f), CircleShape)
                            )
                        }

                        if (isCapture) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .border(4.dp, CaptureMove, CircleShape)
                            )
                        }

                        if (piece != null) {
                            PieceView(piece)
                        }

                        if (col == 0) {
                            Text(
                                text = (8 - row).toString(),
                                fontSize = 9.sp,
                                color = if ((row + col) % 2 == 0) DarkSquare else LightSquare,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(2.dp),
                            )
                        }
                        if (row == 7) {
                            Text(
                                text = ('A'.code + col).toChar().toString(),
                                fontSize = 9.sp,
                                color = if ((row + col) % 2 == 0) DarkSquare else LightSquare,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceView(piece: Piece) {
    val symbol = when (piece.player to piece.type) {
        Player.WHITE to PieceType.KING -> "♔"
        Player.WHITE to PieceType.QUEEN -> "♕"
        Player.WHITE to PieceType.ROOK -> "♖"
        Player.WHITE to PieceType.BISHOP -> "♗"
        Player.WHITE to PieceType.KNIGHT -> "♘"
        Player.WHITE to PieceType.PAWN -> "♙"
        Player.BLACK to PieceType.KING -> "♚"
        Player.BLACK to PieceType.QUEEN -> "♛"
        Player.BLACK to PieceType.ROOK -> "♜"
        Player.BLACK to PieceType.BISHOP -> "♝"
        Player.BLACK to PieceType.KNIGHT -> "♞"
        Player.BLACK to PieceType.PAWN -> "♟"
        else -> "기물 오류"
    }

    val background = if (piece.player == Player.WHITE) Color(0xFF263238) else Color(0xFFF5F5F5)
    val foreground = if (piece.player == Player.WHITE) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxSize(0.78f)
            .background(background.copy(alpha = 0.92f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            fontSize = 30.sp,
            color = foreground,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CardHand(
    cards: List<UnoCard>,
    game: UnoChessGame,
    selectedCardId: Int?,
    selectedExchangeIds: Set<Int>,
    exchangeMode: Boolean,
    gameOver: Boolean,
    onCardClick: (UnoCard) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        cards.forEach { card ->
            val playable = game.playableFiles(card).isNotEmpty()
            val selected = card.id == selectedCardId || card.id in selectedExchangeIds
            val enabled = !gameOver && (exchangeMode || playable)

            val color = when (card.color) {
                CardColor.RED -> Color(0xFFC63D3D)
                CardColor.BLUE -> Color(0xFF3567B7)
                CardColor.GREEN -> Color(0xFF3F8A55)
                CardColor.YELLOW -> Color(0xFFD09B21)
                null -> Color(0xFF4527A0)
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .then(
                        if (selected) Modifier.border(3.dp, ActiveFile, RoundedCornerShape(10.dp))
                        else Modifier
                    )
                    .clickable(enabled = enabled) { onCardClick(card) },
                colors = CardDefaults.cardColors(
                    containerColor = if (enabled) color else color.copy(alpha = 0.32f)
                ),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (card.isJoker) "JOKER" else card.number.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (card.isJoker) 13.sp else 24.sp,
                        )
                        if (!card.isJoker) {
                            Text(
                                text = card.color!!.shortName,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JokerFilePicker(
    files: Set<Int>,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("JOKER: 움직일 열 선택", fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (file in 0..7) {
                OutlinedButton(
                    onClick = { onSelect(file) },
                    enabled = file in files,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(('A'.code + file).toChar().toString())
                }
            }
        }
    }
}

@Composable
private fun LogOverlay(
    logs: List<String>,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(0.86f),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("게임 로그", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        OutlinedButton(onClick = onClose) { Text("닫기") }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(logs.asReversed()) { entry ->
                            Text(entry, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
