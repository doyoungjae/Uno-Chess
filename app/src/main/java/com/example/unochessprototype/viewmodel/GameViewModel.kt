package com.example.unochessprototype.viewmodel

import androidx.lifecycle.ViewModel
import com.example.unochessprototype.game.UnoChessGame

class GameViewModel : ViewModel() {
    val game = UnoChessGame()

    private var resultRecorded = false

    fun markResultRecorded(): Boolean {
        if (resultRecorded) return false
        resultRecorded = true
        return true
    }
}
