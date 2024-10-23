package org.grakovne.lissen.channel.common

sealed class ApiError {
    data object Unauthorized : ApiError()
    data object NetworkError : ApiError()
    data object InvalidCredentialsHost : ApiError()
    data object MissingCredentialsHost : ApiError()
    data object MissingCredentialsUsername : ApiError()
    data object MissingCredentialsPassword : ApiError()
    data object InternalError : ApiError()
    data object UnsupportedError : ApiError()
}
