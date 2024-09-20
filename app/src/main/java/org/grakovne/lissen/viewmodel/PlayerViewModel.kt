package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.grakovne.lissen.ui.screens.player.Track

class PlayerViewModel : ViewModel() {

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

    private val _playlist = MutableLiveData(

        (1..100).map { Track("Chapter $it", "13:15") }
    )
    val playlist: LiveData<List<Track>> = _playlist

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun closePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun expandPlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: true)
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