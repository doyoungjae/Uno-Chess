package com.example.unochessprototype.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.unochessprototype.online.model.OnlineProfile
import com.example.unochessprototype.online.repository.OnlineUserRepository
import com.google.firebase.database.ValueEventListener


data class UserProfileUiState(
    val isLoading: Boolean = true,
    val isConnected: Boolean = false,
    val profile: OnlineProfile = OnlineProfile(),
    val errorMessage: String? = null,
    val saveMessage: String? = null,
)

class UserProfileViewModel : ViewModel() {

    private val repository = OnlineUserRepository()

    var uiState by mutableStateOf(UserProfileUiState())
        private set

    private var observedUid: String? = null
    private var profileListener: ValueEventListener? = null

    init {
        connect()
    }

    fun connect() {
        uiState = uiState.copy(isLoading = true, errorMessage = null)

        repository.ensureSignedIn(
            onSuccess = { uid -> startObserving(uid) },
            onError = { message ->
                uiState = uiState.copy(
                    isLoading = false,
                    isConnected = false,
                    errorMessage = message,
                )
            },
        )
    }

    fun updateIdentity(nickname: String, avatar: String) {
        val uid = observedUid ?: run {
            uiState = uiState.copy(errorMessage = "Firebase 사용자 연결이 아직 완료되지 않았습니다.")
            return
        }

        uiState = uiState.copy(saveMessage = null, errorMessage = null)

        repository.updateIdentity(
            uid = uid,
            nickname = nickname,
            avatar = avatar,
            onSuccess = {
                uiState = uiState.copy(saveMessage = "설정 저장 완료")
            },
            onError = { message ->
                uiState = uiState.copy(errorMessage = message)
            },
        )
    }

    fun toggleFriend(friendUid: String) {
        val uid = observedUid ?: return
        if (friendUid.isBlank() || friendUid == uid) return
        repository.setFriend(
            uid = uid,
            friendUid = friendUid,
            isFriend = friendUid !in uiState.profile.friends,
            onError = { message -> uiState = uiState.copy(errorMessage = message) },
        )
    }

    fun clearTransientMessage() {
        uiState = uiState.copy(saveMessage = null)
    }

    private fun startObserving(uid: String) {
        profileListener?.let { listener ->
            observedUid?.let { oldUid -> repository.removeProfileListener(oldUid, listener) }
        }

        observedUid = uid
        profileListener = repository.observeProfile(
            uid = uid,
            onChanged = { profile ->
                uiState = uiState.copy(
                    isLoading = false,
                    isConnected = true,
                    profile = profile,
                    errorMessage = null,
                )
            },
            onError = { message ->
                uiState = uiState.copy(
                    isLoading = false,
                    isConnected = false,
                    errorMessage = message,
                )
            },
        )
    }

    override fun onCleared() {
        profileListener?.let { listener ->
            observedUid?.let { uid -> repository.removeProfileListener(uid, listener) }
        }
        super.onCleared()
    }
}
