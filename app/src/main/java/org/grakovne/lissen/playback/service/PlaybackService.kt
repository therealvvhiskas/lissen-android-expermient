package org.grakovne.lissen.playback.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.source.SilenceMediaSource
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.grakovne.lissen.LissenApplication
import org.grakovne.lissen.channel.audiobookshelf.common.api.RequestHeadersProvider
import org.grakovne.lissen.common.createOkHttpClient
import org.grakovne.lissen.content.LissenMediaProvider
import org.grakovne.lissen.domain.BookFile
import org.grakovne.lissen.domain.DetailedItem
import org.grakovne.lissen.domain.MediaProgress
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var exoPlayer: ExoPlayer
    @Inject lateinit var mediaSession: MediaSession

    override fun onCreate() {
        super.onCreate()

        // Setup MediaSession as usual
        mediaSession.setPlayer(exoPlayer)

        // Start foreground notification
        val channelId = "playback_channel"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Lissen Player")
            .setContentText("Playing audiobook")
            .setSmallIcon(R.drawable.ic_notification) // Replace with real icon if needed
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionCompatToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()

        startForeground(notificationId, notification)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }
} 
