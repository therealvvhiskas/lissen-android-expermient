package org.grakovne.lissen.channel.audiobookshelf.model

data class PlaybackSessionResponse(
    val id: String,
    val chapters: List<PlaybackChapter>
)

data class PlaybackChapter(
    val start: Double,
    val end: Double,
    val title: String,
    val id: String
)