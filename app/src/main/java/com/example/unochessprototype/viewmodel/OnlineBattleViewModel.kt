package com.example.unochessprototype.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unochessprototype.game.Move
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.model.OnlineRoom
import com.example.unochessprototype.online.repository.OnlineRoomRepository
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnlineBattleUiState(
    val rooms: List<OnlineRoom> = emptyList(),
    val currentRoom: OnlineRoom? = null,
    val loadingRooms: Boolean = true,
    val busy: Boolean = false,
    val errorMessage: String? = null,
)

class OnlineBattleViewModel : ViewModel() {
    private val repository = OnlineRoomRepository()

    var uiState by mutableStateOf(OnlineBattleUiState())
        private set

    private var roomsListener: ValueEventListener? = null
    private var roomListener: ValueEventListener? = null
    private var observedRoomId: String? = null
    private var localUidForObservedRoom: String? = null

    private var roomLoadTimeout: Job? = null

    fun observeWaitingRooms(localUid: String) {
        roomsListener?.let(repository::removeWaitingRoomsListener)
        uiState = uiState.copy(loadingRooms = true, errorMessage = null)
        roomsListener = repository.observeWaitingRooms(
            localUid = localUid,
            onChanged = { rooms ->
                uiState = uiState.copy(rooms = rooms, loadingRooms = false)
            },
            onError = { message ->
                uiState = uiState.copy(loadingRooms = false, errorMessage = message)
            },
        )
    }

    fun createRoom(profile: OnlineProfile, name: String, onCreated: (String) -> Unit) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.createRoom(
            profile = profile,
            requestedName = name,
            onSuccess = { roomId ->
                uiState = uiState.copy(busy = false)
                onCreated(roomId)
            },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun joinRoom(roomId: String, profile: OnlineProfile, onJoined: (String) -> Unit) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.joinRoom(
            roomId = roomId,
            profile = profile,
            onSuccess = {
                uiState = uiState.copy(busy = false)
                onJoined(roomId)
            },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun observeRoom(roomId: String, localUid: String) {
        if (observedRoomId == roomId && localUidForObservedRoom == localUid) return
        stopObservingRoom()
        observedRoomId = roomId
        localUidForObservedRoom = localUid
        uiState = uiState.copy(currentRoom = null, errorMessage = null)

        roomLoadTimeout = viewModelScope.launch {
            delay(15_000)
            if (observedRoomId == roomId && uiState.currentRoom == null) {
                uiState = uiState.copy(errorMessage = "방 정보를 받지 못했습니다. 에뮬레이터 인터넷 연결과 Firebase 연결을 확인한 후 재시도해 주세요.")
            }
        }
        roomListener = repository.observeRoom(
            roomId = roomId,
            onChanged = { room ->
                roomLoadTimeout?.cancel()
                uiState = uiState.copy(
                    currentRoom = room,
                    errorMessage = if (room == null) "방이 삭제되었거나 더 이상 존재하지 않습니다. 홈으로 돌아가 새 방에 입장해 주세요." else null,
                )
                if (room?.game?.hasResult() == true && localUid.isNotBlank()) {
                    repository.recordResultForUser(
                        room = room,
                        uid = localUid,
                        onError = { message -> uiState = uiState.copy(errorMessage = message) },
                    )
                }
            },
            onError = { message -> uiState = uiState.copy(errorMessage = message) },
        )
    }

    fun retryRoom(roomId: String, localUid: String) {
        stopObservingRoom()
        observeRoom(roomId, localUid)
    }

    fun leaveLobby(roomId: String, uid: String, onLeft: () -> Unit) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.leaveLobby(
            roomId = roomId,
            uid = uid,
            onComplete = {
                uiState = uiState.copy(busy = false, currentRoom = null)
                stopObservingRoom()
                onLeft()
            },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun setReady(roomId: String, uid: String, ready: Boolean) {
        repository.setReady(
            roomId = roomId,
            uid = uid,
            ready = ready,
            onError = { message -> uiState = uiState.copy(errorMessage = message) },
        )
    }

    fun startGame(roomId: String, ownerUid: String, onStarted: () -> Unit) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.startGame(
            roomId = roomId,
            ownerUid = ownerUid,
            onSuccess = {
                uiState = uiState.copy(busy = false)
                onStarted()
            },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun playMove(
        roomId: String,
        uid: String,
        cardId: Int,
        move: Move,
        jokerFile: Int?,
    ) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.playMove(
            roomId = roomId,
            actorUid = uid,
            cardId = cardId,
            move = move,
            jokerFile = jokerFile,
            onSuccess = { uiState = uiState.copy(busy = false) },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun exchangeCards(roomId: String, uid: String, cardIds: Set<Int>) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.exchangeCards(
            roomId = roomId,
            actorUid = uid,
            cardIds = cardIds,
            onSuccess = { uiState = uiState.copy(busy = false) },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun forfeit(roomId: String, uid: String, onFinished: () -> Unit) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true, errorMessage = null)
        repository.forfeit(
            roomId = roomId,
            actorUid = uid,
            onSuccess = {
                uiState = uiState.copy(busy = false)
                uiState.currentRoom?.let { room -> repository.recordResultForUser(room, uid) }
                onFinished()
            },
            onError = { message -> uiState = uiState.copy(busy = false, errorMessage = message) },
        )
    }

    fun serverNow(): Long = repository.serverNow()
    fun checkClock(roomId: String, uid: String) {
        if (uiState.busy) return
        uiState = uiState.copy(busy = true)
        repository.checkClock(roomId, uid,
            onSuccess = { uiState = uiState.copy(busy = false) },
            onError = { uiState = uiState.copy(busy = false, errorMessage = it) })
    }

    fun clearError() {
        uiState = uiState.copy(errorMessage = null)
    }

    fun stopObservingRoom() {
        roomLoadTimeout?.cancel()
        val listener = roomListener
        val id = observedRoomId
        if (listener != null && id != null) repository.removeRoomListener(id, listener)
        roomListener = null
        observedRoomId = null
        localUidForObservedRoom = null
    }

    override fun onCleared() {
        roomsListener?.let(repository::removeWaitingRoomsListener)
        stopObservingRoom()
        repository.close()
        super.onCleared()
    }
}
