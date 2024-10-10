package org.grakovne.lissen.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfDataProvider
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.domain.UserAccount
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.repository.FetchTokenApiError
import org.grakovne.lissen.repository.FetchTokenApiError.InternalError
import org.grakovne.lissen.repository.FetchTokenApiError.InvalidCredentialsHost
import org.grakovne.lissen.repository.FetchTokenApiError.MissingCredentialsHost
import org.grakovne.lissen.repository.FetchTokenApiError.MissingCredentialsPassword
import org.grakovne.lissen.repository.FetchTokenApiError.MissingCredentialsUsername
import org.grakovne.lissen.repository.FetchTokenApiError.Unauthorized
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfDataProvider,
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _loginError: MutableLiveData<String> = MutableLiveData()
    val loginError = _loginError

    private val _host = MutableLiveData(preferences.getHost() ?: "")
    val host = _host

    private val _username = MutableLiveData(preferences.getUsername() ?: "")
    val username = _username

    private val _password = MutableLiveData("")
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

    fun readyToLogin() {
        _loginState.value = LoginState.Idle
    }

    fun login() {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val host = host.value ?: run {
                _loginState.value = LoginState.Error(MissingCredentialsHost)
                return@launch
            }

            val username = username.value ?: run {
                _loginState.value = LoginState.Error(MissingCredentialsUsername)
                return@launch
            }

            val password = password.value ?: run {
                _loginState.value = LoginState.Error(MissingCredentialsPassword)
                return@launch
            }

            val response = dataProvider.authorize(host, username, password)

            val result = response
                .foldAsync(
                    onSuccess = { account -> onLoginSuccessful(host, username, account) },
                    onFailure = { error -> onLoginFailure(error.code) }
                )

            _loginState.value = result
        }
    }

    private fun persistCredentials(
        host: String,
        username: String,
        token: String
    ) {
        preferences.saveHost(host)
        preferences.saveUsername(username)
        preferences.saveToken(token)
    }

    private suspend fun onLoginSuccessful(
        host: String,
        username: String,
        account: UserAccount
    ): LoginState.Success {

        persistCredentials(
            host = host,
            username = username,
            token = account.token
        )

        dataProvider
            .fetchLibraries()
            .fold(
                onSuccess = {
                    it
                        .find { item -> item.id == account.preferredLibraryId }
                        ?.let { library ->
                            preferences.savePreferredLibrary(Library(library.id, library.title))
                        }
                },
                onFailure = {
                    preferences.savePreferredLibrary(
                        Library(
                            account.preferredLibraryId,
                            "Default Library"
                        )
                    )
                }
            )

        return LoginState.Success
    }

    private fun onLoginFailure(error: FetchTokenApiError): LoginState.Error {
        _loginError.value = when (error) {
            InternalError -> "Host is down"
            MissingCredentialsHost -> "Host URL is missing"
            MissingCredentialsPassword -> "Username is missing"
            MissingCredentialsUsername -> "Password is missing"
            Unauthorized -> "Credentials are invalid"
            InvalidCredentialsHost -> "Host URL shall be https:// or http://"
            FetchTokenApiError.NetworkError -> "Connection Error"
        }

        return LoginState.Error(error)
    }

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data object Success : LoginState()
        data class Error(val message: FetchTokenApiError) : LoginState()
    }
}