package org.grakovne.lissen.ui.screens.library

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.grakovne.lissen.R

@Composable
fun LibraryScreen(
    navController: NavController
) {

    Scaffold(
        topBar = { Spacer(modifier = Modifier.height(24.dp)) },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        content = { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        text = "Continue Listening"
                    )
                }

                item {
                    ImageCarousel()
                }

                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        text = "Library"
                    )
                }

                items(getSampleBooks()) { book ->
                    BookItem(book = book)
                }
            }
        }
    )
}

@Composable
fun ImageCarousel() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val itemsVisible = 2.3f
    val spacing = 16.dp
    val totalSpacing = spacing * (itemsVisible + 1)
    val itemWidth = (screenWidth - totalSpacing) / itemsVisible

    val images = List(10) { R.drawable.fallback_cover }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(images) { imageRes ->
            Column(
                modifier = Modifier
                    .width(itemWidth)
                    .clickable { }
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "Book Description",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "What Does Fox Says?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 6.dp),
                            maxLines = 1
                        )
                        Text(
                            text = "John Show",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

data class Book(
    val title: String,
    val author: String,
    val downloaded: Boolean
)

fun getSampleBooks(): List<Book> {
    return List(20) { index ->
        Book(
            title = "Book Title $index",
            author = "Author $index",
            downloaded = index % 2 == 0
        )
    }
}

@Composable
fun BookItem(book: Book) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.fallback_cover),
            contentDescription = "Book Cover",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = book.author,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            )
        }

        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (book.downloaded) Icons.Outlined.CloudDownload else Icons.Outlined.Cloud,
                contentDescription = "Cloud Icon",
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "12h 24m",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

