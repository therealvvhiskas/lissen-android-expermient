package org.grakovne.lissen.ui.screens.library

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import org.grakovne.lissen.R
import org.grakovne.lissen.domain.Book
import org.grakovne.lissen.ui.screens.library.composables.LibraryItemComposable
import org.grakovne.lissen.ui.screens.library.composables.MiniPlayerComposable
import org.grakovne.lissen.ui.screens.library.composables.RecentBooksComposable
import org.grakovne.lissen.viewmodel.LibraryViewModel
import org.grakovne.lissen.viewmodel.LoginViewModel

@Composable
fun LibraryScreen(
    navController: NavController,
    viewModel: LibraryViewModel = hiltViewModel()
) {

    val listState = rememberLazyListState()
    val books by viewModel.books.observeAsState(emptyList())

    val showAppBarTitle by remember {
        derivedStateOf {
            val libraryItemIndex = 2
            !listState.layoutInfo.visibleItemsInfo.any { it.index == libraryItemIndex }
        }
    }

    Scaffold(
        topBar = {
            if (showAppBarTitle) {
                Text(
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    text = "Library",
                    modifier = Modifier
                        .padding(16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        },
        bottomBar = {
            MiniPlayerComposable(navController)
        },
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxSize(),
        content = { innerPadding ->
            LazyColumn(
                state = listState,
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
                    RecentBooksComposable()
                }

                item {
                    Text(
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        text = "Library"
                    )
                }

                items(books) { book ->
                    LibraryItemComposable(book = book)
                }
            }
        }
    )
}


