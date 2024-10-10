package org.grakovne.lissen.channel.audiobookshelf.converter

import org.grakovne.lissen.channel.audiobookshelf.model.Author
import org.grakovne.lissen.channel.audiobookshelf.model.LibraryItemIdResponse
import org.grakovne.lissen.channel.audiobookshelf.model.MediaProgressResponse
import org.grakovne.lissen.domain.BookChapter
import org.grakovne.lissen.domain.DetailedBook
import org.grakovne.lissen.domain.MediaProgress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryItemIdResponseConverter @Inject constructor() {

    fun apply(
        item: LibraryItemIdResponse,
        progressResponse: MediaProgressResponse? = null
    ): DetailedBook {
        return DetailedBook(
            id = item.id,
            title = item.media.metadata.title,
            author = item.media.metadata.authors.joinToString(", ", transform = Author::name),
            chapters = item
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
                },
            progress = progressResponse
                ?.let {
                    MediaProgress(
                        currentTime = it.currentTime,
                        isFinished = it.isFinished,
                        lastUpdate = it.lastUpdate,
                    )
                },
        )
    }
}