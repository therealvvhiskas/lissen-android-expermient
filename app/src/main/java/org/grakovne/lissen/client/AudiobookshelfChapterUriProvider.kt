package org.grakovne.lissen.client

import android.net.Uri
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudiobookshelfChapterUriProvider @Inject constructor() {
    private val preferences = LissenSharedPreferences.getInstance()

    fun provideUri(
        libraryItemId: String,
        chapterId: String
    ): Uri = Uri.parse(preferences.getHost())
        .buildUpon()
        .appendPath("api")
        .appendPath("items")
        .appendPath(libraryItemId)
        .appendPath("file")
        .appendPath(chapterId)
        .appendQueryParameter("token", preferences.getToken())
        .build()
}