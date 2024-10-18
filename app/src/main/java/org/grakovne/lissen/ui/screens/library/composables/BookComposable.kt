package org.grakovne.lissen.ui.screens.library.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.ImageLoader
import coil.request.ImageRequest
import org.grakovne.lissen.R
import org.grakovne.lissen.content.cache.CacheProgress
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.domain.BookCachedState
import org.grakovne.lissen.ui.components.AsyncShimmeringImage
import org.grakovne.lissen.ui.extensions.formatShortly
import org.grakovne.lissen.viewmodel.CachingModelView

@Composable
fun BookComposable(
    book: Book,
    imageLoader: ImageLoader,
    navController: NavController,
    cachingModelView: CachingModelView
) {
    val cacheProgress: CacheProgress by cachingModelView.getCacheProgress(book.id).collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController
                    .navigate("player_screen/${book.id}?bookTitle=${book.title}") {
                        launchSingleTop = true
                    }
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val context = LocalContext.current
        val imageRequest = remember(book.id) {
            ImageRequest.Builder(context)
                .data(book.id)
                .size(coil.size.Size.ORIGINAL)
                .build()
        }

        AsyncShimmeringImage(
            imageRequest = imageRequest,
            imageLoader = imageLoader,
            contentDescription = "${book.title} cover",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(64.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp)),
            error = painterResource(R.drawable.cover_fallback)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { cachingModelView.toggleBookLocalCache(book) }
            ) {
                Icon(
                    imageVector = provideCachingStateIcon(book, cacheProgress),
                    contentDescription = "Caching Book State"
                )
            }
            Text(
                text = book.duration.formatShortly(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun provideCachingStateIcon(
    book: Book,
    cacheProgress: CacheProgress
): ImageVector {
    return when (cacheProgress) {
        CacheProgress.Completed -> Icons.Outlined.CloudDownload
        CacheProgress.Error -> Icons.Outlined.Cloud
        CacheProgress.Idle -> provideIdleStateIcon(book)
        is CacheProgress.Started -> Icons.Outlined.Downloading
    }
}

private fun provideIdleStateIcon(book: Book): ImageVector {
    return when (book.cachedState) {
        BookCachedState.ABLE_TO_CACHE -> Icons.Outlined.Cloud
        BookCachedState.CACHED -> Icons.Outlined.CloudDownload
        BookCachedState.STORED_LOCALLY -> Icons.Outlined.SdCard
    }
}