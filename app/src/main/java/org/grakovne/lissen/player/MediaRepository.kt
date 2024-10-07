package org.grakovne.lissen.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
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
import org.grakovne.lissen.repository.ServerMediaRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val serverMediaRepository: ServerMediaRepository
) {

    private val sessionToken =
        SessionToken(context, ComponentName(context, AudioPlayerService::class.java))
    private lateinit var mediaController: MediaController

    val _isPlaying = MutableLiveData(false)
    val _currentPosition = MutableLiveData<Long>()

    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressAction = object : Runnable {
        override fun run() {
            val currentPosition = mediaController.currentPosition
            _currentPosition.postValue(currentPosition / 1000)
            handler.postDelayed(this, 500L)
        }
    }

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
                        ) = _currentPosition.postValue(mediaController.currentPosition)


                        override fun onTimelineChanged(
                            timeline: Timeline,
                            reason: Int
                        ) = _currentPosition.postValue(mediaController.currentPosition)

                    })
                }

                override fun onFailure(t: Throwable) {
                }
            },
            MoreExecutors.directExecutor()
        )
    }


    @OptIn(UnstableApi::class)
    fun playAudio(book: DetailedBook) {
        val mediaItems = book.chapters.map { chapter ->
            MediaItem.Builder()
                .setMediaId(chapter.id)
                .setUri("https://audiobook.grakovne.org/api/items/49fcdfab-2276-47b7-86c9-0b66098d4c5b/file/140182086?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjM2QzMjQ1Mi1lZDFjLTRlZjktYWJkMC00ZTg0MTcwNGVmMTUiLCJ1c2VybmFtZSI6ImdyYWtvdm5lIiwiaWF0IjoxNzIzNTkxMzU2fQ.3G-Kes9PqAycvpMqdo2BKLsZmf-R1ihRBGD568uS0s4")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(book.title)
                        .setArtist(chapter.name)
                        .build()
                )
                .build()
        }

        mediaController.setMediaItems(mediaItems)
        mediaController.prepare()
        mediaController.play()

        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_START_FOREGROUND
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

    fun startUpdatingProgress() {
        handler.post(updateProgressAction)
    }

    fun stopUpdatingProgress() {
        handler.removeCallbacks(updateProgressAction)
    }
}