package org.grakovne.lissen.channel.sources.audiobookshelf.model

data class SyncProgressRequest(
    val timeListened: Int,
    val duration: Double,
    val currentTime: Double
)