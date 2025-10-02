package com.example.healthapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log

class MusicPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var isPaused = false
    private var currentVolume = 80

    companion object {
        const val ACTION_PLAY = "PLAY"
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_STOP = "STOP"
        const val ACTION_SET_VOLUME = "SET_VOLUME"
        const val EXTRA_VOLUME = "volume"
        const val VOLUME_MAX = 100
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val volume = intent.getIntExtra(EXTRA_VOLUME, currentVolume)
                playMusic(volume)
            }
            ACTION_PAUSE -> pauseMusic()
            ACTION_STOP -> stopMusic()
            ACTION_SET_VOLUME -> {
                val volume = intent.getIntExtra(EXTRA_VOLUME, currentVolume)
                setVolume(volume)
            }
        }
        return START_STICKY
    }

    private fun playMusic(volume: Int) {
        currentVolume = volume
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.workout_music)
            mediaPlayer?.isLooping = true
            setVolume(currentVolume)
        }

        if (isPaused) {
            mediaPlayer?.start()
            isPaused = false
        } else {
            mediaPlayer?.start()
        }
        Log.d("MusicPlayer", "Music started with volume: $currentVolume")
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        isPaused = true
        Log.d("MusicPlayer", "Music paused")
    }

    private fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPaused = false
        Log.d("MusicPlayer", "Music stopped")
    }

    private fun setVolume(volume: Int) {
        currentVolume = volume
        val volumeLevel = volume.toFloat() / VOLUME_MAX
        mediaPlayer?.setVolume(volumeLevel, volumeLevel)
        Log.d("MusicPlayer", "Volume set to: $volume")
    }

    override fun onDestroy() {
        stopMusic()
        super.onDestroy()
    }
}