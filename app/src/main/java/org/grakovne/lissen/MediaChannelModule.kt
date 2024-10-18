package org.grakovne.lissen

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.grakovne.lissen.channel.ChannelCode
import org.grakovne.lissen.channel.common.MediaChannel
import org.grakovne.lissen.channel.sources.audiobookshelf.AudiobookshelfChannel
import org.grakovne.lissen.channel.sources.cache.ForceCacheChannel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaChannelModule {

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaChannels(
        audiobookshelfChannel: AudiobookshelfChannel,
        forceCacheChannel: ForceCacheChannel
    ): Map<ChannelCode, @JvmSuppressWildcards MediaChannel> {
        return mapOf(
            audiobookshelfChannel.getChannelCode() to audiobookshelfChannel,
            forceCacheChannel.getChannelCode() to forceCacheChannel
        )
    }
}