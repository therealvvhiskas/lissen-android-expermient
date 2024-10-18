package org.grakovne.lissen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.grakovne.lissen.content.cache.BookCachingService
import org.grakovne.lissen.content.cache.CacheProgress
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject

@HiltViewModel
class CachingModelView @Inject constructor(
    private val cachingService: BookCachingService,
    private val preferences: LissenSharedPreferences
) : ViewModel() {

    private val _bookCachingProgress = mutableMapOf<String, MutableStateFlow<CacheProgress>>()
    val bookCachingProgress = _bookCachingProgress

    fun cacheBook(book: Book) {
        viewModelScope.launch {
            val progressFlow = cachingService.cacheBook(book)
            val cacheProgress = getCacheProgress(book.id)
            progressFlow
                .collect { progress ->
                    _bookCachingProgress[book.id]?.value = progress
                }
        }
    }

    fun getCacheProgress(bookId: String) = _bookCachingProgress
        .getOrPut(bookId) {
            MutableStateFlow(CacheProgress.Idle)
        }

}