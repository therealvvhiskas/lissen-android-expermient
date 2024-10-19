package org.grakovne.lissen.content.cache

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheBookStorageProperties @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun provideBookCoverPath(bookId: String) = File(context.filesDir, "${bookId}_cover.img")
}