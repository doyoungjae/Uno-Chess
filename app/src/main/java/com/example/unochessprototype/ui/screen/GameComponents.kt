package com.example.unochessprototype.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.ui.theme.CardBackFace
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.boardPalette
import com.example.unochessprototype.ui.theme.rememberLocalImage
import kotlin.math.roundToInt
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// 로컬·온라인 경기에서 함께 사용하는 보드, 카드 애니메이션, 시계와 기록 UI.

/** 화면 위를 날아가는 카드 한 장. card == null 이면 뒷면만 보인다. 좌표는 모두 root 기준. */
internal class CardFlight(
    val id: Int,
    val card: UnoCard?,
    val from: Rect,
    val to: Rect,
    val startFaceUp: Boolean,
    val endFaceUp: Boolean,
) {
    val progress = Animatable(0f)
}

internal class CardFlights {
    val active = mutableStateListOf<CardFlight>()
    private var serial = 0

    /** from -> to 로 날린다. keep = true 면 도착 후에도 그 자리에 남는다(직접 remove). */
    suspend fun fly(
        card: UnoCard?,
        from: Rect,
        to: Rect,
        startFaceUp: Boolean,
        endFaceUp: Boolean,
        durationMs: Int = 380,
        keep: Boolean = false,
    ): CardFlight {
        val flight = CardFlight(serial++, card, from, to, startFaceUp, endFaceUp)
        active += flight
        try {
            flight.progress.animateTo(1f, tween(durationMs, easing = FastOutSlowInEasing))
        } finally {
            if (!keep) active.remove(flight)
        }
        return flight
    }
}

/** 애니메이션 출발/도착 지점(root 좌표). 레이아웃 때마다 갱신되며 상태가 아니다. */
internal class CardAnchors {
    var root = Rect.Zero
    var deck = Rect.Zero
    var discard = Rect.Zero
    val hand = mutableMapOf<Int, Rect>()
}

internal data class BoardMoveAnimation(
    val id: Int,
    val from: Square,
    val to: Square,
    val piece: Piece,
)


