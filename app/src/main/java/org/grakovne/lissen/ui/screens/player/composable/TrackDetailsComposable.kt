package org.grakovne.lissen.ui.screens.player.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.request.ImageRequest
import dagger.hilt.android.EntryPointAccessors
import org.grakovne.lissen.R
import org.grakovne.lissen.ui.components.ImageLoaderEntryPoint
import org.grakovne.lissen.ui.components.AsyncShimmeringImage
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
            .size(coil.size.Size.ORIGINAL)
            .build()
    }

    val imageLoader = remember {
        EntryPointAccessors
            .fromApplication(context, ImageLoaderEntryPoint::class.java)
            .getImageLoader()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncShimmeringImage(
            imageRequest = imageRequest,
            imageLoader = imageLoader,
            contentDescription = "${book?.title} cover",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp)),
            error = painterResource(R.drawable.fallback_cover)
        )
    }

    Spacer(Modifier.padding(vertical = 8.dp))

    Text(
        text = book?.title ?: return,
        style = typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = colorScheme.onBackground,
        textAlign = TextAlign.Center,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(Modifier.padding(vertical = 4.dp))

    Text(
        text = "Chapter ${currentTrackIndex + 1} of ${book?.chapters?.size ?: "?"}",
        style = typography.bodyMedium,
        color = colorScheme.onBackground.copy(alpha = 0.6f)
    )
}