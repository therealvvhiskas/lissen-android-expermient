package org.grakovne.lissen.channel.audiobookshelf.converter

import org.grakovne.lissen.channel.audiobookshelf.model.Author
import org.grakovne.lissen.channel.audiobookshelf.model.LibraryItemIdResponse
import org.grakovne.lissen.domain.BookChapter
import org.grakovne.lissen.domain.DetailedBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryItemIdResponseConverter @Inject constructor() {

    fun apply(response: LibraryItemIdResponse): DetailedBook {
        return DetailedBook(
            id = response.id,
            title = response.media.metadata.title,
            author = response.media.metadata.authors.joinToString(", ", transform = Author::name),
            chapters = response
                .media
                .audioFiles
                .map {
                BookChapter(
                    id = it.ino,
                    name = it.metaTags
                        ?.tagTitle
                        ?: (it.metadata.filename.removeSuffix(it.metadata.ext)),
                    duration = it.duration
                )
            }
        )
    }
}