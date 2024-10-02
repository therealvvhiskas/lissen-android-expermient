package org.grakovne.lissen.client.audiobookshelf.model

data class LibraryItemIdResponse(
    val id: String,
    val media: MediaMetadataResponse,
    val audiofiles: List<AudioFileResponse>
)