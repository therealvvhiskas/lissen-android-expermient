package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.LazyPagingItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.ui.extensions.withMinimumTime
import org.grakovne.lissen.ui.screens.library.paging.LibraryPagingSource
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfChannel,
    private val preferences: LissenSharedPreferences
) : ViewModel() {

    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    val libraryPager: Flow<PagingData<Book>> by lazy {
        val libraryId = preferences.getPreferredLibrary()?.id ?: ""
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE
            ),
            pagingSourceFactory = { LibraryPagingSource(dataProvider, libraryId) }
        ).flow.cachedIn(viewModelScope)
    }


    fun refreshContent() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch(Dispatchers.IO) { fetchRecentListening() }
            }
        }
    }

    fun fetchRecentListening() {
        viewModelScope.launch {
            dataProvider
                .fetchRecentListenedBooks(preferences.getPreferredLibrary()?.id ?: return@launch)
                .fold(
                    onSuccess = { _recentBooks.postValue(it) },
                    onFailure = {}
                )
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}