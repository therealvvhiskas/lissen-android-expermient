package org.grakovne.lissen.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.player.service.AudioPlayerService
import org.grakovne.lissen.player.service.AudioPlayerService.Companion.BOOK_EXTRA
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private lateinit var mediaController: MediaController

    private val token = SessionToken(
        context,
        ComponentName(context, AudioPlayerService::class.java)
    )

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Long> = _currentPosition

    private val _currentMediaItemIndex = MutableLiveData<Int>()
    val currentMediaItemIndex: LiveData<Int> = _currentMediaItemIndex

    private val _playingBook: MutableLiveData<DetailedBook> = MutableLiveData<DetailedBook>()
    val playingBook: LiveData<DetailedBook> = _playingBook

    private val handler = Handler(Looper.getMainLooper())

    private val updateProgressAction = object : Runnable {
        override fun run() {
            val currentPosition = mediaController.currentPosition
            _currentPosition.postValue(currentPosition / 1000)
            handler.postDelayed(this, 500L)
        }
    }

    fun preparePlayingBook(book: DetailedBook) {
        _playingBook.postValue(book)

        if (::mediaController.isInitialized && _playingBook.value != book) {
            preparePlay(book)
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
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            _currentMediaItemIndex.postValue(mediaController.currentMediaItemIndex)
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.postValue(isPlaying)
                            _currentMediaItemIndex.postValue(mediaController.currentMediaItemIndex)

                            startUpdatingProgress()
                        }
                    })
                }

                override fun onFailure(t: Throwable) {
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun play() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
        }

        ContextCompat.startForegroundService(context, intent)
    }

    private fun preparePlay(book: DetailedBook) {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_SET_PLAYBACK
            putExtra(BOOK_EXTRA, book)
        }

        _playingBook.postValue(book)
        context.startService(intent)
    }


    fun pauseAudio() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PAUSE
        }

        context.startService(intent)
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
        val duration = mediaController.duration

        if (duration > 0) {
            val newPosition = (1000 * position).toLong()
            mediaController.seekTo(newPosition)
        }
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
}