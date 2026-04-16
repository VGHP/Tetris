package com.example.tetris.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.tetris.R
import com.example.tetris.model.AppTheme

class SoundManager(private val context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmTheme: AppTheme? = null

    private var volume = 1.0f

    private val soundGameOver = soundPool.load(context, R.raw.snd_gameover, 1)

    fun setVolume(vol: Float) {
        volume = vol
        bgmPlayer?.setVolume(vol, vol)
    }

    fun playGameOver() {
        soundPool.play(soundGameOver, volume, volume, 0, 0, 1f)
    }

    fun playBackgroundMusic(theme: AppTheme) {
        // если тема та же самая и музыка уже играет, ничего не делаем
        if (currentBgmTheme == theme && bgmPlayer?.isPlaying == true) return

        bgmPlayer?.release()
        bgmPlayer = null
        currentBgmTheme = theme

        val bgmRes = when (theme) {
            AppTheme.NYANCAT -> R.raw.snd_nyancat
            AppTheme.RETRO -> R.raw.snd_bgm_retro
            AppTheme.MINIMAL -> R.raw.snd_minimal
            AppTheme.NEON -> R.raw.snd_bgm_neon
            AppTheme.CLASSIC -> R.raw.snd_bgm_classic
        }

        bgmPlayer = MediaPlayer.create(context, bgmRes)
        bgmPlayer?.isLooping = true
        bgmPlayer?.setVolume(volume, volume)
        bgmPlayer?.start()
    }

    fun pauseBackgroundMusic() {
        bgmPlayer?.pause()
    }

    // сверяем текущую тему
    fun resumeBackgroundMusic(theme: AppTheme) {
        if (currentBgmTheme != theme) {
            playBackgroundMusic(theme)
        } else {
            bgmPlayer?.start()
        }
    }

    fun stopBackgroundMusic() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
        currentBgmTheme = null
    }
}