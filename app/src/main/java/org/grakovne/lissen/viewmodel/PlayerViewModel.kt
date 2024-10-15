package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.playback.MediaRepository
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfChannel,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val book: LiveData<DetailedBook> = mediaRepository.playingBook

    private val _playingQueueExpanded = MutableLiveData(false)
    val playingQueueExpanded: LiveData<Boolean> = _playingQueueExpanded
    val isPlaybackReady: LiveData<Boolean> = mediaRepository.isPlaybackReady

    val isPlaying: LiveData<Boolean> = mediaRepository.isPlaying
    val currentPosition: LiveData<Long> = mediaRepository.currentPosition

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun calculateTrackIndex(position: Long): Int {
        val currentBook = book.value ?: return 0

        return currentBook
            .files
            .foldIndexed(0) { index, accumulatedDuration, file ->
                val newAccumulatedDuration = accumulatedDuration + file.duration

                if (position < newAccumulatedDuration) {
                    return index
                }

                newAccumulatedDuration.toInt()
            }
    }

    fun calculateTrackPosition(overallPosition: Long): Long {
        val currentBook = book.value ?: return 0L

        var accumulatedDuration = 0.0
        currentBook.files.forEach { file ->
            val fileDuration = file.duration
            if (overallPosition < accumulatedDuration + fileDuration) {
                return (overallPosition - accumulatedDuration).toLong()
            }
            accumulatedDuration += fileDuration
        }

        return (overallPosition - accumulatedDuration).toLong()
    }

    fun preparePlayback(bookId: String) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                mediaRepository.mediaPreparing()
                dataProvider.getLibraryItem(bookId)
            }

            result.foldAsync(
                onSuccess = {
                    withContext(Dispatchers.IO) {
                        mediaRepository.preparePlayingBook(it)
                    }
                },
                onFailure = {
                }
            )
        }
    }

    fun seekTo(position: Float) {
        mediaRepository.seekTo(position)
    }

    fun setChapter(index: Int) {
        mediaRepository.setTrack(index)
    }

    fun nextTrack() {
        mediaRepository.nextTrack()
    }

    fun previousTrack() {
        mediaRepository.previousTrack()
    }

    fun togglePlayPause() {
        when (isPlaying.value) {
            true -> pause()
            else -> play()

        }
    }

    private fun play() {
        mediaRepository.play()
    }

    private fun pause() {
        mediaRepository.pauseAudio()
    }
}