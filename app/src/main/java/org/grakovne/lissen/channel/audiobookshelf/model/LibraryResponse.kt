package org.grakovne.lissen.channel.audiobookshelf.model

data class LibraryResponse(
    val libraries: List<Library>
)

data class Library(
    val id: String,
    val name: String,
    val folders: List<Folder>,
    val displayOrder: Int,
    val icon: String,
    val mediaType: String,
    val provider: String,
    val createdAt: Long,
    val lastUpdate: Long
)

data class Folder(
    val id: String,
    val fullPath: String,
    val libraryId: String
)
