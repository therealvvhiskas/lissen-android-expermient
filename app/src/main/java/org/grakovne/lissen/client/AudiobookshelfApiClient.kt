package org.grakovne.lissen.client

import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemIdResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.client.audiobookshelf.model.LoginRequest
import org.grakovne.lissen.client.audiobookshelf.model.LoginResponse
import org.grakovne.lissen.client.audiobookshelf.model.RecentListeningResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AudiobookshelfApiClient {

    @GET("/api/libraries")
    suspend fun getLibraries(): Response<LibraryResponse>

    @GET("/api/me/listening-stats")
    suspend fun getRecentItems(): Response<RecentListeningResponse>

    @GET("/api/items/{itemId}")
    suspend fun getLibraryItem(
        @Path("itemId") itemId: String
    ): Response<LibraryItemIdResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/libraries/{libraryId}/items")
    suspend fun getLibraryItems(
        @Path("libraryId") libraryId: String
    ): Response<LibraryItemsResponse>
}