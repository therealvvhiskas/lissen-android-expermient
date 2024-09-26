package org.grakovne.lissen.repository

sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val code: FetchTokenApiError, val message: String? = null) : ApiResult<T>()

    fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (Error<T>) -> R
    ): R {
        return when (this) {
            is Success -> onSuccess(this.data)
            is Error -> onFailure(this)
        }
    }
}