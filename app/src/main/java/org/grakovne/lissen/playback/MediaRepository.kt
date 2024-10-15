package org.grakovne.lissen.playback

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.playback.service.AudioPlayerService
import org.grakovne.lissen.playback.service.AudioPlayerService.Companion.ACTION_SEEK_TO
import org.grakovne.lissen.playback.service.AudioPlayerService.Companion.BOOK_EXTRA
import org.grakovne.lissen.playback.service.AudioPlayerService.Companion.PLAYBACK_READY
import org.grakovne.lissen.playback.service.AudioPlayerService.Companion.POSITION
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private lateinit var mediaController: MediaController

    private val token =
        SessionToken(context, ComponentName(context, AudioPlayerService::class.java))

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _isPlaybackReady = MutableLiveData(false)
    val isPlaybackReady: LiveData<Boolean> = _isPlaybackReady

    private val _mediaItemPosition = MutableLiveData<Double>()
    val mediaItemPosition: LiveData<Double> = _mediaItemPosition

    private val _playingBook = MutableLiveData<DetailedBook>()
    val playingBook: LiveData<DetailedBook> = _playingBook

    private val handler = Handler(Looper.getMainLooper())

    private val bookDetailsReadyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PLAYBACK_READY) {
                _isPlaybackReady.postValue(true)
            }
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

                    LocalBroadcastManager
                        .getInstance(context)
                        .registerReceiver(bookDetailsReadyReceiver, IntentFilter(PLAYBACK_READY))

                    mediaController.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                        }
                    })
                }

                override fun onFailure(t: Throwable) {
                    t.printStackTrace()
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    fun mediaPreparing() {
        _isPlaybackReady.postValue(false)
    }

    fun preparePlayingBook(book: DetailedBook) {
        if (::mediaController.isInitialized && _playingBook.value != book) {
            preparePlay(book)
        }
        _playingBook.postValue(book)
        startUpdatingProgress(book)
    }

    private fun preparePlay(book: DetailedBook) {
        _mediaItemPosition.postValue(0.0)
        _isPlaying.postValue(false)

        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_SET_PLAYBACK
            putExtra(BOOK_EXTRA, book)
        }

        context.startService(intent)
    }

    fun play() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseAudio() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun seekTo(position: Double) {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = ACTION_SEEK_TO

            putExtra(BOOK_EXTRA, playingBook.value)
            putExtra(POSITION, position)
        }

        context.startService(intent)
    }

    private fun startUpdatingProgress(detailedBook: DetailedBook) {
        handler.removeCallbacksAndMessages(null)

        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentIndex = mediaController.currentMediaItemIndex
                val accumulated = detailedBook.files.take(currentIndex).sumOf { it.duration }
                val currentFilePosition = mediaController.currentPosition / 1000.0

                _mediaItemPosition.value = (accumulated + currentFilePosition)
                handler.postDelayed(this, 1000)
            }
        }, 500)
    }
}
