package org.grakovne.lissen.domain.error

sealed class LoginError {
    data object Unauthorized : LoginError()
    data object NetworkError : LoginError()
    data object InvalidCredentialsHost : LoginError()
    data object MissingCredentialsHost : LoginError()
    data object MissingCredentialsUsername : LoginError()
    data object MissingCredentialsPassword : LoginError()
    data object InternalError : LoginError()
}