package org.grakovne.lissen.channel.audiobookshelf.model

data class RecentListeningResponse(
    val items: Map<String, Item>
)

data class Item(
    val id: String,
    val timeListening: Double,
    val mediaMetadata: MediaMetadataResponse
)