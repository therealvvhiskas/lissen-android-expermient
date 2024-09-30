package org.grakovne.lissen.domain

import java.util.UUID


data class RecentBook(
    val id: String,
    val title: String,
    val author: String
) {

    companion object {
        fun sample() = RecentBook(UUID.randomUUID().toString(), "Sample", "Sample")
    }
}