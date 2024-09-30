package org.grakovne.lissen.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.RecentBook
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import org.grakovne.lissen.repository.ServerRepository
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ServerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _recentBooks = MutableLiveData<List<RecentBook>>(emptyList())
    val recentBooks: LiveData<List<RecentBook>> = _recentBooks

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    private val _refreshing = MutableLiveData(false)
     val refreshing: LiveData<Boolean> = _refreshing

    fun refreshContent() {
        _refreshing.value = true

        viewModelScope.launch {
            val fetchRecentJob = async { fetchRecentListening() }
            val fetchLibraryJob = async { fetchLibrary() }

            awaitAll(fetchRecentJob, fetchLibraryJob)

            _refreshing.value = false
        }
    }

    fun fetchRecentListening() {
        viewModelScope.launch {
            val response = repository.getRecentItems()

            response.fold(
                onSuccess = { item ->
                    _recentBooks.value = item
                        .items
                        .values
                        .take(5)
                        .map {
                            RecentBook(
                                id = it.id,
                                title = it.mediaMetadata.title,
                                author = it.mediaMetadata.authors.joinToString { it.name }
                            )
                        }
                },
                onFailure = {

                }
            )
        }
    }

    fun fetchLibrary(): Job = viewModelScope.launch {
        val response = repository.fetchLibraryItems(preferences.getPreferredLibrary()?.id ?: "")

        response.fold(
            onSuccess = { item ->
                _books.value = item.results.map {
                    Book(
                        id = it.id,
                        title = it.media.metadata.title,
                        author = it.media.metadata.authorName,
                        downloaded = false,
                        duration = it.media.duration.toInt()
                    )
                }
            },
            onFailure = {
                // fetch local cached books
            }
        )
    }
}