package com.unochess.core

import kotlin.random.Random

/**
 * UNO Chess 전용 40장 덱.
 *
 * - RED/YELLOW/GREEN/BLUE 각 색마다 숫자 1..8을 1장씩: 32장
 * - Joker 8장
 * - 총 40장
 */
class UnoDeck(
    private val random: Random = Random.Default
) {
    private val drawPile = mutableListOf<UnoCard>()

    init { reset() }

    fun reset() {
        drawPile.clear()
        for (color in UnoColor.entries) {
            for (number in 1..8) {
                drawPile += UnoCard.NumberCard(color, number)
            }
        }
        repeat(8) { drawPile += UnoCard.Joker }
        drawPile.shuffle(random)
    }

    fun draw(): UnoCard {
        check(drawPile.isNotEmpty()) { "No cards left to draw" }
        return drawPile.removeLast()
    }

    fun returnAndShuffle(cards: Collection<UnoCard>) {
        drawPile += cards
        drawPile.shuffle(random)
    }

    /** 현재 덱에 남은 카드까지 전부 꺼낸다. 덱 재구성용. */
    fun drainAll(): List<UnoCard> {
        val cards = drawPile.toList()
        drawPile.clear()
        return cards
    }

    /** 전달된 카드만으로 새 드로우 덱을 만든다. */
    fun rebuild(cards: Collection<UnoCard>) {
        drawPile.clear()
        drawPile += cards
        drawPile.shuffle(random)
    }

    fun remaining(): Int = drawPile.size

    companion object {
        const val TOTAL_CARDS = 40
        const val HAND_SIZE = 5
    }
}
