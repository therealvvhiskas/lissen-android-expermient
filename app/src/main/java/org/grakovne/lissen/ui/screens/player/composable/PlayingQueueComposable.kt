package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun PlayingQueueComposable(viewModel: PlayerViewModel) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(1)
    val playlist by viewModel.playlist.observeAsState(emptyList())

    val endIndex = (currentTrackIndex + 2).coerceAtMost(playlist.size - 1)

    val adjustedStartIndex = (endIndex - 3).coerceAtLeast(0)
    val adjustedEndIndex = (adjustedStartIndex + 3).coerceAtMost(playlist.size - 1)

    val visiblePlaylist = playlist.subList(adjustedStartIndex, adjustedEndIndex + 1)

    Column(
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn {
            itemsIndexed(visiblePlaylist) { visibleIndex, track ->
                val realIndex = adjustedStartIndex + visibleIndex

                PlaylistItemComposable(
                    track = track,
                    isPlaying = realIndex == currentTrackIndex,
                    onClick = {
                        viewModel.setChapter(realIndex)
                    },
                )
                if (visibleIndex < visiblePlaylist.size - 1) {
                    HorizontalDivider(
                        color = colorScheme.onBackground.copy(alpha = 0.1f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
