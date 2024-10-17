package org.grakovne.lissen

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.grakovne.lissen.channel.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.channel.common.MediaChannel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaChannelModule {

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaChannels(audiobookshelfChannel: AudiobookshelfChannel): List<@JvmSuppressWildcards MediaChannel> {
        return listOf(audiobookshelfChannel)
    }
}