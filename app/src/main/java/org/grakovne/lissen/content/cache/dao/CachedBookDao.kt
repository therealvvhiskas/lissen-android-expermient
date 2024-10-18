package org.grakovne.lissen.content.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.grakovne.lissen.content.cache.entity.BookChapterEntity
import org.grakovne.lissen.content.cache.entity.BookEntity
import org.grakovne.lissen.content.cache.entity.BookFileEntity
import org.grakovne.lissen.content.cache.entity.CachedBookEntity
import org.grakovne.lissen.content.cache.entity.MediaProgressEntity
import org.grakovne.lissen.domain.DetailedBook

@Dao
interface CachedBookDao {

    @Transaction
    suspend fun upsertCachedBook(book: DetailedBook) {
        val bookEntity = BookEntity(
            id = book.id,
            title = book.title,
            author = book.author,
            duration = book.chapters.sumOf { it.duration }.toInt(),
        )

        val bookFiles = book
            .files
            .map { file ->
                BookFileEntity(
                    id = file.id,
                    name = file.name,
                    duration = file.duration,
                    mimeType = file.mimeType,
                    bookId = book.id
                )
            }

        val bookChapters = book
            .chapters
            .map { chapter ->
                BookChapterEntity(
                    id = chapter.id,
                    duration = chapter.duration,
                    start = chapter.start,
                    end = chapter.end,
                    title = chapter.title,
                    bookId = book.id,
                )
            }

        val mediaProgress = book
            .progress
            ?.let { progress ->
                MediaProgressEntity(
                    bookId = book.id,
                    currentTime = progress.currentTime,
                    isFinished = progress.isFinished,
                    lastUpdate = progress.lastUpdate
                )
            }

        upsertBook(bookEntity)
        upsertBookFiles(bookFiles)
        upsertBookChapters(bookChapters)
        mediaProgress?.let { upsertMediaProgress(it) }
    }

    @Transaction
    @Query("SELECT * FROM detailed_books ORDER BY title LIMIT :pageSize OFFSET :pageNumber * :pageSize")
    suspend fun fetchCachedBooks(pageNumber: Int, pageSize: Int): List<BookEntity>

    @Transaction
    @Query("SELECT * FROM detailed_books WHERE id = :bookId")
    suspend fun fetchCachedBook(bookId: String): CachedBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookFiles(files: List<BookFileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookChapters(chapters: List<BookChapterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMediaProgress(progress: MediaProgressEntity)

    @Update
    suspend fun updateMediaProgress(progress: MediaProgressEntity)

}