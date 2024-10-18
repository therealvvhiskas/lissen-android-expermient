package org.grakovne.lissen.content.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import org.grakovne.lissen.content.cache.dao.CachedBookDao
import org.grakovne.lissen.content.cache.entity.BookChapterEntity
import org.grakovne.lissen.content.cache.entity.BookEntity
import org.grakovne.lissen.content.cache.entity.BookFileEntity
import org.grakovne.lissen.content.cache.entity.MediaProgressEntity

@Database(
    entities = [
        BookEntity::class,
        BookFileEntity::class,
        BookChapterEntity::class,
        MediaProgressEntity::class,
    ],
    version = 1
)
abstract class LocalCacheStorage : RoomDatabase() {

    abstract fun cachedBookDao(): CachedBookDao
}