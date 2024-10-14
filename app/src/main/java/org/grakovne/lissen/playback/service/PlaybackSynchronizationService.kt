package org.grakovne.lissen.playback.service

import androidx.media3.common.Player
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSynchronizationService @Inject constructor(
    private val exoPlayer: ExoPlayer,
    private val channel: AudiobookshelfChannel
) {
    private var playbackSession: PlaybackSession? = null
    private val serviceScope = MainScope()

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    playbackSession?.let {
                        startSynchronization()
                    }
                } else {
                    executeSynchronization()
                }
            }
        })
    }

    fun startPlaybackSynchronization(session: PlaybackSession) {
        serviceScope.coroutineContext.cancelChildren()
        playbackSession = session
    }

    fun stopPlaybackSynchronization() {
        serviceScope.launch(Dispatchers.IO) {
            playbackSession?.let {
                channel.stopPlayback(
                    sessionId = it.sessionId
                )

                serviceScope.coroutineContext.cancelChildren()
                playbackSession = null
            }
        }
    }

    private fun startSynchronization() {
        serviceScope.launch {
            while (exoPlayer.isPlaying) {
                executeSynchronization()
                delay(SYNC_INTERVAL)
            }
        }
    }

    private fun executeSynchronization() {
        val elapsedMs = exoPlayer.currentPosition
        val overallProgress = getProgress(elapsedMs)

        serviceScope.launch(Dispatchers.IO) {
            playbackSession?.let {
                channel.syncProgress(
                    itemId = it.sessionId,
                    progress = overallProgress
                )
            }
        }
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