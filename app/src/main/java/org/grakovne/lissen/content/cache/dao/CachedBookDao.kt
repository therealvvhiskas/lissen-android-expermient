package org.grakovne.lissen.content.cache.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.grakovne.lissen.content.cache.entity.BookChapterEntity
import org.grakovne.lissen.content.cache.entity.BookEntity
import org.grakovne.lissen.content.cache.entity.BookFileEntity
import org.grakovne.lissen.content.cache.entity.CachedBookEntity
import org.grakovne.lissen.content.cache.entity.MediaProgressEntity

@Dao
interface CachedBookDao {

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