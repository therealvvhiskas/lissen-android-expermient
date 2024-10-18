package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.content.LissenMediaChannel
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val mediaChannel: LissenMediaChannel,
    private val preferences: LissenSharedPreferences
) : ViewModel() {

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

        _host.value = preferences.getHost()
        _username.value = preferences.getUsername()
    }

    fun fetchLibraries() {
        viewModelScope.launch {
            when (val response = mediaChannel.fetchLibraries()) {
                is ApiResult.Success -> {
                    val libraries = response.data
                    _libraries.value = libraries

                    when (val preferredLibrary = preferences.getPreferredLibrary()) {
                        null -> libraries.firstOrNull()
                        else -> libraries
                            .find { it.id == preferredLibrary.id }
                            ?: libraries.firstOrNull()
                    }
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