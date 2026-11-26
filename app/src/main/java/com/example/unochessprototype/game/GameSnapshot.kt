package com.example.unochessprototype.game

/** 로컬 규칙 엔진의 완전한 상태. 전송 형식과 사용자 UID는 온라인 계층이 담당한다. */
data class GameSnapshot(
    val board: List<String>,
    val currentPlayer: Player,
    val completedTurns: Int,
    val result: GameResult?,
    val whiteHand: List<UnoCard>,
    val blackHand: List<UnoCard>,
    val drawPile: List<UnoCard>,
    val discardPile: List<UnoCard>,
    val firstTurnRounds: List<FirstTurnRound>,
    val logs: List<String>,
)
