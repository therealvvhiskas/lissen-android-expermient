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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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


    val booksPager: Flow<PagingData<Book>> by lazy {
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

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    fun onPullRefreshed(lazyPagingItems: LazyPagingItems<Book>) {
        _refreshing.postValue(true)

        viewModelScope.launch {
            withMinimumTime(500) {
                listOf(
                    async { lazyPagingItems.refresh() },
                    async { fetchRecentListening() },
                ).awaitAll()
            }
            _refreshing.postValue(false)
        }
    }

    fun refreshContent() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch(Dispatchers.IO) { fetchRecentListening() }
            }
        }
    }

    private fun fetchRecentListening() {
        viewModelScope.launch {
            val response = dataProvider
                .fetchRecentListenedBooks(preferences.getPreferredLibrary()?.id ?: return@launch)

            response.fold(
                onSuccess = { _recentBooks.postValue(it) },
                onFailure = {}
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 1
    }
}