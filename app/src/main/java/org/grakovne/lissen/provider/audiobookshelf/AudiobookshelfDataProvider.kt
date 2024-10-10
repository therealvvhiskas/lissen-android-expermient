package org.grakovne.lissen.provider.audiobookshelf

import android.net.Uri
import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemIdResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.client.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.client.audiobookshelf.model.RecentListeningResponse
import org.grakovne.lissen.domain.UserAccount
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.repository.ApiResult
import org.grakovne.lissen.repository.audiobookshelf.AudioBookshelfDataRepository
import org.grakovne.lissen.repository.audiobookshelf.AudioBookshelfMediaRepository
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookshelfDataProvider @Inject constructor(
    private val dataRepository: AudioBookshelfDataRepository,
    private val mediaRepository: AudioBookshelfMediaRepository
) {

    private val preferences = LissenSharedPreferences.getInstance()

    fun provideUri(
        libraryItemId: String,
        chapterId: String
    ): Uri = Uri.parse(preferences.getHost())
        .buildUpon()
        .appendPath("api")
        .appendPath("items")
        .appendPath(libraryItemId)
        .appendPath("file")
        .appendPath(chapterId)
        .appendQueryParameter("token", preferences.getToken())
        .build()

    suspend fun fetchBookCover(
        itemId: String
    ): ApiResult<InputStream> = mediaRepository.fetchBookCover(itemId)

    suspend fun fetchLibraryItems(
        libraryId: String
    ): ApiResult<LibraryItemsResponse> = dataRepository.fetchLibraryItems(libraryId)

    suspend fun fetchLibraries(): ApiResult<LibraryResponse> = dataRepository.fetchLibraries()

    suspend fun getRecentItems(): ApiResult<RecentListeningResponse> =
        dataRepository.getRecentItems()

    suspend fun getLibraryItem(itemId: String): ApiResult<LibraryItemIdResponse> =
        dataRepository.getLibraryItem(itemId)

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> = dataRepository.authorize(host, username, password)


}