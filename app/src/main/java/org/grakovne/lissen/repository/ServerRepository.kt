package org.grakovne.lissen.repository

import org.grakovne.lissen.client.AudiobookshelfApiClient
import org.grakovne.lissen.client.audiobookshelf.ApiClient
import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import javax.inject.Inject

class ServerRepository @Inject constructor() {

    suspend fun fetchToken(
        host: String?,
        username: String?,
        password: String?
    ): ApiResult<String> {
        val apiClient = ApiClient(host ?: "")
        val apiService = apiClient.retrofit.create(AudiobookshelfApiClient::class.java)

        val response = apiService.login(LoginRequest(username ?: "", password ?: ""))

        return when (response.isSuccessful) {
            true -> response.body()?.user?.token?.let { ApiResult.Success(it) } ?: ApiResult.Error(
                500,
                ""
            )

            false -> ApiResult.Error(response.code(), response.errorBody()?.string() ?: "")
        }
    }
}


sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val code: Int, val message: String) : ApiResult<T>()

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