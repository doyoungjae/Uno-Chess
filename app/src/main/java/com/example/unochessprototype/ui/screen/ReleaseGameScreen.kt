package com.example.unochessprototype.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
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
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private data class BoardMoveAnimation(
    val id: Int,
    val from: Square,
    val to: Square,
    val piece: Piece,
)

@Composable
fun ReleaseGameScreen(
    game: UnoChessGame,
    profile: LocalProfile,
    opponentName: String,
    settings: LocalSettings,
    player1IsWhite: Boolean,
    onExit: () -> Unit,
    onRematch: () -> Unit,
    onGameFinished: (GameResult) -> Unit,
) {
    var revision by remember { mutableIntStateOf(0) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var focusedCardId by remember { mutableStateOf<Int?>(null) }
    var pendingJokerCardId by remember { mutableStateOf<Int?>(null) }
    var activeFile by remember { mutableStateOf<Int?>(null) }
    var selectedFrom by remember { mutableStateOf<Square?>(null) }
    var exchangeSelection by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showLogs by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showTurnCover by remember { mutableStateOf(false) }
    var moveAnimation by remember { mutableStateOf<BoardMoveAnimation?>(null) }
    var isMoveAnimating by remember { mutableStateOf(false) }
    var animationSerial by remember { mutableIntStateOf(0) }

    fun refresh() { revision += 1 }
    fun clearSelection() {
        selectedCardId = null
        focusedCardId = null
        pendingJokerCardId = null
        activeFile = null
        selectedFrom = null
        exchangeSelection = emptySet()
    }

    val hand = remember(revision) { game.hand() }
    val result = remember(revision) { game.result }
    val canExchange = remember(revision) { game.canExchangeTwoCards() }
    val selectedCard = hand.firstOrNull { it.id == selectedCardId }

    val whiteName = if (player1IsWhite) profile.nickname else opponentName.ifBlank { "PLAYER 2" }
    val blackName = if (player1IsWhite) opponentName.ifBlank { "PLAYER 2" } else profile.nickname
    val whiteAvatar = if (player1IsWhite) profile.avatar else "♜"
    val blackAvatar = if (player1IsWhite) "♜" else profile.avatar

    val currentName = if (game.currentPlayer == Player.WHITE) whiteName else blackName
    val currentAvatar = if (game.currentPlayer == Player.WHITE) whiteAvatar else blackAvatar
    val opponentCurrentName = if (game.currentPlayer == Player.WHITE) blackName else whiteName
    val opponentAvatar = if (game.currentPlayer == Player.WHITE) blackAvatar else whiteAvatar

    val legalMoves = if (selectedCardId != null && activeFile != null && selectedFrom != null) {
        game.legalMovesForCard(
            cardId = selectedCardId!!,
            chosenFileForJoker = if (selectedCard?.isJoker == true) activeFile else null,
        ).filter { it.from == selectedFrom }
    } else {
        emptyList()
    }

    LaunchedEffect(result) {
        if (result != null) onGameFinished(result)
    }

    // Joker 열 선택 중에는 시스템 뒤로가기 역시 막는다.
    BackHandler(enabled = pendingJokerCardId != null) {
        // 반드시 열을 선택해야 게임으로 돌아갈 수 있다.
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            GameHeader(
                turn = game.completedTurns,
                deckCount = game.drawPileCount(),
                onLogs = { showLogs = true },
                onExit = { showExitDialog = true },
            )

            CompactPlayerPanel(
                avatar = opponentAvatar,
                name = opponentCurrentName,
                subtitle = if (game.currentPlayer == Player.WHITE) "BLACK · 상대" else "WHITE · 상대",
                showCardBacks = true,
            )

            GameGuide(
                result = result,
                canExchange = canExchange,
                selectedCard = selectedCard,
                activeFile = activeFile,
            )

            AnimatedChessBoard(
                game = game,
                revision = revision,
                activeFile = activeFile,
                selectedFrom = selectedFrom,
                legalMoves = legalMoves,
                settings = settings,
                enabled = result == null && !showTurnCover && !isMoveAnimating && selectedCardId != null && activeFile != null,
                moveAnimation = moveAnimation,
                onAnimationFinished = {
                    isMoveAnimating = false
                    moveAnimation = null
                    if (game.result == null) showTurnCover = true
                },
                onSquareClick = { square ->
                    if (result != null || selectedCardId == null || activeFile == null || isMoveAnimating) {
                        return@AnimatedChessBoard
                    }

                    val destination = legalMoves.firstOrNull { it.to == square }
                    if (destination != null && selectedFrom != null) {
                        val movingPiece = game.board.pieceAt(destination.from) ?: return@AnimatedChessBoard
                        val play = game.playCardAndMove(
                            cardId = selectedCardId!!,
                            move = destination,
                            chosenFileForJoker = if (selectedCard?.isJoker == true) activeFile else null,
                        )

                        animationSerial += 1
                        moveAnimation = BoardMoveAnimation(
                            id = animationSerial,
                            from = destination.from,
                            to = destination.to,
                            piece = play.moveOutcome.movedPieceAfterPromotion.takeIf { play.moveOutcome.promoted }
                                ?: movingPiece,
                        )
                        isMoveAnimating = true
                        clearSelection()
                        refresh()
                        return@AnimatedChessBoard
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

            PlayerHandPanel(
                name = currentName,
                avatar = currentAvatar,
                player = game.currentPlayer,
                cards = hand,
                game = game,
                canExchange = canExchange,
                selectedCardId = selectedCardId,
                focusedCardId = focusedCardId,
                exchangeSelection = exchangeSelection,
                enabled = result == null && !showTurnCover && !isMoveAnimating && pendingJokerCardId == null,
                onCardFocused = { card ->
                    if (result != null || showTurnCover || isMoveAnimating || pendingJokerCardId != null) {
                        return@PlayerHandPanel
                    }
                    focusedCardId = card.id
                },
                onCardPulled = { card ->
                    if (result != null || showTurnCover || isMoveAnimating || pendingJokerCardId != null) {
                        return@PlayerHandPanel
                    }

                    if (canExchange) {
                        exchangeSelection = if (card.id in exchangeSelection) {
                            exchangeSelection - card.id
                        } else if (exchangeSelection.size < 2) {
                            exchangeSelection + card.id
                        } else {
                            exchangeSelection
                        }
                        focusedCardId = card.id
                    } else if (game.playableFiles(card).isNotEmpty()) {
                        selectedCardId = card.id
                        focusedCardId = card.id
                        selectedFrom = null
                        if (card.isJoker) {
                            activeFile = null
                            pendingJokerCardId = card.id
                        } else {
                            activeFile = card.number!! - 1
                        }
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
                ) {
                    Text("선택한 카드 2장 교환", fontWeight = FontWeight.Black)
                }
            }
        }

        val pendingJoker = hand.firstOrNull { it.id == pendingJokerCardId }
        if (pendingJoker != null && result == null) {
            MandatoryJokerFileDialog(
                files = game.playableFiles(pendingJoker),
                onSelect = { file ->
                    activeFile = file
                    selectedFrom = null
                    pendingJokerCardId = null
                },
            )
        }

        if (showTurnCover && result == null) {
            TurnHandoffOverlay(
                playerName = if (game.currentPlayer == Player.WHITE) whiteName else blackName,
                player = game.currentPlayer,
                onReady = { showTurnCover = false },
            )
        }

        if (showLogs) {
            GameLogOverlay(logs = game.logs, onClose = { showLogs = false })
        }

        if (result != null && !isMoveAnimating) {
            ResultOverlay(
                result = result,
                whiteName = whiteName,
                blackName = blackName,
                onRematch = onRematch,
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
private fun GameHeader(turn: Int, deckCount: Int, onLogs: () -> Unit, onExit: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text("UNO CHESS", color = UnoChessColors.Lime, fontWeight = FontWeight.Black, fontSize = 16.sp)
            Text("TURN $turn  ·  DECK $deckCount", color = UnoChessColors.Muted, fontSize = 10.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            TextButton(onClick = onLogs) {
                Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(17.dp))
                Text(" 로그", fontSize = 11.sp)
            }
            TextButton(onClick = onExit) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun CompactPlayerPanel(
    avatar: String,
    name: String,
    subtitle: String,
    showCardBacks: Boolean,
) {
    Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Text(avatar, fontSize = 21.sp) }
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(name, fontWeight = FontWeight.Black, fontSize = 12.sp)
                Text(subtitle, color = UnoChessColors.Muted, fontSize = 9.sp)
            }
            if (showCardBacks) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(5) { CardBack() }
                }
            }
        }
    }
}

@Composable
private fun GameGuide(
    result: GameResult?,
    canExchange: Boolean,
    selectedCard: UnoCard?,
    activeFile: Int?,
) {
    val text = when (result) {
        is GameResult.Win -> "KING 포획 · 게임 종료"
        is GameResult.Draw -> "무승부 · ${result.reason}"
        null -> when {
            canExchange -> "이동 가능한 카드가 없습니다 · 교환할 카드 2장을 위로 뽑으세요"
            selectedCard == null -> "카드를 터치한 뒤 위로 뽑아 선택하세요 · 1~8은 A~H 열"
            selectedCard.isJoker && activeFile == null -> "JOKER · 열을 선택해야 계속할 수 있습니다"
            activeFile != null -> "${('A'.code + activeFile).toChar()}열 활성화 · 움직일 기물을 선택하세요"
            else -> "카드를 선택하세요"
        }
    }

    Surface(color = UnoChessColors.SurfaceRaised, shape = RoundedCornerShape(10.dp)) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 7.dp),
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun AnimatedChessBoard(
    game: UnoChessGame,
    revision: Int,
    activeFile: Int?,
    selectedFrom: Square?,
    legalMoves: List<Move>,
    settings: LocalSettings,
    enabled: Boolean,
    moveAnimation: BoardMoveAnimation?,
    onAnimationFinished: () -> Unit,
    onSquareClick: (Square) -> Unit,
) {
    val board = remember(revision) { game.board.snapshot() }
    val palette = boardPalette(settings.boardTheme)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .background(palette.border, RoundedCornerShape(8.dp))
            .padding(3.dp),
    ) {
        val cellDp = maxWidth / 8
        val density = LocalDensity.current
        val cellPx = with(density) { cellDp.toPx() }
        val x = remember { Animatable(0f) }
        val y = remember { Animatable(0f) }
        var animating by remember { mutableStateOf(false) }

        LaunchedEffect(moveAnimation?.id) {
            val event = moveAnimation ?: return@LaunchedEffect
            x.snapTo(event.from.col * cellPx)
            y.snapTo(event.from.row * cellPx)
            animating = true
            coroutineScope {
                launch {
                    x.animateTo(
                        event.to.col * cellPx,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    y.animateTo(
                        event.to.row * cellPx,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                    )
                }
            }
            animating = false
            onAnimationFinished()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            for (row in 0..7) {
                Row(modifier = Modifier.weight(1f)) {
                    for (col in 0..7) {
                        val square = Square(row, col)
                        val piece = board[row][col]
                        val destination = legalMoves.firstOrNull { it.to == square }
                        val isCapture = destination != null && piece != null
                        val isMove = destination != null
                        val activeAnimationDestination = animating && moveAnimation?.to == square
                        val base = if ((row + col) % 2 == 0) palette.light else palette.dark

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(base)
                                .then(
                                    if (activeFile == col) Modifier.background(Color(0x22F2D24B)) else Modifier
                                )
                                .then(
                                    if (selectedFrom == square) Modifier.border(3.dp, UnoChessColors.Lime) else Modifier
                                )
                                .clickable(enabled = enabled) { onSquareClick(square) },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (settings.moveHints && isMove && !isCapture) {
                                Box(Modifier.size(13.dp).background(Color(0xBB38B86A), CircleShape))
                            }
                            if (settings.moveHints && isCapture) {
                                Box(
                                    Modifier.fillMaxSize().padding(3.dp)
                                        .border(3.dp, Color(0xFFE35B5B), CircleShape)
                                )
                            }

                            if (piece != null && !activeAnimationDestination) {
                                PieceGlyph(piece)
                            }

                            if (settings.showCoordinates) {
                                if (row == 7) {
                                    Text(
                                        ('a'.code + col).toChar().toString(),
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                                        fontSize = 7.sp,
                                        color = if ((row + col) % 2 == 0) palette.dark else palette.light,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                                if (col == 0) {
                                    Text(
                                        (8 - row).toString(),
                                        modifier = Modifier.align(Alignment.TopStart).padding(2.dp),
                                        fontSize = 7.sp,
                                        color = if ((row + col) % 2 == 0) palette.dark else palette.light,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (animating && moveAnimation != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }
                    .size(cellDp),
                contentAlignment = Alignment.Center,
            ) {
                PieceGlyph(moveAnimation.piece, moving = true)
            }
        }
    }
}

@Composable
private fun PieceGlyph(piece: Piece, moving: Boolean = false) {
    val symbol = when (piece.type) {
        PieceType.KING -> if (piece.player == Player.WHITE) "♔" else "♚"
        PieceType.QUEEN -> if (piece.player == Player.WHITE) "♕" else "♛"
        PieceType.ROOK -> if (piece.player == Player.WHITE) "♖" else "♜"
        PieceType.BISHOP -> if (piece.player == Player.WHITE) "♗" else "♝"
        PieceType.KNIGHT -> if (piece.player == Player.WHITE) "♘" else "♞"
        PieceType.PAWN -> if (piece.player == Player.WHITE) "♙" else "♟"
    }

    val scale by animateFloatAsState(if (moving) 1.12f else 1f, label = "pieceScale")
    Text(
        text = symbol,
        modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale),
        color = if (piece.player == Player.WHITE) Color(0xFFFFF9E9) else Color(0xFF18211B),
        fontSize = 35.sp,
        fontFamily = FontFamily.Serif,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.55f),
                offset = Offset(0f, 2f),
                blurRadius = 3f,
            )
        ),
    )
}

@Composable
private fun PlayerHandPanel(
    name: String,
    avatar: String,
    player: Player,
    cards: List<UnoCard>,
    game: UnoChessGame,
    canExchange: Boolean,
    selectedCardId: Int?,
    focusedCardId: Int?,
    exchangeSelection: Set<Int>,
    enabled: Boolean,
    onCardFocused: (UnoCard) -> Unit,
    onCardPulled: (UnoCard) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface)) {
        Column(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).background(UnoChessColors.SurfaceRaised, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(avatar, fontSize = 19.sp) }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(name, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Text(
                        if (player == Player.WHITE) "WHITE · 현재 차례" else "BLACK · 현재 차례",
                        color = UnoChessColors.Lime,
                        fontSize = 9.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    if (canExchange) "2장을 위로 뽑아 교환" else "위로 드래그해 선택",
                    color = UnoChessColors.Muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            FanCardHand(
                cards = cards,
                game = game,
                canExchange = canExchange,
                selectedCardId = selectedCardId,
                focusedCardId = focusedCardId,
                exchangeSelection = exchangeSelection,
                enabled = enabled,
                onCardFocused = onCardFocused,
                onCardPulled = onCardPulled,
            )
        }
    }
}

@Composable
private fun FanCardHand(
    cards: List<UnoCard>,
    game: UnoChessGame,
    canExchange: Boolean,
    selectedCardId: Int?,
    focusedCardId: Int?,
    exchangeSelection: Set<Int>,
    enabled: Boolean,
    onCardFocused: (UnoCard) -> Unit,
    onCardPulled: (UnoCard) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        val cardWidth = 76.dp
        val cardHeight = 108.dp
        val step = 47.dp
        val totalWidth = if (cards.isEmpty()) 0.dp else cardWidth + step * (cards.size - 1).toFloat()
        val startX = ((maxWidth - totalWidth) / 2).coerceAtLeast(0.dp)
        val center = (cards.size - 1) / 2f

        cards.forEachIndexed { index, card ->
            val distance = index - center
            val rotation = distance * 7.5f
            val arcY = (abs(distance) * 6.5f).dp
            val committed = card.id == selectedCardId || card.id in exchangeSelection
            val focused = card.id == focusedCardId
            val playable = canExchange || game.playableFiles(card).isNotEmpty()

            DraggableFanCard(
                card = card,
                focused = focused,
                committed = committed,
                enabled = enabled && playable,
                modifier = Modifier
                    .offset(x = startX + step * index.toFloat(), y = arcY)
                    .size(width = cardWidth, height = cardHeight)
                    .zIndex(
                        when {
                            committed -> 100f + index
                            focused -> 80f + index
                            else -> index.toFloat()
                        }
                    )
                    .graphicsLayer(
                        rotationZ = rotation,
                        transformOrigin = TransformOrigin(0.5f, 0.92f),
                    ),
                onFocused = { onCardFocused(card) },
                onPulled = { onCardPulled(card) },
            )
        }
    }
}

@Composable
private fun DraggableFanCard(
    card: UnoCard,
    focused: Boolean,
    committed: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onFocused: () -> Unit,
    onPulled: () -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 42.dp.toPx() }
    val maxPullPx = with(density) { 68.dp.toPx() }
    var dragY by remember(card.id) { mutableStateOf(0f) }

    val committedLift by animateDpAsState(
        targetValue = if (committed) (-14).dp else 0.dp,
        animationSpec = tween(170),
        label = "committedCardLift",
    )
    val cardScale by animateFloatAsState(
        targetValue = if (focused || committed) 1.045f else 1f,
        animationSpec = tween(150),
        label = "fanCardScale",
    )

    StyledUnoCard(
        card = card,
        focused = focused,
        committed = committed,
        enabled = enabled,
        modifier = modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = dragY.roundToInt() + with(density) { committedLift.roundToPx() },
                )
            }
            .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
            .clickable(enabled = enabled) { onFocused() }
            .pointerInput(enabled, card.id) {
                if (!enabled) return@pointerInput
                detectVerticalDragGestures(
                    onDragStart = {
                        onFocused()
                        dragY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragY = (dragY + dragAmount).coerceIn(-maxPullPx, 8f)
                    },
                    onDragEnd = {
                        val confirmed = dragY <= -thresholdPx
                        dragY = 0f
                        if (confirmed) onPulled()
                    },
                    onDragCancel = { dragY = 0f },
                )
            },
    )
}

@Composable
private fun StyledUnoCard(
    card: UnoCard,
    focused: Boolean,
    committed: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val baseColors = when (card.color) {
        CardColor.RED -> listOf(Color(0xFFFF6B68), Color(0xFFB51E2B))
        CardColor.BLUE -> listOf(Color(0xFF6DB5FF), Color(0xFF2454B9))
        CardColor.GREEN -> listOf(Color(0xFF76D98D), Color(0xFF237846))
        CardColor.YELLOW -> listOf(Color(0xFFFFD96A), Color(0xFFC17B11))
        null -> listOf(Color(0xFFE84D4D), Color(0xFFF1C84E), Color(0xFF49A866), Color(0xFF4E78D1))
    }
    val outlineColor = when {
        focused || committed -> Color(0xFF111A14)
        else -> Color.White.copy(alpha = 0.76f)
    }
    val outlineWidth = if (focused || committed) 4.dp else 1.2.dp

    Box(
        modifier = modifier
            .shadow(if (focused || committed) 10.dp else 3.dp, RoundedCornerShape(13.dp))
            .background(Brush.linearGradient(baseColors), RoundedCornerShape(13.dp))
            .border(outlineWidth, outlineColor, RoundedCornerShape(13.dp))
            .alpha(if (enabled) 1f else 0.38f),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.75f)
                .graphicsLayer(rotationZ = -24f)
                .background(Color.White.copy(alpha = 0.94f), RoundedCornerShape(50)),
        )

        Text(
            text = if (card.isJoker) "★" else card.number.toString(),
            color = if (card.isJoker) Color(0xFF181818) else baseColors.last(),
            fontSize = 31.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (card.isJoker) "JOKER" else card.color!!.shortName,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            color = Color(0xFF202020),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (card.isJoker) "★" else card.number.toString(),
            modifier = Modifier.align(Alignment.TopStart).padding(7.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (card.isJoker) "★" else card.number.toString(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )

        if (committed) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 5.dp),
                color = Color(0xE6111A14),
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    "SELECTED",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
private fun CardBack() {
    Box(
        modifier = Modifier
            .width(24.dp)
            .height(36.dp)
            .background(
                Brush.linearGradient(listOf(Color(0xFF173E28), Color(0xFF091C12))),
                RoundedCornerShape(6.dp),
            )
            .border(1.dp, UnoChessColors.Lime.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("U", color = UnoChessColors.Lime, fontWeight = FontWeight.Black, fontSize = 10.sp)
    }
}

@Composable
private fun MandatoryJokerFileDialog(
    files: Set<Int>,
    onSelect: (Int) -> Unit,
) {
    Dialog(
        onDismissRequest = { /* 선택 전에는 닫히지 않는다. */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true,
        ),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = Color(0xFF111A14),
                    shape = CircleShape,
                ) {
                    Text("★", modifier = Modifier.padding(14.dp), color = UnoChessColors.Lime, fontSize = 28.sp)
                }
                Text("JOKER 열 선택", fontSize = 21.sp, fontWeight = FontWeight.Black)
                Text(
                    "JOKER를 뽑았습니다. 이번 턴에 활성화할 열을 반드시 선택하세요.\n선택하기 전에는 뒤로 갈 수 없습니다.",
                    color = UnoChessColors.Muted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(2) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            repeat(4) { col ->
                                val file = row * 4 + col
                                Button(
                                    onClick = { onSelect(file) },
                                    enabled = file in files,
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                ) {
                                    Text(
                                        ('A'.code + file).toChar().toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    "이동 가능한 열만 선택할 수 있습니다.",
                    color = UnoChessColors.Muted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@Composable
private fun TurnHandoffOverlay(playerName: String, player: Player, onReady: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xF408120C)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.84f),
            colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(if (player == Player.WHITE) "♔" else "♚", fontSize = 54.sp)
                Text("$playerName 차례", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(
                    "상대가 손패를 보지 않도록 화면을 넘긴 뒤 자신의 턴을 시작하세요.",
                    color = UnoChessColors.Muted,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                )
                Button(onClick = onReady, modifier = Modifier.fillMaxWidth()) {
                    Text("내 턴 보기", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun GameLogOverlay(logs: List<String>, onClose: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxSize(0.72f),
            colors = CardDefaults.cardColors(containerColor = Color(0xF216241B)),
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("게임 로그", color = UnoChessColors.Lime, fontWeight = FontWeight.Black)
                    TextButton(onClick = onClose) { Text("닫기") }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(logs.asReversed()) { log ->
                        Text(log, fontSize = 11.sp, color = Color.White.copy(alpha = 0.88f))
                    }
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
        is GameResult.Win -> if (result.winner == Player.WHITE) "$whiteName 승리" else "$blackName 승리"
        is GameResult.Draw -> "무승부"
    }
    val detail = when (result) {
        is GameResult.Win -> "상대 KING을 직접 포획했습니다."
        is GameResult.Draw -> result.reason
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.86f)),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.86f),
            colors = CardDefaults.cardColors(containerColor = UnoChessColors.Surface),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("♛", fontSize = 54.sp, color = UnoChessColors.Lime)
                Text(title, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text(detail, color = UnoChessColors.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
                Button(onClick = onRematch, modifier = Modifier.fillMaxWidth()) { Text("다시하기") }
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("홈으로") }
            }
        }
    }
}
