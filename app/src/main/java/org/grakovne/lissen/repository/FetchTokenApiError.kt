package org.grakovne.lissen.repository

sealed class FetchTokenApiError {
    data object Unauthorized : FetchTokenApiError()
    data object NetworkError : FetchTokenApiError()
    data object InvalidCredentialsHost : FetchTokenApiError()
    data object MissingCredentialsHost : FetchTokenApiError()
    data object MissingCredentialsUsername : FetchTokenApiError()
    data object MissingCredentialsPassword : FetchTokenApiError()
    data object InternalError : FetchTokenApiError()
}