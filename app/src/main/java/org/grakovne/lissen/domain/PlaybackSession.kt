package org.grakovne.lissen.domain

data class PlaybackSession(
    val sessionId: String,
    val chapters: List<PlaybackChapter>
)

data class PlaybackChapter(
    val start: Double,
    val end: Double,
    val title: String,
    val id: String
)