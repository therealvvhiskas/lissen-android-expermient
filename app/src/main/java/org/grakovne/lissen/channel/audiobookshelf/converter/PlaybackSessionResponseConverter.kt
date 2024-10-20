package org.grakovne.lissen.channel.audiobookshelf.converter

import org.grakovne.lissen.domain.PlaybackSession
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackSessionResponseConverter @Inject constructor() {

    fun apply(response: org.grakovne.lissen.channel.audiobookshelf.model.PlaybackSessionResponse): PlaybackSession =
        PlaybackSession(
            sessionId = response.id,
            bookId = response.libraryItemId
        )
}