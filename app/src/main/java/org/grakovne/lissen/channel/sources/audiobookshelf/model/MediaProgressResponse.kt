package org.grakovne.lissen.channel.sources.audiobookshelf.model

data class MediaProgressResponse(
    val libraryItemId: String,
    val episodeId: String,
    val currentTime: Double,
    val isFinished: Boolean,
    val lastUpdate: Long
)