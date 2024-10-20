package org.grakovne.lissen.content.cache

import android.net.Uri
import android.net.Uri.parse
import org.grakovne.lissen.channel.common.ApiError
import org.grakovne.lissen.channel.common.ApiResult
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.content.cache.api.CachedLibraryRepository
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.domain.PagedItems
import org.grakovne.lissen.domain.PlaybackProgress
import org.grakovne.lissen.domain.PlaybackSession
import org.grakovne.lissen.domain.RecentBook
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCacheRepository @Inject constructor(
    private val cachedBookRepository: CachedBookRepository,
    private val cachedLibraryRepository: CachedLibraryRepository
) {

    fun provideFileUri(libraryItemId: String, fileId: String): Uri =
        cachedBookRepository.provideFileUri(libraryItemId, fileId)

    fun provideBookCover(bookId: String): Uri =
        cachedBookRepository
            .provideBookCover(bookId)
            .toString()
            .let { parse(it) }


    /**
     * For the local cache we avoiding to create intermediary entity like Session and using BookId
     * as a Playback Session Key
     */
    suspend fun syncProgress(
        bookId: String,
        progress: PlaybackProgress
    ): ApiResult<Unit> {
        cachedBookRepository.syncProgress(bookId, progress)
        return ApiResult.Success(Unit)
    }

    suspend fun fetchBookCover(bookId: String): ApiResult<InputStream> {
        val cover = cachedBookRepository
            .provideBookCover(bookId)

        return when (cover.exists()) {
            true -> ApiResult.Success(cover.inputStream())
            false -> ApiResult.Error(ApiError.InternalError)
        }
    }

    suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): ApiResult<PagedItems<Book>> {
        val books = cachedBookRepository
            .fetchBooks(
                libraryId = libraryId,
                pageNumber = pageNumber,
                pageSize = pageSize
            )

        return ApiResult
            .Success(
                PagedItems(
                    items = books,
                    currentPage = pageNumber
                )
            )
    }

    suspend fun fetchLibraries(): ApiResult<List<Library>> = cachedLibraryRepository
        .fetchLibraries()
        .let { ApiResult.Success(it) }

    suspend fun updateLibraries(libraries: List<Library>) {
        cachedLibraryRepository.cacheLibraries(libraries)
    }


    /**
     * For the local cache we avoiding to create intermediary entity like Session and using BookId
     * as a Playback Session Key
     */
    suspend fun startPlayback(
        bookId: String
    ): ApiResult<PlaybackSession> =
        ApiResult
            .Success(
                PlaybackSession(
                    bookId = bookId,
                    sessionId = bookId
                )
            )

    suspend fun fetchRecentListenedBooks(): ApiResult<List<RecentBook>> =
        cachedBookRepository.fetchRecentBooks().let { ApiResult.Success(it) }

    suspend fun fetchBook(bookId: String) = cachedBookRepository
        .fetchBook(bookId)
        ?.let { ApiResult.Success(it) }
        ?: ApiResult.Error(ApiError.InternalError)

    suspend fun fetchCachedBookIds() = cachedBookRepository.fetchCachedBooksIds()
}