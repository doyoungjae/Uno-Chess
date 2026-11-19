package com.example.unochessprototype.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.LocalPlayerSlot
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Piece
import com.example.unochessprototype.game.PieceType
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.game.UnoChessGame
import com.example.unochessprototype.ui.theme.UnoChessColors
import com.example.unochessprototype.ui.theme.CardBackFace
import com.example.unochessprototype.ui.theme.boardPalette
import com.example.unochessprototype.ui.theme.rememberLocalImage
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.os.SystemClock
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val CLOCK_START_MS = 10 * 60 * 1000L

/** 화면 위를 날아가는 카드 한 장. card == null 이면 뒷면만 보인다. 좌표는 모두 root 기준. */
private class CardFlight(
    val id: Int,
    val card: UnoCard?,
    val from: Rect,
    val to: Rect,
    val startFaceUp: Boolean,
    val endFaceUp: Boolean,
) {
    val progress = Animatable(0f)
}

private class CardFlights {
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
private class CardAnchors {
    var root = Rect.Zero
    var deck = Rect.Zero
    var discard = Rect.Zero
    val hand = mutableMapOf<Int, Rect>()
}

private data class BoardMoveAnimation(
    val id: Int,
    val from: Square,
    val to: Square,
    val piece: Piece,
)

@Composable
fun ReleaseGameScreen(
    game: UnoChessGame,
    profile: OnlineProfile,
    opponentName: String,
    settings: LocalSettings,
    onExit: () -> Unit,
    onRematch: () -> Unit,
    onGameFinished: (GameResult) -> Unit,
) {
    var revision by remember { mutableIntStateOf(0) }
    var selectedCardId by remember { mutableStateOf<Int?>(null) }
    var focusedCardId by remember { mutableStateOf<Int?>(null) }
    var activeFile by remember { mutableStateOf<Int?>(null) }
    var selectedFrom by remember { mutableStateOf<Square?>(null) }
    var exchangeSelection by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showLogs by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var moveAnimation by remember { mutableStateOf<BoardMoveAnimation?>(null) }
    var isMoveAnimating by remember { mutableStateOf(false) }
    var animationSerial by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()
    val flights = remember { CardFlights() }
    val anchors = remember { CardAnchors() }
    var introDone by rememberSaveable { mutableStateOf(game.completedTurns > 0) }
    var introText by remember { mutableStateOf<String?>(null) }
    var introLabels by remember { mutableStateOf(false) }
    var cardBusy by remember { mutableStateOf(!introDone) }
    var hiddenCardIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var shownDiscard by remember { mutableStateOf(if (introDone) game.topDiscard() else null) }

    var whiteClockMs by remember { mutableLongStateOf(CLOCK_START_MS) }
    var blackClockMs by remember { mutableLongStateOf(CLOCK_START_MS) }

    fun refresh() { revision += 1 }
    fun clearSelection() {
        selectedCardId = null
        focusedCardId = null
        activeFile = null
        selectedFrom = null
        exchangeSelection = emptySet()
    }

    val hand = remember(revision) { game.hand() }
    val result = remember(revision) { game.result }
    val canExchange = remember(revision) { game.canExchangeTwoCards() }
    val selectedCard = hand.firstOrNull { it.id == selectedCardId }
    val highlightFiles = remember(revision, selectedCardId) {
        selectedCard?.let { game.playableFiles(it) } ?: emptySet()
    }

    val player1IsWhite = game.player1Starts
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
            chosenFileForJoker = if (selectedCard?.let(game::actsAsJoker) == true) activeFile else null,
        ).filter { it.from == selectedFrom }
    } else {
        emptyList()
    }

    LaunchedEffect(result) {
        if (result != null) onGameFinished(result)
    }

    val density = LocalDensity.current
    fun introSlot(player1: Boolean): Rect {
        val w = with(density) { 84.dp.toPx() }
        val h = with(density) { 120.dp.toPx() }
        val gap = with(density) { 28.dp.toPx() }
        val c = anchors.root.center
        val left = if (player1) c.x - gap / 2 - w else c.x + gap / 2
        return Rect(Offset(left, c.y - h / 2), Size(w, h))
    }

    // 턴 시작 드로우: 손패 마지막(방금 뽑은) 카드가 덱에서 손패 자리로 날아온다.
    suspend fun animateTurnDraw() {
        val drawn = game.hand().lastOrNull() ?: return
        if (game.result != null) return
        cardBusy = true
        hiddenCardIds = hiddenCardIds + drawn.id
        withFrameNanos { }
        anchors.hand[drawn.id]?.let { target ->
            flights.fly(drawn, anchors.deck, target, startFaceUp = false, endFaceUp = true, durationMs = 420)
        }
        hiddenCardIds = hiddenCardIds - drawn.id
        cardBusy = false
    }

    // 게임 진입 시 선공 결정 연출. 결과는 엔진이 reset()에서 이미 정했고 여기선 그대로 보여준다.
    LaunchedEffect(Unit) {
        if (introDone) return@LaunchedEffect
        cardBusy = true
        withFrameNanos { }
        val p1Name = profile.nickname
        val p2Name = opponentName.ifBlank { "PLAYER 2" }
        for (round in game.firstTurnRounds) {
            introText = "선공 결정 · 덱에서 1장씩 뽑기"
            introLabels = true
            val p1Slot = introSlot(player1 = true)
            val p2Slot = introSlot(player1 = false)
            lateinit var p1Flight: CardFlight
            lateinit var p2Flight: CardFlight
            coroutineScope {
                launch { p1Flight = flights.fly(round.player1Card, anchors.deck, p1Slot, false, true, 650, keep = true) }
                launch {
                    delay(200)
                    p2Flight = flights.fly(round.player2Card, anchors.deck, p2Slot, false, true, 650, keep = true)
                }
            }
            val winner = round.winner
            introText = when (winner) {
                LocalPlayerSlot.PLAYER1 -> "$p1Name 승리 · 선공(WHITE)"
                LocalPlayerSlot.PLAYER2 -> "$p2Name 승리 · 선공(WHITE)"
                null -> "동점 · 다시 뽑기"
            }
            delay(1100)
            flights.active.remove(p1Flight)
            flights.active.remove(p2Flight)
            introLabels = false
            val loser = round.loserCard
            coroutineScope {
                if (winner == null) {
                    launch { flights.fly(round.player1Card, p1Slot, anchors.deck, true, false, 450) }
                    launch { flights.fly(round.player2Card, p2Slot, anchors.deck, true, false, 450) }
                } else {
                    val loserSlot = if (winner == LocalPlayerSlot.PLAYER1) p2Slot else p1Slot
                    val winnerSlot = if (winner == LocalPlayerSlot.PLAYER1) p1Slot else p2Slot
                    launch {
                        flights.fly(loser, loserSlot, anchors.discard, true, true, 500)
                        shownDiscard = loser
                    }
                    launch { flights.fly(round.winnerCard, winnerSlot, anchors.deck, true, false, 500) }
                }
            }
        }
        introText = null
        introDone = true
        animateTurnDraw()
    }

    // 체스 시계: 현재 차례 플레이어의 시간만 흐른다. 턴 넘김 화면/이동 애니메이션 중에는 정지.
    val ticking = game.currentPlayer
    val clockRunning = result == null && !isMoveAnimating && !cardBusy && introDone
    LaunchedEffect(ticking, clockRunning) {
        if (!clockRunning) return@LaunchedEffect
        var last = SystemClock.elapsedRealtime()
        while (true) {
            delay(100)
            val now = SystemClock.elapsedRealtime()
            val elapsed = now - last
            last = now
            val left = if (ticking == Player.WHITE) {
                (whiteClockMs - elapsed).coerceAtLeast(0).also { whiteClockMs = it }
            } else {
                (blackClockMs - elapsed).coerceAtLeast(0).also { blackClockMs = it }
            }
            if (left == 0L) {
                game.timeOut(ticking)
                clearSelection()
                refresh()
                break
            }
        }
    }
    val currentClockMs = if (game.currentPlayer == Player.WHITE) whiteClockMs else blackClockMs
    val opponentClockMs = if (game.currentPlayer == Player.WHITE) blackClockMs else whiteClockMs

    BackHandler(onBack = onExit)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UnoChessColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .onGloballyPositioned { anchors.root = it.boundsInRoot() },
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
                clockMs = opponentClockMs,
                settings = settings,
            )

            GameGuide(
                result = result,
                canExchange = canExchange,
                selectedCard = selectedCard,
                activeFile = activeFile,
                endgame = result == null && game.isEndgame(),
                topDiscard = game.topDiscard(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
            AnimatedChessBoard(
                modifier = Modifier.weight(1f),
                game = game,
                revision = revision,
                highlightFiles = highlightFiles,
                selectedFrom = selectedFrom,
                legalMoves = legalMoves,
                settings = settings,
                enabled = result == null && !isMoveAnimating && !cardBusy && selectedCardId != null,
                moveAnimation = moveAnimation,
                onAnimationFinished = {
                    isMoveAnimating = false
                    moveAnimation = null
                    if (game.result == null) scope.launch { animateTurnDraw() }
                },
                onSquareClick = { square ->
                    if (result != null || selectedCardId == null || isMoveAnimating) {
                        return@AnimatedChessBoard
                    }

                    val destination = legalMoves.firstOrNull { it.to == square }
                    if (destination != null && selectedFrom != null) {
                        val movingPiece = game.board.pieceAt(destination.from) ?: return@AnimatedChessBoard
                        val usedFrom = anchors.hand[selectedCardId!!]
                        val play = game.playCardAndMove(
                            cardId = selectedCardId!!,
                            move = destination,
                            chosenFileForJoker = if (selectedCard?.let(game::actsAsJoker) == true) activeFile else null,
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
                        // 낸 카드는 손패 자리에서 버림 덱으로 날아간다(기물 이동과 동시에).
                        if (usedFrom != null) {
                            scope.launch {
                                flights.fly(play.usedCard, usedFrom, anchors.discard, true, true, 300)
                                shownDiscard = play.usedCard
                            }
                        } else {
                            shownDiscard = play.usedCard
                        }
                        // 다음 플레이어가 턴 시작에 뽑은 카드는 기물 이동이 끝난 뒤 덱에서 날아오도록 숨겨 둔다.
                        if (game.result == null) game.hand().lastOrNull()?.let { hiddenCardIds = hiddenCardIds + it.id }
                        clearSelection()
                        refresh()
                        return@AnimatedChessBoard
                    }

                    val piece = game.board.pieceAt(square)
                    // JOKER는 하이라이트된 아무 열의 기물을 바로 터치해 고른다.
                    if (
                        square.col in highlightFiles &&
                        piece?.player == game.currentPlayer &&
                        game.board.legalMovesFrom(square).isNotEmpty()
                    ) {
                        activeFile = square.col
                        selectedFrom = square
                    }
                },
            )
            DeckColumn(
                settings = settings,
                drawCount = game.drawPileCount(),
                // 덱 재구성으로 엔진 버림 덱이 비면 빈 슬롯으로 돌아간다.
                topDiscard = shownDiscard.takeIf { game.topDiscard() != null },
                onDeckPlaced = { anchors.deck = it },
                onDiscardPlaced = { anchors.discard = it },
            )
            }

            PlayerHandPanel(
                name = currentName,
                avatar = currentAvatar,
                player = game.currentPlayer,
                cards = hand,
                game = game,
                clockMs = currentClockMs,
                clockRunning = clockRunning,
                canExchange = canExchange,
                selectedCardId = selectedCardId,
                focusedCardId = focusedCardId,
                exchangeSelection = exchangeSelection,
                hiddenCardIds = hiddenCardIds,
                onCardPlaced = { id, rect -> anchors.hand[id] = rect },
                enabled = result == null && !isMoveAnimating && !cardBusy,
                onCardTapped = { card ->
                    if (result != null || isMoveAnimating || cardBusy) {
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
                    } else if (card.id == selectedCardId) {
                        clearSelection()
                    } else if (game.playableFiles(card).isNotEmpty()) {
                        selectedCardId = card.id
                        focusedCardId = card.id
                        selectedFrom = null
                        activeFile = if (game.actsAsJoker(card)) null else card.number!! - 1
                    }
                },
            )

            if (canExchange && result == null) {
                Button(
                    onClick = {
                        val ids = exchangeSelection.toList()
                        val cards = ids.mapNotNull { id -> hand.firstOrNull { it.id == id } }
                        scope.launch {
                            cardBusy = true
                            hiddenCardIds = hiddenCardIds + ids
                            coroutineScope {
                                cards.forEachIndexed { index, card ->
                                    val from = anchors.hand[card.id] ?: return@forEachIndexed
                                    launch {
                                        delay(index * 140L)
                                        flights.fly(card, from, anchors.discard, true, true, 360)
                                        shownDiscard = card
                                    }
                                }
                            }
                            // 새로 뽑는 1장(뒷면)이 덱에서 손패로 들어온다.
                            anchors.hand[ids.first()]?.let { slot ->
                                flights.fly(null, anchors.deck, slot, false, false, 380)
                            }
                            game.exchangeTwoCards(ids.toSet())
                            hiddenCardIds = emptySet()
                            clearSelection()
                            refresh()
                            cardBusy = false
                            if (game.result == null) animateTurnDraw()
                        }
                    },
                    enabled = exchangeSelection.size == 2 && !cardBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("선택한 카드 2장 교환", fontWeight = FontWeight.Black)
                }
            }
        }

        if (!introDone) {
            FirstTurnIntroOverlay(
                text = introText,
                player1Name = profile.nickname,
                player2Name = opponentName.ifBlank { "PLAYER 2" },
                player1Slot = if (introLabels) introSlot(player1 = true) else null,
                player2Slot = if (introLabels) introSlot(player1 = false) else null,
                origin = anchors.root.topLeft,
            )
        }

        CardFlightLayer(flights = flights.active, origin = anchors.root.topLeft, settings = settings)

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
    clockMs: Long,
    settings: LocalSettings,
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
                    repeat(5) { CardBackFace(settings) }
                }
            }
            ChessClock(clockMs, running = false, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun GameGuide(
    result: GameResult?,
    canExchange: Boolean,
    selectedCard: UnoCard?,
    activeFile: Int?,
    endgame: Boolean,
    topDiscard: UnoCard?,
) {
    val baseText = when (result) {
        is GameResult.Win -> "${result.reason} · 게임 종료"
        is GameResult.Draw -> "무승부 · ${result.reason}"
        null -> when {
            canExchange -> "바닥 카드와 맞으면서 움직일 수 있는 카드가 없습니다 · 버릴 카드 2장을 탭하세요"
            selectedCard == null && topDiscard != null && !topDiscard.isJoker ->
                "바닥 ${topDiscard.displayName()} · 같은 색 또는 같은 숫자, JOKER만 낼 수 있습니다"
            selectedCard == null -> "카드를 탭해 선택하세요 · 1~8은 A~H 열"
            activeFile == null -> "JOKER · 하이라이트된 열의 내 기물을 터치하세요"
            activeFile != null -> "${('A'.code + activeFile).toChar()}열 활성화 · 움직일 기물을 선택하세요"
            else -> "카드를 선택하세요"
        }
    }
    val text = if (endgame) "종반전 · 기물 4개 이하 · 모든 카드 JOKER\n$baseText" else baseText

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
    modifier: Modifier = Modifier,
    game: UnoChessGame,
    revision: Int,
    highlightFiles: Set<Int>,
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

        boardImage?.let { Image(it, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
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
                        // 사진 보드면 칸 색을 반투명하게 깔아 사진이 비치게 한다.
                        val base = (if ((row + col) % 2 == 0) palette.light else palette.dark)
                            .let { if (boardImage != null) it.copy(alpha = 0.45f) else it }
                        val fileLit = col in highlightFiles
                        val movablePiece = fileLit && piece?.player == game.currentPlayer &&
                            game.board.legalMovesFrom(square).isNotEmpty()

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
private fun PlayerHandPanel(
    name: String,
    avatar: String,
    player: Player,
    cards: List<UnoCard>,
    game: UnoChessGame,
    clockMs: Long,
    clockRunning: Boolean,
    canExchange: Boolean,
    selectedCardId: Int?,
    focusedCardId: Int?,
    exchangeSelection: Set<Int>,
    hiddenCardIds: Set<Int>,
    onCardPlaced: (Int, Rect) -> Unit,
    enabled: Boolean,
    onCardTapped: (UnoCard) -> Unit,
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
                    if (canExchange) "2장 탭 → 버리고 1장 드로우" else "탭해서 선택",
                    color = UnoChessColors.Muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
                ChessClock(clockMs, running = clockRunning, modifier = Modifier.padding(start = 8.dp))
            }

            FanCardHand(
                cards = cards,
                game = game,
                canExchange = canExchange,
                selectedCardId = selectedCardId,
                focusedCardId = focusedCardId,
                exchangeSelection = exchangeSelection,
                hiddenCardIds = hiddenCardIds,
                onCardPlaced = onCardPlaced,
                enabled = enabled,
                onCardTapped = onCardTapped,
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
    hiddenCardIds: Set<Int>,
    onCardPlaced: (Int, Rect) -> Unit,
    enabled: Boolean,
    onCardTapped: (UnoCard) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        val cardWidth = 76.dp
        val cardHeight = 108.dp
        val step = if (cards.size > 1) minOf(47.dp, (maxWidth - cardWidth) / (cards.size - 1)) else 47.dp
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

            FanCard(
                card = card,
                showAsJoker = game.actsAsJoker(card),
                focused = focused,
                committed = committed,
                enabled = enabled && playable,
                dimmed = !playable,
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
                    )
                    .onGloballyPositioned { onCardPlaced(card.id, it.boundsInRoot()) }
                    .alpha(if (card.id in hiddenCardIds) 0f else 1f),
                onTap = { onCardTapped(card) },
            )
        }
    }
}

@Composable
private fun FanCard(
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
private fun StyledUnoCard(
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
private fun DeckColumn(
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
private fun CardFlightLayer(flights: List<CardFlight>, origin: Offset, settings: LocalSettings) {
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
private fun FirstTurnIntroOverlay(
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
private fun ChessClock(ms: Long, running: Boolean, modifier: Modifier = Modifier) {
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
        is GameResult.Win -> result.reason
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
