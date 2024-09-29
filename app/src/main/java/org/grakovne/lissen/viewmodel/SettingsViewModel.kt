package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.ServerRepository
import org.grakovne.lissen.domain.Library
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _host = MutableLiveData(preferences.getHost())
    val host = _host

    private val _username = MutableLiveData(preferences.getUsername())
    val username = _username

    private val _isConnected = MutableLiveData(true)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _libraries = MutableLiveData<List<Library>>()
    val libraries = _libraries

    private val _preferredLibrary = MutableLiveData<Library>(preferences.getPreferredLibrary())
    val preferredLibrary = _preferredLibrary

    init {
        fetchLibraries()
    }

    fun logout() {
        preferences.clearCredentials()
        serverRepository.logout()

        _host.value = preferences.getHost()
        _username.value = preferences.getUsername()
    }

    fun fetchLibraries() {
        viewModelScope.launch {
            when (val response = serverRepository.fetchLibraries()) {
                is ApiResult.Success -> {
                    _libraries.value = response.data.libraries.map { Library(it.id, it.name) }
                    _preferredLibrary.value = preferences.getPreferredLibrary()
                }

                is ApiResult.Error -> {
                    preferences.getPreferredLibrary()
                }
            }

        }
    }

    fun preferLibrary(library: Library) {
        _preferredLibrary.value = library
        preferences.savePreferredLibrary(library)
    }
}