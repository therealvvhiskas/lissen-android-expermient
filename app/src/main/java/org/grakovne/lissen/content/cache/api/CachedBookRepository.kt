package org.grakovne.lissen.content.cache.api

import org.grakovne.lissen.content.cache.converter.CachedBookEntityConverter
import org.grakovne.lissen.content.cache.converter.CachedBookEntityDetailedConverter
import org.grakovne.lissen.content.cache.dao.CachedBookDao
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.DetailedBook
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CachedBookRepository @Inject constructor(
    private val dao: CachedBookDao,
    private val cachedBookEntityConverter: CachedBookEntityConverter,
    private val cachedBookEntityDetailedConverter: CachedBookEntityDetailedConverter
) {

    suspend fun fetchBooks(
        pageNumber: Int,
        pageSize: Int
    ): List<Book> = dao
        .fetchCachedBooks(pageNumber = pageNumber, pageSize = pageSize)
        .map { cachedBookEntityConverter.apply(it) }

    suspend fun fetchBook(
        bookId: String
    ): DetailedBook? = dao
        .fetchCachedBook(bookId)
        ?.let { cachedBookEntityDetailedConverter.apply(it) }
}