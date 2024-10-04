package org.grakovne.lissen.player.service

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer
) {

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue(isPlaying)
            }
        })
    }

     val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    fun playAudio(url: String) {
        startAudioService()
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    fun pauseAudio() {
        exoPlayer.playWhenReady = false
        _isPlaying.postValue(false)
    }

    fun stopAudio() {
        exoPlayer.stop()
        _isPlaying.postValue(false)
    }

    @OptIn(UnstableApi::class)
    private fun startAudioService() {
        val intent = Intent(context, AudioPlayerService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}