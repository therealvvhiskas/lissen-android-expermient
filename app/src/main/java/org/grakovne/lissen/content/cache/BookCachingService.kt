package org.grakovne.lissen.content.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.grakovne.lissen.content.LissenMediaChannel
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.domain.Book
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

    fun cacheBook(book: Book) = flow {
        emit(CacheProgress.Started(0))

        emitAll(cacheBookInfo(book))
        emitAll(cacheBookCover(book))

        emit(CacheProgress.Completed)
    }

    private suspend fun cacheBookCover(book: Book) = flow {
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

    private suspend fun cacheBookInfo(book: Book) = flow {
        mediaChannel
            .fetchBook(book.id)
            .foldAsync(
                onSuccess = {
                    repository.cacheBook(it)
                    emit(CacheProgress.Completed)
                },
                onFailure = { emit(CacheProgress.Error) }
            )
    }

}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data class Started(val percent: Int) : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}