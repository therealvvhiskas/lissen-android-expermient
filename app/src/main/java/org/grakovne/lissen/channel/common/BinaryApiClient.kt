package org.grakovne.lissen.channel.common

import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.io.File
import java.util.concurrent.TimeUnit


class BinaryApiClient(
    host: String,
    token: String,
    cacheDir: File
) {

    private val cacheSize: Long = 128 * 1024 * 1024

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val httpClient = OkHttpClient.Builder()
        .cache(Cache(cacheDir, cacheSize))
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(host)
        .client(httpClient)
        .client(secureClient(token))
        .build()

    companion object {
        fun secureClient(token: String): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    val request = chain
                        .request()
                        .newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()

                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}