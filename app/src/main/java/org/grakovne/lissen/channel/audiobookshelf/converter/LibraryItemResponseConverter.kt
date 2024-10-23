package org.grakovne.lissen.channel.audiobookshelf.converter

import org.grakovne.lissen.channel.audiobookshelf.model.LibraryItemsResponse
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.BookCachedState
import org.grakovne.lissen.domain.PagedItems
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryItemResponseConverter @Inject constructor() {

    fun apply(response: LibraryItemsResponse): PagedItems<Book> = response
        .results
        .map {
            Book(
                id = it.id,
                title = it.media.metadata.title,
                author = it.media.metadata.authorName,
                cachedState = BookCachedState.ABLE_TO_CACHE,
                duration = it.media.duration.toInt()
            )
        }
        .let {
            PagedItems(
                items = it,
                currentPage = response.page
            )
        }
}
