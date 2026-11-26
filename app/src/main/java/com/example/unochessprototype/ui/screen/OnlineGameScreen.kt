package com.example.unochessprototype.ui.screen

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Handshake
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.unochessprototype.data.LocalSettings
import com.example.unochessprototype.game.CardColor
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.game.Player
import com.example.unochessprototype.game.Square
import com.example.unochessprototype.game.UnoCard
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineResultType
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.online.rules.OnlineClock
import com.example.unochessprototype.online.rules.OnlineGameRules
import com.example.unochessprototype.ui.theme.UnoChessColors
import kotlinx.coroutines.delay

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
    serverNow: () -> Long,
    onClockExpired: () -> Unit,
) {
            val chessAudio = rememberChessAudio(settings)
            val game = room?.game

    if (game.version != OnlineGameRules.VERSION) {
        BackHandler { if (!busy) onForfeit() }
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("이전 규칙으로 시작한 경기입니다. 방에서 나가 새 방을 만들어 주세요.")
            TextButton(onClick = onForfeit, enabled = !busy) { Text("방 나가기") }
        }
        return
    }

    var clockNow by remember { mutableLongStateOf(serverNow()) }
    var showLogs by remember { mutableStateOf(false) }
    val currentTimeoutCallback by rememberUpdatedState(onClockExpired)
    LaunchedEffect(room.roomId, game) {
        while (!game.hasResult()) {
            clockNow = serverNow()
            val expired = OnlineClock.remaining(game, true, clockNow) == 0L ||
                OnlineClock.remaining(game, false, clockNow) == 0L
            if (expired || game.turnStartedAt == 0L) currentTimeoutCallback()
            delay(if (expired) 1500 else 100)
        }
    }
    val clockAllowsMove = game.turnStartedAt > 0 && clockNow >= game.turnStartedAt &&
        OnlineClock.remaining(game, game.currentUid == game.whiteUid, clockNow) > 0
    var selectedCardId by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var focusedCardId by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var activeFile by remember(game.currentUid, game.completedTurns) { mutableStateOf<Int?>(null) }
    var selectedFrom by remember(game.currentUid, game.completedTurns) { mutableStateOf<Square?>(null) }
    var exchangeSelection by remember(game.currentUid, game.completedTurns) { mutableStateOf<Set<Int>>(emptySet()) }
    var showFirstTurn by rememberSaveable(room.roomId) { mutableStateOf(game.completedTurns == 0 && !game.hasResult() && serverNow() < game.turnStartedAt) }
    var showExitDialog by remember { mutableStateOf(false) }

    val flights = remember { CardFlights() }
    val anchors = remember { CardAnchors() }
    var previous by remember(room.roomId) { mutableStateOf(game) }
    var moveAnimation by remember { mutableStateOf<BoardMoveAnimation?>(null) }
    var animating by remember { mutableStateOf(false) }
    var hidden by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val discard = game.lastDiscardId.takeIf { it >= 0 }?.let(OnlineGameRules::card)
    var introText by remember { mutableStateOf("선공 결정 · 덱에서 1장씩 뽑기") }
    var leftSlot by remember { mutableStateOf<Rect?>(null) }
    var rightSlot by remember { mutableStateOf<Rect?>(null) }
    LaunchedEffect(room.roomId) {
        if (!showFirstTurn) return@LaunchedEffect
        withFrameNanos { }; withFrameNanos { }
        val width = anchors.root.width * 0.22f
        val height = width * 1.45f
        val center = anchors.root.center
        val left = Rect(Offset(center.x - width - 12f, center.y - height / 2), Size(width, height))
        val right = Rect(Offset(center.x + 12f, center.y - height / 2), Size(width, height))
        leftSlot = left; rightSlot = right
        try {
            for (round in game.firstTurnRounds) {
                val owner = OnlineGameRules.card(round.ownerCardId)
                val guest = OnlineGameRules.card(round.guestCardId)
                val decision = com.example.unochessprototype.game.FirstTurnRound(owner, guest)
                val first = flights.fly(owner, anchors.deck, left, false, true, 650, keep = true)
                chessAudio.playCardDraw()
                val first = flights.fly(owner, anchors.deck, left, false, true, 650, keep = true)
                chessAudio.playCardDraw()
                val second = flights.fly(guest, anchors.deck, right, false, true, 650, keep = true)
                introText = if (decision.winner == null) "동점 · 다시 뽑기"
                    else "${room.displayNameFor(game.whiteUid)} · WHITE 선공"
                delay(1100)
                flights.active.remove(first); flights.active.remove(second)
                flights.fly(owner, left, if (decision.loserCard == owner) anchors.discard else anchors.deck,
                    true, decision.loserCard == owner, 400)
                flights.fly(guest, right, if (decision.loserCard == guest) anchors.discard else anchors.deck,
                    true, decision.loserCard == guest, 400)
            }
        } finally { showFirstTurn = false; flights.active.clear() }
    }
    LaunchedEffect(game) {
        val old = previous
        previous = game
        val before = OnlineGameRules.boardFromState(old)
        val after = OnlineGameRules.boardFromState(game)
        val squares = (0..7).flatMap { row -> (0..7).map { col -> Square(row, col) } }
        val from = squares.firstOrNull { before.pieceAt(it) != null && after.pieceAt(it) == null }
        val to = squares.firstOrNull { after.pieceAt(it) != null && before.pieceAt(it) != after.pieceAt(it) }
        if (from != null && to != null) {
            moveAnimation = BoardMoveAnimation(game.completedTurns, from, to, after.pieceAt(to)!!)
            chessAudio.playMove(captured = before.pieceAt(to) != null)
        }
        animating = true
        try {
            val removed = old.handFor(profile.uid).filter { it !in game.handFor(profile.uid) }
            for (id in removed) {
                val card = OnlineGameRules.card(id)
                anchors.hand[id]?.let { flights.fly(card, it, anchors.discard, true, true) }
            }
            val added = game.handFor(profile.uid).filter { it !in old.handFor(profile.uid) }
            hidden = added.toSet()
            withFrameNanos { }; withFrameNanos { }
            for (id in added) anchors.hand[id]?.let { flights.fly(OnlineGameRules.card(id), anchors.deck, it, false, true, 420) }
            for (id in added) {
                chessAudio.playCardDraw()
                anchors.hand[id]?.let { flights.fly(OnlineGameRules.card(id), anchors.deck, it, false, true, 420) }
            }
        } finally { hidden = emptySet(); animating = false }
    }

    val isMyTurn = game.currentUid == profile.uid && !game.hasResult() && clockAllowsMove
    val myHand = game.handFor(profile.uid)
    val mySide = runCatching { OnlineGameRules.sideFor(game, profile.uid) }.getOrNull()
    val board = remember(game.board) { OnlineGameRules.boardFromState(game) }
    val selectedCard = selectedCardId?.let(OnlineGameRules::card)
    val highlightFiles = selectedCardId?.let {
        OnlineGameRules.playableFiles(game, profile.uid, it)
    } ?: emptySet()
    val canExchange = OnlineGameRules.canExchange(game, profile.uid)
    val legalMoves = if (selectedCardId != null && activeFile != null && selectedFrom != null) {
        OnlineGameRules.legalMovesForCard(
            state = game,
            uid = profile.uid,
            cardId = selectedCardId!!,
            chosenFileForJoker = if (selectedCard != null && OnlineGameRules.actsAsJoker(game, profile.uid, selectedCard.id)) activeFile else null,
        ).filter { it.from == selectedFrom }
    } else emptyList()

    val opponentUid = room.otherUid(profile.uid)
    val opponentName = room.displayNameFor(opponentUid)
    val opponentAvatar = room.avatarFor(opponentUid)

    BackHandler {
        if (showLogs) showLogs = false else if (!busy) {
            if (game.hasResult()) onExitFinished() else showExitDialog = true
        }
    }

    Box(Modifier.fillMaxSize().onGloballyPositioned { anchors.root = it.boundsInRoot() }) {
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
            TextButton(onClick = { showLogs = true }) { Text("기록") }
            TextButton(enabled = !busy, onClick = { if (game.hasResult()) onExitFinished() else showExitDialog = true }) {
                Icon(Icons.Rounded.Close, contentDescription = null)
                Text("나가기")
            }
        }

        PlayerLine(
            avatar = opponentAvatar,
            name = opponentName,
            subtitle = if (opponentUid == game.whiteUid) "WHITE · 상대" else "BLACK · 상대",
            active = !game.hasResult() && game.currentUid == opponentUid && clockAllowsMove,
            clockMs = OnlineClock.remaining(game, opponentUid == game.whiteUid, clockNow),
        )

        if (errorMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                Text(errorMessage, modifier = Modifier.padding(8.dp), color = UnoChessColors.Warning, fontSize = 10.sp)
            }
        }

        Text(
            when {
                game.hasResult() -> resultText(game.resultType, game.winnerUid, profile.uid, game.resultReason)
                isMyTurn && canExchange -> "바닥 카드와 맞는 사용 가능한 카드가 없습니다. 카드 2장을 선택해 교환하세요."
                isMyTurn && selectedCardId == null -> "내 차례 · 바닥 카드와 같은 색/숫자 또는 JOKER를 선택하세요."
                isMyTurn && selectedCardId != null && activeFile == null -> "JOKER · 하이라이트된 열의 내 기물을 터치하세요"
                isMyTurn && activeFile != null && selectedFrom == null -> "${(65 + activeFile!!).toChar()}열에서 움직일 기물을 선택하세요."
                isMyTurn -> "이동할 칸을 선택하세요."
                !clockAllowsMove -> "선공 추첨 또는 턴 전환 중..."
                else -> "상대방의 턴을 기다리는 중..."
            },
            color = if (isMyTurn) UnoChessColors.Lime else UnoChessColors.Muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DeckColumn(settings, game.drawPile.size, discard, { anchors.deck = it }, { anchors.discard = it })
        SharedChessBoard(
            modifier = Modifier.weight(1f),
            chessBoard = board,
            currentPlayer = mySide ?: Player.WHITE,
            flipped = mySide == Player.BLACK,
            moveAnimation = moveAnimation,
            onAnimationFinished = { moveAnimation = null },
            highlightFiles = highlightFiles,
            selectedFrom = selectedFrom,
            legalMoves = legalMoves,
            settings = settings,
            enabled = isMyTurn && !busy && !animating && !showFirstTurn && selectedCardId != null,
            onSquareClick = { square ->
                if (!isMyTurn || busy || selectedCardId == null) return@SharedChessBoard

                val destination = legalMoves.firstOrNull { it.to == square }
                if (destination != null && selectedFrom != null) {
                    onMove(
                        selectedCardId!!,
                        destination,
                        if (selectedCard != null && OnlineGameRules.actsAsJoker(game, profile.uid, selectedCard.id)) activeFile else null,
                    )
                    selectedCardId = null
                    focusedCardId = null
                    activeFile = null
                    selectedFrom = null
                    return@SharedChessBoard
=======
            LaunchedEffect(game?.resultType, game?.winnerUid, game?.resultReason) {
                if (game?.hasResult() == true) chessAudio.playEnd()
            }
            LaunchedEffect(game?.completedTurns, game?.board) {
                if (game != null && game.completedTurns > 0 && !game.hasResult()) {
                    val currentBoard = OnlineGameRules.boardFromState(game)
                    val sideToMove = OnlineGameRules.sideFor(game, game.currentUid)
                    if (currentBoard.isKingInCheck(sideToMove)) chessAudio.playCheck()
>>>>>>> origin/feat-victory-defeat
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
                    TextButton(
                        onClick = onForfeit,
                        enabled = !busy
                    ) { Text("뒤로 · 방 나가기 (진행 중이면 기권)") }
                }
                return
            }

            if (game.version != OnlineGameRules.VERSION) {
                BackHandler { if (!busy) onForfeit() }
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("이전 규칙으로 시작한 경기입니다. 방에서 나가 새 방을 만들어 주세요.")
                    TextButton(onClick = onForfeit, enabled = !busy) { Text("방 나가기") }
                }
                return
            }

            var clockNow by remember { mutableLongStateOf(serverNow()) }
            var showLogs by remember { mutableStateOf(false) }
            val currentTimeoutCallback by rememberUpdatedState(onClockExpired)
            LaunchedEffect(room.roomId, game) {
                while (!game.hasResult()) {
                    clockNow = serverNow()
                    val expired = OnlineClock.remaining(game, true, clockNow) == 0L ||
                            OnlineClock.remaining(game, false, clockNow) == 0L
                    if (expired || game.turnStartedAt == 0L) currentTimeoutCallback()
                    delay(if (expired) 1500 else 100)
                }
            }
            val clockAllowsMove = game.turnStartedAt > 0 && clockNow >= game.turnStartedAt &&
                    OnlineClock.remaining(game, game.currentUid == game.whiteUid, clockNow) > 0
            var selectedCardId by remember(
                game.currentUid,
                game.completedTurns
            ) { mutableStateOf<Int?>(null) }
            var focusedCardId by remember(
                game.currentUid,
                game.completedTurns
            ) { mutableStateOf<Int?>(null) }
            var activeFile by remember(game.currentUid, game.completedTurns) {
                mutableStateOf<Int?>(
                    null
                )
            }
            var selectedFrom by remember(game.currentUid, game.completedTurns) {
                mutableStateOf<Square?>(
                    null
                )
            }
            var exchangeSelection by remember(
                game.currentUid,
                game.completedTurns
            ) { mutableStateOf<Set<Int>>(emptySet()) }
            var showFirstTurn by rememberSaveable(room.roomId) { mutableStateOf(game.completedTurns == 0 && !game.hasResult() && serverNow() < game.turnStartedAt) }
            var showExitDialog by remember { mutableStateOf(false) }

            val flights = remember { CardFlights() }
            val anchors = remember { CardAnchors() }
            var previous by remember(room.roomId) { mutableStateOf(game) }
            var moveAnimation by remember { mutableStateOf<BoardMoveAnimation?>(null) }
            var animating by remember { mutableStateOf(false) }
            var hidden by remember { mutableStateOf<Set<Int>>(emptySet()) }
            val discard = game.lastDiscardId.takeIf { it >= 0 }?.let(OnlineGameRules::card)
            var introText by remember { mutableStateOf("선공 결정 · 덱에서 1장씩 뽑기") }
            var leftSlot by remember { mutableStateOf<Rect?>(null) }
            var rightSlot by remember { mutableStateOf<Rect?>(null) }
            LaunchedEffect(room.roomId) {
                if (!showFirstTurn) return@LaunchedEffect
                withFrameNanos { }; withFrameNanos { }
                val width = anchors.root.width * 0.22f
                val height = width * 1.45f
                val center = anchors.root.center
                val left =
                    Rect(Offset(center.x - width - 12f, center.y - height / 2), Size(width, height))
                val right = Rect(Offset(center.x + 12f, center.y - height / 2), Size(width, height))
                leftSlot = left; rightSlot = right
                try {
                    for (round in game.firstTurnRounds) {
                        val owner = OnlineGameRules.card(round.ownerCardId)
                        val guest = OnlineGameRules.card(round.guestCardId)
                        val decision =
                            com.example.unochessprototype.game.FirstTurnRound(owner, guest)
                        val first =
                            flights.fly(owner, anchors.deck, left, false, true, 650, keep = true)
                        val second =
                            flights.fly(guest, anchors.deck, right, false, true, 650, keep = true)
                        introText = if (decision.winner == null) "동점 · 다시 뽑기"
                        else "${room.displayNameFor(game.whiteUid)} · WHITE 선공"
                        delay(1100)
                        flights.active.remove(first); flights.active.remove(second)
                        flights.fly(
                            owner,
                            left,
                            if (decision.loserCard == owner) anchors.discard else anchors.deck,
                            true,
                            decision.loserCard == owner,
                            400
                        )
                        flights.fly(
                            guest,
                            right,
                            if (decision.loserCard == guest) anchors.discard else anchors.deck,
                            true,
                            decision.loserCard == guest,
                            400
                        )
                    }
                } finally {
                    showFirstTurn = false; flights.active.clear()
                }
            }
            LaunchedEffect(game) {
                val old = previous
                previous = game
                val before = OnlineGameRules.boardFromState(old)
                val after = OnlineGameRules.boardFromState(game)
                val squares = (0..7).flatMap { row -> (0..7).map { col -> Square(row, col) } }
                val from =
                    squares.firstOrNull { before.pieceAt(it) != null && after.pieceAt(it) == null }
                val to = squares.firstOrNull {
                    after.pieceAt(it) != null && before.pieceAt(it) != after.pieceAt(it)
                }
                if (from != null && to != null) {
                    moveAnimation =
                        BoardMoveAnimation(game.completedTurns, from, to, after.pieceAt(to)!!)
                    chessAudio.playMove(captured = before.pieceAt(to) != null)
                }
                animating = true
                try {
                    val removed =
                        old.handFor(profile.uid).filter { it !in game.handFor(profile.uid) }
                    for (id in removed) {
                        val card = OnlineGameRules.card(id)
                        anchors.hand[id]?.let { flights.fly(card, it, anchors.discard, true, true) }
                    }
                    val added = game.handFor(profile.uid).filter { it !in old.handFor(profile.uid) }
                    hidden = added.toSet()
                    withFrameNanos { }; withFrameNanos { }
                    for (id in added) anchors.hand[id]?.let {
                        flights.fly(
                            OnlineGameRules.card(id),
                            anchors.deck,
                            it,
                            false,
                            true,
                            420
                        )
                    }
                } finally {
                    hidden = emptySet(); animating = false
                }
            }

        ) {
            val step = if (myHand.size > 1) minOf(47.dp, (maxWidth - 76.dp) / (myHand.size - 1)) else 0.dp
            val start = (maxWidth - 76.dp - step * (myHand.size - 1).coerceAtLeast(0)) / 2
            myHand.forEachIndexed { index, id ->
                val card = OnlineGameRules.card(id)
                val selected = if (canExchange) id in exchangeSelection else id == selectedCardId
                FanCard(
                    card = card,
                    focused = focusedCardId == id,
                    committed = selected,
                    showAsJoker = OnlineGameRules.actsAsJoker(game, profile.uid, id),
                    dimmed = !canExchange && OnlineGameRules.playableFiles(game, profile.uid, id).isEmpty(),
                    enabled = isMyTurn && !busy && !animating && !showFirstTurn,
                    modifier = Modifier.offset(x = start + step * index, y = (kotlin.math.abs(index - (myHand.size - 1) / 2f) * 6.5f).dp)
                        .size(76.dp, 108.dp).zIndex(if (selected) 100f else index.toFloat())
                        .graphicsLayer(rotationZ = (index - (myHand.size - 1) / 2f) * 7.5f)
                        .onGloballyPositioned { anchors.hand[id] = it.boundsInRoot() }.alpha(if (id in hidden) 0f else 1f),
                    onTap = {
                        if (!isMyTurn || busy) return@FanCard
                        if (canExchange) {
                            focusedCardId = id
                            exchangeSelection = when {
                                id in exchangeSelection -> exchangeSelection - id
                                exchangeSelection.size < 2 -> exchangeSelection + id
                                else -> exchangeSelection
                            }
                        } else if (id == selectedCardId) {
                            selectedCardId = null
                            focusedCardId = null
                            activeFile = null
                            selectedFrom = null
                        } else {
                            val files = OnlineGameRules.playableFiles(game, profile.uid, id)
                            if (files.isNotEmpty()) {
                                selectedCardId = id
                                focusedCardId = id
                                selectedFrom = null
                                if (OnlineGameRules.actsAsJoker(game, profile.uid, id)) {
                                    activeFile = null
                                } else {
                                    activeFile = OnlineGameRules.fileForNumber(game, profile.uid, card.number!!)
                                }
                            }
=======
            val isMyTurn = game.currentUid == profile.uid && !game.hasResult() && clockAllowsMove
            val myHand = game.handFor(profile.uid)
            val mySide = runCatching { OnlineGameRules.sideFor(game, profile.uid) }.getOrNull()
            val board = remember(game.board) { OnlineGameRules.boardFromState(game) }
            val selectedCard = selectedCardId?.let(OnlineGameRules::card)
            val highlightFiles = selectedCardId?.let {
                OnlineGameRules.playableFiles(game, profile.uid, it)
            } ?: emptySet()
            val canExchange = OnlineGameRules.canExchange(game, profile.uid)
            val legalMoves =
                if (selectedCardId != null && activeFile != null && selectedFrom != null) {
                    OnlineGameRules.legalMovesForCard(
                        state = game,
                        uid = profile.uid,
                        cardId = selectedCardId!!,
                        chosenFileForJoker = if (selectedCard != null && OnlineGameRules.actsAsJoker(
                                game,
                                profile.uid,
                                selectedCard.id
                            )
                        ) activeFile else null,
                    ).filter { it.from == selectedFrom }
                } else emptyList()

            val opponentUid = room.otherUid(profile.uid)
            val opponentName = room.displayNameFor(opponentUid)
            val opponentAvatar = room.avatarFor(opponentUid)

            BackHandler {
                if (showLogs) showLogs = false else if (!busy) {
                    if (game.hasResult()) onExitFinished() else showExitDialog = true
                }
            }

            Box(Modifier.fillMaxSize().onGloballyPositioned { anchors.root = it.boundsInRoot() }) {
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
                            Text(
                                "ONLINE MATCH",
                                color = UnoChessColors.Lime,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                            Text(
                                "TURN ${game.completedTurns + 1}",
                                color = UnoChessColors.Muted,
                                fontSize = 10.sp
                            )
>>>>>>> origin/feat-victory-defeat
                        }
                        TextButton(onClick = { showLogs = true }) { Text("기록") }
                        TextButton(
                            enabled = !busy,
                            onClick = {
                                if (game.hasResult()) onExitFinished() else showExitDialog = true
                            }) {
                            Icon(Icons.Rounded.Close, contentDescription = null)
                            Text("나가기")
                        }
                    }

                    PlayerLine(
                        avatar = opponentAvatar,
                        name = opponentName,
                        subtitle = if (opponentUid == game.whiteUid) "WHITE · 상대" else "BLACK · 상대",
                        active = !game.hasResult() && game.currentUid == opponentUid && clockAllowsMove,
                        clockMs = OnlineClock.remaining(
                            game,
                            opponentUid == game.whiteUid,
                            clockNow
                        ),
                    )

                    if (errorMessage != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = UnoChessColors.SurfaceRaised)) {
                            Text(
                                errorMessage,
                                modifier = Modifier.padding(8.dp),
                                color = UnoChessColors.Warning,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Text(
                        when {
                            game.hasResult() -> resultText(
                                game.resultType,
                                game.winnerUid,
                                profile.uid,
                                game.resultReason
                            )

                            isMyTurn && canExchange -> "바닥 카드와 맞는 사용 가능한 카드가 없습니다. 카드 2장을 선택해 교환하세요."
                            isMyTurn && selectedCardId == null -> "내 차례 · 바닥 카드와 같은 색/숫자 또는 JOKER를 선택하세요."
                            isMyTurn && selectedCardId != null && activeFile == null -> "JOKER · 하이라이트된 열의 내 기물을 터치하세요"
                            isMyTurn && activeFile != null && selectedFrom == null -> "${(65 + activeFile!!).toChar()}열에서 움직일 기물을 선택하세요."
                            isMyTurn -> "이동할 칸을 선택하세요."
                            !clockAllowsMove -> "선공 추첨 또는 턴 전환 중..."
                            else -> "상대방의 턴을 기다리는 중..."
                        },
                        color = if (isMyTurn) UnoChessColors.Lime else UnoChessColors.Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeckColumn(
                            settings,
                            game.drawPile.size,
                            discard,
                            { anchors.deck = it },
                            { anchors.discard = it })
                        SharedChessBoard(
                            modifier = Modifier.weight(1f),
                            chessBoard = board,
                            currentPlayer = mySide ?: Player.WHITE,
                            flipped = mySide == Player.BLACK,
                            moveAnimation = moveAnimation,
                            onAnimationFinished = { moveAnimation = null },
                            highlightFiles = highlightFiles,
                            selectedFrom = selectedFrom,
                            legalMoves = legalMoves,
                            settings = settings,
                            enabled = isMyTurn && !busy && !animating && !showFirstTurn && selectedCardId != null,
                            onSquareClick = { square ->
                                if (!isMyTurn || busy || selectedCardId == null) return@SharedChessBoard

                                val destination = legalMoves.firstOrNull { it.to == square }
                                if (destination != null && selectedFrom != null) {
                                    onMove(
                                        selectedCardId!!,
                                        destination,
                                        if (selectedCard != null && OnlineGameRules.actsAsJoker(
                                                game,
                                                profile.uid,
                                                selectedCard.id
                                            )
                                        ) activeFile else null,
                                    )
                                    selectedCardId = null
                                    focusedCardId = null
                                    activeFile = null
                                    selectedFrom = null
                                    return@SharedChessBoard
                                }

                                val piece = board.pieceAt(square)
                                val side = mySide
                                if (
                                    side != null &&
                                    square.col in highlightFiles &&
                                    piece?.player == side &&
                                    board.legalMovesFrom(square).isNotEmpty()
                                ) {
                                    activeFile = square.col
                                    selectedFrom = square
                                }
                            },
                        )

                    }

                    PlayerLine(
                        avatar = profile.avatar,
                        name = profile.nickname,
                        subtitle = if (profile.uid == game.whiteUid) "WHITE · 나" else "BLACK · 나",
                        active = isMyTurn,
                        clockMs = OnlineClock.remaining(
                            game,
                            profile.uid == game.whiteUid,
                            clockNow
                        ),
                    )

                    BoxWithConstraints(
                        modifier = Modifier.fillMaxWidth().height(150.dp),

                        ) {
                        val step = if (myHand.size > 1) minOf(
                            47.dp,
                            (maxWidth - 76.dp) / (myHand.size - 1)
                        ) else 0.dp
                        val start =
                            (maxWidth - 76.dp - step * (myHand.size - 1).coerceAtLeast(0)) / 2
                        myHand.forEachIndexed { index, id ->
                            val card = OnlineGameRules.card(id)
                            val selected =
                                if (canExchange) id in exchangeSelection else id == selectedCardId
                            FanCard(
                                card = card,
                                focused = focusedCardId == id,
                                committed = selected,
                                showAsJoker = OnlineGameRules.actsAsJoker(game, profile.uid, id),
                                dimmed = !canExchange && OnlineGameRules.playableFiles(
                                    game,
                                    profile.uid,
                                    id
                                ).isEmpty(),
                                enabled = isMyTurn && !busy && !animating && !showFirstTurn,
                                modifier = Modifier.offset(
                                    x = start + step * index,
                                    y = (kotlin.math.abs(index - (myHand.size - 1) / 2f) * 6.5f).dp
                                )
                                    .size(76.dp, 108.dp)
                                    .zIndex(if (selected) 100f else index.toFloat())
                                    .graphicsLayer(rotationZ = (index - (myHand.size - 1) / 2f) * 7.5f)
                                    .onGloballyPositioned { anchors.hand[id] = it.boundsInRoot() }
                                    .alpha(if (id in hidden) 0f else 1f),
                                onTap = {
                                    if (!isMyTurn || busy) return@FanCard
                                    if (canExchange) {
                                        focusedCardId = id
                                        exchangeSelection = when {
                                            id in exchangeSelection -> exchangeSelection - id
                                            exchangeSelection.size < 2 -> exchangeSelection + id
                                            else -> exchangeSelection
                                        }
                                    } else if (id == selectedCardId) {
                                        selectedCardId = null
                                        focusedCardId = null
                                        activeFile = null
                                        selectedFrom = null
                                    } else {
                                        val files =
                                            OnlineGameRules.playableFiles(game, profile.uid, id)
                                        if (files.isNotEmpty()) {
                                            selectedCardId = id
                                            focusedCardId = id
                                            selectedFrom = null
                                            if (OnlineGameRules.actsAsJoker(
                                                    game,
                                                    profile.uid,
                                                    id
                                                )
                                            ) {
                                                activeFile = null
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
                            enabled = isMyTurn && !busy && !animating && !showFirstTurn && exchangeSelection.size == 2,
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

                if (showFirstTurn) FirstTurnIntroOverlay(
                    introText,
                    room.ownerName,
                    room.guestName,
                    leftSlot,
                    rightSlot,
                    anchors.root.topLeft
                )
                CardFlightLayer(flights.active, anchors.root.topLeft, settings)
                if (showLogs) GameLogOverlay(game.logs) { showLogs = false }
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
                        TextButton(
                            enabled = !busy,
                            onClick = { showExitDialog = false }) { Text("계속하기") }
                    },
                )
            }


            if (game.hasResult()) {
                val isDraw = game.resultType == OnlineResultType.DRAW
                val isWinner = game.winnerUid == profile.uid

                when {
                    isDraw -> {
                        AlertDialog(
                            onDismissRequest = {
                                if (!busy) onExitFinished()
                            },
                            title = {
                                Text(
                                    "무승부입니다",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            text = {
                                Text(
                                    game.resultReason.ifBlank {
                                        "승자 없이 경기가 종료되었습니다."
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = onExitFinished,
                                    enabled = !busy
                                ) {
                                    Text("돌아가기")
                                }
                            }
                        )
                    }

                    isWinner -> {
                        VictoryScreen(
                            onReplay = onExitFinished
                        )
                    }

                    else -> {
                        DefeatScreen(
                            onReplay = onExitFinished
                        )
                    }
                }
            }
        }

        @Composable
        private fun PlayerLine(
            avatar: String,
            name: String,
            subtitle: String,
            active: Boolean,
            clockMs: Long
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = if (active) UnoChessColors.SurfaceRaised else UnoChessColors.Surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(36.dp)
                            .background(UnoChessColors.SurfaceSoft, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(avatar, fontSize = 21.sp) }
                    Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
                        Text(
                            name,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(subtitle, color = UnoChessColors.Muted, fontSize = 9.sp)
                    }
                    Row(
                        modifier = Modifier.padding(start = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChessClock(clockMs, active)
                        if (active) Text(
                            "TURN", color = UnoChessColors.Lime, fontWeight = FontWeight.Black,
                            fontSize = 10.sp, maxLines = 1
                        )
                    }
                }
            }
        }

        private fun resultText(
            type: String,
            winnerUid: String,
            myUid: String,
            reason: String
        ): String = when (type) {
            OnlineResultType.DRAW -> "무승부 · $reason"
            OnlineResultType.WIN -> if (winnerUid == myUid) "승리 · $reason" else "패배 · $reason"
            else -> ""
        }

