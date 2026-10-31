package com.example.unochessprototype.ui

import androidx.compose.material3.Surface
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.unochessprototype.data.LocalProfile
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.game.UnoChessGame

private val ForestLight = Color(0xFFC7D9A7)
private val ForestDark = Color(0xFF41634A)
private val ClassicLight = Color(0xFFF0D9B5)
private val ClassicDark = Color(0xFF8B5A3C)
private val BoardBorder = Color(0xFF26382D)
private val ActiveFileColor = Color(0xFFF2D24B)
private val SelectedColor = Color(0xFF8BE36B)
private val LegalColor = Color(0xFF38B86A)
private val CaptureColor = Color(0xFFE35B5B)
private val Panel = Color(0xFF102219)
private val Panel2 = Color(0xFF173126)
private val Lime = Color(0xFFA8E85A)
private val Muted = Color(0xFFAAB8AE)

@Composable
fun UnoChessGameScreen(
    profile: LocalProfile,
    opponentName: String,
    settings: LocalSettings,
    onExit: () -> Unit,
    onGameFinished: (GameResult) -> Unit,
) {
    val game = remember { UnoChessGame() }
    var revision by remember { mutableStateOf(0) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var activeFile by remember { mutableStateOf<Int?>(null) }
    var selectedFrom by remember { mutableStateOf<Square?>(null) }
    var exchangeSelection by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showLogs by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showTurnCover by remember { mutableStateOf(false) }

    fun refresh() { revision += 1 }
    fun clearSelection() {
        selectedCardId = null
        activeFile = null
        selectedFrom = null
        exchangeSelection = emptySet()
    }

    val hand = remember(revision) { game.hand() }
    val result = remember(revision) { game.result }
    val canExchange = remember(revision) { game.canExchangeTwoCards() }
    val selectedCard = hand.firstOrNull { it.id == selectedCardId }
    val currentName = if (game.currentPlayer == Player.WHITE) profile.nickname else opponentName
    val opponentCurrentName = if (game.currentPlayer == Player.WHITE) opponentName else profile.nickname
    val currentAvatar = if (game.currentPlayer == Player.WHITE) profile.avatar else "♜"
    val opponentAvatar = if (game.currentPlayer == Player.WHITE) "♜" else profile.avatar

    val legalMoves = if (selectedCardId != null && activeFile != null && selectedFrom != null) {
        game.legalMovesForCard(selectedCardId!!, if (selectedCard?.isJoker == true) activeFile else null)
            .filter { it.from == selectedFrom }
    } else emptyList()

    LaunchedEffect(result) {
        if (result != null) onGameFinished(result)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF08140D))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            GameTopBar(
                turn = game.completedTurns,
                drawCount = game.drawPileCount(),
                onLogs = { showLogs = true },
                onExit = { showExitDialog = true },
            )

            OpponentPanel(
                name = opponentCurrentName,
                avatar = opponentAvatar,
                isWhite = game.currentPlayer != Player.WHITE,
            )

            GameInstruction(
                result = result,
                canExchange = canExchange,
                selectedCard = selectedCard,
                activeFile = activeFile,
            )

            ChessBoard(
                game = game,
                activeFile = activeFile,
                selectedFrom = selectedFrom,
                legalMoves = legalMoves,
                showHints = settings.moveHints,
                showCoordinates = settings.showCoordinates,
                boardTheme = settings.boardTheme,
                revision = revision,
                enabled = result == null && selectedCardId != null && activeFile != null && !showTurnCover,
                onSquareClick = { square ->
                    if (result != null || selectedCardId == null || activeFile == null) return@ChessBoard

                    val destination = legalMoves.firstOrNull { it.to == square }
                    if (destination != null && selectedFrom != null) {
                        game.playCardAndMove(
                            cardId = selectedCardId!!,
                            move = destination,
                            chosenFileForJoker = if (selectedCard?.isJoker == true) activeFile else null,
                        )
                        clearSelection()
                        refresh()
                        if (game.result == null) showTurnCover = true
                        return@ChessBoard
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

            if (selectedCard?.isJoker == true && activeFile == null && result == null) {
                JokerFilePicker(
                    files = game.playableFiles(selectedCard),
                    onSelect = {
                        activeFile = it
                        selectedFrom = null
                    },
                )
            }

            CurrentPlayerPanel(
                name = currentName,
                avatar = currentAvatar,
                player = game.currentPlayer,
                cards = hand,
                game = game,
                canExchange = canExchange,
                selectedCardId = selectedCardId,
                exchangeSelection = exchangeSelection,
                enabled = result == null && !showTurnCover,
                onCard = { card ->
                    if (result != null || showTurnCover) return@CurrentPlayerPanel
                    if (canExchange) {
                        exchangeSelection = if (card.id in exchangeSelection) {
                            exchangeSelection - card.id
                        } else if (exchangeSelection.size < 2) {
                            exchangeSelection + card.id
                        } else exchangeSelection
                    } else if (game.playableFiles(card).isNotEmpty()) {
                        selectedCardId = card.id
                        selectedFrom = null
                        activeFile = if (card.isJoker) null else card.number!! - 1
                    }
                },
            )

            if (canExchange && result == null) {
                Button(
                    onClick = {
                        game.exchangeTwoCards(exchangeSelection)
                        clearSelection()
                        refresh()
                        if (game.result == null) showTurnCover = true
                    },
                    enabled = exchangeSelection.size == 2 && !showTurnCover,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Lime),
                ) {
                    Text("선택한 2장 버리고 2장 뽑기", color = Color(0xFF0B160D), fontWeight = FontWeight.Black)
                }
            }
        }

        if (showTurnCover && result == null) {
            TurnHandoffOverlay(
                playerName = if (game.currentPlayer == Player.WHITE) profile.nickname else opponentName,
                player = game.currentPlayer,
                onReady = { showTurnCover = false },
            )
        }

        if (showLogs) {
            LogOverlay(logs = game.logs, onClose = { showLogs = false })
        }

        if (result != null) {
            ResultOverlay(
                result = result,
                whiteName = profile.nickname,
                blackName = opponentName,
                onRematch = {
                    game.reset()
                    clearSelection()
                    showTurnCover = false
                    refresh()
                },
                onHome = onExit,
            )
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("게임에서 나갈까요?") },
            text = { Text("현재 로컬 게임 진행 상태는 저장되지 않습니다.") },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onExit() }) { Text("나가기") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("계속하기") }
            },
        )
    }
}

