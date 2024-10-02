package org.grakovne.lissen.converter

import org.grakovne.lissen.client.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.domain.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryItemResponseConverter @Inject constructor() {

    fun apply(response: LibraryItemsResponse): List<Book> = response
        .results
        .map {
            Book(
                id = it.id,
                title = it.media.metadata.title,
                author = it.media.metadata.authorName,
                downloaded = false,
                duration = it.media.duration.toInt()
            )
        }
}