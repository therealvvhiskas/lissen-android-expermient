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
        const val ACTION_PLAY = "org.grakovne.lissen.player.service.PLAY"
        const val ACTION_PAUSE = "org.grakovne.lissen.player.service.PAUSE"
        const val ACTION_SET_PLAYBACK = "org.grakovne.lissen.player.service.SET_PLAYBACK"
        
        const val BOOK_EXTRA = "org.grakovne.lissen.player.service.BOOK"
    }

    @Suppress("DEPRECATION")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        return when (intent?.action) {
            ACTION_PLAY -> {
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                START_STICKY
            }

            ACTION_PAUSE -> {
                exoPlayer.playWhenReady = false

                stopForeground(true)
                stopSelf()

                START_NOT_STICKY
            }

            ACTION_SET_PLAYBACK -> {
                intent
                    .getSerializableExtra(BOOK_EXTRA)
                    ?.let { setPlaybackQueue(it as DetailedBook) }

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
        exoPlayer.clearMediaItems()
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
        exoPlayer.playWhenReady = false
        exoPlayer.seekTo(0,0)
    }
}