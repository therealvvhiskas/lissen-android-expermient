package org.grakovne.lissen.repository

import LissenSharedPreferences
import org.grakovne.lissen.client.AudiobookshelfApiClient
import org.grakovne.lissen.client.audiobookshelf.ApiClient
import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import org.grakovne.lissen.client.audiobookshelf.model.LoginResponse
import org.grakovne.lissen.domain.UserAccount
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerRepository @Inject constructor(
) {
    private val preferences = LissenSharedPreferences.getInstance()

    @Volatile
    private var secureClient: AudiobookshelfApiClient? = null

    suspend fun fetchLibraryItems(
        libraryId: String
    ): ApiResult<LibraryItemsResponse> {
        return safeApiCall { getClientInstance().getLibraryItems(libraryId) }
    }

    suspend fun fetchLibraries(): ApiResult<LibraryResponse> = safeApiCall { getClientInstance().getLibraries() }
    suspend fun fetchBookCover(itemId: String): ApiResult<ByteArray> = safeApiCall { getClientInstance().getItemCover(itemId) }

    fun logout() {
        secureClient = null
    }

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> {

        if (host.isBlank() || !urlPattern.matches(host)) {
            return ApiResult.Error(FetchTokenApiError.InvalidCredentialsHost)
        }

        lateinit var apiService: AudiobookshelfApiClient

        try {
            val apiClient = ApiClient(host)
            apiService = apiClient.retrofit.create(AudiobookshelfApiClient::class.java)
        } catch (e: Exception) {
            return ApiResult.Error(FetchTokenApiError.InternalError)
        }

        val response: ApiResult<LoginResponse> = safeApiCall { apiService.login(LoginRequest(username, password)) }

        return when (response) {
            is ApiResult.Error -> ApiResult.Error(response.code)
            is ApiResult.Success -> response.data
                .let {
                    UserAccount(
                        token = it.user.token,
                        preferredLibraryId = it.userDefaultLibraryId
                    )
                }
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

    private fun getClientInstance(): AudiobookshelfApiClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        if (host.isNullOrBlank() || token.isNullOrBlank()) {
            throw IllegalStateException("Host or token is missing")
        }

        return secureClient ?: run {
            val apiClient = ApiClient(host, token)
            apiClient.retrofit.create(AudiobookshelfApiClient::class.java)
        }
    }

    companion object {
        val urlPattern = Regex("^(http|https)://.*\$")
    }
}