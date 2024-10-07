package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.converter.LibraryItemIdResponseConverter
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.player.MediaRepository
import org.grakovne.lissen.repository.ServerRepository
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val libraryItemIdResponseConverter: LibraryItemIdResponseConverter,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    private val _book = MutableLiveData<DetailedBook>()
    val book: LiveData<DetailedBook> = _book

    private val _playingQueueExpanded = MutableLiveData(false)
    val playingQueueExpanded = _playingQueueExpanded

    val isPlaying = mediaRepository._isPlaying
    val currentPosition = mediaRepository._currentPosition

    private val _currentTrackIndex = mediaRepository._currentMediaItemIndex
    val currentTrackIndex: LiveData<Int> = _currentTrackIndex

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun fetchBookDetails(bookId: String) {
        viewModelScope
            .launch {
                serverRepository
                    .getLibraryItem(bookId)
                    .fold(
                        onSuccess = {
                            _book.value = libraryItemIdResponseConverter.apply(it)
                        },
                        onFailure = {
                            // ahaha, loshara
                        }
                    )
            }

    }

    fun play() {
        book.value?.let { mediaRepository.playAudio(it) }
    }

    fun pause() {
        mediaRepository.pauseAudio()
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
        if (isPlaying.value == true) {
            pause()
        } else {
            play()
        }
    }
}