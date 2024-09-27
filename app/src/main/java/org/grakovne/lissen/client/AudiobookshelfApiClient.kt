package org.grakovne.lissen.client

import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import org.grakovne.lissen.client.audiobookshelf.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AudiobookshelfApiClient {

    @GET("/api/libraries")
    suspend fun getLibraries(): Response<LibraryResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/libraries/{libraryId}/items")
    suspend fun getLibraryItems(
        @Path("libraryId") libraryId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<LibraryItemsResponse>
}