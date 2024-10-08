package org.grakovne.lissen.player.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.client.AudiobookshelfChapterUriProvider
import org.grakovne.lissen.domain.DetailedBook
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

    @Inject
    lateinit var uriProvider: AudiobookshelfChapterUriProvider

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
                intent
                    .getSerializableExtra("BOOK")
                    ?.let { setPlaybackQueue(it as DetailedBook) }
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

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    private fun setPlaybackQueue(book: DetailedBook) {
        val chapterSources = book
            .chapters
            .mapIndexed { index, chapter ->
                MediaItem.Builder()
                    .setMediaId(chapter.id)
                    .setUri(uriProvider.provideUri(book.id, chapter.id))
                    .setTag(book)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(chapter.name)
                            .setArtist(book.title)
                            .setTrackNumber(index)
                            .build()
                    )
                    .build()
            }
        exoPlayer.setMediaItems(chapterSources)
    }
}