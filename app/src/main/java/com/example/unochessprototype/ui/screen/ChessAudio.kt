package com.example.unochessprototype.ui.screen

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.unochessprototype.R
import com.example.unochessprototype.data.LocalSettings

internal class ChessAudio(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val moveSound = soundPool.load(context.applicationContext, R.raw.chess_move, 1)
    private val captureSound = soundPool.load(context.applicationContext, R.raw.chess_capture, 1)
    private val checkSound = soundPool.load(context.applicationContext, R.raw.chess_check, 1)
    private val endSound = soundPool.load(context.applicationContext, R.raw.chess_end, 1)
    private val cardDrawSound = soundPool.load(context.applicationContext, R.raw.card_draw, 1)

    private var effectsVolume = 0.8f
    private var cardDrawLoaded = false
    private var pendingCardDraws = 0

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (sampleId == cardDrawSound && status == 0) {
                cardDrawLoaded = true
                repeat(pendingCardDraws) {
                    soundPool.play(cardDrawSound, effectsVolume, effectsVolume, 1, 0, 1f)
                }
                pendingCardDraws = 0
            }
        }
    }

    fun configure(settings: LocalSettings) {
        effectsVolume = if (settings.effectsEnabled) settings.effectsVolume.coerceIn(0f, 1f) else 0f
    }

    fun playCardDraw() {
        if (cardDrawLoaded) {
            soundPool.play(cardDrawSound, effectsVolume, effectsVolume, 1, 0, 1f)
        } else {
            pendingCardDraws++
        }
    }

    fun playMove(captured: Boolean = false) {
        val sound = if (captured) captureSound else moveSound
        soundPool.play(sound, effectsVolume, effectsVolume, 1, 0, 1f)
    }

    fun playCheck() {
        soundPool.play(checkSound, effectsVolume, effectsVolume, 2, 0, 1f)
    }

    fun playEnd() {
        soundPool.play(endSound, effectsVolume, effectsVolume, 3, 0, 1f)
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
internal fun rememberChessAudio(settings: LocalSettings): ChessAudio {
    val context = LocalContext.current
    val audio = remember(context) { ChessAudio(context) }
    DisposableEffect(audio) {
        onDispose { audio.release() }
    }
    androidx.compose.runtime.SideEffect { audio.configure(settings) }
    return audio
}
