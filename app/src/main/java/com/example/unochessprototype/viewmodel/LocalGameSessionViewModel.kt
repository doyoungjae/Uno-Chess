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

    private var player1IsWhiteState by mutableStateOf(
        savedStateHandle[KEY_PLAYER1_WHITE] ?: true
    )

    val opponentName: String
        get() = opponentNameState

    val player2Ready: Boolean
        get() = player2ReadyState

    val player1IsWhite: Boolean
        get() = player1IsWhiteState

    fun setOpponentName(value: String) {
        opponentNameState = value.take(16)
        savedStateHandle[KEY_OPPONENT] = opponentNameState
    }

    fun setPlayer2Ready(value: Boolean) {
        player2ReadyState = value
        savedStateHandle[KEY_READY] = value
    }

    fun setFirstPlayer(player1Starts: Boolean) {
        player1IsWhiteState = player1Starts
        savedStateHandle[KEY_PLAYER1_WHITE] = player1Starts
    }

    fun prepareNewMatch() {
        setPlayer2Ready(false)
    }

    companion object {
        private const val KEY_OPPONENT = "opponent_name"
        private const val KEY_READY = "player2_ready"
        private const val KEY_PLAYER1_WHITE = "player1_is_white"
    }
}
