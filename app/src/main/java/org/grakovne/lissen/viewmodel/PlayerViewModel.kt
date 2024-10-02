package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.ui.screens.player.Track
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {
    private val _book = MutableLiveData<DetailedBook>()
    private val book: LiveData<DetailedBook> = _book

    private val _playlist = MutableLiveData((1..100).map { Track("Chapter $it", "13:15") })
    val playlist: LiveData<List<Track>> = _playlist

    private val _playingQueueExpanded = MutableLiveData(false)
    val playingQueueExpanded = _playingQueueExpanded

    private val _isPlaying = MutableLiveData(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData(136f)
    val currentPosition: LiveData<Float> = _currentPosition

    val duration = (1 * 60 * 60 + 31 * 60 + 14).toFloat()

    private val _speed = MutableLiveData(1f)
    val speed: LiveData<Float> = _speed

    private val _currentTrackIndex = MutableLiveData(0)
    val currentTrackIndex: LiveData<Int> = _currentTrackIndex

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun fetchBookDetails(bookId: String) {

    }


    fun changeSpeed(float: Float) {
        _speed.value = float
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