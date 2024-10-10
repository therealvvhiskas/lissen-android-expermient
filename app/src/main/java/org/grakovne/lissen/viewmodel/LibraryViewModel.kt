package org.grakovne.lissen.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.grakovne.lissen.converter.LibraryItemResponseConverter
import org.grakovne.lissen.converter.RecentBookResponseConverter
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.provider.audiobookshelf.AudiobookshelfDataProvider
import org.grakovne.lissen.ui.extensions.withMinimumTime
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val dataProvider: AudiobookshelfDataProvider,
    private val recentBookResponseConverter: RecentBookResponseConverter,
    private val libraryItemResponseConverter: LibraryItemResponseConverter
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    init {
        fetchRecentListening()
        fetchLibrary()
    }

    fun onPullRefreshed() {
        _refreshing.value = true

        viewModelScope.launch {
            withMinimumTime(700) {
                coroutineScope {
                    val fetchRecentJob = async { fetchRecentListening() }
                    val fetchLibraryJob = async { fetchLibrary() }

                    awaitAll(fetchRecentJob, fetchLibraryJob)
                }
            }

            _refreshing.value = false
        }
    }

    private fun fetchRecentListening() {
        viewModelScope.launch {
            val response = dataProvider
                .getRecentItems()

            response.fold(
                onSuccess = { _recentBooks.value = recentBookResponseConverter.apply(it) },
                onFailure = {}
            )
        }
    }

    private fun fetchLibrary(): Job = viewModelScope.launch {
        val response =
            dataProvider
                .fetchLibraryItems(
                    preferences.getPreferredLibrary()?.id ?: return@launch
                )

        response.fold(
            onSuccess = { _books.value = libraryItemResponseConverter.apply(it) },
            onFailure = {
                // fetch local cached books
            }
        )
    }
}