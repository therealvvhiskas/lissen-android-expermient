package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

    private val mediaItemPosition: LiveData<Long> = mediaRepository.mediaItemPosition
    private val _playingQueueExpanded = MutableLiveData(false)

    val playingQueueExpanded: LiveData<Boolean> = _playingQueueExpanded
    val isPlaybackReady: LiveData<Boolean> = mediaRepository.isPlaybackReady

    val isPlaying: LiveData<Boolean> = mediaRepository.isPlaying

    private val _currentTrackIndex = MediatorLiveData<Int>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }
    val currentTrackIndex: LiveData<Int> = _currentTrackIndex

    private val _currentTrackPosition = MediatorLiveData<Long>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }
    val currentTrackPosition: LiveData<Long> = _currentTrackPosition

    private val _currentTrackDuration = MediatorLiveData<Float>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }

    val currentTrackDuration: LiveData<Float> = _currentTrackDuration

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    private fun updateCurrentTrackData() {
        val book = book.value ?: return
        val position = mediaItemPosition.value ?: return

        val trackIndex = calculateTrackIndex(position)
        _currentTrackIndex.value = trackIndex
        _currentTrackPosition.value = calculateTrackPosition(position)
        _currentTrackDuration.value = book
            .chapters
            .getOrNull(trackIndex)
            ?.let { it.end - it.start }
            ?.toFloat() ?: 0f
    }

    fun calculateTrackIndex(position: Long): Int {
        val currentBook = book.value ?: return 0

        return currentBook
            .chapters
            .foldIndexed(0) { index, accumulatedDuration, file ->
                val newAccumulatedDuration = accumulatedDuration + (file.end - file.start)

                if (position < newAccumulatedDuration) {
                    return index
                }

                newAccumulatedDuration.toInt()
            }
    }

    fun calculateTrackPosition(overallPosition: Long): Long {
        val currentBook = book.value ?: return 0L

        var accumulatedDuration = 0.0
        currentBook
            .chapters
            .forEach { file ->
                val fileDuration = (file.end - file.start)
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