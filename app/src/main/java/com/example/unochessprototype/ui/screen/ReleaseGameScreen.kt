package com.example.unochessprototype.ui.screen

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.GameResult
import com.example.unochessprototype.game.LocalPlayerSlot
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.game.UnoChessGame
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.ui.theme.CardBackFace
import com.example.unochessprototype.ui.theme.UnoChessColors
import kotlin.math.abs
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val CLOCK_START_MS = 10 * 60 * 1000L

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
    val chessAudio = rememberChessAudio(settings)
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
        if (result != null) {
            chessAudio.playEnd()
            onGameFinished(result)
        }
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
        chessAudio.playCardDraw()
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
                launch {
                    chessAudio.playCardDraw()
                    p1Flight = flights.fly(round.player1Card, anchors.deck, p1Slot, false, true, 650, keep = true)
                }
                launch {
                    delay(200)
                    chessAudio.playCardDraw()
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
                        chessAudio.playMove(captured = play.moveOutcome.capturedPiece != null)
                                if (game.result == null && game.board.isKingInCheck(game.currentPlayer)) {
                            chessAudio.playCheck()
                        }

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
                                chessAudio.playCardDraw()
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
            else -> "${('A'.code + activeFile).toChar()}열 활성화 · 움직일 기물을 선택하세요"
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
    SharedChessBoard(modifier, game.board, game.currentPlayer, false, highlightFiles, selectedFrom, legalMoves, settings, enabled, moveAnimation, onAnimationFinished, onSquareClick)
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
