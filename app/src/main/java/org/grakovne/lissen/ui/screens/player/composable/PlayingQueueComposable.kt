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

@Composable
fun PlayingQueueComposable(viewModel: PlayerViewModel, modifier: Modifier = Modifier) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val playlist by viewModel.playlist.observeAsState(emptyList())

    val listState = rememberLazyListState()

    // Прокручиваем список к нужной позиции
    LaunchedEffect(currentTrackIndex) {
        // Если текущий трек находится на одном из последних 2-х мест, корректируем scroll
        val scrollToIndex = if (currentTrackIndex >= playlist.size - 2) {
            (playlist.size - 4).coerceAtLeast(0) // Скроллим, чтобы показывалось последние 4 элемента
        } else {
            (currentTrackIndex - 1).coerceAtLeast(0) // Стандартный случай: текущий трек на второй позиции
        }
        listState.scrollToItem(scrollToIndex)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
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
            val start = if (currentTrackIndex >= playlist.size - 2) {
                (playlist.size - 4).coerceAtLeast(0)
            } else {
                (currentTrackIndex - 2).coerceAtLeast(0)
            }

            val end = (start + 3).coerceAtMost(playlist.size - 1)

            itemsIndexed(playlist.subList(start, end + 1)) { relativeIndex, track ->
                val index = start + relativeIndex

                PlaylistItemComposable(
                    track = track,
                    isPlaying = index == currentTrackIndex,
                    onClick = { viewModel.setChapter(index) },
                )

                if (index < end) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}