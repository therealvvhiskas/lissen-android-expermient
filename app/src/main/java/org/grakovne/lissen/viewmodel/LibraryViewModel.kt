package org.grakovne.lissen.viewmodel

import LissenSharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.repository.ServerRepository
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ServerRepository
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books


    init {
        fetchLibrary()
    }

    fun fetchLibrary() {
        viewModelScope.launch {
            val response = repository.fetchLibraryItems(
                preferences.getPreferredLibrary()?.id ?: "",
                1,
                100
            )

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
}