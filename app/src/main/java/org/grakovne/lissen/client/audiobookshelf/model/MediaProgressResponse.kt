package org.grakovne.lissen.client.audiobookshelf.model

data class MediaProgressResponse(
    val libraryItemId: String,
    val episodeId: String,
    val currentTime: Double,
    val isFinished: Boolean,
    val lastUpdate: Long
)