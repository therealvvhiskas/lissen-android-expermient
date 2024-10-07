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

    @GET("/api/items/49fcdfab-2276-47b7-86c9-0b66098d4c5b/file/140182086?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjM2QzMjQ1Mi1lZDFjLTRlZjktYWJkMC00ZTg0MTcwNGVmMTUiLCJ1c2VybmFtZSI6ImdyYWtvdm5lIiwiaWF0IjoxNzIzNTkxMzU2fQ.3G-Kes9PqAycvpMqdo2BKLsZmf-R1ihRBGD568uS0s4")
    @Streaming
    suspend fun getChapterContent(
    ): Response<ResponseBody>

}