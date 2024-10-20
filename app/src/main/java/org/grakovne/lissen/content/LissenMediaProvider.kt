package org.grakovne.lissen.content

import android.net.Uri
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.channel.common.ChannelCode
import org.grakovne.lissen.channel.common.ChannelCode.AUDIOBOOKSHELF
import org.grakovne.lissen.channel.common.MediaChannel
import org.grakovne.lissen.content.cache.LocalCacheRepository
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.BookCachedState.CACHED
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
class LissenMediaProvider @Inject constructor(
    private val sharedPreferences: LissenSharedPreferences,
    private val channels: Map<ChannelCode, @JvmSuppressWildcards MediaChannel>,
    private val localCacheRepository: LocalCacheRepository
) {

    fun provideFileUri(
        libraryItemId: String,
        chapterId: String
    ): Uri = when (useCache()) {
        true -> localCacheRepository.provideFileUri(libraryItemId, chapterId)
        false -> providePreferredChannel().provideFileUri(libraryItemId, chapterId)
    }

    fun provideBookCoverUri(
        bookId: String
    ): Uri = when (useCache()) {
        true -> localCacheRepository.provideBookCover(bookId)
        false -> providePreferredChannel().provideBookCover(bookId)
    }

    suspend fun syncProgress(
        sessionId: String,
        bookId: String,
        progress: PlaybackProgress
    ): ApiResult<Unit> =
        when (useCache()) {
            true -> localCacheRepository.syncProgress(bookId, progress)
            false -> providePreferredChannel()
                .syncProgress(sessionId, progress)
                .also { localCacheRepository.syncProgress(bookId, progress) }
        }

    suspend fun fetchBookCover(
        bookId: String
    ): ApiResult<InputStream> = when (useCache()) {
        true -> localCacheRepository.fetchBookCover(bookId)
        false -> providePreferredChannel().fetchBookCover(bookId)
    }

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): ApiResult<PagedItems<Book>> = when (useCache()) {
        true -> localCacheRepository.fetchBooks(pageSize, pageNumber)
        false -> {
            providePreferredChannel()
                .fetchBooks(libraryId, pageSize, pageNumber)
                .map { flagCached(it) }
        }
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> = when (useCache()) {
        true -> localCacheRepository.fetchLibraries()
        false -> providePreferredChannel()
            .fetchLibraries()
            .also {
                it.foldAsync(
                    onSuccess = { libraries -> localCacheRepository.updateLibraries(libraries) },
                    onFailure = {}
                )
            }
    }

    suspend fun startPlayback(
        bookId: String,
        supportedMimeTypes: List<String>,
        deviceId: String
    ): ApiResult<PlaybackSession> = when (useCache()) {
        true -> localCacheRepository.startPlayback(bookId)
        false -> providePreferredChannel().startPlayback(bookId, supportedMimeTypes, deviceId)
    }

    suspend fun fetchRecentListenedBooks(
        libraryId: String
    ): ApiResult<List<RecentBook>> = when (useCache()) {
        true -> localCacheRepository.fetchRecentListenedBooks()
        false -> providePreferredChannel().fetchRecentListenedBooks(libraryId)
    }

    suspend fun fetchBook(
        bookId: String
    ): ApiResult<DetailedBook> = when(useCache()) {
        true -> localCacheRepository.fetchBook(bookId)
        false -> providePreferredChannel().fetchBook(bookId)
    }

    suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> = when(sharedPreferences.getPreferredChannel()) {
        AUDIOBOOKSHELF -> providePreferredChannel().authorize(host, username, password)
    }

    private suspend fun flagCached(
        it: PagedItems<Book>
    ): PagedItems<Book> {
        val cachedBooks = localCacheRepository.fetchCachedBookIds()

        val items = it
            .items
            .map { book ->
                when (cachedBooks.contains(book.id)) {
                    true -> book.copy(cachedState = CACHED)
                    false -> book
                }
            }

        return it.copy(items = items)
    }

    private fun useCache(): Boolean {
        return when (sharedPreferences.getPreferredChannel()) {
            AUDIOBOOKSHELF -> sharedPreferences.isForceCache()
        }
    }

    private fun providePreferredChannel(): MediaChannel = sharedPreferences
        .getPreferredChannel()
        .let { channels[it] }
        ?: throw IllegalStateException("Selected Channel has been requested but not selected")
}