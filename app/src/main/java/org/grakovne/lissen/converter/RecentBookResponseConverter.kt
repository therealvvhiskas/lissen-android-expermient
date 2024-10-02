package org.grakovne.lissen.converter

import org.grakovne.lissen.client.audiobookshelf.model.RecentListeningResponse
import org.grakovne.lissen.domain.RecentBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecentBookResponseConverter @Inject constructor() {

    fun apply(response: RecentListeningResponse): List<RecentBook> = response.items.values
        .map {
            RecentBook(
                id = it.id,
                title = it.mediaMetadata.title,
                author = it.mediaMetadata.authors.joinToString { it.name }
            )
        }
}