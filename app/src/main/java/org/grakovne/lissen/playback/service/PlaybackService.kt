package org.grakovne.lissen.playback.service

import android.content.Intent
import androidx.annotation.OptIn
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.BookFile
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.domain.MediaProgress
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var dataProvider: AudiobookshelfChannel

    @Inject
    lateinit var playbackSynchronizationService: PlaybackSynchronizationService

    @Inject
    lateinit var sharedPreferences: LissenSharedPreferences

    private val playerServiceScope = MainScope()

    @Suppress("DEPRECATION")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_PLAY -> {
                playerServiceScope
                    .launch {
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                return START_STICKY
            }

            ACTION_PAUSE -> {
                playerServiceScope
                    .launch {
                        exoPlayer.playWhenReady = false
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                return START_NOT_STICKY
            }

            ACTION_SET_PLAYBACK -> {
                val book = intent.getSerializableExtra(BOOK_EXTRA) as? DetailedBook
                book?.let {
                    playerServiceScope
                        .launch { preparePlayback(it) }
                }
                return START_NOT_STICKY
            }

            else -> {
                return START_NOT_STICKY
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        playerServiceScope.cancel()

        mediaSession.release()
        exoPlayer.release()
        exoPlayer.clearMediaItems()

        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private suspend fun preparePlayback(book: DetailedBook) {
        exoPlayer.playWhenReady = false


        withContext(Dispatchers.IO) {
            async { playbackSynchronizationService.stopPlaybackSynchronization() }.await()
            val prepareQueue = async {
                val playingQueue = book
                    .files
                    .mapIndexed { index, file ->
                        MediaItem.Builder()
                            .setMediaId(file.id)
                            .setUri(dataProvider.provideFileUri(book.id, file.id))
                            .setTag(book)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(book.author)
                                    .setArtist(book.title)
                                    .setTrackNumber(index)
                                    .setDurationMs(file.duration.toLong() * 1000)
                                    .setArtworkUri(dataProvider.provideChapterCoverUri(book.id))
                                    .build()
                            )
                            .build()
                    }

                withContext(Dispatchers.Main) {
                    exoPlayer.setMediaItems(playingQueue)
                    setPlaybackProgress(book.files, book.progress)
                }
            }

            val prepareSession = async {
                dataProvider
                    .startPlayback(
                        itemId = book.id,
                        deviceId = sharedPreferences.getDeviceId(),
                        supportedMimeTypes = SUPPORTED_MIME_TYPES
                    )
                    .fold(
                        onSuccess = {
                            playbackSynchronizationService.startPlaybackSynchronization(it)
                        },
                        onFailure = {}
                    )
            }

            awaitAll(prepareSession, prepareQueue)

            LocalBroadcastManager
                .getInstance(baseContext)
                .sendBroadcast(Intent(PLAYBACK_READY))
        }

    }

    private fun setPlaybackProgress(
        chapters: List<BookFile>,
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
        const val PLAYBACK_READY = "org.grakovne.lissen.player.service.PLAYBACK_READY"

        val SUPPORTED_MIME_TYPES = listOf(
            "audio/flac",
            "audio/mp4",
            "audio/aac",
            "audio/mpeg",
            "audio/mp3",
            "audio/webm",
            "audio/ac3",
            "audio/opus",
            "audio/vorbis",
        )
    }
}