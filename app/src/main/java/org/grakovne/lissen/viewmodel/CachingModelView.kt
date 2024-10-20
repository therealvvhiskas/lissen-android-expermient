package org.grakovne.lissen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.grakovne.lissen.content.cache.BookCachingService
import org.grakovne.lissen.content.cache.CacheProgress
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.BookCachedState
import javax.inject.Inject

@HiltViewModel
class CachingModelView @Inject constructor(
    private val cachingService: BookCachingService
) : ViewModel() {

    private val _bookCachingProgress = mutableMapOf<String, MutableStateFlow<CacheProgress>>()

    fun provideCacheAction(book: Book): BookCacheAction? = when (book.cachedState) {
        BookCachedState.ABLE_TO_CACHE -> BookCacheAction.CACHE
        BookCachedState.CACHED -> BookCacheAction.DROP
        BookCachedState.STORED_LOCALLY -> null
    }


    fun dropCache(book: Book) {
        viewModelScope.launch {
            cachingService
                .removeBook(book)
                .collect {
                    _bookCachingProgress[book.id]?.value = it
                }
        }
    }

    fun cacheBook(book: Book) {
        viewModelScope.launch {
            cachingService
                .cacheBook(book)
                .collect { _bookCachingProgress[book.id]?.value = it }
        }
    }

    fun getCacheProgress(bookId: String) = _bookCachingProgress
        .getOrPut(bookId) {
            MutableStateFlow(CacheProgress.Idle)
        }

}