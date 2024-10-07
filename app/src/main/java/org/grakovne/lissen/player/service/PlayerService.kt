package org.grakovne.lissen.player.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.player.service.datasource.StreamingDatasourceFactory
import org.grakovne.lissen.repository.ServerMediaRepository
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
@Suppress("DEPRECATION")
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var mediaRepository: ServerMediaRepository

    @Inject
    lateinit var streamingDatasourceFactory: StreamingDatasourceFactory

    companion object {
        const val ACTION_START_FOREGROUND = "org.grakovne.lissen.player.service.START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "org.grakovne.lissen.player.service.STOP_FOREGROUND"
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        return when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                intent.getSerializableExtra("BOOK")?.let { playAudiobookStream(it as DetailedBook) }
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

    @OptIn(UnstableApi::class)
    private fun playAudiobookStream(book: DetailedBook) {
        exoPlayer.clearMediaItems()

        val chapterSources = book
            .chapters
            .mapIndexed { index, chapter ->
                val mediaItem = MediaItem.Builder()
                    .setMediaId(chapter.id)
                    .setUri("action_play?bookId=${book.id}&chapterId=${chapter.id}")
                    .setMediaMetadata(
                        MediaMetadata
                            .Builder()
                            .setTitle(chapter.name)
                            .setArtist(book.title)
                            .setTrackNumber(index)
                            .build()
                    )
                    .build()

                ProgressiveMediaSource
                    .Factory(streamingDatasourceFactory)
                    .createMediaSource(mediaItem)
            }

        exoPlayer.addMediaSources(chapterSources)

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }
}