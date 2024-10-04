package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.converter.LibraryItemIdResponseConverter
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.player.service.MediaRepository
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
        mediaRepository.playAudio("https://audiobook.grakovne.org/api/items/82c176ce-4387-4df6-976e-42669ed0502b/file/-113301814924710012?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjM2QzMjQ1Mi1lZDFjLTRlZjktYWJkMC00ZTg0MTcwNGVmMTUiLCJ1c2VybmFtZSI6ImdyYWtvdm5lIiwiaWF0IjoxNzIzNTkxMzU2fQ.3G-Kes9PqAycvpMqdo2BKLsZmf-R1ihRBGD568uS0s4")
        mediaRepository._isPlaying.postValue(true)
    }

    fun pause() {
        mediaRepository._isPlaying.postValue(false)
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
        if (isPlaying.value == true) {
            pause()
        } else {
            play()
        }
    }
}