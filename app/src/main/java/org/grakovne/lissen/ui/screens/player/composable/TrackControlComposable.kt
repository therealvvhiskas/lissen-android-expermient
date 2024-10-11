package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward30
import androidx.compose.material.icons.rounded.PauseCircleFilled
import androidx.compose.material.icons.rounded.PlayCircleFilled
import androidx.compose.material.icons.rounded.Replay30
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import org.grakovne.lissen.ui.extensions.hhmmss
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun TrackControlComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.observeAsState(false)
    val currentPosition by viewModel.currentPosition.observeAsState(0L)
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)

    val book by viewModel.book.observeAsState()
    val chapters = book?.chapters ?: emptyList()

    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val duration = book?.chapters?.get(currentTrackIndex)?.duration?.toFloat() ?: 0f


    val draggableTrackProgress by remember {
        derivedStateOf {
            if (!isDragging) currentPosition.toFloat() else sliderPosition
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {

        Slider(
            value = draggableTrackProgress,
            onValueChange = { newPosition ->
                sliderPosition = newPosition
                isDragging = true
            },
            onValueChangeFinished = {
                viewModel.seekTo(sliderPosition)
                isDragging = false
            },
            valueRange = 0f..(duration),
            modifier = Modifier
                .height(36.dp)
                .fillMaxWidth(),
            colors = SliderDefaults
                .colors(
                    thumbColor = colorScheme.primary,
                    activeTrackColor = colorScheme.primary
                )
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentPosition.toInt().hhmmss(),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = "-${(duration - currentPosition).toInt().hhmmss()}",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                if (currentTrackIndex > 0) {
                    viewModel.previousTrack()
                }
            },
            modifier = Modifier
                .weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                tint = colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = {
                viewModel.seekTo(maxOf(0f, currentPosition - 10f))
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.Replay30,
                contentDescription = "Replay",
                tint = colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = { viewModel.togglePlayPause() },
            modifier = Modifier
                .size(72.dp)
                .weight(1.5f)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.PauseCircleFilled else Icons.Rounded.PlayCircleFilled,
                contentDescription = "Play / Pause toggle",
                tint = colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        }

        IconButton(
            onClick = {
                viewModel.seekTo(minOf(duration, currentPosition + 30f))
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Forward30,
                contentDescription = "Forward",
                tint = colorScheme.onBackground,
                modifier = Modifier.size(36.dp)
            )
        }

        IconButton(
            onClick = {
                if (currentTrackIndex < chapters.size.minus(1)) {
                    viewModel.nextTrack()
                }
            },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Next Track",
                tint = colorScheme.onBackground,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}


@Composable
fun PlaceholderSlider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .shimmer()
            .background(Color.Gray)
    )
}