package org.grakovne.lissen.converter

import org.grakovne.lissen.client.audiobookshelf.model.LibraryResponse
import org.grakovne.lissen.domain.Library
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibraryResponseConverter @Inject constructor() {

    fun apply(response: LibraryResponse): List<Library> =
        response.libraries.map { Library(it.id, it.name) }
}