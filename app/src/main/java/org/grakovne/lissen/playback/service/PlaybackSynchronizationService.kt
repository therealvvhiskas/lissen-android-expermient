package org.grakovne.lissen.playback.service

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.domain.PlaybackProgress
import org.grakovne.lissen.domain.PlaybackSession
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSynchronizationService @OptIn(UnstableApi::class)
@Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val channel: AudiobookshelfChannel,
    private val sharedPreferences: LissenSharedPreferences
) {
    private var currentBook: DetailedBook? = null
    private var playbackSession: PlaybackSession? = null
    private val serviceScope = MainScope()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    scheduleSynchronization()
                } else {
                    executeSynchronization()
                }
            }
        })
    }

    fun startPlaybackSynchronization(book: DetailedBook) {
        serviceScope.coroutineContext.cancelChildren()
        currentBook = book
    }

    private fun scheduleSynchronization() {
        serviceScope
            .launch {
                if (exoPlayer.isPlaying) {
                    executeSynchronization()
                    delay(SYNC_INTERVAL)
                    scheduleSynchronization()
                }
            }
    }

    private fun executeSynchronization() {
        val elapsedMs = exoPlayer.currentPosition
        val overallProgress = getProgress(elapsedMs)

        serviceScope
            .launch(Dispatchers.IO) {
                playbackSession
                    ?.takeIf { it.itemId == currentBook?.id }
                    ?.let { synchronizeProgress(it, overallProgress) }
                    ?: openPlaybackSession()
            }
    }

    private suspend fun synchronizeProgress(
        it: PlaybackSession,
        overallProgress: PlaybackProgress
    ) = channel
        .syncProgress(
            itemId = it.sessionId,
            progress = overallProgress
        )
        .foldAsync(
            onSuccess = {},
            onFailure = { openPlaybackSession() }
        )

    private suspend fun openPlaybackSession() =
        currentBook
            ?.let { book ->
                channel
                    .startPlayback(
                        itemId = book.id,
                        deviceId = sharedPreferences.getDeviceId(),
                        supportedMimeTypes = MimeTypeProvider.getSupportedMimeTypes()
                    )
                    .fold(
                        onSuccess = { playbackSession = it },
                        onFailure = {}
                    )
            }

    private fun getProgress(currentElapsedMs: Long): PlaybackProgress {
        val currentBook = exoPlayer
            .currentMediaItem
            ?.localConfiguration
            ?.tag as? DetailedBook
            ?: return PlaybackProgress(0.0, 0.0)

        val currentIndex = exoPlayer.currentMediaItemIndex

        val previousDuration = currentBook.files
            .take(currentIndex)
            .sumOf { it.duration * 1000 }

        val totalDuration = currentBook.files.sumOf { it.duration * 1000 }

        val totalElapsedMs = previousDuration + currentElapsedMs
        return PlaybackProgress(
            currentTime = totalElapsedMs / 1000.0,
            totalTime = totalDuration / 1000.0
        )
    }

    companion object {
        private const val SYNC_INTERVAL = 30_000L
    }
}