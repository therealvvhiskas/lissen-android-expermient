package org.grakovne.lissen.viewmodel

import ServerConnectionPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.grakovne.lissen.repository.ServerRepository
import javax.inject.Inject

@HiltViewModel
class ServerConnectionViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val preferences = ServerConnectionPreferences.getInstance()

    private val _host = MutableLiveData(preferences.getHost() ?: "https://")
    val host = _host

    private val _username = MutableLiveData(preferences.getUsername() ?: "")
    val username = _username

    private val _password = MutableLiveData(preferences.getPassword() ?: "")
    val password = _password

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun setHost(host: String) {
        _host.value = host
    }

    fun setUsername(username: String) {
        _username.value = username
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            //val result = repository.fetchToken(host.value, username.value, password.value)
            val response = Result.success("host")

            val result = response
                .fold(
                    onSuccess = { token -> LoginState.Success(token) },
                    onFailure = { error -> LoginState.Error(error.message ?: "Unknown error") }
                )

            _loginState.value = result
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val token: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }

}