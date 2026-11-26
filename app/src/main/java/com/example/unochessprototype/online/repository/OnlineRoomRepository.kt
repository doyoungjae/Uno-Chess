package com.example.unochessprototype.online.repository

import com.example.unochessprototype.game.Move
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineResultType
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.online.model.RoomStatus
import com.example.unochessprototype.online.rules.OnlineClock
import com.example.unochessprototype.online.rules.OnlineGameRules
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

class OnlineRoomRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val rootRef: DatabaseReference = database.reference
    private val roomsRef: DatabaseReference = rootRef.child("rooms")
    private val usersRef: DatabaseReference = rootRef.child("users")

    var serverOffsetMs: Long = 0L; private set
    private val offsetRef = rootRef.child(".info/serverTimeOffset")
    private val offsetListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) { serverOffsetMs = snapshot.getValue(Long::class.java) ?: 0L }
        override fun onCancelled(error: DatabaseError) = Unit
    }
    init { offsetRef.addValueEventListener(offsetListener) }
    fun serverNow(): Long = System.currentTimeMillis() + serverOffsetMs
    fun close() { offsetRef.removeEventListener(offsetListener) }
    fun checkClock(roomId: String, uid: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        updateGameRoom(roomId, { room ->
            require(room.isParticipant(uid))
            room
        }, onSuccess, onError, clockOnly = true, actorUid = uid)
    }

    fun observeWaitingRooms(
        localUid: String,
        onChanged: (List<OnlineRoom>) -> Unit,
        onError: (String) -> Unit,
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children
                    .mapNotNull { child -> child.getValue(OnlineRoom::class.java)?.copy(roomId = child.key.orEmpty()) }
                    .filter {
                        (it.status == RoomStatus.WAITING && it.guestUid.isBlank()) ||
                            (localUid.isNotBlank() && it.isParticipant(localUid) && it.status != RoomStatus.FINISHED)
                    }
                    .sortedByDescending { it.createdAt }
                onChanged(rooms)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        roomsRef.addValueEventListener(listener)
        return listener
    }

    fun removeWaitingRoomsListener(listener: ValueEventListener) {
        roomsRef.removeEventListener(listener)
    }

    fun observeRoom(
        roomId: String,
        onChanged: (OnlineRoom?) -> Unit,
        onError: (String) -> Unit,
    ): ValueEventListener {
        val ref = roomsRef.child(roomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    onChanged(snapshot.getValue(OnlineRoom::class.java)?.copy(roomId = roomId))
                } catch (error: com.google.firebase.database.DatabaseException) {
                    onError("방 데이터를 읽지 못했습니다: ${error.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        }
        ref.addValueEventListener(listener)
        return listener
    }

    fun removeRoomListener(roomId: String, listener: ValueEventListener) {
        roomsRef.child(roomId).removeEventListener(listener)
    }

    fun createRoom(
        profile: OnlineProfile,
        requestedName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (profile.uid.isBlank()) {
            onError("온라인 사용자 연결이 완료되지 않았습니다.")
            return
        }

        val ref = roomsRef.push()
        val roomId = ref.key
        if (roomId == null) {
            onError("방 ID를 생성하지 못했습니다.")
            return
        }

        val now = System.currentTimeMillis()
        val roomName = requestedName.trim().take(24).ifBlank { "${profile.nickname}의 방" }
        val room = OnlineRoom(
            roomId = roomId,
            roomName = roomName,
            ownerUid = profile.uid,
            ownerName = profile.nickname,
            ownerAvatar = profile.avatar,
            ownerReady = false,
            status = RoomStatus.WAITING,
            createdAt = now,
            updatedAt = now,
        )

        ref.setValue(room)
            .addOnSuccessListener {
                // 방장이 대기실에서 네트워크를 잃거나 앱이 종료되면 유령 방을 남기지 않는다.
                ref.onDisconnect().removeValue()
                onSuccess(roomId)
            }
            .addOnFailureListener { error ->
                onError(error.localizedMessage ?: "방 생성에 실패했습니다.")
            }
    }

    fun joinRoom(
        roomId: String,
        profile: OnlineProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        if (profile.uid.isBlank()) {
            onError("온라인 사용자 연결이 완료되지 않았습니다.")
            return
        }

        val ref = roomsRef.child(roomId)
        var reconnectAttempts = 0
        var abortReason = "방에 입장할 수 없습니다."
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(OnlineRoom::class.java) ?: run {
                    abortReason = "이미 삭제된 방입니다."
                    return Transaction.abort()
                }

                // A retry must also succeed if the first request reached the server
                // but its acknowledgement was lost. Never reset ready/game state.
                if (room.isParticipant(profile.uid)) {
                    return Transaction.success(currentData)
                }
                if (room.status != RoomStatus.WAITING || room.guestUid.isNotBlank()) {
                    abortReason = "이미 다른 사용자가 입장한 방입니다."
                    return Transaction.abort()
                }

                currentData.value = room.copy(
                    guestUid = profile.uid,
                    guestName = profile.nickname,
                    guestAvatar = profile.avatar,
                    guestReady = false,
                    status = RoomStatus.FULL,
                    updatedAt = System.currentTimeMillis(),
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    (error?.code == DatabaseError.DISCONNECTED || error?.code == DatabaseError.NETWORK_ERROR)
                        && reconnectAttempts++ == 0 -> {
                        ref.runTransaction(this, false)
                    }
                    error?.code == DatabaseError.DISCONNECTED || error?.code == DatabaseError.NETWORK_ERROR ->
                        onError("입장 확인 중 연결이 끊겼습니다. 연결이 복구되면 목록에서 내 방의 '다시 입장'을 눌러 주세요.")
                    error != null -> onError(error.message)
                    committed -> onSuccess()
                    else -> onError(abortReason)
                }
            }
        }, false)
    }

    fun leaveLobby(
        roomId: String,
        uid: String,
        onComplete: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val ref = roomsRef.child(roomId)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(OnlineRoom::class.java) ?: return Transaction.success(currentData)
                if (room.status == RoomStatus.PLAYING || room.status == RoomStatus.FINISHED) {
                    val game = room.game ?: return Transaction.abort()
                    if (!room.isParticipant(uid)) return Transaction.abort()
                    currentData.value = room.copy(
                        game = if (game.hasResult()) game else OnlineGameRules.forfeit(game, uid),
                        status = RoomStatus.FINISHED,
                        updatedAt = System.currentTimeMillis(),
                    )
                    return Transaction.success(currentData)
                }

                when (uid) {
                    room.ownerUid -> currentData.value = null
                    room.guestUid -> currentData.value = room.copy(
                        guestUid = "",
                        guestName = "",
                        guestAvatar = "♜",
                        ownerReady = false,
                        guestReady = false,
                        status = RoomStatus.WAITING,
                        updatedAt = System.currentTimeMillis(),
                    )
                    else -> return Transaction.abort()
                }
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) onError(error.message)
                else if (!committed) onError("방에서 나가지 못했습니다. 다시 시도해 주세요.")
                else {
                    currentData?.getValue(OnlineRoom::class.java)?.let { recordResultForUser(it, uid) }
                    onComplete()
                }
            }
        })
    }

    fun setReady(
        roomId: String,
        uid: String,
        ready: Boolean,
        onError: (String) -> Unit,
    ) {
        val ref = roomsRef.child(roomId)
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(OnlineRoom::class.java) ?: return Transaction.abort()
                if (room.status != RoomStatus.WAITING && room.status != RoomStatus.FULL) return Transaction.abort()

                val updated = when (uid) {
                    room.ownerUid -> room.copy(ownerReady = ready, updatedAt = System.currentTimeMillis())
                    room.guestUid -> room.copy(guestReady = ready, updatedAt = System.currentTimeMillis())
                    else -> return Transaction.abort()
                }
                currentData.value = updated
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) onError(error.message)
                else if (!committed) onError("준비 상태를 변경하지 못했습니다.")
            }
        })
    }

    fun startGame(
        roomId: String,
        ownerUid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val ref = roomsRef.child(roomId)
        var abortReason = "게임을 시작할 수 없습니다."
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(OnlineRoom::class.java) ?: return Transaction.abort()
                if (room.ownerUid != ownerUid) {
                    abortReason = "방장만 게임을 시작할 수 있습니다."
                    return Transaction.abort()
                }
                if (room.guestUid.isBlank()) {
                    abortReason = "상대방이 아직 입장하지 않았습니다."
                    return Transaction.abort()
                }
                if (!room.ownerReady || !room.guestReady) {
                    abortReason = "두 플레이어 모두 준비해야 합니다."
                    return Transaction.abort()
                }
                if (room.status != RoomStatus.FULL) {
                    abortReason = "이미 시작되었거나 종료된 방입니다."
                    return Transaction.abort()
                }

                val game = OnlineGameRules.createInitial(room.ownerUid, room.guestUid).let { initial ->
                    initial.copy(turnStartedAt = serverNow() + OnlineClock.INTRO_MS * initial.firstTurnRounds.size.coerceAtLeast(1))
                }
                currentData.value = room.copy(
                    status = RoomStatus.PLAYING,
                    game = game,
                    updatedAt = System.currentTimeMillis(),
                )
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> onError(error.message)
                    committed -> {
                        // 게임 시작 후에는 연결이 잠시 끊겨도 대기실용 자동 삭제를 실행하지 않는다.
                        ref.onDisconnect().cancel()
                        onSuccess()
                    }
                    else -> onError(abortReason)
                }
            }
        })
    }

    fun playMove(
        roomId: String,
        actorUid: String,
        cardId: Int,
        move: Move,
        jokerFile: Int?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateGameRoom(
            roomId = roomId,
            actorUid = actorUid,
            transform = { room ->
                val game = room.game ?: error("게임 상태가 없습니다.")
                val updatedGame = OnlineGameRules.playMove(game, actorUid, cardId, move, jokerFile)
                room.copy(
                    game = updatedGame,
                    status = if (updatedGame.hasResult()) RoomStatus.FINISHED else RoomStatus.PLAYING,
                    updatedAt = System.currentTimeMillis(),
                )
            },
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    fun exchangeCards(
        roomId: String,
        actorUid: String,
        cardIds: Set<Int>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateGameRoom(
            roomId = roomId,
            actorUid = actorUid,
            transform = { room ->
                val game = room.game ?: error("게임 상태가 없습니다.")
                val updatedGame = OnlineGameRules.exchangeTwoCards(game, actorUid, cardIds)
                room.copy(
                    game = updatedGame,
                    status = if (updatedGame.hasResult()) RoomStatus.FINISHED else RoomStatus.PLAYING,
                    updatedAt = System.currentTimeMillis(),
                )
            },
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    fun forfeit(
        roomId: String,
        actorUid: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateGameRoom(
            roomId = roomId,
            actorUid = actorUid,
            allowDuringTransition = true,
            transform = { room ->
                val game = room.game ?: error("게임 상태가 없습니다.")
                val updatedGame = if (game.hasResult()) game else OnlineGameRules.forfeit(game, actorUid)
                room.copy(
                    game = updatedGame,
                    status = RoomStatus.FINISHED,
                    updatedAt = System.currentTimeMillis(),
                )
            },
            onSuccess = onSuccess,
            onError = onError,
        )
    }

    fun recordResultForUser(
        room: OnlineRoom,
        uid: String,
        onError: (String) -> Unit = {},
    ) {
        val game = room.game ?: return
        if (!game.hasResult() || !room.isParticipant(uid)) return

        val userRef = usersRef.child(uid)
        userRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                if (currentData.child("processedRooms").child(room.roomId).getValue(Boolean::class.java) == true) {
                    return Transaction.abort()
                }

                val games = (currentData.child("onlineGames").getValue(Long::class.java) ?: 0L) + 1L
                val wins = currentData.child("onlineWins").getValue(Long::class.java) ?: 0L
                val losses = currentData.child("onlineLosses").getValue(Long::class.java) ?: 0L
                val draws = currentData.child("onlineDraws").getValue(Long::class.java) ?: 0L

                currentData.child("onlineGames").value = games
                when (game.resultType) {
                    OnlineResultType.WIN -> {
                        if (game.winnerUid == uid) currentData.child("onlineWins").value = wins + 1L
                        else currentData.child("onlineLosses").value = losses + 1L
                    }
                    OnlineResultType.DRAW -> currentData.child("onlineDraws").value = draws + 1L
                }
                currentData.child("processedRooms").child(room.roomId).value = true
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) onError(error.message)
            }
        })
    }

    private fun updateGameRoom(
        roomId: String,
        transform: (OnlineRoom) -> OnlineRoom,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        clockOnly: Boolean = false,
        actorUid: String,
        allowDuringTransition: Boolean = false,
    ) {
        val ref = roomsRef.child(roomId)
        var abortReason = "게임 상태를 변경하지 못했습니다."
        ref.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val room = currentData.getValue(OnlineRoom::class.java) ?: return Transaction.abort()
                if (room.status != RoomStatus.PLAYING && room.status != RoomStatus.FINISHED) {
                    abortReason = "아직 게임이 시작되지 않았습니다."
                    return Transaction.abort()
                }
                return try {
                    require(room.isParticipant(actorUid)) { "Not a room participant." }
                    val now = serverNow()
                    val original = room.game ?: error("게임 상태가 없습니다.")
                    if (original.hasResult()) return Transaction.success(currentData)
                    val settled = OnlineClock.settle(original, now)
                    if (!clockOnly && !allowDuringTransition && !settled.hasResult() && now < settled.turnStartedAt) {
                        error("선공 추첨 또는 턴 전환 중입니다. 잠시 후 다시 시도해 주세요.")
                    }
                    val timedRoom = room.copy(game = settled)
                    var updated = if (settled.hasResult()) timedRoom else transform(timedRoom)
                    val next = updated.game!!
                    if (!next.hasResult() && next.currentUid != original.currentUid) {
                        updated = updated.copy(game = next.copy(turnStartedAt = now + OnlineClock.TRANSITION_MS))
                    }
                    currentData.value = updated.copy(status = if (updated.game!!.hasResult()) RoomStatus.FINISHED else updated.status)
                    Transaction.success(currentData)
                } catch (error: Throwable) {
                    abortReason = error.message ?: "유효하지 않은 게임 동작입니다."
                    Transaction.abort()
                }
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> onError(error.message)
                    committed -> onSuccess()
                    else -> onError(abortReason)
                }
            }
        }, false)
    }
}
