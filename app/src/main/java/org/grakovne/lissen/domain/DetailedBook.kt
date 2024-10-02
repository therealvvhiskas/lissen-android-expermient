package org.grakovne.lissen.domain

data class DetailedBook(
    val id: String,
    val title: String,
    val author: String,
    val chapters: List<BookChapter>,
    val progress: Progress?
)

data class BookChapter(
    val id: String,
    val name: String,
    val duration: Double,
)

data class Progress(
    val total: Int,
    val current: Int
)