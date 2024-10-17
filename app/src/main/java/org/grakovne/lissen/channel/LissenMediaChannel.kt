package org.grakovne.lissen.channel

import android.net.Uri
import org.grakovne.lissen.ChannelCode
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.domain.PagedItems
import org.grakovne.lissen.domain.PlaybackProgress
import org.grakovne.lissen.domain.PlaybackSession
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.domain.UserAccount
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LissenMediaChannel @Inject constructor(
    private val sharedPreferences: LissenSharedPreferences,
    private val channels: Map<ChannelCode, @JvmSuppressWildcards AudiobookshelfChannel>
) {

    fun provideFileUri(
        libraryItemId: String,
        chapterId: String
    ): Uri = providePreferredChannel().provideFileUri(libraryItemId, chapterId)

    fun provideBookCover(
        bookId: String
    ): Uri = providePreferredChannel().provideBookCover(bookId)

    suspend fun syncProgress(
        itemId: String,
        progress: PlaybackProgress
    ): ApiResult<Unit> = providePreferredChannel().syncProgress(itemId, progress)

    suspend fun fetchBookCover(
        itemId: String
    ): ApiResult<InputStream> = providePreferredChannel().fetchBookCover(itemId)

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): ApiResult<PagedItems<Book>> =
        providePreferredChannel().fetchBooks(libraryId, pageSize, pageNumber)

    suspend fun fetchLibraries(): ApiResult<List<Library>> =
        providePreferredChannel().fetchLibraries()

    suspend fun startPlayback(
        itemId: String,
        supportedMimeTypes: List<String>,
        deviceId: String
    ): ApiResult<PlaybackSession> =
        providePreferredChannel().startPlayback(itemId, supportedMimeTypes, deviceId)

    suspend fun fetchRecentListenedBooks(
        libraryId: String
    ): ApiResult<List<RecentBook>> =
        providePreferredChannel().fetchRecentListenedBooks(libraryId)

    suspend fun fetchBook(
        bookId: String
    ): ApiResult<DetailedBook> =
        providePreferredChannel().fetchBook(bookId)

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> = providePreferredChannel()
        .authorize(host, username, password)

    private fun providePreferredChannel(): AudiobookshelfChannel = sharedPreferences
        .getPreferredChannel()
        ?.let { channels[it] }
        ?: throw IllegalStateException("Selected Channel has been requested but not selected")
}