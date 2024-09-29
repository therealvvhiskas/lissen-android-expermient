package org.grakovne.lissen.client.audiobookshelf.model

data class RecentListeningResponse(
    val items: Map<String, Item>
)

data class Item(
    val id: String,
    val timeListening: Double,
    val mediaMetadata: MediaMetadata
)

data class MediaMetadata(
    val title: String,
    val subtitle: String?,
    val authors: List<Author>,
    val genres: List<String>,
    val publishedYear: Int?,
    val publishedDate: String?,
    val publisher: String?,
    val description: String?,
    val isbn: String?,
    val asin: String?,
    val language: String?,
    val explicit: Boolean,
    val abridged: Boolean
)

data class Author(
    val id: String,
    val name: String
)
