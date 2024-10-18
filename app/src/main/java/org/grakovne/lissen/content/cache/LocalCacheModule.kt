package org.grakovne.lissen.content.cache

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.grakovne.lissen.content.cache.dao.CachedBookDao
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object LocalCacheModule {

    private const val DATABASE_NAME = "lissen_local_cache_storage"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): LocalCacheStorage {
        context.deleteDatabase(DATABASE_NAME) // DEBUG ONLY

        val database = Room.databaseBuilder(
            context = context,
            klass = LocalCacheStorage::class.java,
            name = DATABASE_NAME
        )

        return database
            .fallbackToDestructiveMigration() // DEBUG ONLY
            .build()
    }

    @Provides
    @Singleton
    fun provideCachedBookDao(appDatabase: LocalCacheStorage): CachedBookDao =
        appDatabase.cachedBookDao()

}