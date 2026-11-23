package com.example.unochessprototype.ui.screen

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.unochessprototype.R

internal class ChessAudio(context: Context) {
    private val appContext = context.applicationContext
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val moveSound = soundPool.load(appContext, R.raw.chess_move, 1)
    private val captureSound = soundPool.load(appContext, R.raw.chess_capture, 1)
    private val checkSound = soundPool.load(appContext, R.raw.chess_check, 1)
    private val endSound = soundPool.load(appContext, R.raw.chess_end, 1)

    private var music: MediaPlayer? = MediaPlayer.create(appContext, R.raw.chess_ambient)?.apply {
        isLooping = true
        setVolume(0.16f, 0.16f)
    }

    fun startMusic() {
        if (music?.isPlaying == false) music?.start()
    }

    fun stopMusic() {
        if (music?.isPlaying == true) music?.pause()
    }

    fun playMove(captured: Boolean = false) {
        val sound = if (captured) captureSound else moveSound
        soundPool.play(sound, 0.85f, 0.85f, 1, 0, 1f)
    }

    fun playCheck() {
        soundPool.play(checkSound, 0.75f, 0.75f, 2, 0, 1f)
    }

    fun playEnd() {
        soundPool.play(endSound, 0.85f, 0.85f, 3, 0, 1f)
    }

    fun release() {
        music?.release()
        music = null
        soundPool.release()
    }
}

@Composable
internal fun rememberChessAudio(): ChessAudio {
    val context = LocalContext.current
    val audio = remember(context) { ChessAudio(context) }
    DisposableEffect(audio) {
        audio.startMusic()
        onDispose {
            audio.stopMusic()
            audio.release()
        }
    }
    return audio
}
