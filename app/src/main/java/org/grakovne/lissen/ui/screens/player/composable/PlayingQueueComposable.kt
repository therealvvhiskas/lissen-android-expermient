package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.sp
import org.grakovne.lissen.viewmodel.PlayerViewModel

private const val HORIZONTAL_PADDING = 16

@Composable
fun PlayingQueueComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val playlist by viewModel.playlist.observeAsState(emptyList())
    val playingQueueExpanded by viewModel.playingQueueExpanded.observeAsState(false)

    val listState = rememberLazyListState()

    val fontSize by animateFloatAsState(
        targetValue = if (playingQueueExpanded) 24f else 18f,
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(currentTrackIndex) {
        listState.animateScrollToItem(currentTrackIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HORIZONTAL_PADDING.dp)
    ) {
        Text(
            text = "Now Playing",
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = listState,
        ) {
            itemsIndexed(playlist) { index, track ->
                PlaylistItemComposable(
                    track = track,
                    onClick = { viewModel.setChapter(index) },
                    isSelected = index == currentTrackIndex
                )

                if (index < playlist.size - 1) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING.dp)
                    )
                }
            }
        }
    }
}
