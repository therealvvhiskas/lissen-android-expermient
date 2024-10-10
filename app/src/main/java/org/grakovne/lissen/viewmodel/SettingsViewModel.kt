package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.channel.audiobookshelf.converter.LibraryResponseConverter
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfDataProvider
import org.grakovne.lissen.repository.ApiResult
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfDataProvider,
    private val libraryResponseConverter: LibraryResponseConverter
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

        _host.value = preferences.getHost()
        _username.value = preferences.getUsername()
    }

    fun fetchLibraries() {
        viewModelScope.launch {
            when (val response = dataProvider.fetchLibraries()) {
                is ApiResult.Success -> {
                    val libraries = libraryResponseConverter.apply(response.data)
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