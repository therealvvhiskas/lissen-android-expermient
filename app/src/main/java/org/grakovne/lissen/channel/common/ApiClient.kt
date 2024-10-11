package org.grakovne.lissen.channel.common

import android.content.Context
import android.content.ContextWrapper
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class ApiClient(
    host: String,
    token: String? = null,
    cacheDir: File
) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .cache(Cache(cacheDir, 10 * 1024 * 1024))
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(host)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .client(secureClient(token))
            .build()

    companion object {
        fun secureClient(token: String?): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain: Interceptor.Chain ->
                    val original: Request = chain.request()
                    val requestBuilder: Request.Builder = original.newBuilder()

                    if (null != token) {
                        requestBuilder
                            .header("Authorization", "Bearer $token")
                    }

                    val request: Request = requestBuilder.build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}