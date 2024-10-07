package org.grakovne.lissen.player.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerMediaRepository
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var mediaRepository: ServerMediaRepository

    companion object {
        const val ACTION_START_FOREGROUND = "org.grakovne.lissen.player.service.START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "org.grakovne.lissen.player.service.STOP_FOREGROUND"
    }

    override fun onCreate() {
        super.onCreate()

        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.let {
                    println("hehhee")
                }
            }
        })
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        return when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                START_STICKY
            }

            ACTION_STOP_FOREGROUND -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

                START_NOT_STICKY
            }

            else -> {
                START_NOT_STICKY
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    private fun playChapter(chapterId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = mediaRepository.fetchChapterContent(chapterId)) {
                is ApiResult.Success -> TODO()
                is ApiResult.Error -> {
                    // ahaha
                }
            }
        }
    }
}