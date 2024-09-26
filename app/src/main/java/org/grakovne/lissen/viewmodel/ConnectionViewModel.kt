package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerRepository
import org.grakovne.lissen.ui.screens.settings.Library
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {
    private val _isConnected = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _libraries = MutableLiveData<List<Library>>()
    val libraries = _libraries

    private val _preferredLibrary = MutableLiveData<Library>()
    val preferredLibrary = _preferredLibrary

    init {
        fetchLibraries()
    }

    private fun fetchLibraries() {
        viewModelScope.launch {
            val response = serverRepository.fetchLibraries()

            when (response) {
                is ApiResult.Success -> {
                    _libraries.value = response.data.libraries.map { Library(it.id, it.name) }
                    _preferredLibrary.value = _libraries.value?.firstOrNull()
                }
                is ApiResult.Error -> {

                }
            }
        }
    }

    fun preferLibrary(library: Library) {
        _preferredLibrary.value = library
    }
}