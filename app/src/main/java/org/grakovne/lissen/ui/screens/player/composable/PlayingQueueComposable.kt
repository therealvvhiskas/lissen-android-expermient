package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.grakovne.lissen.viewmodel.PlayerViewModel

private const val VISIBLE_ITEMS_COUNT = 4
private const val TRACK_POSITION_IN_VIEW = 1
private const val HORIZONTAL_PADDING = 16

@Composable
fun PlayingQueueComposable(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val playlist by viewModel.playlist.observeAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(currentTrackIndex) {
        val scrollToIndex =
            if (currentTrackIndex >= playlist.size - (VISIBLE_ITEMS_COUNT - TRACK_POSITION_IN_VIEW)) {
                (playlist.size - VISIBLE_ITEMS_COUNT).coerceAtLeast(0)
            } else {
                (currentTrackIndex - TRACK_POSITION_IN_VIEW).coerceAtLeast(0)
            }
        listState.scrollToItem(scrollToIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_PADDING.dp)
    ) {
        Text(
            text = "Now Playing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
        ) {
            val start =
                if (currentTrackIndex >= playlist.size - (VISIBLE_ITEMS_COUNT - TRACK_POSITION_IN_VIEW)) {
                    (playlist.size - VISIBLE_ITEMS_COUNT).coerceAtLeast(0)
                } else {
                    (currentTrackIndex - TRACK_POSITION_IN_VIEW).coerceAtLeast(0)
                }

            val end = (start + VISIBLE_ITEMS_COUNT - 1).coerceAtMost(playlist.size - 1)

            itemsIndexed(playlist.subList(start, end + 1)) { relativeIndex, track ->
                val index = start + relativeIndex

                PlaylistItemComposable(
                    track = track,
                    onClick = { viewModel.setChapter(index) },
                    isSelected = index == currentTrackIndex
                )

                if (index < end) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING.dp)
                    )
                }
            }
        }
    }
}
