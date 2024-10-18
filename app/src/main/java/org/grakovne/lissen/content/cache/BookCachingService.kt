package org.grakovne.lissen.content.cache

import kotlinx.coroutines.flow.flow
import org.grakovne.lissen.domain.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCachingService @Inject constructor() {

    fun cacheBook(book: Book) = flow {
        emit(CacheProgress.Started(0))
        kotlinx.coroutines.delay(500)

        (1..10).forEach {
            emit(CacheProgress.Started(it))
            kotlinx.coroutines.delay(100)
        }

        kotlinx.coroutines.delay(500)
        emit(CacheProgress.Completed)
    }
}

sealed class CacheProgress {
    data object Idle : CacheProgress()
    data class Started(val percent: Int) : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}