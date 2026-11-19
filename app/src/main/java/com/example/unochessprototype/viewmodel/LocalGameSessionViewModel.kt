package com.example.unochessprototype.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class LocalGameSessionViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var opponentNameState by mutableStateOf(
        savedStateHandle[KEY_OPPONENT] ?: "PLAYER 2"
    )

    private var player2ReadyState by mutableStateOf(
        savedStateHandle[KEY_READY] ?: false
    )

    val opponentName: String
        get() = opponentNameState

    val player2Ready: Boolean
        get() = player2ReadyState

    fun setOpponentName(value: String) {
        opponentNameState = value.take(16)
        savedStateHandle[KEY_OPPONENT] = opponentNameState
    }

    fun setPlayer2Ready(value: Boolean) {
        player2ReadyState = value
        savedStateHandle[KEY_READY] = value
    }

    fun prepareNewMatch() {
        setPlayer2Ready(false)
    }

    companion object {
        private const val KEY_OPPONENT = "opponent_name"
        private const val KEY_READY = "player2_ready"
    }
}
