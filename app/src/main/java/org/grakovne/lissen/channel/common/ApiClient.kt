package org.grakovne.lissen.channel.common

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
    private val cacheSize: Long = 10 * 1024 * 1024

    private val httpClient = OkHttpClient.Builder()
        .cache(Cache(cacheDir, cacheSize))
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        })
        .addInterceptor { chain: Interceptor.Chain ->
            val original: Request = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()

            if (token != null) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit =
        Retrofit.Builder()
            .baseUrl(host)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}