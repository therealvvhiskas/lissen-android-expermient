package org.grakovne.lissen.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.R
import javax.inject.Inject

@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var mediaSession: MediaSession

    companion object {
        private const val CHANNEL_ID = "audio_player_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START_FOREGROUND = "org.grakovne.lissen.player.service.START_FOREGROUND"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == ACTION_START_FOREGROUND) {
            startForeground(NOTIFICATION_ID, createMediaNotification())
        }

        return START_STICKY
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    @OptIn(UnstableApi::class)
    private fun createMediaNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.fallback_cover)
            .setStyle(androidx.media3.session.MediaStyleNotificationHelper.MediaStyle(mediaSession))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Audio Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications for audio player"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}