package org.grakovne.lissen.channel.audiobookshelf.api

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.grakovne.lissen.channel.audiobookshelf.client.AudiobookshelfApiClient
import org.grakovne.lissen.channel.audiobookshelf.converter.LoginResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.model.LibraryItemIdResponse
import org.grakovne.lissen.channel.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.channel.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.channel.audiobookshelf.model.LoginRequest
import org.grakovne.lissen.channel.audiobookshelf.model.LoginResponse
import org.grakovne.lissen.channel.audiobookshelf.model.MediaProgressResponse
import org.grakovne.lissen.channel.audiobookshelf.model.PlaybackSessionResponse
import org.grakovne.lissen.channel.audiobookshelf.model.RecentListeningResponse
import org.grakovne.lissen.channel.audiobookshelf.model.StartPlaybackRequest
import org.grakovne.lissen.channel.audiobookshelf.model.SyncProgressRequest
import org.grakovne.lissen.channel.common.ApiClient
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.channel.common.FetchTokenApiError
import org.grakovne.lissen.domain.UserAccount
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioBookshelfDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginResponseConverter: LoginResponseConverter,
    private val preferences: LissenSharedPreferences
) {

    @Volatile
    private var secureClient: AudiobookshelfApiClient? = null

    suspend fun fetchLibraryItems(
        libraryId: String
    ): ApiResult<LibraryItemsResponse> {
        return safeApiCall { getClientInstance().getLibraryItems(libraryId) }
    }

    suspend fun startPlayback(
        itemId: String,
        request: StartPlaybackRequest
    ): ApiResult<PlaybackSessionResponse> =
        safeApiCall { getClientInstance().startPlayback(itemId, request) }

    suspend fun stopPlayback(sessionId: String): ApiResult<Unit> =
        safeApiCall { getClientInstance().stopPlayback(sessionId) }

    suspend fun syncProgress(
        itemId: String,
        progress: SyncProgressRequest
    ): ApiResult<Unit> =
        safeApiCall { getClientInstance().syncProgress(itemId, progress) }

    suspend fun fetchLibraries(): ApiResult<LibraryResponse> =
        safeApiCall { getClientInstance().getLibraries() }

    suspend fun getRecentItems(): ApiResult<RecentListeningResponse> =
        safeApiCall { getClientInstance().getRecentItems() }


    suspend fun getItemIdProgress(itemId: String): ApiResult<MediaProgressResponse> =
        safeApiCall {
            getClientInstance().getLibraryItemProgress(itemId)
        }

    suspend fun getLibraryItem(itemId: String): ApiResult<LibraryItemIdResponse> =
        safeApiCall { getClientInstance().getLibraryItem(itemId) }

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> {
        secureClient = null

        if (host.isBlank() || !urlPattern.matches(host)) {
            return ApiResult.Error(FetchTokenApiError.InvalidCredentialsHost)
        }

        lateinit var apiService: AudiobookshelfApiClient

        try {
            val apiClient = ApiClient(host = host, cacheDir = context.cacheDir)
            apiService = apiClient.retrofit.create(AudiobookshelfApiClient::class.java)
        } catch (e: Exception) {
            return ApiResult.Error(FetchTokenApiError.InternalError)
        }

        val response: ApiResult<LoginResponse> =
            safeApiCall { apiService.login(LoginRequest(username, password)) }
        return response.fold(
            onSuccess = {
                loginResponseConverter
                    .apply(it)
                    .let { ApiResult.Success(it) }
            },
            onFailure = { ApiResult.Error(it.code) }
        )

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
            val apiClient = ApiClient(host = host, token = token, cacheDir = context.cacheDir)
            apiClient.retrofit.create(AudiobookshelfApiClient::class.java)
        }
    }

    companion object {
        val urlPattern = Regex("^(http|https)://.*\$")
    }
}