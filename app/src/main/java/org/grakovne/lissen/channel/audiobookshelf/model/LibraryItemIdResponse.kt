package org.grakovne.lissen.channel.audiobookshelf.model

data class LibraryItemIdResponse(
    val id: String,
    val ino: String,
    val media: LibraryIdMedia,
)

data class LibraryIdMedia(
    val metadata: MediaMetadataResponse,
    val audioFiles: List<AudioFileResponse>
)