package org.grakovne.lissen.client

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface AudiobookshelfMediaClient {

    @GET("/api/items/{itemId}/cover")
    @Streaming
    suspend fun getItemCover(
        @Path("itemId") itemId: String,
    ): Response<ResponseBody>

    @GET("/api/items/{libraryItemId}/file/{chapterId}")
    @Streaming
    suspend fun getChapterContent(
        @Path("libraryItemId") libraryItemId: String,
        @Path("chapterId") chapterId: String
    ): Response<ResponseBody>

}