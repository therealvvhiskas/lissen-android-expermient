package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfDataProvider
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.playback.MediaRepository
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfDataProvider,
    private val mediaRepository: MediaRepository
) : ViewModel() {

    val book: LiveData<DetailedBook> = mediaRepository.playingBook

    private val _playingQueueExpanded = MutableLiveData(false)
    val playingQueueExpanded: LiveData<Boolean> = _playingQueueExpanded
    val isPlaybackReady: LiveData<Boolean> = mediaRepository.isPlaybackReady

    val isPlaying: LiveData<Boolean> = mediaRepository.isPlaying
    val currentPosition: LiveData<Long> = mediaRepository.currentPosition
    val currentTrackIndex: LiveData<Int> = mediaRepository.currentMediaItemIndex

    fun togglePlayingQueue() {
        _playingQueueExpanded.value = !(_playingQueueExpanded.value ?: false)
    }

    fun preparePlayback(bookId: String) {
        mediaRepository.mediaPreparing()

        viewModelScope.launch {
            dataProvider.getLibraryItem(bookId).fold(
                onSuccess = { mediaRepository.preparePlayingBook(it) },
                onFailure = {}
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