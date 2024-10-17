package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.LissenMediaChannel
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.ui.screens.library.paging.LibraryPagingSource
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val mediaChannel: LissenMediaChannel,
    private val preferences: LissenSharedPreferences
) : ViewModel() {

    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    private val _recentBookUpdating = MutableLiveData<Boolean>(false)
    val recentBookUpdating: LiveData<Boolean> = _recentBookUpdating

    val libraryPager: Flow<PagingData<Book>> by lazy {
        val libraryId = preferences.getPreferredLibrary()?.id ?: ""
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                initialLoadSize = PAGE_SIZE,
                prefetchDistance = PAGE_SIZE
            ),
            pagingSourceFactory = { LibraryPagingSource(mediaChannel, libraryId) }
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
        _recentBookUpdating.postValue(true)

        viewModelScope.launch {
            mediaChannel
                .fetchRecentListenedBooks(preferences.getPreferredLibrary()?.id ?: return@launch)
                .fold(
                    onSuccess = {
                        _recentBooks.postValue(it)
                        _recentBookUpdating.postValue(false)
                    },
                    onFailure = {
                        _recentBookUpdating.postValue(false)
                    }
                )
        }
    }

    fun toggleCacheForce() {
        when (isCacheForce()) {
            true -> preferences.disableForceCache()
            false -> preferences.enableForceCache()
        }

        refreshContent()
    }

    fun isCacheForce() = preferences.isForceCache()

    companion object {
        private const val PAGE_SIZE = 10
    }
}