package org.grakovne.lissen.content.cache

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_STATUS
import android.app.DownloadManager.Query
import android.app.DownloadManager.Request
import android.app.DownloadManager.STATUS_FAILED
import android.app.DownloadManager.STATUS_SUCCESSFUL
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.grakovne.lissen.content.LissenMediaChannel
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.DetailedBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCachingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CachedBookRepository,
    private val imageLoader: ImageLoader,
    private val mediaChannel: LissenMediaChannel,
    private val properties: CacheBookStorageProperties
) {

    fun removeBook(book: Book) = flow {
        repository.removeBook(book.id)

        val cachedContent = properties
            .provideBookCache(book.id)
            ?: return@flow emit(CacheProgress.Idle)

        when (cachedContent.exists()) {
            true -> cachedContent.deleteRecursively()
            false -> return@flow emit(CacheProgress.Idle)
        }

        return@flow emit(CacheProgress.Idle)
    }

    fun cacheBook(book: Book) = flow {
        emit(CacheProgress.InProgress)

        val detailedBook = mediaChannel
            .fetchBook(book.id)
            .fold(
                onSuccess = { it },
                onFailure = { null }
            )

        if (null == detailedBook) {
            emit(CacheProgress.Error)
            return@flow
        }

        val cacheResult = withContext(Dispatchers.IO) {
            listOf(
                async { cacheBookInfo(detailedBook) },
                async { cacheBookCover(detailedBook) },
                async { cacheBookMedia(detailedBook) }
            ).awaitAll()
        }

        when {
            cacheResult.all { it == CacheProgress.Completed } -> emit(CacheProgress.Completed)
            else -> emit(CacheProgress.Error)
        }
    }

    private suspend fun cacheBookMedia(book: DetailedBook): CacheProgress {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloads = book
            .files
            .map { file ->
                Request(mediaChannel.provideFileUri(book.id, file.id))
                    .setTitle(file.name)
                    .setNotificationVisibility(Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(properties.provideMediaCachePatch(book.id, file.id).toUri())
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .let { downloadManager.enqueue(it) }
            }

        return awaitDownloadProgress(downloads, downloadManager)
    }

    private suspend fun awaitDownloadProgress(
        jobs: List<Long>,
        downloadManager: DownloadManager
    ): CacheProgress {
        val result = checkDownloads(jobs, downloadManager)

        return when {
            result.all { it == STATUS_SUCCESSFUL } -> CacheProgress.Completed
            result.any { it == STATUS_FAILED } -> CacheProgress.Error
            else -> {
                delay(1000)
                awaitDownloadProgress(jobs, downloadManager)
            }
        }
    }


    private fun checkDownloads(jobs: List<Long>, downloadManager: DownloadManager): List<Int> {
        return jobs.map { id ->
            val query = Query().setFilterById(id)
            downloadManager.query(query)
                ?.use { cursor ->
                    if (!cursor.moveToFirst()) {
                        return@map STATUS_FAILED
                    }

                    val statusIndex = cursor.getColumnIndex(COLUMN_STATUS)
                    when (statusIndex >= 0) {
                        true -> cursor.getInt(statusIndex)
                        else -> STATUS_FAILED
                    }
                } ?: STATUS_FAILED
        }
    }

    private suspend fun cacheBookCover(book: DetailedBook): CacheProgress {
        val file = properties.provideBookCoverPath(book.id)
        val coverUrl = mediaChannel.provideBookCover(book.id)

        val request = ImageRequest.Builder(context)
            .data(coverUrl)
            .target { drawable ->
                file.outputStream()
                    .use {
                        (drawable as BitmapDrawable)
                            .bitmap
                            .compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
            }
            .build()

        imageLoader.execute(request)
        return CacheProgress.Completed
    }

    private suspend fun cacheBookInfo(book: DetailedBook) = repository
        .cacheBook(book)
        .let { CacheProgress.Completed }

}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data object InProgress : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}