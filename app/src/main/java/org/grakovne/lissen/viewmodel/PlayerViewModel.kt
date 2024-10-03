package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.converter.LibraryItemIdResponseConverter
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.repository.ServerRepository
import org.grakovne.lissen.ui.screens.player.Track
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val libraryItemIdResponseConverter: LibraryItemIdResponseConverter
) : ViewModel() {
    private val _book = MutableLiveData<DetailedBook>()
    val book: LiveData<DetailedBook> = _book

    private val _playingQueueExpanded = MutableLiveData(false)
    val playingQueueExpanded = _playingQueueExpanded

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData(0f)
    val currentPosition: LiveData<Float> = _currentPosition

    private val _currentTrackIndex = MutableLiveData(0)
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
        _isPlaying.value = true
    }

    fun pause() {
        _isPlaying.value = false
    }

    fun seekTo(position: Float) {
        _currentPosition.value = position
    }

    fun setChapter(index: Int) {
        _currentTrackIndex.value = index
    }

    fun nextTrack() {
        _currentTrackIndex.value = _currentTrackIndex.value?.plus(1)
    }

    fun previousTrack() {
        _currentTrackIndex.value = _currentTrackIndex.value?.minus(1)
    }

    fun togglePlayPause() {
        if (_isPlaying.value == true) {
            pause()
        } else {
            play()
        }
    }
}