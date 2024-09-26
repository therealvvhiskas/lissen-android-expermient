package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.grakovne.lissen.R
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun TrackDetailsComposable(
    navController: NavController,
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val playlist by viewModel.playlist.observeAsState(emptyList())

    Image(

        painter = painterResource(id = R.drawable.fallback_cover),
        contentDescription = "Book Description",
        modifier = modifier
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(16.dp))
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0) {
                            navController.navigate("library_screen")
                        }
                    }
                )
            },
        contentScale = ContentScale.Fit
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "What Does The Fox Say?",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onBackground
    )
    Text(
        text = "Chapter ${currentTrackIndex + 1} of ${playlist.size}",
        style = MaterialTheme.typography.bodyMedium,
        color = colorScheme.onBackground.copy(alpha = 0.6f)
    )

}