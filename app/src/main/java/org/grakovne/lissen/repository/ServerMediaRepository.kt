package org.grakovne.lissen.repository

import okhttp3.ResponseBody
import org.grakovne.lissen.client.AudiobookshelfMediaClient
import org.grakovne.lissen.client.audiobookshelf.BinaryApiClient
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerMediaRepository @Inject constructor(
) {
    private val preferences = LissenSharedPreferences.getInstance()

    @Volatile
    private var secureClient: AudiobookshelfMediaClient? = null

    suspend fun fetchBookCover(itemId: String): ApiResult<InputStream> =
        safeApiCall { getClientInstance().getItemCover(itemId) }

    private suspend fun safeApiCall(
        apiCall: suspend () -> Response<ResponseBody>
    ): ApiResult<InputStream> {
        return try {
            val response = apiCall.invoke()

            return when (response.code()) {
                200 -> when (val body = response.body()) {
                    null -> ApiResult.Error(FetchTokenApiError.InternalError)
                    else -> ApiResult.Success(body.byteStream())
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
            println(e)
            ApiResult.Error(FetchTokenApiError.InternalError)
        }
    }

    private fun getClientInstance(): AudiobookshelfMediaClient {
        val host = preferences.getHost()
        val token = preferences.getToken()

        if (host.isNullOrBlank() || token.isNullOrBlank()) {
            throw IllegalStateException("Host or token is missing")
        }

        return secureClient ?: run {
            val apiClient = BinaryApiClient(host, token)
            apiClient.retrofit.create(AudiobookshelfMediaClient::class.java)
        }
    }

    companion object {
        val urlPattern = Regex("^(http|https)://.*\$")
    }
}