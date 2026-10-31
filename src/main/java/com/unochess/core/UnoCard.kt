package com.unochess.core

enum class UnoColor { RED, YELLOW, GREEN, BLUE }

sealed interface UnoCard {
    data class NumberCard(val color: UnoColor, val number: Int) : UnoCard {
        init { require(number in 1..8) { "UNO Chess uses only number cards 1..8" } }
        override fun toString(): String = "${color.name}-$number"
    }

    data object Joker : UnoCard {
        override fun toString(): String = "JOKER"
    }
}

/**
 * 바닥 카드 상태.
 * - 숫자 카드: fileNumber는 카드 숫자와 동일하다.
 * - Joker: 실제 플레이 때 선택한 열을 기록할 수 있다.
 * - Joker는 색상 조건을 갖지 않는다.
 */
data class ActiveUnoCard(
    val card: UnoCard,
    val fileNumber: Int? = (card as? UnoCard.NumberCard)?.number
) {
    init { require(fileNumber == null || fileNumber in 1..8) }

    val numberOrNull: Int?
        get() = (card as? UnoCard.NumberCard)?.number

    val colorOrNull: UnoColor?
        get() = (card as? UnoCard.NumberCard)?.color
}
