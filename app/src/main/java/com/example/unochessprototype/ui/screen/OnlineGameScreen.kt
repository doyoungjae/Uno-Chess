package com.example.unochessprototype.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineResultType
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.online.rules.OnlineGameRules
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette
import com.example.unochessprototype.ui.theme.rememberLocalImage
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

@Composable
fun OnlineGameScreen(
    room: OnlineRoom?,
    profile: OnlineProfile,
    settings: LocalSettings,
    busy: Boolean,
    errorMessage: String?,
    onMove: (cardId: Int, move: Move, jokerFile: Int?) -> Unit,
    onExchange: (Set<Int>) -> Unit,
    onForfeit: () -> Unit,
    onExitFinished: () -> Unit,
) {
    val chessAudio = rememberChessAudio()
    val game = room?.game
    LaunchedEffect(game?.resultType, game?.winnerUid, game?.resultReason) {
        if (game?.hasResult() == true) chessAudio.playEnd()
    }
    LaunchedEffect(game?.completedTurns, game?.board) {
        if (game != null && game.completedTurns > 0 && !game.hasResult()) {
            val currentBoard = OnlineGameRules.boardFromState(game)
            val sideToMove = OnlineGameRules.sideFor(game, game.currentUid)
            if (currentBoard.isKingInCheck(sideToMove)) chessAudio.playCheck()
        }
    }
    if (room == null || game == null) {
        BackHandler { if (!busy) onForfeit() }
        Column(
            modifier = Modifier.fillMaxSize().background(UnoChessColors.Background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("온라인 게임 상태를 불러오는 중...", color = UnoChessColors.Muted)
            errorMessage?.let { Text(it, color = UnoChessColors.Warning) }
            TextButton(onClick = onForfeit, enabled = !busy) { Text("뒤로 · 방 나가기 (진행 중이면 기권)") }
        }
        return
    }

    var selectedCardId by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var focusedCardId by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var activeFile by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var selectedFrom by remember(game.currentUid, game.completedTurns) { mutableStateOf<Square?>(null) }
    var exchangeSelection by remember(game.currentUid, game.completedTurns) { mutableStateOf<Set<Int>>(emptySet()) }
    var pendingJokerCardId by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var showFirstTurn by rememberSaveable(room.roomId) { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    val isMyTurn = game.currentUid == profile.uid && !game.hasResult()
    val myHand = game.handFor(profile.uid)
    val mySide = runCatching { OnlineGameRules.sideFor(game, profile.uid) }.getOrNull()
    val board = remember(game.board) { OnlineGameRules.boardFromState(game) }
    val selectedCard = selectedCardId?.let(OnlineGameRules::card)
    val canExchange = OnlineGameRules.canExchange(game, profile.uid)
    val legalMoves = if (selectedCardId != null && activeFile != null && selectedFrom != null) {
        OnlineGameRules.legalMovesForCard(
            state = game,
            uid = profile.uid,
            cardId = selectedCardId!!,
            chosenFileForJoker = if (selectedCard?.isJoker == true) activeFile else null,
        ).filter { it.from == selectedFrom }
    } else emptyList()

    val opponentUid = room.otherUid(profile.uid)
    val opponentName = room.displayNameFor(opponentUid)
    val opponentAvatar = room.avatarFor(opponentUid)

    BackHandler {
        if (!busy) {
            if (game.hasResult()) onExitFinished() else showExitDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("ONLINE MATCH", color = UnoChessColors.Lime, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text("TURN ${game.completedTurns + 1}", color = UnoChessColors.Muted, fontSize = 10.sp)
            }
            TextButton(enabled = !busy, onClick = { if (game.hasResult()) onExitFinished() else showExitDialog = true }) {
                Icon(Icons.Rounded.Close, contentDescription = null)
                Text("나가기")
            }
        }

        PlayerLine(
            avatar = opponentAvatar,
            name = opponentName,
            subtitle = if (opponentUid == game.whiteUid) "WHITE · 상대" else "BLACK · 상대",
            active = game.currentUid == opponentUid,
        )

        if (errorMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                Text(errorMessage, modifier = Modifier.padding(8.dp), color = UnoChessColors.Warning, fontSize = 10.sp)
            }
        }

        Text(
            when {
                game.hasResult() -> resultText(game.resultType, game.winnerUid, profile.uid, game.resultReason)
                isMyTurn && canExchange -> "사용 가능한 카드가 없습니다. 카드 2장을 선택해 교환하세요."
                isMyTurn && selectedCardId == null -> "내 차례 · 사용할 카드를 선택하세요."
                isMyTurn && activeFile != null && selectedFrom == null -> "${(65 + activeFile!!).toChar()}열에서 움직일 기물을 선택하세요."
                isMyTurn -> "이동할 칸을 선택하세요."
                else -> "상대방의 턴을 기다리는 중..."
            },
            color = if (isMyTurn) UnoChessColors.Lime else UnoChessColors.Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        OnlineBoard(
            board = board,
            activeFile = activeFile,
            selectedFrom = selectedFrom,
            legalMoves = legalMoves,
            settings = settings,
            enabled = isMyTurn && !busy && selectedCardId != null && activeFile != null,
            onSquareClick = { square ->
                if (!isMyTurn || busy || selectedCardId == null || activeFile == null) return@OnlineBoard

                val destination = legalMoves.firstOrNull { it.to == square }
                if (destination != null && selectedFrom != null) {
                    chessAudio.playMove(captured = board.pieceAt(destination.to) != null)
                    onMove(
                        selectedCardId!!,
                        destination,
                        if (selectedCard?.isJoker == true) activeFile else null,
                    )
                    selectedCardId = null
                    focusedCardId = null
                    activeFile = null
                    selectedFrom = null
                    pendingJokerCardId = null
                    return@OnlineBoard
                }

                val piece = board.pieceAt(square)
                val side = mySide
                if (
                    side != null &&
                    square.col == activeFile &&
                    piece?.player == side &&
                    OnlineGameRules.legalMovesForCard(
                        game,
                        profile.uid,
                        selectedCardId!!,
                        if (selectedCard?.isJoker == true) activeFile else null,
                    ).any { it.from == square }
                ) {
                    selectedFrom = square
                }
            },
        )

        PlayerLine(
            avatar = profile.avatar,
            name = profile.nickname,
            subtitle = if (profile.uid == game.whiteUid) "WHITE · 나" else "BLACK · 나",
            active = isMyTurn,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            myHand.forEach { id ->
                val card = OnlineGameRules.card(id)
                val selected = if (canExchange) id in exchangeSelection else id == selectedCardId
                OnlineHandCard(
                    card = card,
                    focused = focusedCardId == id,
                    selected = selected,
                    enabled = isMyTurn && !busy,
                    modifier = Modifier.weight(1f),
                    onFocused = { focusedCardId = id },
                    onPulled = {
                        if (!isMyTurn || busy) return@OnlineHandCard
                        focusedCardId = id
                        if (canExchange) {
                            exchangeSelection = when {
                                id in exchangeSelection -> exchangeSelection - id
                                exchangeSelection.size < 2 -> exchangeSelection + id
                                else -> exchangeSelection
                            }
                        } else {
                            val files = OnlineGameRules.playableFiles(game, profile.uid, id)
                            if (files.isNotEmpty()) {
                                selectedCardId = id
                                selectedFrom = null
                                if (card.isJoker) {
                                    activeFile = null
                                    pendingJokerCardId = id
                                } else {
                                    activeFile = card.number!! - 1
                                }
                            }
                        }
                    },
                )
            }
        }

        if (canExchange && !game.hasResult()) {
            Button(
                onClick = {
                    onExchange(exchangeSelection)
                    exchangeSelection = emptySet<Int>()
                    focusedCardId = null
                },
                enabled = isMyTurn && !busy && exchangeSelection.size == 2,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("선택한 카드 2장 교환", fontWeight = FontWeight.Black) }
        }

        Spacer(Modifier.weight(1f))
        Text(
            game.logs.takeLast(2).joinToString("\n"),
            color = UnoChessColors.Muted,
            fontSize = 9.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    val pendingJoker = pendingJokerCardId
    if (pendingJoker != null && !game.hasResult()) {
        val files = OnlineGameRules.playableFiles(game, profile.uid, pendingJoker)
        MandatoryOnlineJokerDialog(
            files = files,
            onSelect = { file ->
                activeFile = file
                pendingJokerCardId = null
                selectedFrom = null
            },
        )
    }

    if (showFirstTurn && !game.hasResult()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("선공 결정") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("HOST  ${game.ownerFirstCard}", fontWeight = FontWeight.Bold)
                    Text("GUEST ${game.guestFirstCard}", fontWeight = FontWeight.Bold)
                    val firstName = room.displayNameFor(game.whiteUid)
                    Text("$firstName 님이 WHITE · 선공입니다.", color = UnoChessColors.Lime, fontWeight = FontWeight.Black)
                }
            },
            confirmButton = {
                TextButton(onClick = { showFirstTurn = false }) { Text("확인") }
            },
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { if (!busy) showExitDialog = false },
            title = { Text("온라인 경기에서 나갈까요?") },
            text = { Text("진행 중인 경기에서 나가면 기권패로 처리됩니다.") },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        showExitDialog = false
                        onForfeit()
                    },
                ) { Text("기권하고 나가기", color = UnoChessColors.Danger) }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { showExitDialog = false }) { Text("계속하기") }
            },
        )
    }

    if (game.hasResult()) {
        AlertDialog(
            onDismissRequest = { if (!busy) onExitFinished() },
            title = { Text(if (game.resultType == OnlineResultType.DRAW) "무승부" else if (game.winnerUid == profile.uid) "승리" else "패배") },
            text = { Text(game.resultReason.ifBlank { "온라인 경기가 종료되었습니다." }) },
            confirmButton = {
                Button(onClick = onExitFinished) { Text("홈으로 돌아가기") }
            },
        )
    }
}

