package org.grakovne.lissen.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import org.grakovne.lissen.ui.activity.AppActivity
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class AudioPlayerService : MediaSessionService() {

    @Inject
    lateinit var mediaSession: MediaSession

    private lateinit var playerNotificationManager: PlayerNotificationManager

    companion object {
        private const val CHANNEL_ID = "audio_player_channel"
        private const val NOTIFICATION_ID = 1

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for audio player notifications"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MediaDescriptionAdapterImpl())
            .setNotificationListener(NotificationListenerImpl())
            .build()

        playerNotificationManager.setPlayer(mediaSession.player)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession
    }

    @UnstableApi
    private inner class MediaDescriptionAdapterImpl :
        PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            return player.mediaMetadata.title.toString()
        }

        override fun getCurrentContentText(player: Player): String {
            return player.mediaMetadata.artist.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return null
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent = Intent(this@AudioPlayerService, AppActivity::class.java)
            return PendingIntent.getActivity(
                this@AudioPlayerService,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    /**
     * Внутренний класс для прослушивания событий уведомления.
     */
    private inner class NotificationListenerImpl : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: android.app.Notification,
            ongoing: Boolean
        ) {
            if (ongoing) {
                startForeground(notificationId, notification)
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
}