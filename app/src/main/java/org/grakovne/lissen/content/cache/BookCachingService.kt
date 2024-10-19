package org.grakovne.lissen.content.cache

import android.app.DownloadManager
import android.app.DownloadManager.COLUMN_STATUS
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.core.net.toUri
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import org.grakovne.lissen.content.LissenMediaChannel
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.DetailedBook
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

@Singleton
class BookCachingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CachedBookRepository,
    private val imageLoader: ImageLoader,
    private val mediaChannel: LissenMediaChannel,
    private val properties: CacheBookStorageProperties
) {

    fun cacheBook(book: Book) = flow {
        emit(CacheProgress.Started(0))

        val detailedBook = mediaChannel
            .fetchBook(book.id)
            .fold(
                onSuccess = { it },
                onFailure = { null }
            )

        when (detailedBook) {
            null -> emit(CacheProgress.Error)
            else -> {
                cacheBookInfo(detailedBook)
                cacheBookCover(detailedBook)
                cacheBookMedia(detailedBook)
            }
        }

        emit(CacheProgress.Completed)
    }

    private suspend fun cacheBookMedia(book: DetailedBook) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloads = mutableMapOf<Long, Int>()
        val isDownloading = true

        book
            .files
            .map { file ->
                DownloadManager
                    .Request(mediaChannel.provideFileUri(book.id, file.id))
                    .setTitle(file.name)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(properties.provideMediaCachePatch(book.id).toUri())
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .let { downloadManager.enqueue(it) }
                    .let { downloads[it] = DownloadManager.STATUS_PENDING }
            }

        val progressTracking = coroutineScope {
            async(Dispatchers.IO) {
                while (isDownloading) {
                    downloads.map { (id, _) ->
                        val query = DownloadManager.Query().setFilterById(id)

                        downloadManager
                            .query(query)
                            ?.use { result ->
                                if (result.moveToFirst()) {
                                    result
                                        .getColumnIndex(COLUMN_STATUS)
                                        .takeIf { value -> value >= 0 }
                                        ?.let { result.getInt(it) }
                                        ?.let { downloads[id] = it }
                                }
                            }
                    }
                }
            }
        }

        progressTracking.await()
    }


    private suspend fun cacheBookCover(book: DetailedBook) = flow {
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
        emit(CacheProgress.Started(10))
    }

    private suspend fun cacheBookInfo(book: DetailedBook) = flow {
        repository.cacheBook(book)
        emit(CacheProgress.Completed)
    }

}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data class Started(val percent: Int) : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}