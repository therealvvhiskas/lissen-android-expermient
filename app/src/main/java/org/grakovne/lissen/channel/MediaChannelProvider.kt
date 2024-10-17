package org.grakovne.lissen.channel

import org.grakovne.lissen.channel.common.MediaChannel
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaChannelProvider @Inject constructor(
    private val sharedPreferences: LissenSharedPreferences,
    private val channels: List<@JvmSuppressWildcards MediaChannel>
) {

    init {
        println(channels)
    }
}