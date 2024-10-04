package org.grakovne.lissen.player.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MediaRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sessionToken = SessionToken(context, ComponentName(context, AudioPlayerService::class.java))
    private lateinit var mediaController: MediaController

    val _isPlaying = MutableLiveData(false)

    init {
        val controllerBuilder = MediaController.Builder(context, sessionToken)
        val futureController = controllerBuilder.buildAsync()

        Futures.addCallback(
            futureController,
            object : FutureCallback<MediaController> {
                override fun onSuccess(controller: MediaController) {
                    mediaController = controller

                    controller.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            _isPlaying.postValue(playbackState == Player.STATE_READY && controller.isPlaying)
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.postValue(isPlaying)
                        }
                    })
                }

                override fun onFailure(t: Throwable) {
                }
            },
            MoreExecutors.directExecutor()
        )

    }

    fun playAudio(url: String) {
        startAudioService()

        val mediaItem = MediaItem.fromUri(url)
        mediaController.setMediaItem(mediaItem)
        mediaController.prepare()
        mediaController.play()

        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_START_FOREGROUND
        }

        ContextCompat.startForegroundService(context, intent)
    }


    fun pauseAudio() {
        //mediaController.pause()
    }

    fun stopAudio() {
        // mediaController.stop()
    }

    private fun startAudioService() {
        val intent = Intent(context, AudioPlayerService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }
}