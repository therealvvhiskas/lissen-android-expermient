package org.grakovne.lissen.playback.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfDataProvider
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.domain.BookChapter
import org.grakovne.lissen.domain.MediaProgress
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var dataProvider: AudiobookshelfDataProvider

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
        exoPlayer.playWhenReady = false

        CoroutineScope(Dispatchers.Main)
            .launch {
                val cover = withContext(Dispatchers.IO) {
                    when (val response = dataProvider.fetchBookCover(book.id)) {
                        is ApiResult.Error -> null
                        is ApiResult.Success -> response.data.use { it.readBytes() }
                    }
                }

                val chapterSources = book.chapters.mapIndexed { index, chapter ->
                    MediaItem.Builder()
                        .setMediaId(chapter.id)
                        .setUri(dataProvider.provideUri(book.id, chapter.id))
                        .setTag(book)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(chapter.name)
                                .setArtist(book.title)
                                .setTrackNumber(index)
                                .setArtworkData(cover, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                .build()
                        )
                        .build()
                }

                exoPlayer.setMediaItems(chapterSources)
                setPlaybackProgress(book.chapters, book.progress)
            }
    }

    private fun setPlaybackProgress(
        chapters: List<BookChapter>,
        progress: MediaProgress?
    ) {
        when (progress) {
            null -> exoPlayer.seekTo(0, 0)
            else -> {
                val totalDuration =
                    chapters.runningFold(0.0) { acc, chapter -> acc + chapter.duration }
                val targetChapter = totalDuration.indexOfFirst { it > progress.currentTime }
                val chapterProgress = progress.currentTime - totalDuration[targetChapter - 1]

                exoPlayer.seekTo(targetChapter - 1, (chapterProgress * 1000).toLong())
            }
        }
    }

    companion object {
        const val ACTION_PLAY = "org.grakovne.lissen.player.service.PLAY"
        const val ACTION_PAUSE = "org.grakovne.lissen.player.service.PAUSE"
        const val ACTION_SET_PLAYBACK = "org.grakovne.lissen.player.service.SET_PLAYBACK"

        const val BOOK_EXTRA = "org.grakovne.lissen.player.service.BOOK"
    }
}