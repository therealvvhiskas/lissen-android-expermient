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

    private val _currentChapterIndex = MediatorLiveData<Int>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }
    val currentChapterIndex: LiveData<Int> = _currentChapterIndex

    private val _currentChapterPosition = MediatorLiveData<Long>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }
    val currentChapterPosition: LiveData<Long> = _currentChapterPosition

    private val _currentChapterDuration = MediatorLiveData<Long>().apply {
        addSource(mediaItemPosition) { updateCurrentTrackData() }
        addSource(book) { updateCurrentTrackData() }
    }
    val currentChapterDuration: LiveData<Long> = _currentChapterDuration

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    private fun updateCurrentTrackData() {
        val book = book.value ?: return
        val position = mediaItemPosition.value ?: return

        val trackIndex = calculateChapterIndex(position)
        val trackPosition = calculateChapterPosition(position)

        _currentChapterIndex.value = trackIndex
        _currentChapterPosition.value = trackPosition
        _currentChapterDuration.value = book
            .chapters
            .getOrNull(trackIndex)
            ?.duration
            ?.toLong()
            ?: 0L
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

    fun seekTo(chapterPosition: Long) {
        val absolutePosition = currentChapterIndex.value
            ?.let { chapterIndex ->
                book
                    .value
                    ?.chapters
                    ?.get(chapterIndex)
                    ?.start
                    ?.toLong()
            }
            ?.let { it + chapterPosition } ?: return

        mediaRepository.seekTo(absolutePosition)
    }

    fun setChapter(index: Int) {
        val chapterStartsAt = book
            .value
            ?.chapters
            ?.get(index)
            ?.start
            ?.toLong()
            ?: 0L

        mediaRepository.seekTo(chapterStartsAt)
    }

    fun nextTrack() {
        val nextChapterIndex = currentChapterIndex.value?.let { it + 1 } ?: return
        setChapter(nextChapterIndex)
    }

    fun previousTrack() {
        val previousChapterIndex = currentChapterIndex.value?.let { it - 1 } ?: return
        setChapter(previousChapterIndex)
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


    private fun calculateChapterIndex(position: Long): Int {
        val currentBook = book.value ?: return 0
        var accumulatedDuration = 0.0

        for ((index, chapter) in currentBook.chapters.withIndex()) {
            accumulatedDuration += chapter.duration
            if (position < accumulatedDuration) {
                return index
            }
        }

        return currentBook.chapters.size - 1
    }

    private fun calculateChapterPosition(overallPosition: Long): Long {
        val currentBook = book.value ?: return 0L
        var accumulatedDuration = 0.0

        for (chapter in currentBook.chapters) {
            val chapterEnd = accumulatedDuration + chapter.duration
            if (overallPosition < chapterEnd) {
                return (overallPosition - accumulatedDuration).toLong()
            }
            accumulatedDuration = chapterEnd
        }

        return 0L
    }
}