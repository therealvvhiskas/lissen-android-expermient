package org.grakovne.lissen

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.grakovne.lissen.channel.ChannelCode
import org.grakovne.lissen.channel.sources.audiobookshelf.AudiobookshelfChannel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaChannelModule {

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaChannels(audiobookshelfChannel: AudiobookshelfChannel): Map<ChannelCode, @JvmSuppressWildcards AudiobookshelfChannel> {
        return mapOf(audiobookshelfChannel.getChannelCode() to audiobookshelfChannel)
    }
}