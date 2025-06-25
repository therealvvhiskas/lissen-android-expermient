package org.grakovne.lissen.playback.service

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.SilenceMediaSource
import org.grakovne.lissen.common.RunningComponent
import org.grakovne.lissen.persistence.preferences.LissenSharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(UnstableApi::class)
class PlaybackNotificationService
  @Inject
  constructor(
    private val exoPlayer: ExoPlayer,
    private val sharedPreferences: LissenSharedPreferences,
  ) : RunningComponent {
    override fun onCreate() {
      exoPlayer.addListener(
        object : Player.Listener {
          override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean,
            reason: Int,
          ) {
            super.onPlayWhenReadyChanged(playWhenReady, reason)

            if (playWhenReady) {
              exoPlayer.setPlaybackSpeed(sharedPreferences.getPlaybackSpeed())
            }
            val context = exoPlayer.applicationLooper.thread.contextClassLoader
                ?.loadClass("android.app.AppGlobals")
                ?.getMethod("getInitialApplication")
                ?.invoke(null) as? android.content.Context
                ?: return
            
            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            val channelId = "playback_channel"
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Playback",
                    android.app.NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }
            
            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setContentTitle("Lissen Player")
                .setContentText("Playing audiobook")
                .setSmallIcon(android.R.drawable.ic_media_play) // use a real icon if possible
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(exoPlayer.currentMediaItem?.mediaMetadata?.extras?.getParcelable("android.media.session.MEDIA_SESSION") as? android.support.v4.media.session.MediaSessionCompat.Token)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .build()
            
            notificationManager.notify(1, notification)

          }

          override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
          ) {
            val previousIndex = oldPosition.mediaItemIndex
            val currentIndex = newPosition.mediaItemIndex

            if (exoPlayer.currentMediaItem?.mediaId != SilenceMediaSource::class.simpleName) {
              return
            }

            if (currentIndex != previousIndex) {
              val direction =
                when (
                  currentIndex > previousIndex ||
                    (currentIndex == 0 && previousIndex == exoPlayer.mediaItemCount - 1)
                ) {
                  true -> Direction.FORWARD
                  false -> Direction.BACKWARD
                }

              val nextTrack =
                findAvailableTrackIndex(exoPlayer.currentMediaItemIndex, direction, exoPlayer, 0)
              nextTrack?.let { exoPlayer.seekTo(it, 0) }

              if (nextTrack == null || nextTrack < currentIndex) {
                exoPlayer.pause()
              }
            }
          }
        },
      )
    }

    private fun findAvailableTrackIndex(
      currentItem: Int,
      direction: Direction,
      exoPlayer: ExoPlayer,
      iteration: Int,
    ): Int? {
      if (exoPlayer.getMediaItemAt(currentItem).mediaId != SilenceMediaSource::class.simpleName) {
        return currentItem
      }

      if (iteration > 4096) {
        return null
      }

      val foundItem =
        when (direction) {
          Direction.FORWARD -> (currentItem + 1) % exoPlayer.mediaItemCount
          Direction.BACKWARD -> if (currentItem - 1 < 0) exoPlayer.mediaItemCount - 1 else currentItem - 1
        }

      return findAvailableTrackIndex(foundItem, direction, exoPlayer, iteration + 1)
    }
  }

private enum class Direction {
  FORWARD,
  BACKWARD,
}
