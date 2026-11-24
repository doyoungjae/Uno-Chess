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

    private var effectsVolume = 0.8f
    fun configure(settings: com.example.unochessprototype.data.LocalSettings) {
        effectsVolume = if (settings.effectsEnabled) settings.effectsVolume.coerceIn(0f, 1f) else 0f
        music?.setVolume(settings.bgmVolume * 0.23f, settings.bgmVolume * 0.23f)
        if (settings.bgmEnabled) startMusic() else stopMusic()
    }
    fun startMusic() {
        if (music?.isPlaying == false) music?.start()
    }

    fun stopMusic() {
        if (music?.isPlaying == true) music?.pause()
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
        music?.release()
        music = null
        soundPool.release()
    }
}

@Composable
internal fun rememberChessAudio(settings: com.example.unochessprototype.data.LocalSettings): ChessAudio {
    val context = LocalContext.current
    val audio = remember(context) { ChessAudio(context) }
    DisposableEffect(audio) {

        onDispose {
            audio.stopMusic()
            audio.release()
        }
    }
    androidx.compose.runtime.SideEffect { audio.configure(settings) }
    return audio
}
