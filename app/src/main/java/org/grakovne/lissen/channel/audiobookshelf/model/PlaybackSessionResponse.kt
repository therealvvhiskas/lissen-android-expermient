package org.grakovne.lissen.channel.audiobookshelf.model

data class PlaybackSessionResponse(
    val id: String,
    val chapters: List<PlaybackChapterResponse>
)

data class PlaybackChapterResponse(
    val start: Double,
    val end: Double,
    val title: String,
    val id: String
)