package org.grakovne.lissen.domain

import java.io.Serializable

data class DetailedBook(
    val id: String,
    val title: String,
    val author: String,
    val chapters: List<BookChapter>
) : Serializable

data class BookChapter(
    val id: String,
    val name: String,
    val duration: Double,
) : Serializable