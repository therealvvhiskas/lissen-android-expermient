package org.grakovne.lissen.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import org.grakovne.lissen.ui.activity.AppActivity

@Module
@InstallIn(ServiceComponent::class)
object MediaModule {

    @Provides
    @ServiceScoped
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }

    @Provides
    @ServiceScoped
    fun provideMediaSession(
        @ApplicationContext context: Context,
        exoPlayer: ExoPlayer
    ): MediaSession {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, AppActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return MediaSession.Builder(context, exoPlayer)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }
}