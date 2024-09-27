package org.grakovne.lissen.ui.screens.library.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import org.grakovne.lissen.R
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.ui.extensions.hhmm
import org.grakovne.lissen.viewmodel.LibraryViewModel

@Composable
fun LibraryItemComposable(book: Book, viewModel: LibraryViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(book.id)
                .crossfade(true)
                .build(),
            imageLoader = viewModel.imageLoader,
            contentDescription = "${book.title} cover",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(4.dp)),
            placeholder = painterResource(R.drawable.fallback_cover),
            error = painterResource(R.drawable.fallback_cover)
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
            Icon(
                imageVector = if (book.downloaded) Icons.Outlined.CloudDownload else Icons.Outlined.Cloud,
                contentDescription = if (book.downloaded) "Downloaded" else "Not Downloaded",
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.duration.hhmm(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}