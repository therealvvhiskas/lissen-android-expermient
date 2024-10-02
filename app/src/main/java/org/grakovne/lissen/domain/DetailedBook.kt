package org.grakovne.lissen.domain

import org.grakovne.lissen.client.audiobookshelf.model.Author

data class DetailedBook(
    val id: String,
    val title: String,
    val author: Author,
    val chapters: List<BookChapter>,
    val progress: Progress?
)

data class BookChapter(
    val id: String,
    val name: String,
    val duration: Int,
)

data class Progress(
    val total: Int,
    val current: Int
)