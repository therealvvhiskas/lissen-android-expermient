package org.grakovne.lissen.content.cache

import kotlinx.coroutines.flow.flow
import org.grakovne.lissen.content.LissenMediaChannel
import org.grakovne.lissen.content.cache.api.CachedBookRepository
import org.grakovne.lissen.domain.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCachingService @Inject constructor(
    private val repository: CachedBookRepository,
    private val mediaChannel: LissenMediaChannel
) {

    fun cacheBook(book: Book) = flow {
        emit(CacheProgress.Started(0))

        mediaChannel
            .fetchBook(book.id)
            .foldAsync(
                onSuccess = {
                    repository.cacheBook(it)
                    emit(CacheProgress.Completed)
                },
                onFailure = { emit(CacheProgress.Error) }
            )


        emit(CacheProgress.Completed)
    }
}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data class Started(val percent: Int) : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}