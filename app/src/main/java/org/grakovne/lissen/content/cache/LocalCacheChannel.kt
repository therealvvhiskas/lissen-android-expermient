package org.grakovne.lissen.content.cache

import android.net.Uri
import android.net.Uri.parse
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.content.channel.common.ApiError
import org.grakovne.lissen.content.channel.common.ApiResult
import org.grakovne.lissen.content.channel.common.ChannelCode
import org.grakovne.lissen.content.channel.common.MediaChannel
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.Library
import org.grakovne.lissen.domain.PagedItems
import org.grakovne.lissen.domain.PlaybackProgress
import org.grakovne.lissen.domain.PlaybackSession
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.domain.UserAccount
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalCacheChannel @Inject constructor(
    private val cachedBookRepository: CachedBookRepository,

    ) : MediaChannel {

    override fun getChannelCode(): ChannelCode = ChannelCode.LOCAL_CACHE

    override fun provideFileUri(libraryItemId: String, fileId: String): Uri =
        cachedBookRepository.provideFileUri(libraryItemId, fileId)

    override fun provideBookCover(bookId: String): Uri =
        cachedBookRepository
            .provideBookCover(bookId)
            .toString()
            .let { parse(it) }


    override suspend fun syncProgress(itemId: String, progress: PlaybackProgress): ApiResult<Unit> {
        return ApiResult.Success(Unit)
    }

    override suspend fun fetchBookCover(bookId: String): ApiResult<InputStream> {
        val cover = cachedBookRepository
            .provideBookCover(bookId)

        return when (cover.exists()) {
            true -> ApiResult.Success(cover.inputStream())
            false -> ApiResult.Error(ApiError.InternalError)
        }
    }

    override suspend fun fetchBooks(
        libraryId: String,
        pageSize: Int,
        pageNumber: Int
    ): ApiResult<PagedItems<Book>> {
        val books = cachedBookRepository
            .fetchBooks(
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

    override suspend fun fetchLibraries(): ApiResult<List<Library>> = ApiResult.Success(emptyList())

    override suspend fun startPlayback(
        itemId: String,
        supportedMimeTypes: List<String>,
        deviceId: String
    ): ApiResult<PlaybackSession> =
        ApiResult.Error(ApiError.InternalError)

    override suspend fun fetchRecentListenedBooks(libraryId: String): ApiResult<List<RecentBook>> =
        ApiResult.Success(emptyList())

    override suspend fun fetchBook(bookId: String) = cachedBookRepository
        .fetchBook(bookId)
        ?.let { ApiResult.Success(it) }
        ?: ApiResult.Error(ApiError.InternalError)

    override suspend fun authorize(
        host: String,
        username: String,
        password: String
    ): ApiResult<UserAccount> = ApiResult.Error(ApiError.InternalError)
}