@Composable
private fun PlayerLine(avatar: String, name: String, subtitle: String, active: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (active) UnoChessColors.SurfaceRaised else UnoChessColors.Surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(UnoChessColors.SurfaceSoft, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(avatar, fontSize = 21.sp) }
            Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
                Text(name, fontWeight = FontWeight.Black, fontSize = 13.sp)
                Text(subtitle, color = UnoChessColors.Muted, fontSize = 9.sp)
            }
            if (active) Text("TURN", color = UnoChessColors.Lime, fontWeight = FontWeight.Black, fontSize = 10.sp)
        }
    }
}

@Composable
private fun OnlineBoard(
    board: com.example.unochessprototype.game.ChessBoard,
    activeFile: Int?,
    selectedFrom: Square?,
    legalMoves: List<Move>,
    settings: LocalSettings,
    enabled: Boolean,
    onSquareClick: (Square) -> Unit,
) {
    val palette = boardPalette(settings.boardTheme)
    val boardImage = rememberLocalImage(settings.boardImagePath)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.border, RoundedCornerShape(8.dp))
            .padding(4.dp),
    ) {
    boardImage?.let { Image(it, contentDescription = null, modifier = Modifier.matchParentSize(), contentScale = ContentScale.Crop) }
    Column(modifier = Modifier.fillMaxWidth()) {
        repeat(8) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(8) { col ->
                    val square = Square(row, col)
                    val piece = board.pieceAt(square)
                    val destination = legalMoves.firstOrNull { it.to == square }
                    val base = (if ((row + col) % 2 == 0) palette.light else palette.dark)
                        .let { if (boardImage != null) it.copy(alpha = 0.45f) else it }
                    val bg = when {
                        activeFile == col -> UnoChessColors.Lime.copy(alpha = 0.28f).compositeOver(base)
                        else -> base
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(bg)
                            .then(if (selectedFrom == square) Modifier.border(3.dp, UnoChessColors.Warning) else Modifier)
                            .clickable(enabled = enabled) { onSquareClick(square) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (destination != null && settings.moveHints) {
                            Box(
                                modifier = Modifier
                                    .size(if (piece == null) 14.dp else 30.dp)
                                    .background(
                                        if (piece == null) Color(0xAA59D26F) else Color(0xAAE56D6D),
                                        CircleShape,
                                    )
                            )
                        }
                        if (piece != null) {
                            Text(
                                pieceGlyph(piece),
                                fontSize = 28.sp,
                                color = if (piece.player == Player.WHITE) Color(0xFFF8F5EC) else Color(0xFF111713),
                                fontWeight = FontWeight.Black,
                            )
                        }
                        if (settings.showCoordinates && col == 0) {
                            Text(
                                (8 - row).toString(),
                                modifier = Modifier.align(Alignment.TopStart).padding(1.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                fontSize = 7.sp,
                            )
                        }
                        if (settings.showCoordinates && row == 7) {
                            Text(
                                ('a'.code + col).toChar().toString(),
                                modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp),
                                color = Color.Black.copy(alpha = 0.55f),
                                fontSize = 7.sp,
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
private fun OnlineHandCard(
    card: UnoCard,
    focused: Boolean,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onPulled: () -> Unit,
) {
    val background = when (card.color) {
        CardColor.RED -> Color(0xFFD84A4A)
        CardColor.BLUE -> Color(0xFF4676D7)
        CardColor.GREEN -> Color(0xFF3F9A62)
        CardColor.YELLOW -> Color(0xFFD6A92D)
        null -> Color.Transparent
    }
    var dragY by remember(card.id) { mutableFloatStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 42.dp.toPx() }

    val cardModifier = modifier
        .height(92.dp)
        .graphicsLayer {
            translationY = dragY.coerceAtMost(0f) + if (selected) -12.dp.toPx() else 0f
            scaleX = if (focused || selected) 1.04f else 1f
            scaleY = if (focused || selected) 1.04f else 1f
        }
        .then(
            if (focused || selected) Modifier.border(3.dp, UnoChessColors.Lime, RoundedCornerShape(12.dp))
            else Modifier
        )
        .clickable(enabled = enabled) { onFocused() }
        .pointerInput(card.id, enabled) {
            if (!enabled) return@pointerInput
            detectVerticalDragGestures(
                onDragStart = {
                    dragY = 0f
                    onFocused()
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    dragY = (dragY + dragAmount).coerceAtMost(0f)
                },
                onDragCancel = { dragY = 0f },
                onDragEnd = {
                    val pulled = dragY <= -thresholdPx
                    dragY = 0f
                    if (pulled) onPulled()
                },
            )
        }

    if (card.isJoker) {
        Surface(modifier = cardModifier, shape = RoundedCornerShape(12.dp), color = Color.Transparent) {
            Box(
                modifier = Modifier.background(
                    Brush.linearGradient(listOf(Color(0xFFE24E4E), Color(0xFF4B79D8), Color(0xFF49A765), Color(0xFFE0B135)))
                ),
                contentAlignment = Alignment.Center,
            ) { Text("JOKER", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black) }
        }
    } else {
        Surface(modifier = cardModifier, shape = RoundedCornerShape(12.dp), color = background) {
            Box(contentAlignment = Alignment.Center) {
                Text(card.number.toString(), color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun MandatoryOnlineJokerDialog(files: Set<Int>, onSelect: (Int) -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface), shape = RoundedCornerShape(22.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("JOKER 열 선택", fontWeight = FontWeight.Black, fontSize = 19.sp)
                Text("움직일 수 있는 열 중 하나를 반드시 선택하세요.", color = UnoChessColors.Muted, fontSize = 11.sp)
                repeat(2) { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(4) { offset ->
                            val file = row * 4 + offset
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
}

private fun pieceGlyph(piece: Piece): String = when (piece.player to piece.type) {
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
    else -> ""
}

private fun resultText(type: String, winnerUid: String, myUid: String, reason: String): String = when (type) {
    OnlineResultType.DRAW -> "무승부 · $reason"
    OnlineResultType.WIN -> if (winnerUid == myUid) "승리 · $reason" else "패배 · $reason"
    else -> ""
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha + background.alpha * (1f - alpha)
    if (a == 0f) return Color.Transparent
    return Color(
        red = (red * alpha + background.red * background.alpha * (1f - alpha)) / a,
        green = (green * alpha + background.green * background.alpha * (1f - alpha)) / a,
        blue = (blue * alpha + background.blue * background.alpha * (1f - alpha)) / a,
        alpha = a,
    )
}
