package org.grakovne.lissen.repository

import org.grakovne.lissen.client.AudiobookshelfApiClient
import org.grakovne.lissen.client.audiobookshelf.ApiClient
import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

class ServerRepository @Inject constructor() {
    private lateinit var apiService: AudiobookshelfApiClient

    suspend fun fetchToken(
        host: String,
        username: String,
        password: String
    ): ApiResult<String> {

        if (host.isBlank() || !urlPattern.matches(host)) {
            return ApiResult.Error(FetchTokenApiError.InvalidCredentialsHost)
        }

        try {
            val apiClient = ApiClient(host)
            apiService = apiClient.retrofit.create(AudiobookshelfApiClient::class.java)
        } catch (e: Exception) {
            return ApiResult.Error(FetchTokenApiError.InternalError)
        }

        val response = safeApiCall { apiService.login(LoginRequest(username, password)) }

        return when (response) {
            is ApiResult.Error -> ApiResult.Error(response.code)
            is ApiResult.Success -> response.data
                .user
                .token
                .let { ApiResult.Success(it) }
        }
    }

    private suspend fun <T> safeApiCall(
        apiCall: suspend () -> Response<T>
    ): ApiResult<T> {
        return try {
            val response = apiCall.invoke()

            return when (response.code()) {
                200 -> when (val body = response.body()) {
                    null -> ApiResult.Error(FetchTokenApiError.InternalError)
                    else -> ApiResult.Success(body)
                }

                400 -> ApiResult.Error(FetchTokenApiError.InternalError)
                401 -> ApiResult.Error(FetchTokenApiError.Unauthorized)
                403 -> ApiResult.Error(FetchTokenApiError.Unauthorized)
                404 -> ApiResult.Error(FetchTokenApiError.InternalError)
                500 -> ApiResult.Error(FetchTokenApiError.InternalError)
                else -> ApiResult.Error(FetchTokenApiError.InternalError)
            }
        } catch (e: IOException) {
            ApiResult.Error(FetchTokenApiError.NetworkError)
        } catch (e: Exception) {
            ApiResult.Error(FetchTokenApiError.InternalError)
        }
    }

    companion object {
        val urlPattern = Regex("^(http|https)://.*\$")
    }
}