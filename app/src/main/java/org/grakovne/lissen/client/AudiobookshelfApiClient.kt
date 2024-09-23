package org.grakovne.lissen.client

import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import org.grakovne.lissen.client.audiobookshelf.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AudiobookshelfApiClient {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}