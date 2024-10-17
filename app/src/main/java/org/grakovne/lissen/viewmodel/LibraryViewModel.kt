package org.grakovne.lissen.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.ui.extensions.withMinimumTime
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfChannel,
    private val preferences: LissenSharedPreferences
) : ViewModel() {

    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    private val _currentPage = MutableLiveData(0)

    fun onPullRefreshed() {
        _refreshing.postValue(true)

        viewModelScope.launch {
            withMinimumTime(500) {
                listOf(
                    async { fetchRecentListening() },
                    async { fetchLibrary() }
                ).awaitAll()
            }
            _refreshing.postValue(false)
        }
    }

    fun fetchNextLibraryPage() {
        viewModelScope.launch {

            dataProvider
                .fetchBooks(
                    libraryId = preferences.getPreferredLibrary()?.id ?: return@launch,
                    pageNumber = _currentPage.value?.plus(1) ?: 0,
                    pageSize = PAGE_SIZE
                )
                .fold(
                    onFailure = {

                    },
                    onSuccess = {

                        if (it.isNotEmpty()) {
                            val items = (_books.value ?: emptyList()) + it
                            _books.postValue(items.distinct())

                            _currentPage.postValue((_currentPage.value ?: 0) + 1)
                        }
                    }
                )
        }
    }

    fun refreshContent() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch(Dispatchers.IO) { fetchRecentListening() }
                launch(Dispatchers.IO) { fetchLibrary() }
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

    private fun fetchLibrary(): Job = viewModelScope.launch {
        val response =
            dataProvider
                .fetchBooks(
                    libraryId = preferences.getPreferredLibrary()?.id ?: return@launch,
                    pageNumber = _currentPage.value ?: 0,
                    pageSize = PAGE_SIZE
                )

        response
            .fold(
                onSuccess = { _books.postValue(it) },
                onFailure = {}
            )
    }

    companion object {
        private val PAGE_SIZE = 10
    }
}