package org.grakovne.lissen.channel.audiobookshelf

import android.net.Uri
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.grakovne.lissen.channel.audiobookshelf.api.AudioBookshelfDataRepository
import org.grakovne.lissen.channel.audiobookshelf.api.AudioBookshelfMediaRepository
import org.grakovne.lissen.channel.audiobookshelf.api.AudioBookshelfSyncService
import org.grakovne.lissen.channel.audiobookshelf.converter.LibraryItemIdResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.converter.LibraryItemResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.converter.LibraryResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.converter.PlaybackSessionResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.converter.RecentBookResponseConverter
import org.grakovne.lissen.channel.audiobookshelf.model.DeviceInfo
import org.grakovne.lissen.channel.audiobookshelf.model.StartPlaybackRequest
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.channel.common.ApiResult.Success
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.domain.PlaybackProgress
import org.grakovne.lissen.domain.PlaybackSession
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.domain.UserAccount
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookshelfChannel @Inject constructor(
    private val dataRepository: AudioBookshelfDataRepository,
    private val mediaRepository: AudioBookshelfMediaRepository,
    private val recentBookResponseConverter: RecentBookResponseConverter,
    private val libraryItemResponseConverter: LibraryItemResponseConverter,
    private val libraryResponseConverter: LibraryResponseConverter,
    private val libraryItemIdResponseConverter: LibraryItemIdResponseConverter,
    private val sessionResponseConverter: PlaybackSessionResponseConverter,
    private val preferences: LissenSharedPreferences,
    private val syncService: AudioBookshelfSyncService
) {

    fun provideFileUri(
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

    fun provideBookCover(
        libraryItemId: String
    ): Uri = Uri.parse(preferences.getHost())
        .buildUpon()
        .appendPath("api")
        .appendPath("items")
        .appendPath(libraryItemId)
        .appendPath("cover")
        .appendQueryParameter("token", preferences.getToken())
        .build()

    suspend fun syncProgress(
        itemId: String,
        progress: PlaybackProgress
    ): ApiResult<Unit> = syncService.syncProgress(itemId, progress)

    suspend fun fetchBookCover(
        itemId: String
    ): ApiResult<InputStream> = mediaRepository.fetchBookCover(itemId)

    suspend fun fetchBooks(
        libraryId: String
    ): ApiResult<List<Book>> = dataRepository
        .fetchLibraryItems(libraryId)
        .map { libraryItemResponseConverter.apply(it) }

    suspend fun fetchLibraries(): ApiResult<List<Library>> = dataRepository
        .fetchLibraries()
        .map { libraryResponseConverter.apply(it) }

    suspend fun startPlayback(
        itemId: String,
        supportedMimeTypes: List<String>,
        deviceId: String
    ): ApiResult<PlaybackSession> {
        val request = StartPlaybackRequest(
            supportedMimeTypes = supportedMimeTypes,
            deviceInfo = DeviceInfo(
                clientName = getClientName(),
                deviceId = deviceId
            ),
            forceTranscode = false,
            forceDirectPlay = false,
            mediaPlayer = getClientName()
        )

        return dataRepository
            .startPlayback(
                itemId = itemId,
                request = request
            )
            .map { sessionResponseConverter.apply(it) }
    }

    suspend fun fetchRecentListenedBooks(libraryId: String): ApiResult<List<RecentBook>> =
        dataRepository
            .fetchPersonalizedFeed(libraryId)
            .map { recentBookResponseConverter.apply(it) }

    suspend fun fetchBook(itemId: String): ApiResult<DetailedBook> = coroutineScope {
        val libraryItem = async { dataRepository.fetchLibraryItem(itemId) }
        val itemProgress = async { dataRepository.fetchLibraryItemProgress(itemId) }

        libraryItem.await().foldAsync(
            onSuccess = { item ->
                itemProgress
                    .await()
                    .fold(
                        onSuccess = { Success(libraryItemIdResponseConverter.apply(item, it)) },
                        onFailure = { Success(libraryItemIdResponseConverter.apply(item, null)) }
                    )
            },
            onFailure = { ApiResult.Error(it.code) }
        )
    }

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> = dataRepository.authorize(host, username, password)


    private fun getClientName() = "Lissen App Android"
}