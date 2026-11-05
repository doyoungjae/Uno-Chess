package com.example.unochessprototype.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class LocalGameSessionViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var opponentName by mutableStateOf(
        savedStateHandle[KEY_OPPONENT] ?: "PLAYER 2"
    )
        private set

    var player2Ready by mutableStateOf(
        savedStateHandle[KEY_READY] ?: false
    )
        private set

    var player1IsWhite by mutableStateOf(
        savedStateHandle[KEY_PLAYER1_WHITE] ?: true
    )
        private set

    fun updateOpponentName(value: String) {
        opponentName = value.take(16)
        savedStateHandle[KEY_OPPONENT] = opponentName
    }

    fun updatePlayer2Ready(value: Boolean) {
        player2Ready = value
        savedStateHandle[KEY_READY] = value
    }

    fun setFirstPlayer(player1Starts: Boolean) {
        player1IsWhite = player1Starts
        savedStateHandle[KEY_PLAYER1_WHITE] = player1Starts
    }

    fun prepareNewMatch() {
        updatePlayer2Ready(false)
    }

    companion object {
        private const val KEY_OPPONENT = "opponent_name"
        private const val KEY_READY = "player2_ready"
        private const val KEY_PLAYER1_WHITE = "player1_is_white"
    }
}