@Composable
private fun GameTopBar(turn: Int, drawCount: Int, onLogs: () -> Unit, onExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("♞ UNO CHESS", color = Lime, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("TURN $turn · DECK $drawCount", color = Muted, fontSize = 11.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            OutlinedButton(onClick = onLogs) { Text("로그", fontSize = 12.sp) }
            OutlinedButton(onClick = onExit) { Text("나가기", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun OpponentPanel(name: String, avatar: String, isWhite: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerBadge(avatar, name, if (isWhite) "WHITE" else "BLACK", Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(5) { CardBack() }
            }
        }
    }
}

@Composable
private fun CurrentPlayerPanel(
    name: String,
    avatar: String,
    player: Player,
    cards: List<UnoCard>,
    game: UnoChessGame,
    canExchange: Boolean,
    selectedCardId: Int?,
    exchangeSelection: Set<Int>,
    enabled: Boolean,
    onCard: (UnoCard) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel)) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PlayerBadge(avatar, name, if (player == Player.WHITE) "WHITE · 현재 차례" else "BLACK · 현재 차례")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                cards.forEach { card ->
                    val playable = canExchange || game.playableFiles(card).isNotEmpty()
                    UnoCardView(
                        card = card,
                        selected = card.id == selectedCardId || card.id in exchangeSelection,
                        enabled = enabled && playable,
                        modifier = Modifier.weight(1f),
                        onClick = { onCard(card) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBadge(avatar: String, name: String, subtitle: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(38.dp).background(Panel2, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(avatar, fontSize = 22.sp) }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(name, fontWeight = FontWeight.Black, fontSize = 13.sp)
            Text(subtitle, color = Muted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .width(27.dp)
            .height(40.dp)
            .background(Color(0xFF0D2C1C), RoundedCornerShape(5.dp))
            .border(1.dp, Lime.copy(alpha = 0.5f), RoundedCornerShape(5.dp)),
        contentAlignment = Alignment.Center,
    ) { Text("U", color = Lime, fontWeight = FontWeight.Black, fontSize = 12.sp) }
}

@Composable
private fun GameInstruction(
    result: GameResult?,
    canExchange: Boolean,
    selectedCard: UnoCard?,
    activeFile: Int?,
) {
    val text = when (result) {
        is GameResult.Win -> "KING 포획 · 게임 종료"
        is GameResult.Draw -> "무승부 · ${result.reason}"
        null -> when {
            canExchange -> "움직일 수 있는 숫자가 없습니다. 카드 2장을 선택해 교환하세요."
            selectedCard == null -> "카드를 선택하세요 · 1~8은 A~H 열에 대응합니다."
            selectedCard.isJoker && activeFile == null -> "JOKER · 움직일 수 있는 열을 선택하세요."
            activeFile != null -> "${('A'.code + activeFile).toChar()}열 활성화 · 기물을 선택하세요."
            else -> "카드를 선택하세요."
        }
    }
    Surface(color = Panel2, shape = RoundedCornerShape(10.dp)) {
        Text(text, modifier = Modifier.fillMaxWidth().padding(8.dp), fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ChessBoard(
    game: UnoChessGame,
    activeFile: Int?,
    selectedFrom: Square?,
    legalMoves: List<Move>,
    showHints: Boolean,
    showCoordinates: Boolean,
    boardTheme: Int,
    revision: Int,
    enabled: Boolean,
    onSquareClick: (Square) -> Unit,
) {
    val board = remember(revision) { game.board.snapshot() }
    val light = if (boardTheme == 0) ForestLight else ClassicLight
    val dark = if (boardTheme == 0) ForestDark else ClassicDark

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, BoardBorder, RoundedCornerShape(5.dp)),
    ) {
        for (row in 0..7) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0..7) {
                    val square = Square(row, col)
                    val piece = board[row][col]
                    val destination = legalMoves.firstOrNull { it.to == square }
                    val isCapture = destination != null && piece != null
                    val isMove = destination != null
                    val base = if ((row + col) % 2 == 0) light else dark

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .background(base)
                            .then(if (activeFile == col) Modifier.border(2.dp, ActiveFileColor) else Modifier)
                            .then(if (selectedFrom == square) Modifier.border(3.dp, SelectedColor) else Modifier)
                            .clickable(enabled = enabled) { onSquareClick(square) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (showHints && isMove && !isCapture) {
                            Box(Modifier.size(14.dp).background(LegalColor.copy(alpha = 0.88f), CircleShape))
                        }
                        if (showHints && isCapture) {
                            Box(Modifier.fillMaxSize().padding(3.dp).border(3.dp, CaptureColor, CircleShape))
                        }
                        if (piece != null) PieceView(piece)

                        if (showCoordinates) {
                            if (row == 7) {
                                Text(
                                    ('a'.code + col).toChar().toString(),
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                                    fontSize = 8.sp,
                                    color = if ((row + col) % 2 == 0) dark else light,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            if (col == 0) {
                                Text(
                                    (8 - row).toString(),
                                    modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                                    fontSize = 8.sp,
                                    color = if ((row + col) % 2 == 0) dark else light,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceView(piece: Piece) {
    val symbol = when (piece.type) {
        PieceType.KING -> if (piece.player == Player.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.player == Player.WHITE) "♙" else "♟"
    }
    Box(
        modifier = Modifier
            .fillMaxSize(0.78f)
            .background(if (piece.player == Player.WHITE) Color(0xFFF5F2E8) else Color(0xFF18211B), CircleShape)
            .border(1.dp, if (piece.player == Player.WHITE) Color(0xFF555B55) else Color(0xFFCAD3CB), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 28.sp, color = if (piece.player == Player.WHITE) Color(0xFF1C251F) else Color.White)
    }
}

@Composable
private fun UnoCardView(
    card: UnoCard,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val color = when (card.color) {
        CardColor.RED -> Color(0xFFC84545)
        CardColor.BLUE -> Color(0xFF3870C6)
        CardColor.GREEN -> Color(0xFF3B925B)
        CardColor.YELLOW -> Color(0xFFD5A834)
        null -> Color(0xFF2B2B35)
    }
    Box(
        modifier = modifier
            .height(72.dp)
            .background(color.copy(alpha = if (enabled) 1f else 0.38f), RoundedCornerShape(9.dp))
            .border(if (selected) 3.dp else 1.dp, if (selected) Lime else Color.White.copy(alpha = 0.55f), RoundedCornerShape(9.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (card.isJoker) "★" else card.number.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(if (card.isJoker) "JOKER" else card.color!!.shortName, fontSize = 9.sp, color = Color.White)
        }
    }
}

@Composable
private fun JokerFilePicker(files: Set<Int>, onSelect: (Int) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel2)) {
        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("JOKER · 열 선택", fontWeight = FontWeight.Black, fontSize = 12.sp)
            repeat(2) { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(4) { index ->
                        val file = row * 4 + index
                        OutlinedButton(
                            onClick = { onSelect(file) },
                            enabled = file in files,
                            modifier = Modifier.weight(1f),
                        ) { Text(('A'.code + file).toChar().toString()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TurnHandoffOverlay(playerName: String, player: Player, onReady: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xF20A1510)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.84f),
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(if (player == Player.WHITE) "♔" else "♚", fontSize = 52.sp)
                Text("$playerName 차례", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("상대방이 손패를 보지 않도록 화면을 넘긴 뒤 시작하세요.", textAlign = TextAlign.Center, color = Muted, fontSize = 13.sp)
                Button(onClick = onReady, modifier = Modifier.fillMaxWidth()) { Text("내 턴 보기") }
            }
        }
    }
}

@Composable
private fun LogOverlay(logs: List<String>, onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)).clickable { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxSize(0.72f).clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = Color(0xE616241B)),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("게임 로그", fontWeight = FontWeight.Black, color = Lime)
                    TextButton(onClick = onClose) { Text("닫기") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(logs.asReversed()) { log -> Text(log, fontSize = 12.sp, color = Color.White.copy(alpha = 0.88f)) }
                }
            }
        }
    }
}

@Composable
private fun ResultOverlay(
    result: GameResult,
    whiteName: String,
    blackName: String,
    onRematch: () -> Unit,
    onHome: () -> Unit,
) {
    val title = when (result) {
        is GameResult.Win -> {
            val name = if (result.winner == Player.WHITE) whiteName else blackName
            "$name 승리"
        }
        is GameResult.Draw -> "무승부"
    }
    val detail = when (result) {
        is GameResult.Win -> "상대 KING을 직접 포획했습니다."
        is GameResult.Draw -> result.reason
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.86f)), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(0.86f),
            colors = CardDefaults.cardColors(containerColor = Panel),
            shape = RoundedCornerShape(22.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("♛", fontSize = 52.sp, color = Lime)
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Text(detail, color = Muted, textAlign = TextAlign.Center)
                Button(onClick = onRematch, modifier = Modifier.fillMaxWidth()) { Text("다시하기") }
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("홈으로") }
            }
        }
    }
}
