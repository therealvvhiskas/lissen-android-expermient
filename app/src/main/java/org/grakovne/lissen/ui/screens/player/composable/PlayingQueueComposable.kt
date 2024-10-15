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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.grakovne.lissen.R
import org.grakovne.lissen.viewmodel.PlayerViewModel


@Composable
fun PlayingQueueComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    val isPlaybackReady by viewModel.isPlaybackReady.observeAsState(false)

    val book by viewModel.book.observeAsState()
    val chapters = book?.files ?: emptyList()

    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val playingQueueExpanded by viewModel.playingQueueExpanded.observeAsState(false)

    val listState = rememberLazyListState()

    val fontSize by animateFloatAsState(
        targetValue = if (playingQueueExpanded) 24f else 20f,
        animationSpec = tween(durationMillis = 300),
        label = "playing_queue_font_size"
    )

    LaunchedEffect(isPlaybackReady) {
        if (isPlaybackReady) {
            when {
                currentTrackIndex > 0 -> listState.scrollToItem(currentTrackIndex - 1)
                else -> listState.scrollToItem(currentTrackIndex)
            }
        }
    }

    LaunchedEffect(currentTrackIndex) {
        when {
            currentTrackIndex > 0 -> listState.animateScrollToItem(currentTrackIndex - 1)
            else -> listState.animateScrollToItem(currentTrackIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.player_screen_now_playing_title),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .nestedScroll(object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return if (playingQueueExpanded) Offset.Zero else available
                    }
                }),
            state = listState,

            ) {
            itemsIndexed(chapters) { index, track ->
                PlaylistItemComposable(
                    track = track,
                    onClick = { viewModel.setChapter(index) },
                    isSelected = index == currentTrackIndex
                )

                if (index < chapters.size - 1) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

        }
    }
}
