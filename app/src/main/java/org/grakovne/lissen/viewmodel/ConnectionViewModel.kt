package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.grakovne.lissen.ui.screens.settings.Library

class ConnectionViewModel : ViewModel() {
    private val _isConnected = MutableLiveData(false)
    val isConnected: LiveData<Boolean> = _isConnected

    private val _libraries = MutableLiveData<List<Library>>()
    val libraries = _libraries

    private val _url = MutableLiveData<String>()
    val url = _url

    private val _login = MutableLiveData<String>()
    val login = _login

    private val _preferredLibrary = MutableLiveData<Library>()
    val preferredLibrary = _preferredLibrary

    fun preferLibrary(library: Library) {
        _preferredLibrary.value = library
    }

    fun updateServerUrl(url: String) {
        _url.value = url
    }

    fun updateLogin(login: String) {
        _login.value = login
    }
}