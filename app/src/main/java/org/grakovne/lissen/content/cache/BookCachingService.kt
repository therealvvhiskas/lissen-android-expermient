package org.grakovne.lissen.content.cache

import kotlinx.coroutines.flow.flow
import org.grakovne.lissen.domain.DetailedBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookCachingService @Inject constructor() {

    fun cacheBook(book: DetailedBook) = flow {
        emit(CacheProgress.Started)
        kotlinx.coroutines.delay(500)

        (1..100).forEach {
            emit(CacheProgress.Updated(it))
            kotlinx.coroutines.delay(100)
        }

        kotlinx.coroutines.delay(500)
        emit(CacheProgress.Completed)
    }
}

sealed class CacheProgress {
    data object Started : CacheProgress()
    data class Updated(val percent: Int) : CacheProgress()
    data object Completed : CacheProgress()
    data object Error : CacheProgress()
}