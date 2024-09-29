package org.grakovne.lissen.viewmodel

import LissenSharedPreferences
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.repository.ServerMediaRepository
import org.grakovne.lissen.repository.ServerRepository
import org.grakovne.lissen.repository.provideCustomImageLoader
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: ServerRepository,
    private val mediaRepository: ServerMediaRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val preferences = LissenSharedPreferences.getInstance()

    private val _books = MutableLiveData<List<Book>>(emptyList())
    val books: LiveData<List<Book>> = _books

    val imageLoader: ImageLoader = provideCustomImageLoader(context, mediaRepository)

    init {
        fetchLibrary()
    }

    fun fetchLibrary() {
        viewModelScope.launch {
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
}