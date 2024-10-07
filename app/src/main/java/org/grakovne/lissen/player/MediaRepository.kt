package org.grakovne.lissen.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.player.service.AudioPlayerService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class MediaRepository
@Inject constructor(
    @ApplicationContext private val context: Context
) {

    private lateinit var mediaController: MediaController

    private val token = SessionToken(
        context,
        ComponentName(context, AudioPlayerService::class.java)
    )

    val _isPlaying = MutableLiveData(false)
    val _currentPosition = MutableLiveData<Long>()
    val _currentMediaItemIndex = MutableLiveData<Int>()

    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressAction = object : Runnable {
        override fun run() {
            val currentPosition = mediaController.currentPosition
            _currentPosition.postValue(currentPosition / 1000)
            handler.postDelayed(this, 500L)
        }
    }

    init {
        val controllerBuilder = MediaController.Builder(context, token)
        val futureController = controllerBuilder.buildAsync()

        Futures.addCallback(
            futureController,
            object : FutureCallback<MediaController> {
                override fun onSuccess(controller: MediaController) {
                    mediaController = controller

                    controller.addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            _isPlaying.postValue(playbackState == Player.STATE_READY && controller.isPlaying)
                            _currentMediaItemIndex.postValue(mediaController.currentMediaItemIndex)

                            when (controller.isPlaying) {
                                true -> startUpdatingProgress()
                                else -> stopUpdatingProgress()
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.postValue(isPlaying)

                            when (isPlaying) {
                                true -> startUpdatingProgress()
                                false -> stopUpdatingProgress()
                            }
                        }

                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            _currentMediaItemIndex.postValue(mediaController.currentMediaItemIndex)
                            _currentPosition.postValue(mediaController.currentPosition)
                        }


                        override fun onTimelineChanged(
                            timeline: Timeline,
                            reason: Int
                        ) {
                            _currentMediaItemIndex.postValue(mediaController.currentMediaItemIndex)
                            _currentPosition.postValue(mediaController.currentPosition)
                        }

                    })
                }

                override fun onFailure(t: Throwable) {
                }
            },
            MoreExecutors.directExecutor()
        )
    }


    fun playAudio(book: DetailedBook) {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_START_FOREGROUND
            putExtra("BOOK", book)
        }

        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseAudio() {
        mediaController.pause()

        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_STOP_FOREGROUND
        }

        ContextCompat.startForegroundService(context, intent)
    }

    fun nextTrack() {
        val nextIndex = mediaController.currentMediaItemIndex + 1
        val timeline = mediaController.currentTimeline

        if (nextIndex < timeline.windowCount) {
            mediaController.seekTo(nextIndex, 0)
        }
    }

    fun setTrack(index: Int) {
        val timeline = mediaController.currentTimeline

        if (index < timeline.windowCount && index >= 0) {
            mediaController.seekTo(index, 0)
        }
    }

    fun seekTo(position: Float) {
        stopUpdatingProgress()

        val duration = mediaController.duration
        if (duration > 0) {
            val newPosition = (1000 * position).toLong()
            mediaController.seekTo(newPosition)
        }

        startUpdatingProgress()
    }

    fun previousTrack() {
        val previousIndex = mediaController.currentMediaItemIndex - 1

        if (previousIndex > 0) {
            mediaController.seekTo(previousIndex, 0)
        }
    }

    private fun startUpdatingProgress() {
        handler.post(updateProgressAction)
    }

    private fun stopUpdatingProgress() {
        handler.removeCallbacks(updateProgressAction)
    }
}