@Composable
internal fun SharedChessBoard(
    modifier: Modifier = Modifier,
    chessBoard: com.example.unochessprototype.game.ChessBoard,
    currentPlayer: Player,
    flipped: Boolean = false,
    highlightFiles: Set<Int>,
    selectedFrom: Square?,
    legalMoves: List<Move>,
    settings: LocalSettings,
    enabled: Boolean,
    moveAnimation: BoardMoveAnimation?,
    onAnimationFinished: () -> Unit,
    onSquareClick: (Square) -> Unit,
) {
    val board = chessBoard.snapshot()
    val palette = boardPalette(settings.boardTheme)
    val boardImage = rememberLocalImage(settings.boardImagePath)

    BoxWithConstraints(
        modifier = modifier
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
            x.snapTo((if (flipped) 7 - event.from.col else event.from.col) * cellPx)
            y.snapTo((if (flipped) 7 - event.from.row else event.from.row) * cellPx)
            animating = true
            coroutineScope {
                launch {
                    x.animateTo(
                        (if (flipped) 7 - event.to.col else event.to.col) * cellPx,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                    )
                }
                launch {
                    y.animateTo(
                        (if (flipped) 7 - event.to.row else event.to.row) * cellPx,
                        animationSpec = tween(280, easing = FastOutSlowInEasing),
                    )
                }
            }
            animating = false
            onAnimationFinished()
        }

        boardImage?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        Column(modifier = Modifier.fillMaxSize()) {
            for (displayRow in 0..7) {
                val row = if (flipped) 7 - displayRow else displayRow
                Row(modifier = Modifier.weight(1f)) {
                    for (displayCol in 0..7) {
                        val col = if (flipped) 7 - displayCol else displayCol
                        val square = Square(row, col)
                        val piece = board[row][col]
                        val destination = legalMoves.firstOrNull { it.to == square }
                        val isCapture = destination != null && piece != null
                        val isMove = destination != null
                        val activeAnimationDestination = animating && moveAnimation?.to == square
                        // 사진 보드면 칸 색을 반투명하게 깔아 사진이 비치게 한다.
                        val base = (if ((row + col) % 2 == 0) palette.light else palette.dark)
                            .let { if (boardImage != null) it.copy(alpha = 0.45f) else it }
                        val fileLit = col in highlightFiles
                        val movablePiece = fileLit && piece?.player == currentPlayer &&
                            chessBoard.legalMovesFrom(square).isNotEmpty()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .background(base)
                                .then(
                                    if (fileLit) Modifier.background(Color(0x80F2D24B)) else Modifier
                                )
                                .then(
                                    if (movablePiece && selectedFrom != square) {
                                        Modifier.border(2.5.dp, Color(0xFFFFB300))
                                    } else {
                                        Modifier
                                    }
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
                                if (displayRow == 7) {
                                    Text(
                                        ('a'.code + col).toChar().toString(),
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp),
                                        fontSize = 7.sp,
                                        color = if ((row + col) % 2 == 0) palette.dark else palette.light,
                                        fontWeight = FontWeight.Black,
                                    )
                                }
                                if (displayCol == 0) {
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
internal fun PieceGlyph(piece: Piece, moving: Boolean = false) {
    // 양 진영 모두 채워진 글리프를 쓰고 색으로 구분한다. \uFE0E: ♟ 등이 이모지로 그려지지 않게 강제.
    val symbol = when (piece.type) {
        PieceType.KING -> "♚"
        PieceType.QUEEN -> "♛"
        PieceType.ROOK -> "♜"
        PieceType.BISHOP -> "♝"
        PieceType.KNIGHT -> "♞"
        PieceType.PAWN -> "♟"
    } + "\uFE0E"
    val isWhite = piece.player == Player.WHITE

    val scale by animateFloatAsState(if (moving) 1.12f else 1f, label = "pieceScale")
    Box(modifier = Modifier.graphicsLayer(scaleX = scale, scaleY = scale), contentAlignment = Alignment.Center) {
        Text(
            text = symbol,
            color = if (isWhite) Color(0xFFFFF6DC) else Color(0xFF18211B),
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
        // 외곽선: 흰 기물은 어두운 테두리, 검은 기물은 밝은 테두리로 어느 칸에서도 보이게 한다.
        Text(
            text = symbol,
            color = if (isWhite) Color(0xFF18211B) else Color(0xFFFFF6DC).copy(alpha = 0.55f),
            fontSize = 35.sp,
            fontFamily = FontFamily.Serif,
            style = TextStyle(drawStyle = Stroke(width = if (isWhite) 4f else 2f)),
        )
    }
}


@Composable
internal fun FanCard(
    card: UnoCard,
    showAsJoker: Boolean,
    focused: Boolean,
    committed: Boolean,
    enabled: Boolean,
    dimmed: Boolean,
    modifier: Modifier = Modifier,
    onTap: () -> Unit,
) {
    val density = LocalDensity.current

    val committedLift by animateDpAsState(
        targetValue = if (committed) (-22).dp else 0.dp,
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
        showAsJoker = showAsJoker,
        focused = focused,
        committed = committed,
        // 애니메이션 중 잠깐 터치가 막히는 것과 규칙상 낼 수 없는 카드를 구분한다. 어둡게는 후자만.
        enabled = !dimmed,
        modifier = modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = with(density) { committedLift.roundToPx() },
                )
            }
            .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
            .clickable(enabled = enabled) { onTap() },
    )
}

@Composable
internal fun StyledUnoCard(
    card: UnoCard,
    focused: Boolean,
    committed: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    showAsJoker: Boolean = card.isJoker,
) {
    val baseColors = when (if (showAsJoker) null else card.color) {
        CardColor.RED -> listOf(Color(0xFFFF6B68), Color(0xFFB51E2B))
        CardColor.BLUE -> listOf(Color(0xFF6DB5FF), Color(0xFF2454B9))
        CardColor.GREEN -> listOf(Color(0xFF76D98D), Color(0xFF237846))
        CardColor.YELLOW -> listOf(Color(0xFFFFD96A), Color(0xFFC17B11))
        null -> listOf(Color(0xFF3C3C3C), Color(0xFF050505))
    }
    val outlineColor = when {
        committed -> UnoChessColors.Lime
        focused -> Color(0xFF111A14)
        else -> Color.White.copy(alpha = 0.76f)
    }
    val outlineWidth = if (focused || committed) 4.dp else 1.2.dp

    Box(
        modifier = modifier
            .shadow(
                elevation = if (focused || committed) 12.dp else 3.dp,
                shape = RoundedCornerShape(13.dp),
                ambientColor = if (committed) UnoChessColors.Lime else Color.Black,
                spotColor = if (committed) UnoChessColors.Lime else Color.Black,
            )
            .background(Brush.linearGradient(baseColors), RoundedCornerShape(13.dp))
            .border(outlineWidth, outlineColor, RoundedCornerShape(13.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.62f)
                .graphicsLayer(rotationZ = -24f)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)),
        )

        Text(
            text = if (showAsJoker) "★" else card.number.toString(),
            color = if (showAsJoker) Color(0xFFFFD54F) else Color.White,
            fontSize = 31.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (showAsJoker) "JOKER" else card.color!!.shortName,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (showAsJoker) "★" else card.number.toString(),
            modifier = Modifier.align(Alignment.TopStart).padding(7.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )
        Text(
            text = if (showAsJoker) "★" else card.number.toString(),
            modifier = Modifier.align(Alignment.BottomEnd).padding(7.dp),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
        )

        if (!enabled) {
            Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(13.dp)))
        }

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
internal fun DeckColumn(
    settings: LocalSettings,
    drawCount: Int,
    topDiscard: UnoCard?,
    onDeckPlaced: (Rect) -> Unit,
    onDiscardPlaced: (Rect) -> Unit,
) {
    val cardModifier = Modifier.width(54.dp).height(78.dp)
    val discardModifier = cardModifier.onGloballyPositioned { onDiscardPlaced(it.boundsInRoot()) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("DECK", color = UnoChessColors.Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
        CardBackFace(settings, cardModifier.onGloballyPositioned { onDeckPlaced(it.boundsInRoot()) })
        Text("${drawCount}장", color = UnoChessColors.Lime, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))
        Text("DISCARD", color = UnoChessColors.Muted, fontSize = 8.sp, fontWeight = FontWeight.Black)
        if (topDiscard != null) {
            StyledUnoCard(topDiscard, focused = false, committed = false, enabled = true, modifier = discardModifier)
        } else {
            Box(discardModifier.border(1.dp, UnoChessColors.Muted.copy(alpha = 0.5f), RoundedCornerShape(13.dp)))
        }
    }
}

@Composable
internal fun CardFlightLayer(flights: List<CardFlight>, origin: Offset, settings: LocalSettings) {
    val density = LocalDensity.current
    flights.forEach { flight ->
        key(flight.id) {
            val p = flight.progress.value
            val rect = lerp(flight.from, flight.to, p)
            val flips = flight.startFaceUp != flight.endFaceUp
            // 뒤집기: 앞 절반은 시작 면이 0->90도, 뒤 절반은 끝 면이 -90->0도.
            val faceUp = if (flips && p < 0.5f) flight.startFaceUp else flight.endFaceUp
            val flipAngle = when {
                !flips -> 0f
                p < 0.5f -> p * 180f
                else -> (p - 1f) * 180f
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset((rect.left - origin.x).roundToInt(), (rect.top - origin.y).roundToInt()) }
                    .size(with(density) { rect.width.toDp() }, with(density) { rect.height.toDp() })
                    .zIndex(200f)
                    .graphicsLayer {
                        rotationY = flipAngle
                        cameraDistance = 12f * density.density
                    },
            ) {
                val card = flight.card
                if (faceUp && card != null) {
                    StyledUnoCard(card, focused = false, committed = false, enabled = true, modifier = Modifier.fillMaxSize())
                } else {
                    CardBackFace(settings, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
internal fun FirstTurnIntroOverlay(
    text: String?,
    player1Name: String,
    player2Name: String,
    player1Slot: Rect?,
    player2Slot: Rect?,
    origin: Offset,
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xD908120C))
            // 연출 중에는 아래 화면 터치를 막는다.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
    ) {
        if (text != null) {
            Column(
                modifier = Modifier.align(Alignment.Center).offset(y = (-120).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("높은 숫자가 선공 · JOKER는 패배", color = UnoChessColors.Muted, fontSize = 11.sp)
                Text(text, color = UnoChessColors.Lime, fontSize = 20.sp, fontWeight = FontWeight.Black)
            }
        }
        listOf(player1Slot to player1Name, player2Slot to player2Name).forEach { (slot, name) ->
            if (slot != null) {
                Text(
                    name,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (slot.left - origin.x).roundToInt(),
                                (slot.bottom - origin.y).roundToInt() + with(density) { 8.dp.roundToPx() },
                            )
                        }
                        .width(with(density) { slot.width.toDp() }),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Composable
internal fun ChessClock(ms: Long, running: Boolean, modifier: Modifier = Modifier) {
    val totalSeconds = (ms + 999) / 1000
    Surface(
        modifier = modifier,
        color = if (running) UnoChessColors.Lime else UnoChessColors.SurfaceRaised,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = when {
                running -> Color(0xFF111A14)
                ms < 60_000 -> Color(0xFFE35B5B)
                else -> Color.White
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
internal fun GameLogOverlay(logs: List<String>, onClose: () -> Unit) {
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
