package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import dagger.hilt.android.EntryPointAccessors
import org.grakovne.lissen.R
import org.grakovne.lissen.ui.components.ImageLoaderEntryPoint
import org.grakovne.lissen.ui.screens.AsyncShimmeringImage
import org.grakovne.lissen.viewmodel.PlayerViewModel

@Composable
fun TrackDetailsComposable(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrackIndex by viewModel.currentTrackIndex.observeAsState(0)
    val book by viewModel.book.observeAsState()

    val context = LocalContext.current
    val imageRequest = remember(book?.id) {
        ImageRequest.Builder(context)
            .data(book?.id)
            .build()
    }

    val imageLoader = remember {
        EntryPointAccessors
            .fromApplication(context, ImageLoaderEntryPoint::class.java)
            .getImageLoader()
    }

    AsyncShimmeringImage(
        imageRequest = imageRequest,
        imageLoader = imageLoader,
        contentDescription = "${book?.title} cover",
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .aspectRatio(1f),
        error = painterResource(R.drawable.fallback_cover)
    )

    Text(
        text = book?.title ?: "Unknown",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = colorScheme.onBackground
    )
    Text(
        text = "Chapter ${currentTrackIndex + 1} of ${book?.chapters?.size}",
        style = MaterialTheme.typography.bodyMedium,
        color = colorScheme.onBackground.copy(alpha = 0.6f)
    )

}