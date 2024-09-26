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

    private val preferences = ServerConnectionPreferences.getInstance()

    private val _host = MutableLiveData(preferences.getHost())
    val host = _host

    private val _username = MutableLiveData(preferences.getUsername())
    val username = _username

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isConnected = MutableLiveData(true)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _libraries = MutableLiveData<List<Library>>()
    val libraries = _libraries

    private val _preferredLibrary = MutableLiveData<Library>()
    val preferredLibrary = _preferredLibrary

    init {
        fetchLibraries()
    }

    fun logout() {
        preferences.clearCredentials()

        _host.value = preferences.getHost()
        _username.value = preferences.getUsername()
    }

    fun fetchLibraries() {
        viewModelScope.launch {
            _isLoading.value = true

            val response = serverRepository.fetchLibraries()
            when (response) {
                is ApiResult.Success -> {
                    _libraries.value = response.data.libraries.map { Library(it.id, it.name) }
                    _preferredLibrary.value = _libraries.value?.firstOrNull()
                }
                is ApiResult.Error -> {
                    // show from cache if any
                }
            }

            _isLoading.value = false
        }
    }

    fun preferLibrary(library: Library) {
        _preferredLibrary.value = library
    }
}